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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

// TODO : instead of a JsonTypeIdResolver, use something like for AdvancedSearchFieldModel
@JsonTypeInfo(include=JsonTypeInfo.As.PROPERTY, property="itype", use=JsonTypeInfo.Id.CUSTOM)
@JsonTypeIdResolver(CustomFieldModelIdTypeResolver.class)
public abstract class CustomFieldModel<VALUETYPE> {

	private long id;

	private String name;

	private String label;

	private boolean optional;

	private String friendlyOptional;

	private String code;

	private InputTypeModel inputType;

	// this one is required by Jackson, this is how Jackson discriminates (see annotations above)
	//private String itype;

	private boolean isDenormalized;


	/* ************** abstract thing : here is all you have to do ******/
	public abstract VALUETYPE getDefaultValue();

	abstract void setDefaultValue(VALUETYPE defaultValue);

	/* ************* /abstract thing ***********************************/

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public InputTypeModel getInputType() {
		return inputType;
	}

	public void setInputType(InputTypeModel inputType) {
		this.inputType = inputType;
	}
/*
	public String getItype() {
		return itype;
	}

	public void setIType(String itype) {
		this.itype = itype;
	}
*/
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFriendlyOptional() {
		return friendlyOptional;
	}

	public void setFriendlyOptional(String friendlyOptional) {
		this.friendlyOptional = friendlyOptional;
	}

	public boolean isDenormalized() {
		return isDenormalized;
	}

	public void setDenormalized(boolean isDenormalized) {
		this.isDenormalized = isDenormalized;
	}



}

