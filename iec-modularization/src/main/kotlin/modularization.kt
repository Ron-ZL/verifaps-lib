package edu.kit.iti.formal.automation.modularization

import edu.kit.iti.formal.automation.Console
import edu.kit.iti.formal.automation.IEC61131Facade
import edu.kit.iti.formal.automation.st.ast.PouElements
import edu.kit.iti.formal.automation.st.ast.PouExecutable
import edu.kit.iti.formal.smv.NuXMVInvariantsCommand
import edu.kit.iti.formal.smv.NuXMVOutput
import edu.kit.iti.formal.smv.NuXMVProcess
import java.io.File

fun readProgramsOrError(p: String): PouElements {
    val (c, ok) = IEC61131Facade.fileResolve(File(p))
    if (ok.isNotEmpty()) {
        ok.forEach { Console.fatal(it.print()) }
        throw IllegalStateException("Aborted due to errors")
    }
    return c
}

/**
 *
 * @author Alexander Weigl
 * @version 1 (14.07.18)
 */
class ModularProver(val args: ModularizationApp) {
    val oldProgram = ModularProgram(args.old)
    val newProgram = ModularProgram(args.new)
    var prove: Pred = { false }
    val callSitePairs: CallSiteMapping by lazy {
        args.allowedCallSites
                .map {
                    val (a, b) = it.split("=")
                    val o = oldProgram.callSites.find { it.repr() == a }
                            ?: throw IllegalArgumentException("could not find $a")
                    val n = newProgram.callSites.find { it.repr() == b }
                            ?: throw IllegalArgumentException("could not find $a")

                    o to n
                }
    }
    val proveStrategy: ProofStrategy = DefaultStrategy(this)

    fun printCallSites(): Unit {
        Console.info("Call sites for the old program: ${oldProgram.filename}")
        oldProgram.callSites.forEach {
            Console.info("${it.repr()} in line ${it.statement.startPosition}")
        }
        Console.info("Call sites for the new program: ${newProgram.filename}")
        newProgram.callSites.forEach {
            Console.info("${it.repr()} in line ${it.statement.startPosition}")
        }
    }

    fun createFiles() {
        args.outputFolder.mkdirs()
        prove = proveStrategy.createTask(oldProgram.entry, newProgram.entry, callSitePairs)
    }

    fun runSolvers() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

abstract class PredTask(val name: String) : Pred {
    abstract fun check(): Boolean
    final override fun invoke(): Boolean {
        Console.info("Run: $name")
        val b = check()
        Console.info("Finish: $name")
        return b
    }
}

class SourceEqualTask(val oldProgram: PouExecutable, val newProgram: PouExecutable)
    : PredTask("Check source code of ${oldProgram.name} against $newProgram.name") {
    override fun check() = oldProgram.stBody!! == newProgram.stBody!!
}

class NuXmvTask(val smvFile: File, val logFile: File, name: String = "") : PredTask(name) {
    private fun runSolver(): Boolean {
        Console.info("Run solver for $name")
        val nuxmv = NuXMVProcess(smvFile)
        nuxmv.outputFile = logFile
        nuxmv.commands = NuXMVInvariantsCommand.IC3.commands as Array<String>
        val result = nuxmv.call()
        Console.info("Solver finished for $name with $result")
        return when (result) {
            NuXMVOutput.Verified -> true
            is NuXMVOutput.Error -> throw IllegalStateException("Error in SMV file: $smvFile")
            is NuXMVOutput.NotVerified -> false
        }
    }

    override fun check(): Boolean {
        smvFile.parentFile.mkdirs()
        logFile.parentFile.mkdirs()
        return runSolver()
    }
}

