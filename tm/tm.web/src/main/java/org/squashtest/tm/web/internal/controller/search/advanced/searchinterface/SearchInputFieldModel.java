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

import java.util.ArrayList;
import java.util.List;

public class SearchInputFieldModel {

	private String title;

	private String inputType;

	private String id;

	private boolean ignoreBridge = false;

	private List<SearchInputPossibleValueModel> possibleValues = new ArrayList<>();

	public SearchInputFieldModel() {
	}

	public SearchInputFieldModel(String id, String title, String inputType) {
		this.id = id;
		this.title = title;
		this.inputType = inputType;
	}

	public SearchInputFieldModel(String id, String title, String inputType,
			List<SearchInputPossibleValueModel> possibleValues) {
		this(id, title, inputType);
		this.possibleValues = possibleValues;
	}

	public void addPossibleValue(SearchInputPossibleValueModel value) {
		this.possibleValues.add(value);
	}

	public void addPossibleValue(String value, String code) {
		this.possibleValues.add(new SearchInputPossibleValueModel(value, code));
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public List<SearchInputPossibleValueModel> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(List<SearchInputPossibleValueModel> possibleValues) {
		this.possibleValues = possibleValues;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isIgnoreBridge() {
		return ignoreBridge;
	}

	public void setIgnoreBridge(boolean ignoreBridge) {
		this.ignoreBridge = ignoreBridge;
	}

	/**
	 * adds the given values to the current possible values.
	 * 
	 * @param values
	 */
	public void addPossibleValues(List<SearchInputPossibleValueModel> values) {
		possibleValues.addAll(values);

	}
}
