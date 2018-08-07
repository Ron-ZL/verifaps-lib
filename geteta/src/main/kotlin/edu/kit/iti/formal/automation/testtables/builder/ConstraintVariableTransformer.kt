/*
 * geteta
 *
 * Copyright (C) 2016-2018 -- Alexander Weigl <weigl@kit.edu>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package edu.kit.iti.formal.automation.testtables.builder


import edu.kit.iti.formal.automation.testtables.GetetaFacade
import edu.kit.iti.formal.automation.testtables.schema.DataType

/**
 * Created by weigl on 17.12.16.
 */
class ConstraintVariableTransformer : TableTransformer {
    override fun accept(tt: ConstructionModel) {
        val gtt = tt.testTable
        val mt = tt.tableModule
        for (cv in gtt.constraintVariables) {
            val svar = cv.internalVariable(gtt.programRuns)
            if (cv.dataType == DataType.ENUM) {
                svar.dataType = tt.superEnumType
            }
            mt.frozenVars.add(svar)

            if (cv.constraint != null) {
                mt.initExpr.add(
                        GetetaFacade.exprToSMV(cv.constraint!!,
                                svar, 0, tt.variableContext))
            }
            //TODO add invar for each frozenvar
        }
    }
}
