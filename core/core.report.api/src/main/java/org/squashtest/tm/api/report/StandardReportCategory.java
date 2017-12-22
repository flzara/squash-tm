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
package org.squashtest.tm.api.report;

import org.squashtest.tm.core.foundation.i18n.Internationalizable;

/**
 * @author bsiri
 * @author Gregory Fouquet
 *
 */
public enum StandardReportCategory implements Internationalizable {
	EXECUTION_PHASE("report.category.executionphase.name"), 
	PREPARATION_PHASE("report.category.preparationphase.name"), 
	VARIOUS("report.category.various.name");

	private final String i18nKey;
	
	/**
	 * @param i18nKey
	 */
	private StandardReportCategory(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	/**
	 * @see org.squashtest.tm.core.foundation.i18n.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return i18nKey;
	}

}
