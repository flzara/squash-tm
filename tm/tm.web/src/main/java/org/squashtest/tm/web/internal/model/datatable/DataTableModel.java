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
package org.squashtest.tm.web.internal.model.datatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of a jQuery ui datatable used when populating a datatable through ajax. Created according to the api defined at
 * http://www.datatables.net/usage/server-side
 * 
 * @author Gregory Fouquet
 * 
 */
public class DataTableModel<T> {
	// total nr of records before filtering.
	private long iTotalRecords;

	// total nr of records after filtering.
	private long iTotalDisplayRecords;

	// should be the synchronization token sent by the datatable which requested data
	public final String sEcho; // NOSONAR Field is immutable

	// optional comma-sep'd list of column names.
	private String sColumns;

	// list of data rows as arrays of objects.
	private List<T> aaData = new ArrayList<>();



	public DataTableModel(String sEcho) {
		super();
		this.sEcho = sEcho;
	}

	public void setAaData(List<T> aaData) {
		this.aaData = aaData;
	}

	public List<T> getAaData() {
		return aaData;
	}

	public void setiTotalRecords(int iTotalRecords) {
		this.iTotalRecords = iTotalRecords;
	}

	public long getiTotalRecords() {
		return iTotalRecords;
	}

	public void setiTotalDisplayRecords(int iTotalDisplayRecords) {
		this.iTotalDisplayRecords = iTotalDisplayRecords;
	}

	public long getiTotalDisplayRecords() {
		return iTotalDisplayRecords;
	}

	public void setComumnNames(String[] comumnNames) {
		StringBuilder sb = new StringBuilder();

		for (String colName : comumnNames) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(colName);
		}

		sColumns = sb.toString();
	}

	public String getsColumns() {
		return sColumns;
	}

	public void addRow(T row) {
		aaData.add(row);
	}

	public void displayAllRows() {
		iTotalRecords = aaData.size();
		iTotalDisplayRecords = iTotalRecords;
	}

	public void displayRowsFromTotalOf(long rowsTotal) {
		iTotalDisplayRecords = rowsTotal;
		iTotalRecords = rowsTotal;
	}
}
