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
package org.squashtest.tm.web.internal.controller.search.advanced.searchinterface;

public class SearchInputPossibleValueModel {
	/**
	 * the option's label
	 */
	private String value;

	/**
	 * sometimes the value is the composite of sub inputs.
	 */
	private SearchInputFieldModel subInput;

	/**
	 * the thing that is posted
	 */
	private String code;

	private boolean selected = false;

	public SearchInputPossibleValueModel() {

	}

	public SearchInputPossibleValueModel(String value, String code) {
		this(value, code, false);
	}

	public SearchInputPossibleValueModel(String value, String code, boolean selected) {
		this.value = value;
		this.code = code;
		this.selected = selected;
	}

	public SearchInputPossibleValueModel(String value, String code, boolean selected, SearchInputFieldModel subInput){
		this.value = value;
		this.code = code;
		this.subInput = subInput;
		this.selected = selected;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public SearchInputFieldModel getSubInput() {
		return subInput;
	}

	public void setSubInput(SearchInputFieldModel subInput) {
		this.subInput = subInput;
	}

}
