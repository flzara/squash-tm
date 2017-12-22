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
package org.squashtest.tm.service.internal.dto.json;

import org.squashtest.tm.domain.Identified;

import java.util.List;



public class JsonInfoList implements Identified {

	private long id;
	private String uri;
	private String code;
	private String label;
	private String description;
	private List<JsonInfoListItem> items;


	public JsonInfoList(){
		super();
	}

	public JsonInfoList(long id, String uri, String code, String label, String description) {
		this.id = id;
		this.uri = uri;
		this.code = code;
		this.label = label;
		this.description = description;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<JsonInfoListItem> getItems() {
		return items;
	}

	public void setItems(List<JsonInfoListItem> items) {
		this.items = items;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		JsonInfoList that = (JsonInfoList) o;

		if (id != that.id) return false;
		if (code == null || that.code == null) {
			return false;
		}
		return code.equals(that.code);
	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + code.hashCode();
		return result;
	}
}
