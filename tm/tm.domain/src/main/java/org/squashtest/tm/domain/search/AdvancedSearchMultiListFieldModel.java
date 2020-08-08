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
package org.squashtest.tm.domain.search;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class AdvancedSearchMultiListFieldModel implements AdvancedSearchFieldModel{

	private AdvancedSearchFieldModelType type;

	private List<String> values = new ArrayList<>();

	private Integer minValue;

	private Integer maxValue;

	public AdvancedSearchMultiListFieldModel() {
		type  = AdvancedSearchFieldModelType.MULTILIST;
	}

	public AdvancedSearchMultiListFieldModel(AdvancedSearchFieldModelType type) {
		this.type = type;
	}

	@Override
	public AdvancedSearchFieldModelType getType(){
		return this.type;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}


	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public boolean isSet() {
		return ! (values == null || values.isEmpty() );
	}

	@JsonIgnore
	public boolean hasMinValue(){
		return minValue != null;
	}

	@JsonIgnore
	public boolean hasMaxValue(){
		return maxValue != null;
	}
}
