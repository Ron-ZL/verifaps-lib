package edu.kit.iti.formal.jpox;

/*-
 * #%L
 * iec-xml
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

import edu.kit.iti.formal.automation.plcopenxml.IECXMLFacade;
import org.jdom2.JDOMException;
 import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Alexander Weigl
 * @version 1 (30.05.17)
 */
public class RunPPUTest {
    @Test
    public void test() throws JDOMException, IOException {
        String out = IECXMLFacade.INSTANCE.extractPLCOpenXml(
                new File("src/test/resources/test.xml").getAbsolutePath());
        System.out.println(out);
    }

    @Test
    public void testPPUNew() throws JDOMException, IOException {
        String out = IECXMLFacade.INSTANCE.extractPLCOpenXml(
                new File("ppu.xml").getAbsolutePath());
        System.out.println(out);
    }
}
