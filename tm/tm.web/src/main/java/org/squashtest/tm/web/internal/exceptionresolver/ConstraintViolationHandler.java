/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.exceptionresolver;

import java.util.List;

import javax.validation.ConstraintViolation;

/**
 * Interface of objects able to populate a list of {@link FieldValidationErrorModel} from a {@link ConstraintViolation}.
 * 
 * @author Gregory Fouquet
 * 
 */
interface ConstraintViolationHandler {
	/**
	 * If this object is able to handle the violation, it adds a FieldValidationErrorModel to the list and returns true.
	 * 
	 * @param violation
	 *            violation to handled
	 * @param errors
	 *            list of errors to populate
	 * @return true if handled violation
	 */
	boolean handle(ConstraintViolation<?> violation, List<FieldValidationErrorModel> errors);
}
