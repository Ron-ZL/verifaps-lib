package edu.kit.iti.formal.automation.rvt

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import edu.kit.iti.formal.automation.Console
import edu.kit.iti.formal.automation.IEC61131Facade
import edu.kit.iti.formal.automation.SymbExFacade
import edu.kit.iti.formal.automation.builtin.BuiltinLoader
import edu.kit.iti.formal.automation.st.ast.PouElements
import edu.kit.iti.formal.automation.st.ast.PouExecutable
import edu.kit.iti.formal.automation.st.ast.ProgramDeclaration
import edu.kit.iti.formal.automation.visitors.Utils
import edu.kit.iti.formal.smv.NuXMVInvariantsCommand
import edu.kit.iti.formal.smv.NuXMVOutput
import edu.kit.iti.formal.smv.NuXMVProcess
import edu.kit.iti.formal.smv.SMVFacade
import edu.kit.iti.formal.smv.ast.SLiteral
import edu.kit.iti.formal.smv.ast.SMVModule
import org.antlr.v4.runtime.CharStreams
import java.io.File

object RvtAps {
    @JvmStatic
    fun main(args: Array<String>) {
        RvtApsApp().main(args)
    }
}

class RvtApsApp : CliktCommand(
        help = "",
        name = "rvt-aps.sh") {

    enum class Miter {
        NONE, UNTIL
    }

    val verbose by option("-v", "--verbose",
            help = "enable verbose mode, set the logger to info")
            .flag()

    val debugMode by option("--debug",
            help = "sets the logger to DEBUG level")
            .flag()

    val oldVersion by option("--old",
            help = "old version of the plc software", metavar = "FILE")
            .file(readable = true)
            .default(File("old.st"))

    val newVersion by option("--new",
            help = "new version of the plc software", metavar = "FILE")
            .file(readable = true)
            .default(File("new.st"))

    val disableST0Pipeline by option("-P", help = "disable ST0 pipeline")
            .flag("-p")

    val outputSMVOutputName by option("--to-smv-file",
            help = "name of the smv file",
            metavar = "FILENAME")
            //.file(writable = true)
            .default(("main.smv"))

    val outputDirectory by option("--output", "-o",
            help = "name of the smv-module", metavar = "FOLDER")
            .file()
            .default(File("."))

    val doNotVerify by option("-D", "--do-not-verify",
            help = "skips the call of nuXmv if set")
            .flag("-d")

    val nuxmvMethod by option("-m", "--method",
            help = "")
            .convert { NuXMVInvariantsCommand.valueOf(it) }
            .default(NuXMVInvariantsCommand.IC3)

    val nuxmvExecutable by option("--nuxmv",
            help = "", envvar = "NUXMV")

    val library by option("-L", "--library", help = "library files").file().multiple()

    val oldName by option("--old-name",
            help = "", metavar = "STRING")

    val newName by option("--new-name",
            help = "", metavar = "STRING")

    val nuxmvOutput = "nuxmv.log"

    var nuxmvResult: NuXMVOutput? = null

    val miter by option("--miter",
            help = "", metavar = "{NONE|UNTIL}")
            .convert { Miter.valueOf(it) }
            .default(Miter.NONE)

    val untilMiterEndTrigger by option("--until-miter-cond", metavar = "FILE").file()

    override fun run() {
        Console.configureLoggingConsole()

        val newModule = loadPouExecutable(library, newVersion, newName)
        val oldModule = loadPouExecutable(library, oldVersion, oldName)
        val factory = when (miter) {
            Miter.NONE -> { a: SMVModule, b: SMVModule -> RegressionVerification(a, b) }
            Miter.UNTIL -> {

                val endExpr = untilMiterEndTrigger?.let {
                    SMVFacade.expr(it.bufferedReader().readText())
                } ?: SLiteral.FALSE
                { a: SMVModule, b: SMVModule -> ReVeWithMiter(a, b, UntilMiter(endExpr, a, b, GloballyMiter(a, b))) }
            }
        }

        val pipeline = RvtApsPipeline(oldModule, newModule,
                verbose = verbose,
                nuxmvOutput = nuxmvOutput, debugMode = debugMode, disableST0Pipeline = disableST0Pipeline,
                outputSMVOutputName = outputSMVOutputName,
                outputDirectory = outputDirectory,
                nuxmvExecutable = nuxmvExecutable ?: "nuXmv not supplied",
                nuxmvMethod = nuxmvMethod,
                reveFactory = factory
        )
        pipeline.build()
        if (!doNotVerify) pipeline.verify()
        else
            Console.info("Skip verification on user request.")
    }
}

