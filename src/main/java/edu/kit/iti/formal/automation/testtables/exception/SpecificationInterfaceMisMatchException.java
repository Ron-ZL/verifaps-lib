package edu.kit.iti.formal.automation.testtables.exception;

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

import edu.kit.iti.formal.smv.ast.SMVModule;
import edu.kit.iti.formal.smv.ast.SVariable;

/**
 * @author Alexander Weigl
 * @version 1 (13.12.16)
 */
public class SpecificationInterfaceMisMatchException extends RuntimeException {
    public SpecificationInterfaceMisMatchException() {
        super();
    }

    public SpecificationInterfaceMisMatchException(String message) {
        super(message);
    }

    public SpecificationInterfaceMisMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpecificationInterfaceMisMatchException(Throwable cause) {
        super(cause);
    }

    protected SpecificationInterfaceMisMatchException(String message, Throwable cause,
                                                      boolean enableSuppression,
                                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SpecificationInterfaceMisMatchException(SMVModule code, SVariable v) {
        super(String.format("Could not find variable '%s' in module: %s", v.getName(), code.getName()));
    }
}
