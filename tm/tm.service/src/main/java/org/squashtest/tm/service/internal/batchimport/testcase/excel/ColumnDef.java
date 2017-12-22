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

/**
 * DEfinition of a column as found in an import workbook.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface ColumnDef {
	/**
	 * The index of the column, i.e. its position in a row.
	 * 
	 * @return the index
	 */
	int getIndex();

	/**
	 * Returns the header of the column i.e. the actual content of first row.
	 * 
	 * @return
	 */
	String getHeader();

	/**
	 * Tells if this column def matches the given processing mode.
	 * 
	 * @param processingMode
	 * @return
	 */
	boolean is(ColumnProcessingMode processingMode);
}
