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
package org.squashtest.tm.validation.validator;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.ReflectionUtils;
import org.squashtest.tm.core.foundation.lang.Assert;
import org.squashtest.tm.validation.constraint.HasDefaultItem;

/**
 * @author Gregory Fouquet
 *
 */
public class HasDefaultItemCollectionValidator implements ConstraintValidator<HasDefaultItem, Collection<?>> {
	private String prop;

	/**
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
	 */
	@Override
	public void initialize(HasDefaultItem constraintAnnotation) {
		prop = constraintAnnotation.value();
		Assert.propertyNotBlank("prop", "HasDefaultItem.value should not be a blank String");

	}

	/**
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(Collection<?> values, ConstraintValidatorContext context) {
		boolean hasDefault = false;

		for (Object item : values) {
			Field f = findField(item);
			Boolean val = readField(f, item);

			if (Boolean.TRUE.equals(val)) {
				if (hasDefault) {
					// more than 1 default is not valid
					return false;
				}
				hasDefault = true;
			}
		}

		return hasDefault;
	}

	private Boolean readField(Field f, Object item) throws IllegalArgumentException {
		Boolean val;
		try {
			val = (Boolean) f.get(item);

		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
		return val;
	}

	private Field findField(Object item) throws IllegalArgumentException, SecurityException {
		Class<?> c = item.getClass();
		Field f = ReflectionUtils.findField(c, prop, boolean.class);
		f = f != null ? f : ReflectionUtils.findField(c, prop, Boolean.class);

		if (f == null) {
			throw new IllegalArgumentException("Item of type " + c + " does not have any " + prop + " field");
		}

		f.setAccessible(true);
		return f;
	}

}
