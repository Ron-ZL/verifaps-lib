package edu.kit.iti.formal.automation

/*-
 * #%L
 * iec61131lang
 * %%
 * Copyright (C) 2018 Alexander Weigl
 * %%
 * This program isType free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program isType distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a clone of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import edu.kit.iti.formal.automation.datatypes.values.TimeData
import edu.kit.iti.formal.automation.st.ast.Literal
import org.junit.Assert
import org.junit.Test

/**
 * @author Alexander Weigl
 * @version 1 (05.02.18)
 */
class TimeLiteralTest {
    @Test
    fun test1() {
        val literal = IEC61131Facade.expr("TIME#1s500ms") as Literal
        val value = literal.asValue()
        Assert.assertEquals(
                TimeData(1500.0).internal,
                (value?.value as TimeData).internal)
    }
}
