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

import javax.validation.constraints.NotNull;

/**
 * Definition of a custom field column.
 *
 * @author Gregory Fouquet
 *
 */
public class CustomFieldColumnDef implements ColumnDef {
	private final int index;
	private final String code;

	CustomFieldColumnDef(@NotNull String code, int index) {
		super();
		this.index = index;
		this.code = code;
	}

	@Override
	/**
	 *
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnDef#getIndex()
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return the code of the custom field.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnDef#getHeader()
	 */
	@Override
	public String getHeader() {
		return getCode();
	}

	/**
	 * @see org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnDef#is(org.squashtest.tm.service.internal.batchimport.testcase.excel.ColumnProcessingMode)
	 */
	@Override
	public boolean is(@NotNull ColumnProcessingMode processingMode) {
		return processingMode == ColumnProcessingMode.OPTIONAL;
	}

	// GENERATED:START
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (code == null ? 0 : code.hashCode());
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CustomFieldColumnDef other = (CustomFieldColumnDef) obj;
		if (code == null) {
			if (other.code != null) {
				return false;
			}
		} else if (!code.equals(other.code)) {
			return false;
		}
		if (index != other.index) {
			return false;
		}
		return true;
	}
	// GENERATED:END

}
