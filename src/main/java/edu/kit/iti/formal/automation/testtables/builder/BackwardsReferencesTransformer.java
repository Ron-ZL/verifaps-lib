package edu.kit.iti.formal.automation.testtables.builder;

/*-
 * #%L
 * geteta
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

import edu.kit.iti.formal.automation.testtables.DelayModuleBuilder;
import edu.kit.iti.formal.automation.testtables.Facade;
import edu.kit.iti.formal.smv.ast.SVariable;

/**
 * Created by weigl on 17.12.16.
 */
public class BackwardsReferencesTransformer implements TableTransformer {
    @Override
    public void accept(TableTransformation tt) {
        tt.getTestTable().getReferences().forEach(
                (key, value) -> {
                    DelayModuleBuilder b = new DelayModuleBuilder(key, value);
                    b.run();
                    //Add Var
                    SVariable var = new SVariable(Facade.getHistoryName(key), b.getModuleType());
                    tt.getTableModule().getStateVars().add(var);

                    //add helper module
                    tt.getHelperModules().add(b.getModule());
                }
        );
    }
}