internal fun loadPouExecutable(library: List<File>, file: File, name: String?): PouExecutable {
    Console.info("Parse program ${file.absolutePath} with libraries ${library}")
    val r = if (name != null)
        IEC61131Facade.readProgramsWithLibrary(library, listOf(file), name)[0]
    else
        IEC61131Facade.readProgramsWithLibrary(library, listOf(file))[0]
    if (r == null) {
        Console.fatal("Could not find a program in $file. Given selector: $name")
        System.exit(2)
        throw IllegalStateException()
    } else return r
}

class RvtApsPipeline(val oldModule: PouExecutable,
                     val newModule: PouExecutable,
                     val reveFactory: (old: SMVModule, new: SMVModule) -> RegressionVerification,
                     val verbose: Boolean = false,
                     val debugMode: Boolean = false,
                     val disableST0Pipeline: Boolean = false,
                     val outputSMVOutputName: String = "dual.smv",
                     val outputDirectory: File = File("."),
                     val nuxmvMethod: NuXMVInvariantsCommand = NuXMVInvariantsCommand.IC3,
                     val nuxmvExecutable: String = "nuXmv",
                     val nuxmvOutput: String = "nuxmv.log") {
    var nuxmvResult: NuXMVOutput? = null
    var outputSMV: File? = null

    fun build() {
        Console.info("Symbolic Execution!")
        val newVersion = SymbExFacade.evaluateProgram(newModule, disableST0Pipeline)
        val oldVersion = SymbExFacade.evaluateProgram(oldModule, disableST0Pipeline)

        Console.info("Construction of Miter")
        val rvt = reveFactory(oldVersion, newVersion)
        rvt.run()

        //write output
        outputSMV = File(outputDirectory, outputSMVOutputName)
        Console.info("Create output folder $outputDirectory")
        outputDirectory.mkdirs()
        outputSMV?.bufferedWriter()?.use { fw -> rvt.writeTo(fw) }
    }

    fun verify() {
        val mc = NuXMVProcess(outputSMV
                ?: throw IllegalStateException("verify() called before build()"))
        mc.commands = nuxmvMethod.commands as Array<String>
        mc.executablePath = nuxmvExecutable
                ?: throw IllegalArgumentException("No nuXmv executable set! export NUXMV=... or --nuxmv ")
        mc.outputFile = File(outputDirectory, nuxmvOutput)
        mc.workingDirectory = outputDirectory
        nuxmvResult = mc.call()
        when (nuxmvResult!!) {
            is NuXMVOutput.Verified ->
                Console.info("verified")
            is NuXMVOutput.Cex ->
                Console.info("not verified, counterexample available in log file")
            is NuXMVOutput.Error ->
                Console.error("Error occured in nuXmv. Please refer to log file for more information.")
        }
    }

    companion object {
        /**
         * Finds the first two programs in the given code and creates an [RvtApp]
         * with the other elements.
         */
        fun createRvtForSingleSource(code: String): RvtApsPipeline {
            val elements = IEC61131Facade.file(CharStreams.fromString(code))
            elements.addAll(BuiltinLoader.loadDefault())
            val (fst, snd) = separatePrograms(elements)
            val old = Utils.findProgram(fst)!!
            val new = Utils.findProgram(snd)!!
            val aps = RvtApsPipeline(old, new, { a, b -> RegressionVerification(a, b) })
            return aps
        }
    }
}


fun separatePrograms(elements: PouElements): Pair<PouElements, PouElements> {
    val (fst, snd) = elements.filter { it is ProgramDeclaration }
    val copy = PouElements(elements)
    copy.removeAll { it is ProgramDeclaration }
    val a = PouElements(copy);a.add(fst)
    val b = PouElements(copy);b.add(snd)
    return a to b
}

