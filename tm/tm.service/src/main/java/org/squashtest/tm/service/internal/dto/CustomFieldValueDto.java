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
package org.squashtest.tm.service.internal.dto;

public class CustomFieldValueDto {
	private Long id;
	private Long boundEntityId;
	private Long cufId;
	private String value="";

	public CustomFieldValueDto(Long id, Long boundEntityId, Long cufId, String value) {
		super();
		this.id = id;
		this.boundEntityId = boundEntityId;
		this.cufId = cufId;
		this.value = value;
	}

	public CustomFieldValueDto(){
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBoundEntityId() {
		return boundEntityId;
	}

	public void setBoundEntityId(Long boundEntityId) {
		this.boundEntityId = boundEntityId;
	}

	public Long getCufId() {
		return cufId;
	}

	public void setCufId(Long cufId) {
		this.cufId = cufId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
