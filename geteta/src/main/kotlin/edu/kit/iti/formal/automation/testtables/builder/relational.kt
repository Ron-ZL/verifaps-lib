package edu.kit.iti.formal.automation.testtables.builder


import edu.kit.iti.formal.automation.datatypes.AnyBit
import edu.kit.iti.formal.automation.testtables.model.ColumnCategory
import edu.kit.iti.formal.automation.testtables.model.ControlCommand
import edu.kit.iti.formal.automation.testtables.model.ProgramVariable
import edu.kit.iti.formal.automation.testtables.model.chapterMarksForProgramRuns
import edu.kit.iti.formal.automation.testtables.rtt.*
import edu.kit.iti.formal.smv.SMVTypes
import edu.kit.iti.formal.smv.ast.SLiteral
import edu.kit.iti.formal.util.fail

/**
 * Translate the [ControlCommand.Pause] into the assumption
 * @author Alexander Weigl
 * @version 1 (04.08.18)
 */
object PlayPauseToAssumption : AbstractTransformer<SMVConstructionModel>() {
    override fun transform() {
        // Add the pause variable into the table signature
        val pauseVars = model.testTable.programRuns.mapIndexed { i, _ ->
            ProgramVariable(pauseVariableP(), AnyBit.BOOL, SMVTypes.BOOLEAN,
                    ColumnCategory.ASSUME, programRun = i)
        }
        model.testTable.programVariables.addAll(pauseVars)
        model.testTable.region.flat().forEach {
            pauseVars.forEachIndexed { i, run ->
                val stutter = i in it.pauseProgramRuns
                val variable = run.internalVariable(model.testTable.programRuns)
                it.inputExpr[variable.name] =
                        if (stutter) variable else variable.not()
            }
        }
    }
}

object BackwardToAssumption : AbstractTransformer<SMVConstructionModel>() {
    override fun transform() {
        val cmarks = model.testTable.chapterMarksForProgramRuns
        //add input variables set and reset to the table signature
        cmarks.forEach { (run, tableRows) ->
            tableRows.forEach { row ->
                model.testTable.programVariables +=
                        ProgramVariable(setVariableP(row), AnyBit.BOOL, SMVTypes.BOOLEAN,
                                ColumnCategory.ASSUME, programRun = run)
                model.testTable.programVariables +=
                        ProgramVariable(resetVariableP(row), AnyBit.BOOL, SMVTypes.BOOLEAN,
                                ColumnCategory.ASSUME, programRun = run)
            }
        }

        val rows = model.testTable.region.flat()
        //translate backward(n)
        for (tableRow in rows) {
            for ((run, targets) in cmarks) {
                val runName = model.testTable.programRuns[run]
                for (target in targets) {
                    val isJumpTarget = tableRow.id == target
                    tableRow.inputExpr[setVariableTT(tableRow.id, runName)] = //TODO how to distinguish from the other inputs? run is needed
                            if (isJumpTarget) SLiteral.TRUE else SLiteral.FALSE

                    if (isJumpTarget&&  !tableRow.duration.isOneStep) {
                        fail("The table row `${tableRow.id}? in table `${model.testTable.name}` is addressed by a backward-jump, " +
                                "but does not have the required of duration [1,1]. Please split up the row manually. ")
                    }
                }
            }

            val backwardCommands =
                    tableRow.controlCommands.filterIsInstance<ControlCommand.Backward>()

            for ((run, targets) in cmarks) {
                val runName = model.testTable.programRuns[run]
                for (target in targets) {
                    val resetActivated = null != backwardCommands.find {
                        it.affectedProgramRun == run && it.jumpToRow == target
                    }
                    tableRow.inputExpr[resetVariableTT(target, runName)] = if (resetActivated) SLiteral.TRUE else SLiteral.FALSE
                }
            }
        }
    }
}