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
package edu.kit.iti.formal.automation.testtables

import edu.kit.iti.formal.automation.testtables.exception.SpecificationInterfaceMisMatchException
import edu.kit.iti.formal.automation.testtables.model.options.TableOptions
import edu.kit.iti.formal.smv.SMVFacade
import edu.kit.iti.formal.smv.ast.SMVExpr
import edu.kit.iti.formal.smv.ast.SMVModule
import edu.kit.iti.formal.smv.ast.SMVType
import edu.kit.iti.formal.smv.ast.SVariable
import java.util.stream.Collectors

/**
 * @author Alexander Weigl
 * @version 1 (13.12.16)
 */
class BinaryModelGluer(private val options: TableOptions,
                       private val table: SMVModule,
                       private val code: SMVModule) : Runnable {
    val product = SMVModule()

    override fun run() {
        product.name = MAIN_MODULE
        product.inputVars.addAll(code.moduleParameter)

        product.stateVars.add(SVariable(
                CODE_MODULE,
                SMVType.Module(code.name,
                        code.moduleParameter)))

        val taP = table.moduleParameter.stream().map { v ->
            if (code.moduleParameter.contains(v)) {
                return@table.getModuleParameter().stream().map v
            } else {
                //output of program code
                if (!code.stateVars.contains(v)) {
                    throw SpecificationInterfaceMisMatchException(code, v)
                }

                val `var` = SVariable(CODE_MODULE + "." + v, v.smvType)
                return@table.getModuleParameter().stream().map if (options.isUseNext)
                    SMVFacade.next(`var`)
                else
                    `var`
            }
        }.collect<List<SMVExpr>, Any>(Collectors.toList())
        product.stateVars.add(SVariable(TABLE_MODULE,
                SMVType.Module(table.name, taP)))
    }

    companion object {
        val TABLE_MODULE = "table"
        val CODE_MODULE = "code"
        val MAIN_MODULE = "main"
    }
}
