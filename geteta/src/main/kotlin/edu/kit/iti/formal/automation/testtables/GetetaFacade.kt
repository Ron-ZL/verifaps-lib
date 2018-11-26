package edu.kit.iti.formal.automation.testtables


import edu.kit.iti.formal.automation.Console
import edu.kit.iti.formal.automation.datatypes.AnyDt
import edu.kit.iti.formal.automation.rvt.translators.DefaultTypeTranslator
import edu.kit.iti.formal.automation.scope.Scope
import edu.kit.iti.formal.automation.st.ast.EnumerationTypeDeclaration
import edu.kit.iti.formal.automation.testtables.algorithms.BinaryModelGluer
import edu.kit.iti.formal.automation.testtables.algorithms.DelayModuleBuilder
import edu.kit.iti.formal.automation.testtables.builder.AutomataTransformerState
import edu.kit.iti.formal.automation.testtables.builder.AutomatonBuilderPipeline
import edu.kit.iti.formal.automation.testtables.builder.SmvConstructionPipeline
import edu.kit.iti.formal.automation.testtables.grammar.TestTableLanguageLexer
import edu.kit.iti.formal.automation.testtables.grammar.TestTableLanguageParser
import edu.kit.iti.formal.automation.testtables.io.*
import edu.kit.iti.formal.automation.testtables.model.*
import edu.kit.iti.formal.automation.testtables.model.automata.TestTableAutomaton
import edu.kit.iti.formal.automation.testtables.model.options.TableOptions
import edu.kit.iti.formal.automation.testtables.print.DSLTablePrinter
import edu.kit.iti.formal.automation.testtables.viz.CounterExampleAnalyzer
import edu.kit.iti.formal.automation.testtables.viz.Mapping
import edu.kit.iti.formal.smv.*
import edu.kit.iti.formal.smv.ast.SMVExpr
import edu.kit.iti.formal.smv.ast.SMVModule
import edu.kit.iti.formal.smv.ast.SVariable
import edu.kit.iti.formal.util.CodeWriter
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import javax.xml.bind.JAXBException

object GetetaFacade {
    @JvmStatic
    @Throws(JAXBException::class)
    fun parseTableXML(filename: String): GeneralizedTestTable {
        val tr = TableReader(File(filename))
        tr.run()
        return tr.product
    }


    fun createParser(input: CharStream): TestTableLanguageParser {
        val lexer = TestTableLanguageLexer(input)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE)

