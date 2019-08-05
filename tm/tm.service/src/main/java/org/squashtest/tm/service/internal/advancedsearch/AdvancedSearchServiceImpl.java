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
package org.squashtest.tm.service.internal.advancedsearch;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTimeIntervalFieldModel;
import org.squashtest.tm.service.advancedsearch.AdvancedSearchService;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customfield.CustomFieldModelService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.milestone.MilestoneModelService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AdvancedSearchServiceImpl implements AdvancedSearchService {

	private static final String PROJECT_CRITERIA_NAME = "project.id";

	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchServiceImpl.class);

	private static final String SEARCH_BY_MILESTONE = "searchByMilestone";

	private static final List<String> MILESTONE_SEARCH_FIELD = Arrays.asList("milestone.label", "milestone.status",
		"milestone.endDate", "milestones.id", SEARCH_BY_MILESTONE, "activeMilestoneMode");


	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private FeatureManager featureManager;

	@PersistenceContext
	private EntityManager em;

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private CustomFieldModelService customFieldModelService;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private MilestoneModelService milestoneModelService;

	@Inject
	private CustomFieldBindingFinderService customFieldBindingFinderService;


	private static final Integer EXPECTED_LENGTH = 7;

	private static final String FAKE_MILESTONE_ID = "-9000";

	protected FeatureManager getFeatureManager() {
		return featureManager;
	}

	@Override
	public List<CustomFieldModel> findAllQueryableCustomFieldsByBoundEntityType(BindableEntity entity, List<Long> readableProjectIds) {

		Map<Long, CustomFieldModel> cufMap = customFieldModelService.findAllUsedCustomFieldsByEntity(readableProjectIds, entity);
		List<CustomFieldModel> cufList = new ArrayList<>(cufMap.values());

		return cufList;
	}

	public List<CustomField> findAllQueryableCustomFieldsByBoundEntityType(BindableEntity entity) {

		Set<CustomField> result = new LinkedHashSet<>();

		List<Project> readableProjects = projectFinder.findAllReadable();
		for (Project project : readableProjects) {
			result.addAll(customFieldBindingFinderService.findBoundCustomFields(project.getId(), entity));
		}

		return new ArrayList<>(result);

	}

	public List<JsonMilestone> findAllVisibleMilestonesToCurrentUser() {

		List<JsonMilestone> list = new ArrayList<>();
		milestoneModelService.findMilestoneByProject(findAllReadablesId()).values().stream().forEach(r -> {
			for (JsonMilestone milestone : r) {
				if (!list.contains(milestone)) {
					list.add(milestone);
				}
			}
		});
		return list;
	}

	private String padRawValue(Integer rawValue) {
		return StringUtils.leftPad(rawValue.toString(), EXPECTED_LENGTH, '0');
	}



	private List<String> getTokens(String value) {

		if (value != null && StringUtils.isNotBlank(value)) {
			return parseInput(value);
		}

		return Collections.emptyList();
	}

	private List<String> parseInput(String textInput) {
		return new StrTokenizer(textInput, StrMatcher.trimMatcher(), StrMatcher.doubleQuoteMatcher()).getTokenList();
	}



	@SuppressWarnings("unchecked")
	protected void addMilestoneFilter(AdvancedSearchModel searchModel) {
		Map<String, AdvancedSearchFieldModel> fields = searchModel.getFields();

		AdvancedSearchSingleFieldModel searchByMilestone = (AdvancedSearchSingleFieldModel) fields
			.get(SEARCH_BY_MILESTONE);

		if (searchByMilestone != null && "true".equals(searchByMilestone.getValue())) {

			Criteria crit = createMilestoneHibernateCriteria(fields);

			List<String> milestoneIds = new ArrayList<>();
			List<Long> foundIds = crit.list();
			for (Long milestoneId : foundIds) {
				milestoneIds.add(String.valueOf(milestoneId));
			}

			// if there is no milestone id that means we didn't found any milestones
			// matching search criteria, so we use a fake milestoneId to find no result.
			if (milestoneIds.isEmpty()) {
				milestoneIds.add(FAKE_MILESTONE_ID);
			}

			AdvancedSearchListFieldModel milestonesModel = new AdvancedSearchListFieldModel();
			milestonesModel.setValues(milestoneIds);

			fields.put("milestones.id", milestonesModel);
		}

	}

	public void addWorkflowAutomationFilter(AdvancedSearchModel searchModel) {
		Map<String, AdvancedSearchFieldModel> fields = searchModel.getFields();
		AdvancedSearchSingleFieldModel projectAllowAutomationWorkflow = new AdvancedSearchSingleFieldModel();

		projectAllowAutomationWorkflow.setValue("true");
		fields.put("project.allowAutomationWorkflow", projectAllowAutomationWorkflow);
	}

	protected Criteria createMilestoneHibernateCriteria(Map<String, AdvancedSearchFieldModel> fields) {

		Session session = em.unwrap(Session.class);
		Criteria crit = session.createCriteria(Milestone.class, "milestone");

		for (Entry<String, AdvancedSearchFieldModel> entry : fields.entrySet()) {

			AdvancedSearchFieldModel model = entry.getValue();
			if (model != null) {

				switch (entry.getKey()) {

					case "milestone.label":

						creatingMilestoneLabelCriteria( model, crit);
						break;

					case "milestone.status":
						creatingMilestoneStatusCriteria( model, crit);

						break;

					case "milestone.endDate":
						creatingMilestoneEndDateCriteria( model, crit);

						break;
					default:
						// do nothing
				}
			}
		}

		// set the criteria projection so that we only fetch the ids
		crit.setProjection(Projections.property("milestone.id"));

		return crit;
	}

	private void creatingMilestoneLabelCriteria(AdvancedSearchFieldModel model,Criteria crit){
		List<String> labelValues = ((AdvancedSearchListFieldModel) model).getValues();

		if (labelValues != null && !labelValues.isEmpty()) {

			Collection<Long> ids = CollectionUtils.collect(labelValues, new Transformer() {
				@Override
				public Object transform(Object val) {
					return Long.parseLong((String) val);
				}
			});

			crit.add(Restrictions.in("id", ids));// milestone.label now contains ids
		}
	}

	private void creatingMilestoneStatusCriteria(AdvancedSearchFieldModel model,Criteria crit) {
		List<String> statusValues = ((AdvancedSearchListFieldModel) model).getValues();

		if (statusValues != null && !statusValues.isEmpty()) {
			crit.add(Restrictions.in("status", convertStatus(statusValues)));
		}
	}


	private void creatingMilestoneEndDateCriteria(AdvancedSearchFieldModel model,Criteria crit) {
		Date startDate = ((AdvancedSearchTimeIntervalFieldModel) model).getStartDate();
		Date endDate = ((AdvancedSearchTimeIntervalFieldModel) model).getEndDate();

		if (startDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.set(Calendar.HOUR, 0);
			crit.add(Restrictions.ge("endDate", cal.getTime()));
		}

		if (endDate != null) {
			crit.add(Restrictions.le("endDate", endDate));

		}
	}


		protected void removeMilestoneSearchFields(AdvancedSearchModel model) {
		Map<String, AdvancedSearchFieldModel> fields = model.getFields();

		for (String s : MILESTONE_SEARCH_FIELD) {
			fields.remove(s);
		}
	}

	private List<MilestoneStatus> convertStatus(List<String> values) {
		List<MilestoneStatus> status = new ArrayList<>();
		for (String value : values) {
			int level = Integer.valueOf(value.substring(0, 1));
			status.add(MilestoneStatus.getByLevel(level));
		}
		return status;
	}

	// Issue #5079 : ensure that criteria project.id contains only
	// projects the user can read
	private void secureProjectCriteria(AdvancedSearchModel model) {

		// Issue #5079 again
		// first task is to locate which name has the project criteria because it may differ depending on the interface
		// (test case, requirement, test-case-through-requirements
		String key = null;
		Set<String> keys = model.getFields().keySet();
		for (String k : keys) {
			if (k.contains(PROJECT_CRITERIA_NAME)) {
				key = k;
				break;
			}
		}
		// if no projectCriteria was set -> nothing to do
		if (key == null) {
			return;
		}

		AdvancedSearchListFieldModel projectCriteria = (AdvancedSearchListFieldModel) model.getFields().get(key);

		List<String> approvedIds;
		List<String> selectedIds = projectCriteria.getValues();

		// case 1 : no project is selected
		if (selectedIds == null || selectedIds.isEmpty()) {

			approvedIds = new ArrayList<>();
			findAllReadablesId().stream().forEach(r -> approvedIds.add(String.valueOf(r)));
		}
		// case 2 : some projects were selected
		else {
			approvedIds = new ArrayList<>();
			for (String id : selectedIds) {
				if (permissionService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "READ", Long.valueOf(id),
					Project.class.getName())) {
					approvedIds.add(id);
				} else {
					LOGGER.info("AdvancedSearchService : removed element '" + id
						+ "' from criteria 'project.id' because the user is not approved for 'READ' operation on it");
				}
			}
		}

		projectCriteria.setValues(approvedIds);

	}

	public boolean shouldSearchByMilestones(AdvancedSearchModel model) {
		boolean enabled = getFeatureManager().isEnabled(Feature.MILESTONE);

		AdvancedSearchSingleFieldModel searchByMilestone = (AdvancedSearchSingleFieldModel) model.getFields().get(SEARCH_BY_MILESTONE);
		AdvancedSearchSingleFieldModel activeMilestoneMode = (AdvancedSearchSingleFieldModel) model.getFields().get("activeMilestoneMode");
		boolean hasCriteria = (searchByMilestone != null && "true".equals(searchByMilestone.getValue())) || (activeMilestoneMode != null && "true".equals(activeMilestoneMode.getValue()));

		return enabled && hasCriteria;
	}

	public boolean shouldSearchByAutomationWorkflow(AdvancedSearchModel model) {
		boolean enabled = false;
		AdvancedSearchListFieldModel searchByAutomatable = (AdvancedSearchListFieldModel) model.getFields().get("automatable");
		AdvancedSearchListFieldModel searchByAutomationRequest = (AdvancedSearchListFieldModel) model.getFields().get("automationRequest.requestStatus");

		AdvancedSearchListFieldModel searchByTcAutomatable = (AdvancedSearchListFieldModel) model.getFields().get("referencedTestCase.automatable");
		AdvancedSearchListFieldModel searchByTcAutomationRequest = (AdvancedSearchListFieldModel) model.getFields().get("referencedTestCase.automationRequest.requestStatus");

		if((searchByAutomatable != null && searchByAutomatable.getValues().size() > 0) || (searchByAutomationRequest != null &&searchByAutomationRequest.getValues().size() > 0)) {
			enabled = true;
		}

		if((searchByTcAutomatable != null && searchByTcAutomatable.getValues().size() > 0) || (searchByTcAutomationRequest != null &&searchByTcAutomationRequest.getValues().size() > 0)) {
			enabled = true;
		}

		return enabled;
	}

	public List<Long> findAllReadablesId() {
		UserDto currentUser = userAccountService.findCurrentUserDto();
		List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
		return readableProjectIds;
	}

	public List<Long> findMilestonesIds(AdvancedSearchModel modelCopy) {
		addMilestoneFilter(modelCopy);

		List<String> strMilestoneIds =
			((AdvancedSearchListFieldModel) modelCopy.getFields().get("milestones.id")).getValues();
		List<Long> milestoneIds = new ArrayList<>(strMilestoneIds.size());
		for (String str : strMilestoneIds) {
			milestoneIds.add(Long.valueOf(str));
		}
		return milestoneIds;
	}


}






