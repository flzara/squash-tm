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

import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Gregory Fouquet
 *
 */
public class JsonProject {


	private long id;
	private String uri;
	private String name;

	private Map<String, List<CustomFieldBindingModel>> customFieldBindings;

	private Set<JsonMilestone> milestones;

	private JsonInfoList requirementCategories;

	private JsonInfoList testCaseNatures;

	private JsonInfoList testCaseTypes;


	/**
	 * @param project
	 * @return
	 */
	public static JsonProject toJson(Project project) {
		JsonProject res = new JsonProject();
		res.id = project.getId();
		res.uri = "/projects/" + res.id;
		res.name = project.getName();
		return res;
	}

	private JsonProject(){
		super();
	}

	public JsonProject (Long id, String name) {
		this.id = id;
		this.uri = "/projects/" + id;
		this.name = name;
	}


	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public Map<String, List<CustomFieldBindingModel>> getCustomFieldBindings() {
		return customFieldBindings;
	}

	public JsonInfoList getRequirementCategories() {
		return requirementCategories;
	}

	public JsonInfoList getTestCaseNatures() {
		return testCaseNatures;
	}

	public JsonInfoList getTestCaseTypes() {
		return testCaseTypes;
	}

	public void setCustomFieldBindings(Map<String, List<CustomFieldBindingModel>> customFieldBindings) {
		this.customFieldBindings = customFieldBindings;
	}

	public void addCustomFieldBindingModel (CustomFieldBindingModel customFieldBindingModel){
		String boundEntity = customFieldBindingModel.getBoundEntity().getEnumName();
		if(customFieldBindings.containsKey(boundEntity)){
			customFieldBindings.get(boundEntity).add(customFieldBindingModel);
		} else {
			ArrayList<CustomFieldBindingModel> models = new ArrayList<>();
			models.add(customFieldBindingModel);
			customFieldBindings.put(boundEntity, models);
		}
	}

	public void setRequirementCategories(JsonInfoList requirementCategories) {
		this.requirementCategories = requirementCategories;
	}

	public void setTestCaseNatures(JsonInfoList testCaseNatures) {
		this.testCaseNatures = testCaseNatures;
	}

	public void setTestCaseTypes(JsonInfoList testCaseTypes) {
		this.testCaseTypes = testCaseTypes;
	}

	public Set<JsonMilestone> getMilestones() {
		return milestones;
	}

	public void setMilestones(Set<JsonMilestone> milestones) {
		this.milestones = milestones;
	}

}