        val parser = TestTableLanguageParser(CommonTokenStream(lexer))

        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener.INSTANCE)

        return parser
    }

    fun createParser(input: String) = createParser(CharStreams.fromString(input))

    fun parseCell(cell: String, enableRelational: Boolean = true): TestTableLanguageParser.CellContext =
            createParser(cell).also { it.relational = true }.cell()!!

    fun exprToSMV(cell: String, column: SVariable, programRun: Int, vars: ParseContext): SMVExpr = exprToSMV(parseCell(cell, vars.relational),
            column, programRun, vars)

    fun exprToSMV(cell: TestTableLanguageParser.CellContext, column: SVariable,
                  programRun: Int, vars: ParseContext): SMVExpr {
        val ev = TblLanguageToSmv(column, programRun, vars)
        val expr = cell.accept(ev)
        Console.debug("parsed: %s to %s", cell, expr)
        return expr
    }

    fun parseDuration(duration: String): Duration {
        if (duration == "wait")//old attributes
            return Duration.OpenInterval(0, true)
        val parser = createParser(duration)
        val p = parser.time()
        return p.accept(TimeParser())
    }

    @Deprecated("use external/internalVariable")
    fun asSMVVariable(column: edu.kit.iti.formal.automation.testtables.model.Variable): SVariable {
        return SVariable(column.name, getSMVDataType(column.dataType))
    }

    private fun getSMVDataType(dataType: AnyDt): SMVType {
        return DefaultTypeTranslator.INSTANCE.translate(dataType)
                ?: error("Data type $dataType is not supported by DataTypeTranslator")
    }

    val DEFAULT_PROGRAM_RUN_NAME = { it: Int -> "_$it$" }
    @JvmStatic
    fun parseTableDSL(input: String) = parseTableDSL(CharStreams.fromString(input))

    @JvmStatic
    fun parseTableDSL(input: File) = parseTableDSL(CharStreams.fromFileName(input.absolutePath))

    @JvmStatic
    fun parseTableDSL(input: CharStream): GeneralizedTestTable {
        val parser = createParser(input)
        val ctx = parser.file()
        val ttlb = TestTableLanguageBuilder()
        ctx.accept(ttlb)
        return ttlb.testTables.get(0)
    }

    fun exprsToSMV(vc: ParseContext,
                   constraints: Map<ProgramVariable, TestTableLanguageParser.CellContext>)
            : Map<String, SMVExpr> = constraints.map { (t, u) ->
        t.name to exprToSMV(u, vc.getSMVVariable(t.programRun, t.name), t.programRun, vc)
    }.toMap()

    fun delay(ref: SReference): DelayModuleBuilder {
        return DelayModuleBuilder(ref.variable,
                ref.cycles)
    }


    fun glue(modTable: SMVModule, tableType: SMVType,
             modCode: List<SMVModule>, programRunNames: List<String>, options: TableOptions): SMVModule {
        val mg = BinaryModelGluer(options, modTable, tableType, modCode, programRunNames)
        mg.run()
        return mg.product
    }

    /*
    fun runNuXMV(folder: String, technique: VerificationTechnique, vararg modules: SMVModule): NuXMVOutput {
        return runNuXMV(folder, Arrays.asList(*modules), technique)
    }*/

    fun getHistoryName(variable: SVariable, cycles: Int): String {
        return getHistoryName(variable) + "._$" + cycles
    }

    fun getHistoryName(variable: SVariable): String {
        return variable.name + "__history"
    }

    fun runNuXMV(nuXmvPath: String, folder: String,
                 modules: List<SMVModule>,
                 vt: VerificationTechnique): NuXMVOutput {
        val outputFolder = File(folder)
        outputFolder.mkdirs()
        val moduleFile = File(outputFolder, "modules.smv")
        moduleFile.bufferedWriter().use { w ->
            val p = SMVPrinter(PrintWriter(w))
            modules.forEach { it.accept(p) }
        }
        val adapter = NuXMVProcess(moduleFile)
        adapter.executablePath = nuXmvPath
        adapter.workingDirectory = outputFolder
        adapter.commands = vt.commands
        return adapter.call()
    }

    fun createSuperEnum(scopes: List<Scope>): EnumType {
        val allowedValues =
                scopes.flatMap { scope ->
                    scope.dataTypes.values()
                            .filter { it is EnumerationTypeDeclaration }
                            .map { it as EnumerationTypeDeclaration }
                            .flatMap { it.allowedValues.map { it.text } }
                }
        return EnumType(allowedValues)
    }

    fun generateInterface(name: String = "anonym",
                          scope: Scope = Scope.defaultScope(),
                          includeState: Boolean = true): String {
        val s = StringBuilder()
        s.append("table $name {\n")
        scope.filter { it.isInput }.forEach {
            s.append("\n\tvar input ${it.name} : ${it.dataType?.name}")
        }

        scope.filter { it.isOutput }.forEach {
            s.append("\n\tvar output ${it.name} : ${it.dataType?.name}")
        }

        if (includeState) {
            scope.filter { !it.isOutput && !it.isInput }.forEach {
                s.append("\n\tvar state input ${it.name} : ${it.dataType?.name} as ${it.name}_pre")
                s.append("\n\tvar state output ${it.name} : ${it.dataType?.name} as ${it.name}_post")
            }
        }

        s.append("\n\n\toptions{}")
        s.append("\n\n\tgroup{\n\t}")

        s.append("\n}")
        return s.toString()
    }

    /**
     * Read XML or DSL format.
     */
    fun readTable(file: File): GeneralizedTestTable {
        return if (file.name.endsWith("xml"))
            parseTableXML(file.absolutePath)
        else
            parseTableDSL(file)
    }

    fun constructTable(table: GeneralizedTestTable) =
            AutomatonBuilderPipeline(table).transform()

    fun constructSMV(table: GeneralizedTestTable, superEnum: EnumType) =
            SmvConstructionPipeline(constructTable(table), superEnum).transform()

    fun constructSMV(automaton: AutomataTransformerState, superEnum: EnumType) =
            SmvConstructionPipeline(automaton, superEnum).transform()

    fun analyzeCounterExample(automaton: TestTableAutomaton, testTable: GeneralizedTestTable, counterExample: CounterExample): MutableList<Mapping> {
        val analyzer = CounterExampleAnalyzer(automaton, testTable, counterExample)
        analyzer.run()
        return analyzer.rowMapping
    }

    fun print(gtt: GeneralizedTestTable): String {
        val stream = StringWriter()
        val p = DSLTablePrinter(CodeWriter(stream))
        p.print(gtt)
        return stream.toString()
    }


/*
    private class SuperEnumCreator : AstVisitor<Unit?>() {
        override fun defaultVisit(obj: Any): Unit? = null
        private val seq = ArrayList<String>()
        private val type = EnumType(seq)

        fun getType(): SMVType {
            return type
        }

        override fun visit(etd: EnumerationTypeDeclaration): Unit? {
            seq.addAll(etd.allowedValues.map { it.text })
            return null
        }
    }*/
}