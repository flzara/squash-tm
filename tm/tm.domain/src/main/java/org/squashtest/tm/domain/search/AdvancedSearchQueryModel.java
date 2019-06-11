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

import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancedSearchQueryModel {

	private static final List<String> PARAMS_NOT_QUERYING = Arrays.asList("entity-index",
		"empty-openinterface2-holder", "empty-opentree-holder", "editable", "links");


	private Pageable pageable;

	private List<String> searchResultKeys = new ArrayList<>();

	private AdvancedSearchModel searchFormModel;

	public AdvancedSearchQueryModel() {
	}

	public AdvancedSearchQueryModel(Pageable pageable, List<String> searchResultKeys, AdvancedSearchModel model) {
		this.pageable = pageable;

		this.searchResultKeys = searchResultKeys;

		this.searchFormModel = model;
	}

	public Pageable getPageable() {
		return pageable;
	}

	public void setPageable(Pageable pageable) {
		this.pageable = pageable;
	}

	public List<String> getSearchResultKeys() {
		List<String> columns = new ArrayList<>(searchResultKeys);
		PARAMS_NOT_QUERYING.forEach(columns::remove);
		return columns;
	}

	public void setSearchResultKeys(List<String> searchResultKeys) {
		this.searchResultKeys = searchResultKeys;
	}

	public AdvancedSearchModel getSearchFormModel() {
		return searchFormModel;
	}
	
	public Set<String> getSearchFormKeys(){
		return new HashSet<>(searchFormModel.getFieldKeys());
	}
	

	public void setModel(AdvancedSearchModel model) {
		this.searchFormModel = model;
	}
}
