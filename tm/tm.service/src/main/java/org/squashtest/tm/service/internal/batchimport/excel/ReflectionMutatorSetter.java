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

import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Sets a property using a mutator (ie a "setter").
 *
 * @author Gregory Fouquet
 *
 */
public final class ReflectionMutatorSetter<VAL, TARGET> implements PropertySetter<VAL, TARGET> {
	private final String mutatorName;
	/**
	 * mutator's formal parameter type.
	 */
	private final Class<?> paramType;
	/**
	 * when value is optional, <code>null</code> values are not set.
	 */
	private final boolean optionalValue;
	private Method mutator;

	/**
	 * Creates a {@link ReflectionMutatorSetter} with mandatory values, ie the {@link ReflectionMutatorSetter} shall try
	 * to set any given value. As the value to be set can be <code>null</code>, we cannot infer its type, hence the
	 * paramType param.
	 *
	 * @param propName
	 *            name of the field.
	 * @param paramType
	 *            type of the mutator formal param
	 * @return
	 */
	public static <V, T> ReflectionMutatorSetter<V, T> forProperty(@NotNull String propName, Class<V> paramType) {
		return new ReflectionMutatorSetter<>(propName, paramType, false);

	}

	/**
	 * Creates a {@link ReflectionMutatorSetter} with optional values, ie the {@link ReflectionMutatorSetter} shall
	 * discard any <code>null</code> value.
	 *
	 * @param propName
	 * @return
	 */
	public static <V, T> ReflectionMutatorSetter<V, T> forOptionalProperty(@NotNull String propName) {
		return new ReflectionMutatorSetter<>(propName, null, true);
	}

	private ReflectionMutatorSetter(@NotNull String fieldName, Class<VAL> paramType, boolean optionalValue) {
		super();
		this.mutatorName = "set" + StringUtils.capitalize(fieldName);
		this.paramType = paramType;
		this.optionalValue = optionalValue;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.excel.PropertySetter#set(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void set(VAL value, TARGET target) {
		if (optionalValue && value == null) {
			return;
		}
		if (!optionalValue && value == null) {
			throw new NullMandatoryValueException(mutatorName);
		}

		if (mutator == null) {
			mutator = ReflectionUtils.findMethod(target.getClass(), mutatorName, paramType(value));

			if (mutator == null) {
				throw new IllegalStateException("Could not find method named '" + mutatorName + "(" + paramType(value)
						+ ")' in object of type '" + target.getClass() + "'. Maybe you mistyped field name.");
			}

			mutator.setAccessible(true);
		}

		ReflectionUtils.invokeMethod(mutator, target, value);
	}

	/**
	 * @return
	 */
	private Class<?> paramType(VAL value) {
		// If paramType and value are null we are doomed. Yet, it should not happen.
		return paramType != null ? paramType : value.getClass();
	}

}
