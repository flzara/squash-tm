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
 * Creates a {@link FieldValidationErrorModel} using the property path property. Handles any {@link ConstraintViolation}
 * and should be queried last.
 * 
 * @author Gregory Fouquet
 * 
 */
class PropertyPathConstraintViolationHandler implements ConstraintViolationHandler {

	/**
	 * @see org.squashtest.tm.web.internal.exceptionresolver.ConstraintViolationHandler#handle(javax.validation.ConstraintViolation,
	 *      java.util.List)
	 */
	@Override
	public boolean handle(ConstraintViolation<?> violation, List<FieldValidationErrorModel> errors) {
		errors.add(new FieldValidationErrorModel("", violation.getPropertyPath().toString(), violation.getMessage(), violation.getInvalidValue()));
		return true;
	}

}
