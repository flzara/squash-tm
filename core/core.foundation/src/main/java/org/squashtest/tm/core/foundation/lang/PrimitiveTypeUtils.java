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
package org.squashtest.tm.core.foundation.lang;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author Gregory Fouquet
 *
 */
public final class PrimitiveTypeUtils {
	private static final Map<Class<?>, Class<?>> primitiveByWrapper = new HashMap<>();

	static {
		primitiveByWrapper.put(Long.class, long.class);
		primitiveByWrapper.put(Integer.class, int.class);
		primitiveByWrapper.put(Float.class, float.class);
		primitiveByWrapper.put(Double.class, double.class);
		primitiveByWrapper.put(Boolean.class, boolean.class);
	}

	private PrimitiveTypeUtils() {
		super();
	}

	public static boolean isPrimitiveWrapper(Class<?> type) {
		return primitiveByWrapper.containsKey(type);
	}

	public static Class<?> wrapperToPrimitive(Class<?> wrapperType) {
		return primitiveByWrapper.get(wrapperType);
	}
}
