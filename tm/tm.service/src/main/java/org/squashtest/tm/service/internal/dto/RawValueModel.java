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

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.domain.customfield.RawValue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * Only one of the two properties are expected for each deserialized instance
 *
 * @author bsiri
 *
 */
public class RawValueModel {

	@JsonProperty(required=false)
	private String value;

	@JsonProperty(required=false)
	private List<String> values;



	public RawValueModel() {
		super();
	}


	@JsonCreator
	public RawValueModel(Object any){
		if (any instanceof String){
			this.value = (String)any;
		}
		else if (any instanceof Boolean){
			this.value = ((Boolean)any).toString().toLowerCase();
		}
		else if (any instanceof List<?>){
			this.values = (List<String>) any;
		}
		else{
			throw new IllegalArgumentException("cannot make a RawValue from "+any.getClass());
		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	@JsonIgnore
	public RawValue toRawValue(){
		if (value != null){
			return new RawValue(value);
		}
		else{
			return new RawValue(values);
		}
	}

	@JsonIgnore
	public boolean isEmpty(){
		if (value == null && (values == null || values.isEmpty())){
			return true;
		}
		if (value != null && StringUtils.isBlank(value)){
			return true;
		}
		if (values != null && values.isEmpty()){
			return true;
		}
		return false;
	}

	// that class exists as a helper for Jackson(so that it knows how to deserialize
	// types that otherwise would be generics (and thus erased at runtime)
	public static final class RawValueModelMap extends HashMap<Long, RawValueModel>{

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

	}

}
