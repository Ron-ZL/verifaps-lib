package edu.kit.iti.formal.automation;

/*-
 * #%L
 * iec-symbex
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

import edu.kit.iti.formal.automation.st.ast.PouElements;
import edu.kit.iti.formal.automation.st0.STSimplifier;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Alexander Weigl
 * @version 1 (28.06.17)
 */
public class ST0Tests {
    @Test
    public void fbEmbedding1() throws IOException {
        assertResultST0("fbembedding_1");
    }

    @Test
    public void structEmbedding() throws IOException {
        assertResultST0("struct_embedding");
    }

    private void assertResultST0(String file) throws IOException {
        PouElements st = IEC61131Facade.INSTANCE.file(
                CharStreams.fromStream(getClass().getResourceAsStream(file + ".st")));
        PouElements st0exp = IEC61131Facade.INSTANCE.file(
                CharStreams.fromStream(getClass().getResourceAsStream(file + ".st0")));

        IEC61131Facade.INSTANCE.resolveDataTypes(st);

        STSimplifier stSimplifier = new STSimplifier(st);
        stSimplifier.addDefaultPipeline();
        stSimplifier.transform();
        st = stSimplifier.getProcessed();

        Assert.assertEquals(IEC61131Facade.INSTANCE.print(st0exp), IEC61131Facade.INSTANCE.print(st));
    }

}
