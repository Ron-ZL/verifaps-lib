package edu.kit.iti.formal.automation

import edu.kit.iti.formal.automation.datatypes.INT
import edu.kit.iti.formal.automation.scope.Scope
import edu.kit.iti.formal.automation.st.ast.*
import edu.kit.iti.formal.automation.st.util.AstMutableVisitor
import edu.kit.iti.formal.automation.st.util.UsageFinder
import edu.kit.iti.formal.automation.st0.MultiCodeTransformation
import edu.kit.iti.formal.automation.st0.TransformationState
import edu.kit.iti.formal.automation.st0.trans.CodeTransformation
import edu.kit.iti.formal.automation.st0.trans.RealToInt
import edu.kit.iti.formal.automation.st0.trans.STCodeTransformation
import edu.kit.iti.formal.automation.visitors.Utils
import edu.kit.iti.formal.smv.HistoryModuleBuilder
import edu.kit.iti.formal.smv.ModuleType
import edu.kit.iti.formal.smv.SMVPrinter
import edu.kit.iti.formal.smv.SMVTypes
import edu.kit.iti.formal.smv.ast.SAssignment
import edu.kit.iti.formal.smv.ast.SMVExpr
import edu.kit.iti.formal.smv.ast.SMVModule
import edu.kit.iti.formal.smv.ast.SVariable
import java.io.File
import java.io.PrintWriter
import java.math.BigInteger

/**
 *
 * @author Alexander Weigl
 * @version 1 (10.07.18)
 */
object KastelDemonstrator {
    val FOLDER = File("/home/weigl/Documents/Kastel/Industry4.0/Demonstrator2018")
    val INPUT_FILE = File(FOLDER, "VerificationSubject.st")

    @JvmStatic
    fun main(args: Array<String>) {
        val (pous, errors) = IEC61131Facade.fileResolve(INPUT_FILE)
        errors.forEach {
            println("${it.sourceName}:${it.lineNumber} :: ${it.message} (${it.category}) ")
        }

        val program = Utils.findProgram(pous)!!

        AssignmentDScratch.transform(
                SeqParamsActiveStep.transform(TransformationState(program))
        )

        val simplified = SymbExFacade.simplify(program)

        UsageFinder.markVariablesByUsage(simplified)

        val t = MultiCodeTransformation(
                RealToInt(),
                RemoveVSObj,
                RemoveConversionFunction,
                IntLit1000To1
        )
        t.transform(TransformationState(simplified))

        markVariableAs(simplified.scope,
                "stHmiInt\$rStartVel",
                "stHmiInt\$stMCStatus\$bMC_Error",
                "stHmiInt\$stReq\$stMan\$bDecrVel",
                "stHmiInt\$stReq\$stMan\$bDisable",
                "stHmiInt\$stReq\$stMan\$bIncrVel",
                "stHmiInt\$stReq\$stMan\$bStartVel",
                flag = VariableDeclaration.INPUT)


        val simpFile = File(FOLDER, "Simplified.st")
        simpFile.bufferedWriter().use {
            IEC61131Facade.printTo(it, simplified, true)
        }
        Console.writeln("File $simpFile written")


        //degrade INT with
        INT.bitLength = 7

        val module = SymbExFacade.evaluateProgram(simplified, true)
        val isHigh = { v: String ->
            false //all equal //v.endsWith("Velocity")
        }
        val imb = IFModelBuilder(module, isHigh)
        imb.run()


        val smvFile = File(FOLDER, "noif.smv")
        smvFile.bufferedWriter().use {
            imb.product.forEach { m -> m.accept(SMVPrinter(PrintWriter(it))) }
        }
        Console.writeln("File $smvFile written")
    }
}

object AssignmentDScratch : STCodeTransformation, AstMutableVisitor() {
    override fun transform(stBody: StatementList): StatementList = stBody.accept(this) as StatementList
    override fun visit(assignmentStatement: AssignmentStatement): Statement {
        if ((assignmentStatement.location as SymbolicReference).identifier == "dScratch") {
            return StatementList()
        }
        return assignmentStatement
    }
}


