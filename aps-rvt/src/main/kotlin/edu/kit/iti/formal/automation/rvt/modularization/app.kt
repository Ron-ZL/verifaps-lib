package edu.kit.iti.formal.automation.rvt.modularization

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import edu.kit.iti.formal.automation.IEC61131Facade
import edu.kit.iti.formal.automation.sfclang.getUniqueName
import edu.kit.iti.formal.util.info
import kotlinx.coroutines.newFixedThreadPoolContext
import java.io.File

/**
 *
 * @author Alexander Weigl
 * @version 1 (15.07.18)
 */
object ModApp {
    val processContext = newFixedThreadPoolContext(1, "aps-rvt")

    @JvmStatic
    fun main(args: Array<String>) {
        ModularizationApp().main(args)
    }
}

class ModularizationApp : CliktCommand() {
    val v by option("-v", help = "enable verbose mode").flag()
    val old by option("--old", help = "old program ").required()
    val new by option("--new", help = "new program").required()
    val list: Boolean by option("--list", "-l", help = "show call sites").flag()
    val showContexts by option("--show-context",
            help = "print contextes of call site pairs infered by symbex")
            .multiple().unique()

    val contexts by option("--context",
            help = "expected equality of sub modules",
            metavar = "callsite=callsite/<smv>")
            .multiple()

    val run: Boolean by option("--run", "-r", help = "run prover").flag()

    val allowedCallSites by option("-s", "--site", help = "call sites to abstract").multiple()

    val outputFolder by option("-o", "--output", help = "output folder")
            .convert { File(it) }
            .default(File(getUniqueName("output_")))

    val library by option().file().multiple()

    override fun run() {
        outputFolder.mkdirs()
        val (oldExec, newExec) = IEC61131Facade.readProgramsWLPN(library, listOf(old, new))
        require(oldExec != null) { "Could not find program in $old" }
        require(newExec != null) { "Could not find program in $new" }

        val oldProgram = ModFacade.createModularProgram(oldExec, outputFolder, "old")
        val newProgram = ModFacade.createModularProgram(newExec, outputFolder, "new")
        val m = ModularProver(
                oldProgram, newProgram,
                callSitePairs = ModFacade.findCallSitePairs(allowedCallSites, oldProgram, newProgram),
                context = ReveContext(),
                outputFolder = outputFolder
        )

        when {
            list -> m.printCallSites()
            contexts.isNotEmpty() -> m.printContexts()
            else -> {
                info("Output folder: ${outputFolder}")
                info("Generate SMV files")
                m.proof()
                info("Files generated")
            }
        }
    }
}