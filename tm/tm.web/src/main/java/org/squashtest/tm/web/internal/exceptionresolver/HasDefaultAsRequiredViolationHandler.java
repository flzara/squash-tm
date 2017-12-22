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

import org.squashtest.tm.validation.constraint.HasDefaultAsRequired;

/**
 * Handles {@link ConstraintViolation} from {@link HasDefaultAsRequired} constraints.
 * 
 * NOTE we can build something more elaborate / generic when we have other TYPE level constraints.
 * 
 * @author Gregory Fouquet
 * 
 */
public class HasDefaultAsRequiredViolationHandler implements ConstraintViolationHandler {

	/**
	 * @see org.squashtest.tm.web.internal.exceptionresolver.ConstraintViolationHandler#handle(javax.validation.ConstraintViolation,
	 *      java.util.List)
	 */
	@Override
	public boolean handle(ConstraintViolation<?> violation, List<FieldValidationErrorModel> errors) {
		if (violation.getConstraintDescriptor().getAnnotation() instanceof HasDefaultAsRequired) {
			errors.add(new FieldValidationErrorModel("", "defaultValue", violation.getMessage(), violation.getInvalidValue()));

			return true;
		}
		return false;
	}

}
