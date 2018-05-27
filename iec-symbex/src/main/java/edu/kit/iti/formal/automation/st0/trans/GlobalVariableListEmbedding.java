/*
 * #%L
 * iec61131lang
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

package edu.kit.iti.formal.automation.st0.trans;

import edu.kit.iti.formal.automation.st.ast.SymbolicReference;
import edu.kit.iti.formal.automation.st.ast.VariableDeclaration;
import edu.kit.iti.formal.automation.st.util.AstMutableVisitor;
import edu.kit.iti.formal.automation.st0.STSimplifier;

/**
 * Embed the GVL in the program's scope and rename the GVL variables accordingly.
 *
 * @author Augusto Modanese, Alexander Weigl
 */
public class GlobalVariableListEmbedding implements ST0Transformation {
    /**
     * Prefix to access GVL variables.
     */
    private static final String GVL_NAME = "GVL";

    /**
     * New prefix to rename variables with.
     */
    private static final String GVL_NEW_PREFIX = "GVL" + "$";

    @Override
    public void transform(STSimplifier.State state) {
        // Noop if no GVL
        if (state.globalVariableList == null)
            return;

        // Squash "GVL" prefix with variable name
        //state.inputElements.accept(new GVLRenameVisitor());

        // Move variable declarations to program
        state.globalVariableList.getScope().forEach(v -> {
            // **** Renaming disabled! ****
            //v.setName(GVL_NEW_PREFIX + v.getName());
            v.setType(VariableDeclaration.Companion.getGLOBAL());
            state.theProgram.getScope().add(v);
        });

        state.theProgram.getScope().getParent().forEach(v -> {
            // **** Renaming disabled! ****
            //v.setName(GVL_NEW_PREFIX + v.getName());
            v.setType(VariableDeclaration.Companion.getGLOBAL());
            state.theProgram.getScope().add(v);
        });
    }

    static class GVLRenameVisitor extends AstMutableVisitor {
        @Override
        public Object visit(SymbolicReference symbolicReference) {
            if (symbolicReference.hasSub())
                symbolicReference.getSub().accept(this);
            if (symbolicReference.getIdentifier().equals(GVL_NAME)) {
                assert symbolicReference.hasSub();
                symbolicReference.getSub().setIdentifier(GVL_NEW_PREFIX + symbolicReference.getSub().getIdentifier());
                return symbolicReference.getSub();
            }
            return super.visit(symbolicReference);
        }
    }
}
