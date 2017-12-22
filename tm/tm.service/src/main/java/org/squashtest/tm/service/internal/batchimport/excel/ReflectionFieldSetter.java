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
package org.squashtest.tm.service.internal.batchimport.excel;

import java.lang.reflect.Field;

import javax.validation.constraints.NotNull;

import org.springframework.util.ReflectionUtils;

/**
 * Sets an object's field through reflection. When value is declared optional, <code>null</code> values are not set.
 *
 * @author Gregory Fouquet
 *
 */
public final class ReflectionFieldSetter<VAL, TARGET> implements PropertySetter<VAL, TARGET> {
	private final String fieldName;
	/**
	 * when value is optional, <code>null</code> values are not set.
	 */
	private boolean optionalValue = false;
	private Field field;

	/**
	 * Creates a {@link ReflectionFieldSetter} with mandatory values, ie the {@link ReflectionFieldSetter} shall try to
	 * set any given value.
	 *
	 * @param name
	 *            name of the field.
	 * @return
	 */
	public static <V, T> ReflectionFieldSetter<V, T> forField(@NotNull String name) {
		return new ReflectionFieldSetter<>(name);
	}

	/**
	 * Creates a {@link ReflectionFieldSetter} with optional values, ie the {@link ReflectionFieldSetter} shall discard
	 * any <code>null</code> value.
	 *
	 * @param name
	 * @return
	 */
	public static <V, T> ReflectionFieldSetter<V, T> forOptionalField(@NotNull String name) {
		ReflectionFieldSetter<V, T> res = new ReflectionFieldSetter<>(name);
		res.optionalValue = true;
		return res;
	}

	private ReflectionFieldSetter(@NotNull String fieldName) {
		super();
		this.fieldName = fieldName;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.PropertySetter#set(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void set(VAL value, TARGET target) {
		if (optionalValue && value == null) {
			return; // applies default
		}
		if (!optionalValue && value == null) {
			throw new NullMandatoryValueException(fieldName);
		}

		if (field == null) {
			field = ReflectionUtils.findField(target.getClass(), fieldName);

			if (field == null) {
				throw new IllegalStateException("Could not find field named '" + fieldName + "' in object of type '"
						+ target.getClass() + "'. Maybe you mistyped field name.");
			}

			field.setAccessible(true);
		}

		ReflectionUtils.setField(field, target, value);
	}

}
