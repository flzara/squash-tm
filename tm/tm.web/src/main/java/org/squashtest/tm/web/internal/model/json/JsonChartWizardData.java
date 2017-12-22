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

import static org.squashtest.tm.domain.EntityType.CAMPAIGN;
import static org.squashtest.tm.domain.EntityType.EXECUTION;
import static org.squashtest.tm.domain.EntityType.ITEM_TEST_PLAN;
import static org.squashtest.tm.domain.EntityType.ITERATION;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT;
import static org.squashtest.tm.domain.EntityType.REQUIREMENT_VERSION;
import static org.squashtest.tm.domain.EntityType.TEST_CASE;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.chart.ChartType;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.ColumnRole;
import org.squashtest.tm.domain.chart.DataType;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.infolist.InfoListFinderService;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class JsonChartWizardData {


	private Map<EntityType, Set<ColumnPrototype>> columnPrototypes;

	private EnumSet<ChartType> chartTypes = EnumSet.allOf(ChartType.class);

	private Map<ColumnRole, EnumSet<Operation>> columnRoles = new EnumMap<>(
		ColumnRole.class);

	private Map<DataType, EnumSet<Operation>> dataTypes = new EnumMap<>(DataType.class);

	// don't use EnumMap here cuz the business want a custom order
	private Map<EntityType, String> entityTypes = new LinkedHashMap<>();

	private Map<String, EnumSet<? extends Level>> levelEnums = new HashMap<>();

	private EnumSet<ExecutionStatus> executionStatus = EnumSet.allOf(ExecutionStatus.class);

	private EnumSet<ExecutionStatus> itpiExecutionStatus = EnumSet
			.complementOf(EnumSet.copyOf(ExecutionStatus.TA_STATUSES_ONLY));

	private Map<String, Map<String, InfoList>> projectInfoList = new HashMap<>();

	private Map<String, Set<CustomField>> customFields = new HashMap<>();

	private InfoListFinderService infoListFinder;

	private CustomFieldBindingModificationService cufBindingService;

	private Map<Long, Set<ExecutionStatus>> disabledStatusByProject = new HashMap<>();


	public JsonChartWizardData(Map<EntityType, Set<ColumnPrototype>> columnPrototypes, List<Project> projects,
			InfoListFinderService infoListFinder, CustomFieldBindingModificationService cufBindingService) {

		this.columnPrototypes = columnPrototypes;
		this.infoListFinder = infoListFinder;
		this.cufBindingService = cufBindingService;
		populate(projects);

	}

	private void populate(List<Project> projects) {

		addColumnRoles();
		addDataType();
		addLevelEnums();
		addEntityType();
		addInfoList(projects);
		//For 1.13 we don't support operations on the custom fields.
		//But as it will be in next version, i don't suppress the code.
		//addCustomFields(projects);
		addDisabledStatus(projects);


	}

	private void addDisabledStatus(List<Project> projects) {
		for (Project project : projects) {
			disabledStatusByProject.put(project.getId(), project.getCampaignLibrary().getDisabledStatuses());
		}
	}

	private void addLevelEnums() {
		addLevelEnum("TEST_CASE_STATUS", TestCaseStatus.class);
		addLevelEnum("test-case-execution-mode", TestCaseExecutionMode.class);
		addLevelEnum("TEST_CASE_IMPORTANCE", TestCaseImportance.class);
		addLevelEnum("REQUIREMENT_VERSION_CRITICALITY", RequirementCriticality.class);
		addLevelEnum("REQUIREMENT_VERSION_STATUS", RequirementStatus.class);
		addLevelEnum("REQUIREMENT_CRITICALITY", RequirementCriticality.class);
		addLevelEnum("REQUIREMENT_STATUS", RequirementStatus.class);
	}

	private void addColumnRoles() {
		for (ColumnRole cr : ColumnRole.values()) {
			columnRoles.put(cr, cr.getOperations());
		}
	}

	private void addDataType() {

		for (DataType dt : DataType.values()) {
			dataTypes.put(dt, dt.getOperations());
		}
	}

	private void addEntityType() {

		entityTypes.put(REQUIREMENT, "icon-chart-requirement");
		entityTypes.put(REQUIREMENT_VERSION, "icon-chart-requirement-version");
		entityTypes.put(TEST_CASE, "icon-chart-test-case");
		entityTypes.put(CAMPAIGN, "icon-chart-campaign");
		entityTypes.put(ITERATION, "icon-chart-iteration");
		entityTypes.put(ITEM_TEST_PLAN, "icon-chart-item-test-plan");
		entityTypes.put(EXECUTION, "icon-chart-execution");

	}

	private void addInfoList(List<Project> projects) {
		for (Project project : projects) {

			Map<String, InfoList> infoLists = new HashMap<>();

			infoLists.put("REQUIREMENT_VERSION_CATEGORY", project.getRequirementCategories());
			infoLists.put("REQUIREMENT_CATEGORY", project.getRequirementCategories());
			infoLists.put("TEST_CASE_NATURE", project.getTestCaseNatures());
			infoLists.put("TEST_CASE_TYPE", project.getTestCaseTypes());
			projectInfoList.put(project.getId().toString(), infoLists);
		}

		Map<String, InfoList> defaultList = new HashMap<>();

		defaultList.put("REQUIREMENT_VERSION_CATEGORY",
				infoListFinder.findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode()));
		defaultList.put("REQUIREMENT_CATEGORY",
				infoListFinder.findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode()));
		defaultList.put("TEST_CASE_NATURE", infoListFinder.findByCode(SystemInfoListCode.TEST_CASE_NATURE.getCode()));
		defaultList.put("TEST_CASE_TYPE", infoListFinder.findByCode(SystemInfoListCode.TEST_CASE_TYPE.getCode()));
		projectInfoList.put("default", defaultList);
	}


	private void addCustomFields(List<Project> projects) { // NOSONAR not used yet, should be later

		for (Project project : projects) {
			List<CustomFieldBinding> cufBindings = cufBindingService.findCustomFieldsForGenericProject(project.getId());
			@SuppressWarnings("unchecked")
			List<CustomField> cufs = (List<CustomField>) CollectionUtils.collect(cufBindings, new Transformer() {

				@Override
				public Object transform(Object input) {

					return ((CustomFieldBinding) input).getCustomField();
				}
			});
			customFields.put(project.getId().toString(), new HashSet<>(cufs));
		}
	}


	private <E extends Enum<E> & Level> void addLevelEnum(String name, Class<E> clazz) {
		levelEnums.put(name, EnumSet.allOf(clazz));
	}

	public Map<EntityType, Set<ColumnPrototype>> getColumnPrototypes() {
		return columnPrototypes;
	}

	public EnumSet<ChartType> getChartTypes() {
		return chartTypes;
	}

	public Map<EntityType, String> getEntityTypes() {
		return entityTypes;
	}

	public Map<ColumnRole, EnumSet<Operation>> getcolumnRoles() {
		return columnRoles;
	}

	public Map<DataType, EnumSet<Operation>> getDataTypes() {
		return dataTypes;
	}

	public Map<String, Map<String, InfoList>> getProjectInfoList() {
		return projectInfoList;
	}

	public Map<String, Set<CustomField>> getCustomFields() {
		return customFields;
	}

	@JsonSerialize(contentUsing = LevelEnumSerializer.class)
	public Map<String, EnumSet<? extends Level>> getLevelEnums() {
		return levelEnums;
	}

	@JsonSerialize(using = LevelEnumSerializer.class)
	public EnumSet<ExecutionStatus> getExecutionStatus() {
		return executionStatus;
	}

	public Map<Long, Set<ExecutionStatus>> getDisabledStatusByProject() {
		return disabledStatusByProject;
	}

	@JsonSerialize(using = LevelEnumSerializer.class)
	public EnumSet<ExecutionStatus> getItpiExecutionStatus() {
		return itpiExecutionStatus;
	}

}
