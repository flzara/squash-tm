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
package org.squashtest.tm.web.internal.model.json;

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jthebault on 10/10/2016.
 */
public class JsonDynamicScope {

	//Requirement workspace
	private List<Long> requirementLibraryIds;
	private List<Long> requirementFolderIds;
	private List<Long> requirementIds;

	//Test-case workspace
	private List<Long> testCaseLibraryIds;
	private List<Long> testCaseFolderIds;
	private List<Long> testCaseIds;

	//Campaign workspace
	private List<Long> campaignFolderIds;
	private List<Long> campaignIds;
	private List<Long> iterationIds;

	//Boolean for milestone dashboard (ie when clicking on the milestone button, and ignoring )
	private boolean milestoneDashboard = false;

	//Workspace, as the string identifier in domain entity Workspace
	private String workspaceName;


	public JsonDynamicScope() {
	}

	public List<Long> getRequirementLibraryIds() {
		return requirementLibraryIds;
	}

	public void setRequirementLibraryIds(List<Long> requirementLibraryIds) {
		this.requirementLibraryIds = requirementLibraryIds;
	}

	public List<Long> getRequirementFolderIds() {
		return requirementFolderIds;
	}

	public void setRequirementFolderIds(List<Long> requirementFolderIds) {
		this.requirementFolderIds = requirementFolderIds;
	}

	public List<Long> getRequirementIds() {
		return requirementIds;
	}

	public void setRequirementIds(List<Long> requirementIds) {
		this.requirementIds = requirementIds;
	}

	public List<Long> getTestCaseLibraryIds() {
		return testCaseLibraryIds;
	}

	public void setTestCaseLibraryIds(List<Long> testCaseLibraryIds) {
		this.testCaseLibraryIds = testCaseLibraryIds;
	}

	public List<Long> getTestCaseFolderIds() {
		return testCaseFolderIds;
	}

	public void setTestCaseFolderIds(List<Long> testCaseFolderIds) {
		this.testCaseFolderIds = testCaseFolderIds;
	}

	public List<Long> getTestCaseIds() {
		return testCaseIds;
	}

	public void setTestCaseIds(List<Long> testCaseIds) {
		this.testCaseIds = testCaseIds;
	}

	public List<Long> getCampaignFolderIds() {
		return campaignFolderIds;
	}

	public void setCampaignFolderIds(List<Long> campaignFolderIds) {
		this.campaignFolderIds = campaignFolderIds;
	}

	public List<Long> getCampaignIds() {
		return campaignIds;
	}

	public void setCampaignIds(List<Long> campaignIds) {
		this.campaignIds = campaignIds;
	}

	public List<Long> getIterationIds() {
		return iterationIds;
	}

	public void setIterationIds(List<Long> iterationIds) {
		this.iterationIds = iterationIds;
	}

	public boolean isMilestoneDashboard() {
		return milestoneDashboard;
	}

	public void setMilestoneDashboard(boolean milestoneDashboard) {
		this.milestoneDashboard = milestoneDashboard;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public List<EntityReference> convertToEntityReferences (){
		List<EntityReference> entityReferences = new ArrayList<>();
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.TEST_CASE_LIBRARY,this.testCaseLibraryIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.TEST_CASE_FOLDER,this.testCaseFolderIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.TEST_CASE,this.testCaseIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.REQUIREMENT_LIBRARY,this.requirementLibraryIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.REQUIREMENT_FOLDER,this.requirementFolderIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.REQUIREMENT,this.requirementIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.CAMPAIGN_FOLDER,this.campaignFolderIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.CAMPAIGN,this.campaignIds));
		entityReferences.addAll(convertToEntityReferencesForOneType(EntityType.ITERATION,this.iterationIds));
		return entityReferences;
	}

	public static List<EntityReference> convertToEntityReferencesForOneType (EntityType type, List<Long> ids){
		List<EntityReference> entityReferences = new ArrayList<>();
		for (Long id : ids) {
			entityReferences.add(new EntityReference(type,id));
		}
		return entityReferences;
	}
}
