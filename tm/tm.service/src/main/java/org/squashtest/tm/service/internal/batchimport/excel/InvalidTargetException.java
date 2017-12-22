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

import org.squashtest.tm.service.importer.ImportStatus;

public class InvalidTargetException extends RuntimeException{

	


	/**
	 * 
	 */
	private static final long serialVersionUID = -1667784602112693995L;

	private final ImportStatus status;

	private final String errori18nMessage;

	private final String impacti18nMessage;

	public InvalidTargetException(ImportStatus status, String errori18nMessage, String impacti18nMessage) {
		super();
		this.status = status;
		this.errori18nMessage = errori18nMessage;
		this.impacti18nMessage = impacti18nMessage;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public ImportStatus getStatus() {
		return status;
	}

	public String getErrori18nMessage() {
		return errori18nMessage;
	}

	public String getImpacti18nMessage() {
		return impacti18nMessage;
	}


}
