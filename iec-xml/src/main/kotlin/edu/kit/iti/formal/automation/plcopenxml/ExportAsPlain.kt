package edu.kit.iti.formal.automation.plcopenxml

/*-
 * #%L
 * iec-xml
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

import edu.kit.iti.formal.automation.IEC61131Facade
import org.jdom2.JDOMException

import java.io.IOException

/**
 * @author Alexander Weigl
 * @version 1 (20.02.18)
 */
object ExportAsPlain {
    @Throws(JDOMException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        println(IEC61131Facade.print(IECXMLFacade.readPLCOpenXml(args[0])))
    }
}