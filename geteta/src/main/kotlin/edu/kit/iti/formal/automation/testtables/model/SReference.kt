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
package edu.kit.iti.formal.automation.testtables.model


import edu.kit.iti.formal.automation.Console
import edu.kit.iti.formal.automation.testtables.GetetaFacade
import edu.kit.iti.formal.smv.ast.SVariable

/**
 * Created by weigl on 15.12.16.
 */
class SReference(var cycles: Int, var variable: SVariable) {
    init {
        if (cycles >= 0) {
            Console.fatal("Only support for negative reference (looking backwards).")
            System.exit(0)
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as SReference?

        if (cycles != that!!.cycles) return false
        return variable == that.variable

    }

    override fun hashCode(): Int {
        var result = cycles
        result = 31 * result + variable.hashCode()
        return result
    }

    /**
     * creates a reference variable
     *
     * @return
     */
    fun asVariable(): SVariable {
        val newName = GetetaFacade.getHistoryName(variable, Math.abs(cycles))
        return SVariable(newName, variable.dataType!!)
    }
}