package edu.kit.iti.formal.automation.smv.dt;

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

import edu.kit.iti.formal.automation.datatypes.*;
import edu.kit.iti.formal.automation.exceptions.IllegalTypeException;
import edu.kit.iti.formal.smv.ast.GroundDataType;
import edu.kit.iti.formal.smv.ast.SMVType;

import java.lang.reflect.Type;

/**
 * Created by weigl on 11.12.16.
 */
public class DataTypeTranslator implements TypeTranslator{
    public static final DataTypeTranslator INSTANCE = new DataTypeTranslator();

    @Override
    public SMVType translate(Any datatype) {
        DefaultTranslatorVisitor dtv = new DefaultTranslatorVisitor();
        return datatype.accept(dtv);
    }

    class DefaultTranslatorVisitor implements DataTypeVisitor<SMVType> {
        @Override
        public SMVType visit(AnyReal real) {
            return null;
        }

        @Override
        public SMVType visit(AnyReal.Real real) {
            return null;
        }

        @Override
        public SMVType visit(AnyReal.LReal real) {
            return null;
        }

        @Override
        public SMVType visit(AnyBit anyBit) {
            if (anyBit == AnyBit.BOOL) {
                return SMVType.BOOLEAN;
            }
            return new SMVType.SMVTypeWithWidth(GroundDataType.UNSIGNED_WORD,
                    anyBit.getBitLength());
        }

        @Override
        public SMVType visit(AnyDate.DateAndTime dateAndTime) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(AnyDate.TimeOfDay timeOfDay) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(AnyDate.Date date) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(AnyInt inttype) {
       /*TODO: Make this configurable
            return new SMVType.SMVTypeWithWidth(
                inttype.isSigned() ?
                        GroundDataType.SIGNED_WORD :
                        GroundDataType.UNSIGNED_WORD, inttype.getBitLength());
        */
            return new SMVType.SMVTypeWithWidth(GroundDataType.SIGNED_WORD, 16);
        }

        @Override
        public SMVType visit(EnumerateType enumerateType) {
            return new SMVType.EnumType(enumerateType.getAllowedValues());
        }

        @Override
        public SMVType visit(TimeType timeType) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(RangeType rangeType) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(RecordType recordType) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(PointerType pointerType) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(IECString.String string) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(IECString.WString wString) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(IECArray iecArray) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(AnyNum anyNum) {
            throw new IllegalTypeException("Could not match");
        }

        @Override
        public SMVType visit(ClassDataType classDataType) {
            return null;
        }
    }
}
