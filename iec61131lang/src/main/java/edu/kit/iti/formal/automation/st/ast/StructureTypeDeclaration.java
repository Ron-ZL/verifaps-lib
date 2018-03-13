package edu.kit.iti.formal.automation.st.ast;

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
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import edu.kit.iti.formal.automation.datatypes.Any;
import edu.kit.iti.formal.automation.datatypes.RecordType;
import edu.kit.iti.formal.automation.scope.GlobalScope;
import edu.kit.iti.formal.automation.scope.LocalScope;
import edu.kit.iti.formal.automation.visitors.Utils;
import edu.kit.iti.formal.automation.visitors.Visitor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by weigl on 13.06.14.
 *
 * @author weigl, Augusto Modanese
 * @version $Id: $Id
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class StructureTypeDeclaration extends TypeDeclaration<StructureInitialization> {
    @NotNull LocalScope fields = new LocalScope();

    public StructureTypeDeclaration(@NotNull String typeName, @NotNull List<VariableDeclaration> fields) {
        super(typeName);
        fields.forEach(this.fields::add);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T accept(@NotNull Visitor<T> visitor) {
        return visitor.visit(this);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public Any getDataType(@NotNull GlobalScope globalScope) {
        RecordType rt = new RecordType(getTypeName(), this);
        for (VariableDeclaration s : fields.getLocalVariables().values()) {
            s.setDataType(s.getTypeDeclaration().getDataType(globalScope));
            rt.getFields().add(s.copy());
        }
        setBaseType(rt);
        return rt;
    }

    @NotNull
    @Override public StructureTypeDeclaration copy() {
        StructureTypeDeclaration t = new StructureTypeDeclaration();
        t.setRuleContext(getRuleContext());
        t.initialization = Utils.copyNull(initialization);
        t.fields = fields.copy();
        t.typeName = typeName;
        t.baseType = baseType;
        t.baseTypeName = baseTypeName;
        return t;
    }

}
