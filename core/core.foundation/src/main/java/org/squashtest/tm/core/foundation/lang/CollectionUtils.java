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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * @author Gregory Fouquet
 *
 */
public final class CollectionUtils {
	private CollectionUtils() {
		super();
	}

	/**
	 * Coerces the given non-null collection to a list. Collection order is not garanteed to be preserved.
	 *
	 * @param collection collection
	 * @param <T> <T>
	 * @return List<T>
	 */
	public static <T> List<T> coerceToList(@NotNull Collection<T> collection) {
		if (collection instanceof List) {
			return (List<T>) collection;
		}

		List<T> res = new ArrayList<>(collection.size());
		res.addAll(collection);

		return res;
	}
}
