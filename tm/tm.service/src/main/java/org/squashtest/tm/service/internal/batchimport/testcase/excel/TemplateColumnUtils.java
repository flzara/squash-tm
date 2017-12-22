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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.util.ReflectionUtils;

/**
 * Utilities methods for {@link TemplateColumn} enums
 *
 * @author Gregory Fouquet
 *
 */
final class TemplateColumnUtils {
	private static final TemplateColumnUtils INSTANCE = new TemplateColumnUtils();

	private Map<Class<?>, Map<String, TemplateColumn>> headerCacheByClass = new HashMap<>();

	private TemplateColumnUtils() {
		super();
	}

	/**
	 * Coerce the given "header" (content of cell @ row[0]) into a enum value. Coerces unknown headers to
	 * <code>null</code>.
	 *
	 *
	 * @param enumType
	 *            the enum type that shall be looked up. should not be <code>null</code>.
	 * @param header
	 *            the header, should not be <code>null</code>
	 * @return the mtching enum value or <code>null</code>
	 */
	public static TemplateColumn coerceFromHeader(@NotNull Class<? extends Enum<?>> enumType, @NotNull String header) {
		if (!TemplateColumn.class.isAssignableFrom(enumType)) {
			throw new IllegalArgumentException(enumType.getName() + " should implement TemplateColumn");
		}

		return INSTANCE.doCoerceFromHeader(enumType, header);
	}

	/**
	 * @param object
	 * @param header
	 * @return
	 */
	private TemplateColumn doCoerceFromHeader(Class<? extends Enum<?>> enumType, String header) {
		Map<String, TemplateColumn> headerCache = headerCacheByClass.get(enumType);

		if (headerCache == null) {
			synchronized (headerCacheByClass) {
				headerCache = new HashMap<>();
				populateCache(enumType, headerCache);
				headerCacheByClass.put(enumType, headerCache);
			}
		}

		return headerCache.get(header);
	}

	private <E extends Enum<?>> void populateCache(Class<E> enumType, Map<String, TemplateColumn> headerCache) {
		E[] values = values(enumType);

		for (E value : values) {
			headerCache.put(((TemplateColumn) value).getHeader(), (TemplateColumn) value);
		}
	}

	/**
	 * Retrieves an enum's values from the enum's class.
	 *
	 * @param enumType
	 *            type of the enum which values we want
	 * @return the values of the enum
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<?>> E[] values(Class<E> enumType) {
		Method valuesMethod = ReflectionUtils.findMethod(enumType, "values");

		return (E[]) ReflectionUtils.invokeMethod(valuesMethod, enumType);
	}
}
