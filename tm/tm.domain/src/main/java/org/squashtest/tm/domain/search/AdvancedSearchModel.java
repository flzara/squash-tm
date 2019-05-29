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

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdvancedSearchModel {

	private static final String SEARCH_BY_MILESTONE = "searchByMilestone";
	private static final Set<String> MILESTONE_SEARCH_CRITERIA_KEYS = Sets.newHashSet(SEARCH_BY_MILESTONE, "milestone.label", "milestone.status", "milestone.endDate");

	private Map<String, AdvancedSearchFieldModel> fields = new HashMap<>();


	public AdvancedSearchModel(){
		super();
	}

	AdvancedSearchModel(Map<String, AdvancedSearchFieldModel> entries){
		super();
		this.fields = entries;
	}

	public void addField(String fieldName, AdvancedSearchFieldModel value){
		fields.put(fieldName, value);
	}

	/**
	 * Returns the form fields. If no search on the milestones is required, the corresponding
	 * criteria will be filtered out.
	 *
	 * @return
	 */
	public Map<String, AdvancedSearchFieldModel> getFields(){

		if (searchByMilestone()){
			return stripMilestones();
		}
		else {
			return this.fields;
		}
	}

	public AdvancedSearchModel shallowCopy(){

		Map<String, AdvancedSearchFieldModel> copyfields = new HashMap<>(fields);

		return new AdvancedSearchModel(copyfields);

	}

	private boolean searchByMilestone(){
		AdvancedSearchFieldModel byMilestone = fields.get(SEARCH_BY_MILESTONE);
		if (byMilestone != null){
			AdvancedSearchSingleFieldModel milestoneField = (AdvancedSearchSingleFieldModel) byMilestone;
			return milestoneField.getValue().equals("true");
		}
		return false;
	}

	private Map<String, AdvancedSearchFieldModel> stripMilestones(){
		Map<String, AdvancedSearchFieldModel> copy = new HashMap<>(fields);
		MILESTONE_SEARCH_CRITERIA_KEYS.forEach(copy::remove);
		return copy;
	}
}