class IFModelBuilder(private val code: SMVModule,
                     val isHigh: (String) -> Boolean, private val historyLength: Int = 10) : Runnable {
    val loweq = SVariable("lowEq", SMVTypes.BOOLEAN)
    val hmb = HistoryModuleBuilder("HistoryLowEq", listOf(loweq), historyLength)
    val main = SMVModule("main")
    val product = arrayListOf(main, code, hmb.module)

    override fun run() {
        main.name = MAIN_MODULE

        code.inputVars.addAll(code.moduleParameters)
        code.moduleParameters.clear()

        instantiateCode(FIRST_RUN)
        instantiateCode(SECOND_RUN)

        val inV = code.inputVars
        val inputLow = inV.filter { !isHigh(it.name) }
                .map {
                    it.inModule(FIRST_RUN) equal it.inModule(SECOND_RUN)
                }
                .reduce { a, b -> a and b }

        // History of low inputs.
        hmb.run()
        val historyLowEq = SVariable("hLowEq", hmb.moduleType)
        main.definitions.add(SAssignment(loweq, inputLow))
        main.stateVars.add(historyLowEq)


        val outV = code.definitions.map { it.target } + code.stateVars
        val lowOutput = outV.filter { !isHigh(it.name) }
        /*val stateLow =          outV.filter { !isHigh(it.name) }
                .map {
                    it.inModule(FIRST_RUN) equal it.inModule(SECOND_RUN)
                }
                .reduce { a, b -> a and b }
        */

        val history = hmb.module.stateVars.map { it.inModule(historyLowEq.name) }
        val premise = (history + loweq).reduce { acc: SMVExpr, sVariable: SMVExpr ->
            acc.and(sVariable)
        }

        lowOutput.forEach {
            val eq = it.inModule(FIRST_RUN) equal it.inModule(SECOND_RUN)
            main.invariantSpecs.add(premise implies eq)
        }
        //main.invariantSpecs.add( inputLow implies  stateLow)
    }

    private fun instantiateCode(nameOfRun: String) {
        main.stateVars.add(SVariable(nameOfRun, ModuleType(code.name)))
    }

    companion object {
        val SECOND_RUN = "snd"
        val FIRST_RUN = "fst"
        val MAIN_MODULE = "main"
    }
}


fun <T> nonNullSeq(seq: Collection<T?>): List<T> {
    val al = ArrayList<T>()
    seq.forEach { it?.let { al += it } }
    return al
}

/**
 * Find and tag the variables with the given [flag]
 */
fun markVariableAs(scope: Scope, vararg vars: String, flag: Int) {
    for (variable in scope.variables) {
        if (variable.name in vars)
            variable.type = flag
    }
}


/**
 * Handles access to `aSeqParams[Sequence.iActStep]`
 */
object SeqParamsActiveStep : CodeTransformation, AstMutableVisitor() {
    override fun transform(state: TransformationState): TransformationState {
        val dt = state.scope.resolveDataType("stSeqParams")

        val vd = VariableDeclaration("ActStep",
                0,
                SimpleTypeDeclaration(dt = dt, init = null))
        vd.dataType = dt
        state.scope.variables += vd
        state.stBody = state.stBody.accept(this) as StatementList
        return state
    }

    override fun visit(symbolicReference: SymbolicReference): Expression {
        if (symbolicReference.identifier == "aSeqParams" && symbolicReference.isArrayAccess) {
            try {
                val sub = symbolicReference.subscripts!![0] as SymbolicReference
                if (sub.identifier == "Sequence" && sub.sub?.identifier == "iActStep") {
                    return SymbolicReference("ActStep", symbolicReference.sub)
                }
            } catch (e: ClassCastException) {

            }
        }
        return super.visit(symbolicReference)
    }
}

object IntLit1000To1 : STCodeTransformation, AstMutableVisitor() {
    val _1000 = BigInteger.valueOf(1000)
    override fun transform(stBody: StatementList): StatementList = stBody.accept(this) as StatementList
    override fun visit(literal: Literal): Expression =
            if (literal is IntegerLit && literal.value == _1000)
                IntegerLit(INT, BigInteger.ONE)
            else
                literal
}

object RemoveConversionFunction : STCodeTransformation, AstMutableVisitor() {
    override fun transform(stBody: StatementList): StatementList = stBody.accept(this) as StatementList

    val regex = ".*_TO_.*".toRegex()
    override fun visit(invocation: Invocation): Expression {
        if (regex.matchEntire(invocation.callee.identifier) != null) {
            return invocation.parameters[0].expression
        }
        return invocation
    }
}

object RemoveVSObj : STCodeTransformation, AstMutableVisitor() {
    override fun transform(stBody: StatementList) = stBody.accept(this) as StatementList

    override fun visit(assignmentStatement: AssignmentStatement): Statement {
        val ident = (assignmentStatement.location as SymbolicReference).identifier
        return if (ident == "VSObj_McFaultDescription")
            return StatementList()
        else assignmentStatement
    }
}
