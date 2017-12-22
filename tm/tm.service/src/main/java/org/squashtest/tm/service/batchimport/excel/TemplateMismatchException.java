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
package org.squashtest.tm.service.batchimport.excel;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown when an import file doesnt' match the expected template.
 * 
 * @author Gregory Fouquet
 * 
 */
public class TemplateMismatchException extends RuntimeException {
	private static final long serialVersionUID = -3318286142079157710L;

	private List<WorksheetFormatStatus> worksheetFormatStatuses = new ArrayList<>();

	public TemplateMismatchException() {
		super();
	}

	public TemplateMismatchException(List<WorksheetFormatStatus> worksheetFormatStatuses) {
		this();
		this.worksheetFormatStatuses = worksheetFormatStatuses;
	}
	public void addWorksheetFormatStatus(List<WorksheetFormatStatus> worksheetFormatStatuses){
		this.worksheetFormatStatuses.addAll(worksheetFormatStatuses);
	}
	public List<WorksheetFormatStatus> getWorksheetFormatStatuses() {
		return worksheetFormatStatuses;
	}

}
