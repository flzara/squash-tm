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

import org.squashtest.tm.service.importer.ImportMode;

/**
 * @author Gregory Fouquet
 *
 */
public class CannotCoerceException extends RuntimeException {
	private static final long serialVersionUID = 839887673080933124L;

	public final String errorI18nKey; // NOSONAR immutable const
	public final String updateImpactI18nKey; // NOSONAR immutable const
	public final String createImpactI18nKey;// NOSONAR immutable const

	public CannotCoerceException(Throwable cause, String errorI18nKey) {
		super(cause);
		this.errorI18nKey = errorI18nKey;
		this.updateImpactI18nKey = null;
		this.createImpactI18nKey = null;
	}

	/**
	 * @param string
	 * @param errorI18nKey
	 */
	public CannotCoerceException(String string, String errorI18nKey) {
		this.errorI18nKey = errorI18nKey;
		this.updateImpactI18nKey = null;
		this.createImpactI18nKey = null;
	}

	public CannotCoerceException(Throwable cause, String errorI18nKey, String impactI18nKey) {
		super(cause);
		this.errorI18nKey = errorI18nKey;
		this.updateImpactI18nKey = impactI18nKey;
		this.createImpactI18nKey = impactI18nKey;
	}

	public CannotCoerceException(Throwable cause, String errorI18nKey, String updateImpactI18nKey,
			String createImpactI18nKey) {
		super(cause);
		this.errorI18nKey = errorI18nKey;
		this.updateImpactI18nKey = updateImpactI18nKey;
		this.createImpactI18nKey = createImpactI18nKey;
	}

	/**
	 * @return the errorI18nKey
	 */
	public String getErrorI18nKey() {
		return errorI18nKey;
	}

	public String getImpactI18nKey(ImportMode mode) {
		String toReturn;
		switch (mode) {
		case CREATE:
			toReturn = createImpactI18nKey;
			break;
		case UPDATE :
			toReturn = updateImpactI18nKey;
			break;
		default:
			toReturn = null;
			break;
		}
		return toReturn;
	}



}
