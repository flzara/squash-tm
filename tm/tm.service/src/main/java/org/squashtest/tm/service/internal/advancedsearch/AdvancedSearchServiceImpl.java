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

import java.util.*;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.RangeMatchingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.search.*;
import org.squashtest.tm.domain.search.AdvancedSearchTagsFieldModel.Operation;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.advancedsearch.AdvancedSearchService;
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
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;

public class AdvancedSearchServiceImpl implements AdvancedSearchService {

	private static final String PROJECT_CRITERIA_NAME = "project.id";

	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchServiceImpl.class);

	private static final List<String> MILESTONE_SEARCH_FIELD = Arrays.asList("milestone.label", "milestone.status",
		"milestone.endDate", "milestones.id","searchByMilestone","activeMilestoneMode");

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private FeatureManager featureManager;

	@PersistenceContext
	private EntityManager em;

	@Inject
	protected UserAccountService userAccountService;

	@Inject
	private CustomFieldModelService customFieldModelService;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	MilestoneModelService milestoneModelService;

	@Inject
	private CustomFieldBindingFinderService customFieldBindingFinderService;


	private static final Integer EXPECTED_LENGTH = 7;

	private static final String FAKE_MILESTONE_ID = "-9000";

	protected FeatureManager getFeatureManager() {
		return featureManager;
	}

	@Override
	public List<CustomFieldModel> findAllQueryableCustomFieldsByBoundEntityType(BindableEntity entity, List<Long> readableProjectIds) {

		Map<Long, CustomFieldModel> cufMap = customFieldModelService.findAllUsedCustomFieldsByEntity(readableProjectIds,entity);
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
		milestoneModelService.findMilestoneByProject(findAllReadablesId()).values().stream().forEach(r-> {
			for(JsonMilestone milestone : r) {
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

	private Query buildLuceneRangeQuery(QueryBuilder qb, String fieldName, Integer minValue, Integer maxValue) {

		Query query;

		if (minValue == null) {

			String paddedMaxValue = padRawValue(maxValue);

			query = qb.bool()
				.must(qb.range().onField(fieldName).ignoreFieldBridge().below(paddedMaxValue).createQuery())
				.createQuery();

		} else if (maxValue == null) {

			String paddedMinValue = padRawValue(minValue);

			query = qb.bool()
				.must(qb.range().onField(fieldName).ignoreFieldBridge().above(paddedMinValue).createQuery())
				.createQuery();

		} else {

			String paddedMaxValue = padRawValue(maxValue);
			String paddedMinValue = padRawValue(minValue);

			query = qb.bool().must(qb.range().onField(fieldName).ignoreFieldBridge().from(paddedMinValue)
				.to(paddedMaxValue).createQuery()).createQuery();
		}

		return query;
	}

	private Query buildLuceneNumericRangeQuery(QueryBuilder qb, String fieldKey, Double minValue, Double maxValue) {
		return qb.bool()
				.must(NumericRangeQuery.newDoubleRange(fieldKey,minValue,maxValue,true,true))
				.createQuery();
	}

	protected Query buildLuceneValueInListQuery(QueryBuilder qb, String fieldName, List<String> values, boolean isTag) {
		// TODO write something better when we have some time to do something not 'a minima'
		Query mainQuery = null;

		if (!values.isEmpty()) {
			for (String value : values) {


				if (StringUtils.isBlank(value)) {
					value = "$NO_VALUE";
				}
				Query query;

				if (isTag) {
					query = qb.bool().should(qb.phrase().onField(fieldName).ignoreFieldBridge().ignoreAnalyzer()
						.sentence(value).createQuery()).createQuery();
				} else {
					query = qb.bool().should(qb.keyword().onField(fieldName).ignoreFieldBridge().ignoreAnalyzer()
						.matching(value).createQuery()).createQuery();
				}

				if (query != null && mainQuery == null) {
					mainQuery = query;
				} else if (query != null) {
					mainQuery = qb.bool().should(mainQuery).should(query).createQuery();
				}
			}
		} else {
			mainQuery = qb.all().createQuery();
		}
		return qb.bool().must(mainQuery).createQuery();
	}

	protected Query buildLuceneSingleValueQuery(QueryBuilder qb, String fieldName, List<String> values) {

		Query mainQuery = null;

		for (String value : values) {

			Query query;

			if (value.contains("*")) {
				query = qb.bool().must(
					qb.keyword().wildcard().onField(fieldName).ignoreFieldBridge().matching(value).createQuery())
					.createQuery();
			} else {

				query = qb.bool().must(qb.phrase().onField(fieldName).ignoreFieldBridge().sentence(value).createQuery())
					.createQuery();
			}

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}

		return mainQuery;
	}

	private Query buildLuceneTextQuery(QueryBuilder qb, String fieldName, List<String> values) {

		Query mainQuery = null;

		for (String value : values) {

			Query query;

			query = qb.bool().must(qb.phrase().onField(fieldName).ignoreFieldBridge().sentence(value).createQuery())
				.createQuery();
			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
			}
		}
		return mainQuery;
	}

	private Query buildQueryForSingleCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {
		AdvancedSearchSingleFieldModel model = (AdvancedSearchSingleFieldModel) fieldModel;
		List<String> tokens = getTokens(model.getValue());
		return tokens.isEmpty() ? null : buildLuceneSingleValueQuery(qb, fieldKey, tokens);

	}

	private Query buildQueryForTextCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {

		AdvancedSearchTextFieldModel model = (AdvancedSearchTextFieldModel) fieldModel;
		List<String> tokens = getTokens(model.getValue());
		return tokens.isEmpty() ? null : buildLuceneTextQuery(qb, fieldKey, tokens);
	}

	private List<String> getTokens(String value){

		if (value != null && StringUtils.isNotBlank(value)) {
			return  parseInput(value);
		}

		return Collections.emptyList();
	}

	private Query buildQueryForListCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {

		AdvancedSearchListFieldModel listModel = (AdvancedSearchListFieldModel) fieldModel;
		if (listModel.getValues() != null) {
			return buildLuceneValueInListQuery(qb, fieldKey, listModel.getValues(), false);
		}

		return null;
	}

	private List<String> parseInput(String textInput) {
		return new StrTokenizer(textInput, StrMatcher.trimMatcher(), StrMatcher.doubleQuoteMatcher()).getTokenList();
	}

	private Query buildQueryForRangeCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {
		AdvancedSearchRangeFieldModel rangeModel = (AdvancedSearchRangeFieldModel) fieldModel;
		if (rangeModel.getMinValue() != null || rangeModel.getMaxValue() != null) {
			return buildLuceneRangeQuery(qb, fieldKey, rangeModel.getMinValue(), rangeModel.getMaxValue());
		}

		return null;
	}

	private Query buildQueryForNumericRangeCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {
		AdvancedSearchNumericRangeFieldModel rangeModel = (AdvancedSearchNumericRangeFieldModel) fieldModel;
		if (rangeModel.getMinValue() != null || rangeModel.getMaxValue() != null) {
			return buildLuceneNumericRangeQuery(qb, fieldKey, rangeModel.getMinValueAsDouble(), rangeModel.getMaxValueAsDouble());
		}
		return null;
	}

	private Query buildQueryForTagsCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel, QueryBuilder qb) {

		AdvancedSearchTagsFieldModel model = (AdvancedSearchTagsFieldModel) fieldModel;

		if (model == null) {
			// TODO code cleanup lead to this statement, which reeks of impending NPE
			return null;
		}

		List<String> tags = model.getTags();
		Operation operation = model.getOperation();

		return buildLuceneTagsQuery(qb, fieldKey, tags, operation);

	}

	protected Query buildLuceneQuery(QueryBuilder qb, List<TestCase> testcaseList) {

		Query mainQuery = null;
		Query query;

		for (TestCase testcase : testcaseList) {
			List<String> id = new ArrayList<>();
			id.add(testcase.getId().toString());
			query = buildLuceneSingleValueQuery(qb, "id", id);

			if (query != null && mainQuery == null) {
				mainQuery = query;
			} else if (query != null) {
				mainQuery = qb.bool().should(mainQuery).should(query).createQuery();
			}
		}
		return mainQuery;
	}

	protected Query buildLuceneQuery(QueryBuilder qb, AdvancedSearchModel model) {

		// find the milestone ids and add them to the model
		if (featureManager.isEnabled(Feature.MILESTONE)) {
			addMilestoneFilter(model);
		}

		// now remove the criteria from the form before the main search begins
		removeMilestoneSearchFields(model);

		return buildCoreLuceneQuery(qb, model);
	}

	protected Query buildCoreLuceneQuery(QueryBuilder qb, AdvancedSearchModel model) {

		Query mainQuery = null;

		// issue #5079
		secureProjectCriteria(model);

		Set<String> fieldKeys = model.getFields().keySet();

		for (String fieldKey : fieldKeys) {

			AdvancedSearchFieldModel fieldModel = model.getFields().get(fieldKey);
			AdvancedSearchFieldModelType type = fieldModel.getType();

			Query query = buildQueryDependingOnType(qb, fieldKey, fieldModel, type);

			if (query != null) {
				if (mainQuery == null) {
					mainQuery = query;
				} else {
					mainQuery = qb.bool().must(mainQuery).must(query).createQuery();
				}
			}

		}

		return mainQuery;
	}

	@SuppressWarnings("unchecked")
	protected void addMilestoneFilter(AdvancedSearchModel searchModel) {
		Map<String, AdvancedSearchFieldModel> fields = searchModel.getFields();

		AdvancedSearchSingleFieldModel searchByMilestone = (AdvancedSearchSingleFieldModel) fields
			.get("searchByMilestone");

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

	protected Criteria createMilestoneHibernateCriteria(Map<String, AdvancedSearchFieldModel> fields) {

		Session session = em.unwrap(Session.class);
		Criteria crit = session.createCriteria(Milestone.class, "milestone");

		for (Entry<String, AdvancedSearchFieldModel> entry : fields.entrySet()) {

			AdvancedSearchFieldModel model = entry.getValue();
			if (model != null) {

				switch (entry.getKey()) {

					case "milestone.label":

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
						break;

					case "milestone.status":
						List<String> statusValues = ((AdvancedSearchListFieldModel) model).getValues();

						if (statusValues != null && !statusValues.isEmpty()) {
							crit.add(Restrictions.in("status", convertStatus(statusValues)));
						}

						break;

					case "milestone.endDate":
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

	protected Query buildLuceneTagsQuery(QueryBuilder qb, String fieldKey, List<String> tags, Operation operation) {

		Query main = null;

		@SuppressWarnings("unchecked")
		List<String> lowerTags = (List<String>) CollectionUtils.collect(tags, new Transformer() {
			@Override
			public Object transform(Object input) {
				return ((String) input).toLowerCase();
			}
		});

		switch (operation) {
			case AND:
				Query query;
				for (String tag : lowerTags) {
					query = qb.bool().must(qb.phrase().withSlop(0).onField(fieldKey).ignoreFieldBridge().ignoreAnalyzer()
						.sentence(tag).createQuery()).createQuery();

					if (query == null) {
						break;
					}
					if (main == null) {
						main = query;
					} else {
						main = qb.bool().must(main).must(query).createQuery();
					}
				}

				return qb.bool().must(main).createQuery();

			case OR:
				return buildLuceneValueInListQuery(qb, fieldKey, lowerTags, true);

			default:
				throw new IllegalArgumentException("search on tag '" + fieldKey + "' : operation unknown");

		}
	}

	private Query buildQueryDependingOnType(QueryBuilder qb, String fieldKey,
											AdvancedSearchFieldModel fieldModel, AdvancedSearchFieldModelType type) {
		Query query = null;
		switch (type) {
			case SINGLE:
				query = buildQueryForSingleCriterium(fieldKey, fieldModel, qb);
				break;
			case LIST:
				query = buildQueryForListCriterium(fieldKey, fieldModel, qb);
				break;
			case TEXT:
				query = buildQueryForTextCriterium(fieldKey, fieldModel, qb);
				break;
			case RANGE:
				query = buildQueryForRangeCriterium(fieldKey, fieldModel, qb);
				break;
			case NUMERIC_RANGE:
				query = buildQueryForNumericRangeCriterium(fieldKey, fieldModel, qb);
				break;
			case TIME_INTERVAL:
				query = buildQueryForTimeIntervalCriterium(fieldKey, fieldModel, qb);
				break;
			case CF_TIME_INTERVAL:
			query = buildQueryForTimeIntervalCriterium(fieldKey, fieldModel, qb);
				break;
			case TAGS:
				query = buildQueryForTagsCriterium(fieldKey, fieldModel, qb);
				break;
			default:
				break;
		}
		return query;
	}


	private Query buildQueryForTimeIntervalCriterium(String fieldKey, AdvancedSearchFieldModel fieldModel,
			QueryBuilder qb) {
		AdvancedSearchTimeIntervalFieldModel intervalModel = (AdvancedSearchTimeIntervalFieldModel) fieldModel;
		Date startDate = intervalModel.getStartDate();
		Date endDate = intervalModel.getEndDate();

		Query sub;
		RangeMatchingContext range = qb.range().onField(fieldKey);

		if (startDate != null && endDate != null) {
			long start = dateToLongParam(startDate);
			long end = dateToLongParam(endDate);

			sub = range.from(start).to(end).createQuery();

		} else if (startDate != null) {
			long start = dateToLongParam(startDate);
			sub = range.above(start).createQuery();

		} else if (endDate != null) {
			long end = dateToLongParam(endDate);
			sub = range.below(end).createQuery();

		} else {
			// we're doomed
			return null;

		}

		return qb.bool().must(sub).createQuery();
	}

	/**
	 * Coerces a Date into a long to be used as a hibernate search query param.
	 * This is necessary to work around a bug in NumericFieldUtils.requiresNumericRangeQuery which does not correctly
	 * detect Calendars
	 */
	private long dateToLongParam(Date startDate) {
		return DateTools.round(startDate.getTime(), DateTools.Resolution.DAY);
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
			findAllReadablesId().stream().forEach(r-> {
				approvedIds.add(String.valueOf(r));
			});

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

		AdvancedSearchSingleFieldModel searchByMilestone = (AdvancedSearchSingleFieldModel) model.getFields().get("searchByMilestone");
		AdvancedSearchSingleFieldModel activeMilestoneMode = (AdvancedSearchSingleFieldModel) model.getFields().get("activeMilestoneMode");
		boolean hasCriteria = (searchByMilestone != null && "true".equals(searchByMilestone.getValue())) || (activeMilestoneMode != null && "true".equals(activeMilestoneMode.getValue()));

		return enabled && hasCriteria;
	}

	public List<Long> findAllReadablesId(){
		UserDto currentUser = userAccountService.findCurrentUserDto();
		List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
		return readableProjectIds;
	}



}
