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
package org.squashtest.tm.validation.constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.squashtest.tm.validation.validator.HasDefaultItemCollectionValidator;

/**
 * Add this constraint to a Collection field or a Collection returning method. It checks that the constrained collection
 * contains a unique item marked as default.
 * 
 * Note : for this constraint to work properly, items have to be comparable using equals() !
 * 
 * @author Gregory Fouquet
 * 
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HasDefaultItemCollectionValidator.class)
public @interface HasDefaultItem {
	String message() default "{org.squashtest.tm.service.validation.constraint.HasDefaultItem}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	/**
	 * The name of the boolean propery that shall be read to check if an item is default or not.
	 * 
	 * @return
	 */
	String value() default "default";
}
