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
package org.squashtest.tm.web.internal.model.builder;

/**
 * Helper to translate entities into jeditable combo model
 * 
 * @author Gregory Fouquet
 * 
 */
public final class JeditableComboHelper {
	private static final int ID_OFFSET = 10;

	private JeditableComboHelper() {
		super();
	}

	/**
	 * Coerces numeric entity ids into ids which correctly sorts into a jeditable combo.
	 * When "no value" would be represented by a null id and we want it to appear first in the combo, we have to convert
	 * it to a numeric which would correctly be sorted and which would not conflict with an existing id.
	 * 
	 * <strong>Note :</strong> ids coming from frontend have to be coerced back using {@link #coerceIntoEntityId(Long)}
	 * 
	 * @param entityId
	 * @return id suitable for combo. never <code>null</code>
	 */
	public static long coerceIntoComboId(Long entityId) {
		if (entityId == null) {
			return 0;
		}
		return entityId + ID_OFFSET;
	}

	/**
	 * Inverse operation of {@link #coerceIntoComboId(Long)}
	 * 
	 * @param comboId
	 * @return passes through <code>null</code> values, translates into entity id otherwise.
	 * @see #coerceIntoComboId(Long)
	 */
	public static Long coerceIntoEntityId(Long comboId) {
		if (comboId == null || comboId == 0) {
			return null;
		}
		return comboId - ID_OFFSET;
	}
}
