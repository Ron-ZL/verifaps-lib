package edu.kit.iti.formal.automation

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import edu.kit.iti.formal.automation.analysis.CheckForTypes
import edu.kit.iti.formal.automation.analysis.ReporterMessage
import edu.kit.iti.formal.automation.builtin.BuiltinLoader
import edu.kit.iti.formal.automation.parser.IEC61131Lexer
import edu.kit.iti.formal.automation.parser.IEC61131Parser
import edu.kit.iti.formal.automation.parser.IECParseTreeToAST
import edu.kit.iti.formal.automation.st.ast.PouElements
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.io.File
import java.util.*

object Flycheck {
    @JvmStatic
    fun main(args: Array<String>) = FlycheckApp().main(args)
}

class FlycheckApp : CliktCommand() {
    val verbose by option(help = "enable verbose mode").flag()
    val include by option("-L", help = "folder for looking includes")
            .file()
            .multiple()

    val files by argument(name = "FILE", help = "Files to check")
            .file()
            .multiple()

    override fun run() {
        val base = BuiltinLoader.loadDefault()
        val r = FlycheckRunner(
                files.map { CharStreams.fromFileName(it.absolutePath) },
                base,
                verbose,
                include
        )
        r.run()
        r.messages.forEach {
            Console.writeln("[${it.level.toUpperCase()}] (${it.sourceName}@${it.lineNumber}:${it.charInLine}) ${it.message} (${it.category})"
            )
        }
    }
}

class FlycheckRunner(
        val streams: List<CharStream>,
        val library: PouElements = PouElements(),
        val verbose: Boolean = false,
        val includeStubs: List<File> = arrayListOf()) {

    private val reporter = CheckForTypes.DefaultReporter()
    val messages: MutableList<ReporterMessage>
        get() = reporter.messages

    private val errorListener = MyAntlrErrorListener(messages)

    fun run() {
        streams.forEach { parse(it) }
        types()
    }

    fun parse(stream: CharStream) {
        val lexer = IEC61131Lexer(stream)
        val parser = IEC61131Parser(CommonTokenStream(lexer))

        lexer.removeErrorListeners()
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)
        val ctx = parser.start()
        val tles = ctx.accept(IECParseTreeToAST()) as PouElements
        library.addAll(tles)
    }

    fun types() {
        IEC61131Facade.resolveDataTypes(library)
        library.accept(CheckForTypes(reporter))
    }

    internal class MyAntlrErrorListener(private val messages: MutableCollection<ReporterMessage>)
        : ANTLRErrorListener {
        override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int,
                                 charPositionInLine: Int, msg: String?, e: RecognitionException?) {
            val file = (offendingSymbol as Token?)?.tokenSource?.sourceName
            messages += ReporterMessage(file ?: "", line, charPositionInLine, msg!!, "syntax", "level")
        }

        override fun reportAttemptingFullContext(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, conflictingAlts: BitSet?, configs: ATNConfigSet?) {}
        override fun reportAmbiguity(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, exact: Boolean, ambigAlts: BitSet?, configs: ATNConfigSet?) {}
        override fun reportContextSensitivity(recognizer: Parser?, dfa: DFA?, startIndex: Int, stopIndex: Int, prediction: Int, configs: ATNConfigSet?) {}
    }
}




