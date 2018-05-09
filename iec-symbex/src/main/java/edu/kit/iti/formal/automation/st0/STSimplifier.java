package edu.kit.iti.formal.automation.st0;

/*-
 * #%L
 * iec-symbex
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
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import edu.kit.iti.formal.automation.st.ast.*;
import edu.kit.iti.formal.automation.st0.trans.*;
import edu.kit.iti.formal.automation.stoo.STOOSimplifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Weigl (26.06.2014), Augusto Modanese
 * @version 1
 */
public class STSimplifier {
    public static final int PROGRAM_VARIABLE = VariableDeclaration.FLAG_COUNTER.get();
    private List<ST0Transformation> transformations = new ArrayList<>();
    private State state = new State();

    public STSimplifier(TopLevelElements inputElements) {
        state.inputElements = inputElements;
    }

    public STSimplifier(TopLevelElements inputElements, STOOSimplifier.State stooState) {
        this(inputElements);
        state.stooState = stooState;
    }

    public void addDefaultPipeline() {
        transformations.add(new GlobalVariableListEmbedding());
        transformations.add(new FunctionBlockEmbedding());
        transformations.add(LoopUnwinding.getTransformation());
        transformations.add(TimerToCounter.getTransformation());
        transformations.add(new ArrayEmbedder());
        transformations.add(new StructEmbedding());
        //transformations.add(SFCResetReplacer.getTransformation());
        transformations.add(new RemoveActionsFromProgram());
        transformations.add(new ConstantEmbedder());  // EXPERIMENTAL
        transformations.add(new TrivialBranchReducer());  // EXPERIMENTAL
    }

    public void transform() {
        initializeState();
        for (ST0Transformation t : transformations) {
            t.transform(state);
        }
    }

    public List<ST0Transformation> getTransformations() {
        return transformations;
    }

    public TopLevelElements getProcessed() {
        TopLevelElements l = new TopLevelElements();
        l.add(state.allTypeDeclaration);
        l.addAll(state.functions.values());
        l.add(state.theProgram);
        return l;
    }

    /**
     * Register FUNCTION AND FUNCTIONBLOCKS
     */
    public void initializeState() {
        int programs = 0;
        for (TopLevelElement tle : state.inputElements) {
            if (tle instanceof ProgramDeclaration) {
                programs++;
                state.theProgram = (ProgramDeclaration) tle;
            } else if (tle instanceof FunctionBlockDeclaration) {
                FunctionBlockDeclaration declaration = (FunctionBlockDeclaration) tle;
                state.functionBlocks.put(declaration.getName(), declaration);
            } else if (tle instanceof TypeDeclarations) {
                TypeDeclarations typeDeclarations = (TypeDeclarations) tle;
                appendTypeDeclarations(typeDeclarations);
            } else if (tle instanceof FunctionDeclaration) {
                FunctionDeclaration function = (FunctionDeclaration) tle;
                state.functions.put(function.getName(), function);
            } else if (tle instanceof GlobalVariableListDeclaration) {
                GlobalVariableListDeclaration gvl = (GlobalVariableListDeclaration) tle;
                state.globalVariableList.getScope().addVariables(gvl.getScope());
            } else {
                throw new IllegalArgumentException("TLE: " + tle.getClass() + " is not handled yet.");
            }
        }

        if (programs != 1 || state.theProgram == null) {
            System.out.println(state.inputElements.size());
            throw new IllegalArgumentException("There must be exactly one program in the List of TLE. " + programs + " found. Elements: " + state.inputElements.size());
        }
    }

    /**
     * @return Whether we are transforming OO code
     */
    public boolean isTransformingOO() {
        return state.stooState != null;
    }

    private void appendTypeDeclarations(TypeDeclarations typeDeclarations) {
        for (TypeDeclaration td : typeDeclarations) {
            boolean allowed = true;
            if (td instanceof StructureTypeDeclaration) {
                state.structs.put(td.getTypeName(), (StructureTypeDeclaration) td);
                continue;
            }
            switch (td.getBaseTypeName()) {
                case "SINT":
                case "INT":
                case "LINT":
                case "DINT":
                case "UINT":
                case "USINT":
                case "ULINT":
                case "UDINT":
                case "BOOL":
                case "ENUM":
                    allowed = true;
                    break;
                default:
                    allowed = false;
                    break;

            }
            if (allowed)
                state.allTypeDeclaration.add(td);
            else
                throw new IllegalArgumentException("There is an unsupported type declared! " + td.getTypeName() + " with baseType type " + td.getBaseTypeName());
        }
    }

    public static class State {
        public TopLevelElements inputElements;
        public ProgramDeclaration theProgram;
        public Map<String, FunctionBlockDeclaration> functionBlocks = new HashMap<>();
        public Map<String, FunctionDeclaration> functions = new HashMap<>();
        public Map<String, StructureTypeDeclaration> structs = new HashMap<>();
        public TypeDeclarations allTypeDeclaration = new TypeDeclarations();
        public GlobalVariableListDeclaration globalVariableList = new GlobalVariableListDeclaration();
        public STOOSimplifier.State stooState;
    }
}
