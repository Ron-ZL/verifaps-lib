package edu.kit.iti.formal.automation.st;

/*-
 * #%L
 * iec61131lang
 * %%
 * Copyright (C) 2016 Alexander Weigl
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

import edu.kit.iti.formal.automation.IEC61131Facade;
import edu.kit.iti.formal.automation.TestUtils;
import edu.kit.iti.formal.automation.parser.IEC61131Parser;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Weigl
 * @version 1 (02.08.16)
 */
@RunWith(Parameterized.class)
public class InValidExpressionTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> setup() throws IOException {
        return TestUtils.INSTANCE.loadLines("/edu/kit/iti/formal/automation/st/expressions/invalid.txt");
    }

    @Parameterized.Parameter
    public String invalid = "";

    @Test
    public void testInValidExpression() {
        IEC61131Parser parser = IEC61131Facade.INSTANCE.getParser(CharStreams.fromString(invalid));
        boolean error = false;
        try {
            IEC61131Parser.ExpressionContext ctx = parser.expression();
            System.out.println(ctx.toString(parser));
            System.out.println(IEC61131Facade.INSTANCE.print(IEC61131Facade.INSTANCE.expr(invalid)));
            System.out.println(ctx.stop.getTokenSource().nextToken());
            error = !ctx.stop.getTokenSource().nextToken().getText().equals("EOF");
            //error = false;
        } catch (Exception e) {
            error = true;
        }
        error = error || parser.getNumberOfSyntaxErrors() > 0;
        System.out.println(parser.getNumberOfSyntaxErrors());
        assertTrue(error);
    }
}

