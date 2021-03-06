package edu.kit.iti.formal.automation.st0.trans

/*-
 * #%L
 * iec-symbex
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

import edu.kit.iti.formal.automation.st.Statements
import edu.kit.iti.formal.automation.st.ast.*
import edu.kit.iti.formal.automation.st.util.AstMutableVisitor
import edu.kit.iti.formal.automation.st0.TransformationState

/**
 * Created by weigl on 28/10/14.
 */
class SFCResetReplacer : CodeTransformation {
    override fun transform(state: TransformationState): TransformationState {
        state.stBody = state.stBody.accept(SFCResetReplacerImpl()) as StatementList
        return state
    }
}


class SFCResetReplacerImpl : AstMutableVisitor() {
    override fun visit(assignmentStatement: AssignmentStatement): Statement {
        try {
            val (identifier) = assignmentStatement.location as SymbolicReference
            val expression = assignmentStatement.expression

            if (identifier.endsWith("\$SFCReset")) {
                return Statements.ifthen(
                        expression,
                        AssignmentStatement(
                                SymbolicReference(identifier.replace("SFCReset", "_state")),
                                EnumLit(null, "Init")
                        ))
            }
        } catch (e: ClassCastException) {
        }
        return super.visit(assignmentStatement)
    }
}
