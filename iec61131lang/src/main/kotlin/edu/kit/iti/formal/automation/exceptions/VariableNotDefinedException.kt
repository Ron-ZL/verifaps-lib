package edu.kit.iti.formal.automation.exceptions

/*-
 * #%L
 * iec61131lang
 * %%
 * Copyright (C) 2016 Alexander Weigl
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

import edu.kit.iti.formal.automation.scope.Scope
import edu.kit.iti.formal.automation.st.ast.SymbolicReference

/**
 * Created by weigl on 24.11.16.
 *
 * @author weigl
 * @version $Id: $Id
 */
class VariableNotDefinedException
/**
 *
 * Constructor for VariableNotDefinedException.
 *
 * @param variableDeclarations a [edu.kit.iti.formal.automation.scope.Scope] object.
 * @param reference a [edu.kit.iti.formal.automation.st.ast.SymbolicReference] object.
 */
(
        /**
         *
         * Getter for the field `localScope`.
         *
         * @return a [edu.kit.iti.formal.automation.scope.Scope] object.
         */
        val scope: Scope,
        /**
         *
         * Getter for the field `reference`.
         *
         * @return a [edu.kit.iti.formal.automation.st.ast.SymbolicReference] object.
         */
        val reference: SymbolicReference) : IECException() {

    /** {@inheritDoc}  */
    override fun getMessage(): String {
        return "Variable: " + reference.identifier + " not defined in the given localScope " + scope
    }
}
