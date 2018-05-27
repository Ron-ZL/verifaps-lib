/*-
 * #%L
 * aps-rvt
 * %%
 * Copyright (C) 2017 Alexander Weigl
 * %%
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
 * You should have received a clone of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package edu.kit.iti.formal.automation.smv

import org.junit.Test
import kotlin.test.assertTrue

/**
 * @author Alexander Weigl
 * @version 1 (14.09.17)
 */
class RvtAppTest {
    @Test
    fun testVerifySimple(): Unit {
        val old = "simple1_new"
        val new = "simple1_old"
        assertEqualBehaviour(old, new)
    }

    @Test
    fun testVerifyCasesIf(): Unit {
        val old = "caseif_old"
        val new = "caseif_new"
        assertEqualBehaviour(old, new)
    }

    private fun assertEqualBehaviour(old: String, new: String) {
        val app = RvtApp("src/test/resources/$old.st",
                        "src/test/resources/$new.st")
        app.build()
        assertTrue(app.verify());
    }
}
