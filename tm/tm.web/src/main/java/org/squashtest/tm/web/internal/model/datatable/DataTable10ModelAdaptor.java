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

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gregory Fouquet
 *
 */
public class DataTable10ModelAdaptor<T> implements DataTable10Model<T> {
	private final DataTableModel<T> adapted;

	/**
	 * Adapts a {@link DataTableModel} into a {@link DataTable10Model}
	 *
	 * @param model
	 * @return
	 */
	public static final <U> DataTable10Model<U> adapt(@NotNull DataTableModel<U> model) {
		return new DataTable10ModelAdaptor<>(model);
	}

	private DataTable10ModelAdaptor(DataTableModel<T> adapted) {
		super();
		this.adapted = adapted;
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTable10Model#getDraw()
	 */
	@Override
	@JsonProperty
	public long getDraw() {
		return Long.parseLong(adapted.sEcho);
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTable10Model#getRecordsTotal()
	 */
	@Override
	@JsonProperty
	public long getRecordsTotal() {
		return adapted.getiTotalRecords();
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTable10Model#getRecordsFiltered()
	 */
	@Override
	@JsonProperty
	public long getRecordsFiltered() {
		return adapted.getiTotalDisplayRecords();
	}

	/**
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTable10Model#getData()
	 */
	@Override
	@JsonProperty
	public List<T> getData() {
		return adapted.getAaData();
	}
}
