package edu.kit.iti.formal.automation.testtables.apps

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import edu.kit.iti.formal.automation.Console
import edu.kit.iti.formal.automation.IEC61131Facade
import edu.kit.iti.formal.automation.SymbExFacade
import edu.kit.iti.formal.automation.st.ast.PouExecutable
import edu.kit.iti.formal.automation.st.ast.ProgramDeclaration
import edu.kit.iti.formal.automation.st0.TransformationState
import edu.kit.iti.formal.automation.testtables.GetetaFacade
import edu.kit.iti.formal.automation.testtables.algorithms.MultiModelGluer
import edu.kit.iti.formal.automation.testtables.algorithms.OmegaSimplifier
import edu.kit.iti.formal.automation.testtables.model.GeneralizedTestTable
import edu.kit.iti.formal.automation.testtables.rtt.RTTCodeAugmentation
import edu.kit.iti.formal.smv.ast.SMVModule
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

object RetetaApp {
    @JvmStatic
    fun main(args: Array<String>) {
        Reteta().main(args)
    }
}

class Reteta : CliktCommand(
        epilog = "Reteta -- Tooling for Relational Test Tables.",
        name = "geteta.sh") {

    val disableSimplify by option("--no-simplify", help = "disable")
            .flag(default = false)

    val table by option("-t", "--table", help = "the xml file of the table", metavar = "FILE")
            .file()
            .multiple(required = true)

    val outputFolder by option("-o", "--output", help = "Output directory")
            .defaultLazy { table.first().nameWithoutExtension }

    val library by option("-L", "--lib", help = "program files")
            .convert { File(it) }
            .multiple()

    val verificationTechnique by option("-v", "--technique",
            help = "verification technique")

    val programs by option("-P", "--program", metavar = "NAME")
            .convert { File(it) }
            .multiple()

    val nuxmv by option("--nuxmv", help = "Path to nuXmv binary.", envvar = "NUXMV")
            .file(exists = true)
            .required()


    override fun run() {
        if (table.isEmpty() || programs.isEmpty()) {
            Console.fatal("No code or table file given.")
            System.exit(1)
        }
        //read program
        val programs = readPrograms()


        val superEnumType = GetetaFacade.createSuperEnum(programs.map { it.scope })

        val programRunNames = programs.mapIndexed { index, it ->
            "${it.name.toLowerCase()}$$index"
        }

        //read table
        val gtts = table.flatMap { GetetaFacade.readTable(it) }.map {
            it.ensureProgramRuns()
            it.generateSmvExpression()
            it.simplify()
        }

        gtts.forEach { table ->
            table.programRuns = programRunNames

            val rttPipeline = RTTCodeAugmentation()
            val augmentedPrograms = programs.map {
                val s = rttPipeline.transform(TransformationState(it))
                SymbExFacade.evaluateProgram(ProgramDeclaration(scope = s.scope, stBody = s.stBody),
                        disableSimplify)
            }

            if (!table.options.relational) {
                throw IllegalStateException()
            }

            val tt = GetetaFacade.constructSMV(table, superEnumType)
            val modTable = tt.tableModule
            val mainModule = MultiModelGluer().apply {
                programRunNames.zip(augmentedPrograms).forEach { (n, p) ->
                    addProgramRun(n, p)
                }
                addTable("_${table.name}", tt.ttType!!)
            }

            val modules = LinkedList<SMVModule>()
            modules.add(mainModule.product)
            modules.add(modTable)
            modules.addAll(augmentedPrograms)
            modules.addAll(tt.helperModules)
            val b = GetetaFacade.runNuXMV(
                    nuxmv.absolutePath,
                    this.table.first().nameWithoutExtension, modules,
                    table.options.verificationTechnique)
        }
    }

    fun readPrograms(): List<PouExecutable> {
        val programs = IEC61131Facade.readProgramsWithLibrary(library, programs)
        return if (programs.any { it != null }) {
            Console.fatal("In some given program there is no PROGRAM declaration!")
            System.exit(1)
            listOf()
        } else {
            val a = ArrayList<PouExecutable>()
            programs.forEach { if (it != null) a.add(it) }
            a
        }
    }
}

internal fun GeneralizedTestTable.simplify(): GeneralizedTestTable {
    Console.info("Apply omega simplification")
    val os = OmegaSimplifier(this)
    os.run()
    if (!os.ignored.isEmpty()) {
        Console.warn("I ignore following rows: %s, because they are behind an \\omega duration.%n", os.ignored)
        return os.product
    }
    Console.info("No rows unreachable!")
    return this
}

