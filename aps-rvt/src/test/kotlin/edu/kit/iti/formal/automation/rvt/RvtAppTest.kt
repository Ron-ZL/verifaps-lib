/*-
 * #%L
 * aps-rvt
 * %%
 * Copyright (C) 2017 Alexander Weigl
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
package edu.kit.iti.formal.automation.rvt

import edu.kit.iti.formal.smv.NuXMVOutput
import org.junit.Assert
import org.junit.Assume
import org.junit.Test

/**
 * @author Alexander Weigl
 * @version 1 (14.09.17)
 */
class RvtAppTest {
    @Test
    fun testVerifySimple(): Unit {
        assumeNuxmv()
        val old = "simple1_new"
        val new = "simple1_old"
        assertEqualBehaviour(old, new)
    }

    private fun assumeNuxmv() {
        try {
            var pb = ProcessBuilder("nuxmv")
            var p = pb.start()
            p.waitFor()
        } catch (e: Exception) {
            Assume.assumeFalse(true)
        }
    }

    @Test
    fun testVerifyCasesIf(): Unit {
        assumeNuxmv()
        val old = "caseif_old"
        val new = "caseif_new"
        assertEqualBehaviour(old, new)
    }

    private fun assertEqualBehaviour(old: String, new: String) {
        val app = RvtApp(
                TransformationPipeline.create("src/test/resources/$old.st", false),
                TransformationPipeline.create("src/test/resources/$new.st", false))
        app.build()
        Assert.assertEquals(NuXMVOutput.Verified, app.verify());
    }
}
