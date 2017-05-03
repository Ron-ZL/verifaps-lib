// --------------------------------------------------------
// Code generated by Papyrus Java
// --------------------------------------------------------

package edu.kit.iti.formal.smv.ast;

/*-
 * #%L
 * smv-model
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

import edu.kit.iti.formal.smv.SMVAstVisitor;

/**
 *
 */
public class SVariable extends SMVExpr implements Comparable<SVariable> {
    private String name;
    private SMVType datatype;

    public SVariable() {

    }

    public SVariable(String n, SMVType enumType) {
        name = n;
        datatype = enumType;
    }

    public static SVariable bool(String name) {
        return new SVariable(name, SMVType.BOOLEAN);
    }

    public static Builder create(String name) {
        return new Builder(name);
    }

    public static Builder create(String fmt, Object... vals) {
        return create(String.format(fmt, vals));
    }

    @Override public int hashCode() {
        return name.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SVariable)) {
            return false;
        }
        SVariable other = (SVariable) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public SMVType getDatatype() {
        return datatype;
    }

    public void setDatatype(SMVType datatype) {
        this.datatype = datatype;
    }

    public SMVType getSMVType() {
        return datatype;
    }

    @Override public <T> T accept(SMVAstVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public int compareTo(SVariable o) {
        return name.compareTo(o.name);
    }

    @Override public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    static public class Builder {
        SVariable v = new SVariable();

        public Builder(String name) {
            v.name = name;
        }

        public SVariable with(SMVType type) {
            v.setDatatype(type);
            return v;
        }

        public SVariable with(int width, GroundDataType dt) {
            return with(new SMVType.SMVTypeWithWidth(dt, width));
        }

        public SVariable withSigned(int width) {
            return with(new SMVType.SMVTypeWithWidth(GroundDataType.SIGNED_WORD,
                    width));
        }

        public SVariable withUnsigned(int width) {
            return with(
                    new SMVType.SMVTypeWithWidth(GroundDataType.UNSIGNED_WORD,
                            width));
        }

        public SVariable asBool() {
            return with(SMVType.BOOLEAN);
        }
    }
}
