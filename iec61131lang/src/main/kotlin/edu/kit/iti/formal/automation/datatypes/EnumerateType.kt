package edu.kit.iti.formal.automation.datatypes

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

import java.util.*

/**
 * Created by weigl on 10.06.14.
 *
 * @author weigl
 */
class EnumerateType(name: String = "ENUM",
                    var allowedValues: MutableList<String> = ArrayList())
    : AnyDt(name) {

    val bitlength: Int
        get() {
            return Math.ceil(Math.log(allowedValues.size.toDouble())).toInt()
        }

    var defValue: String? = null
        set(defValue) {
            assert(defValue != null && isAllowedValue(field!!))
            field = defValue
        }

    constructor(name: String, allowedValues: MutableList<String>,
                defValue: String = allowedValues[0]) : this(name, allowedValues) {
        this.defValue = defValue
    }

    /** {@inheritDoc}  */
    override fun repr(obj: Any): String {
        return if (name == null)
            obj.toString()
        else
            "$name#$obj"
    }

    /** {@inheritDoc}  */
    override fun <T> accept(visitor: DataTypeVisitorNN<T>) = visitor.visit(this)

    fun isAllowedValue(value: String) = this.allowedValues.contains(value)
}
