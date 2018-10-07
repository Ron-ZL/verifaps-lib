package edu.kit.iti.formal.automation.smv.translators

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

import edu.kit.iti.formal.automation.datatypes.values.Value
import edu.kit.iti.formal.smv.ast.SLiteral

/**
 * @author Alexander Weigl
 * @version 1 (16.10.17)
 */
class DefaultValueTranslator : ValueTranslator {
    var tt: TypeTranslator = DefaultTypeTranslator.INSTANCE

    override fun translate(init: Value<*, *>): SLiteral {
        return SLiteral.create(init.value)
                .with(this.tt.translate(init.dataType))
    }

    companion object {
        val INSTANCE: ValueTranslator = DefaultValueTranslator()
    }
}