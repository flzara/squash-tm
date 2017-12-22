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
package org.squashtest.tm.domain.customfield;

import org.squashtest.tm.core.foundation.i18n.Internationalizable;

/**
 * Enumerates where a custoim field should be displayed.
 * 
 * @author Gregory Fouquet
 * 
 */
public enum RenderingLocation implements Internationalizable {
	LINK_TABLE,
	TEST_PLAN, 
	EXECUTION, 
	SEARCH_CRITERIA,
	STEP_TABLE;
	
	

	private static final String I18N_NAMESPACE = "label.customField.renderingLocations.";
	
	/**
	 * @see org.squashtest.tm.core.foundation.i18n.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return I18N_NAMESPACE + name();
	}
	
}
