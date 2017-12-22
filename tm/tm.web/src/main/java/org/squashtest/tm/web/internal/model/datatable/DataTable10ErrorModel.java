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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Decorator for a {@link DataTable10Model} which can be used to send back a server error message.
 * 
 * 
 * @author Gregory Fouquet
 * 
 */
public class DataTable10ErrorModel implements DataTable10Model {
	@JsonIgnore
	private final DataTable10Model decorated;
	private final String error;

	/**
	 * Decorates a {@link DataTable10Model} with an error message.
	 * 
	 * @param decorated
	 * @param errorMessage
	 * @return
	 */
	public static final DataTable10Model decorate(@NotNull DataTable10Model decorated, @NotNull String errorMessage) {
		return new DataTable10ErrorModel(decorated, errorMessage);
	}

	/**
	 * 
	 * @param decorated
	 * @param errorMessage
	 * @see #decorate(DataTable10Model, String)
	 */
	private DataTable10ErrorModel(@NotNull DataTable10Model decorated, @NotNull String errorMessage) {
		super();
		this.decorated = decorated;
		this.error = errorMessage;
	}

	/**
	 * @return
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTable10Model#getDraw()
	 */
	@Override
	@JsonProperty
	public long getDraw() {
		return decorated.getDraw();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTable10Model#getRecordsTotal()
	 */
	@Override
	@JsonProperty
	public long getRecordsTotal() {
		return decorated.getRecordsTotal();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTable10Model#getRecordsFiltered()
	 */
	@Override
	@JsonProperty
	public long getRecordsFiltered() {
		return decorated.getRecordsFiltered();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.web.internal.model.datatable.DataTable10Model#getData()
	 */
	@Override
	@JsonProperty
	public List<Object> getData() {
		return decorated.getData();
	}

	/**
	 * @return the error
	 */
	@JsonProperty
	public String getError() {
		return error;
	}
}
