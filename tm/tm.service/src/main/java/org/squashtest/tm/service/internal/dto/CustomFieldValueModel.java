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

import java.util.List;

public class CustomFieldValueModel {

	private long id;

	private long boundEntityId;

	private BindableEntityModel boundEntityType;

	private CustomFieldBindingModel binding;

	// for custom fields having only one value
	private String value;

	// for custom fields having multiple simultaneous values
	private List<String> optionValues;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getOptionValues() {
		return optionValues;
	}

	public void setOptionValues(List<String> optionValues) {
		this.optionValues = optionValues;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getBoundEntityId() {
		return boundEntityId;
	}

	public void setBoundEntityId(long boundEntityId) {
		this.boundEntityId = boundEntityId;
	}

	public BindableEntityModel getBoundEntityType() {
		return boundEntityType;
	}

	public void setBoundEntityType(BindableEntityModel boundEntityType) {
		this.boundEntityType = boundEntityType;
	}

	public CustomFieldBindingModel getBinding() {
		return binding;
	}

	public void setBinding(CustomFieldBindingModel binding) {
		this.binding = binding;
	}





}
