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

import com.querydsl.core.types.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.query.NaturalJoinStyle;
import org.squashtest.tm.domain.query.Operation;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryFilterColumn;
import org.squashtest.tm.domain.query.QueryModel;
import org.squashtest.tm.domain.query.QueryOrderingColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.QueryStrategy;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchMultiListFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchNumericRangeFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.AdvancedSearchRangeFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTagsFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTimeIntervalFieldModel;
import org.squashtest.tm.domain.search.QueryCufLabel;
import org.squashtest.tm.domain.search.SearchCustomFieldCheckBoxFieldModel;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.repository.ColumnPrototypeDao;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.query.ConfiguredQuery;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This converter is used to create a ConfiguredQuery with some parameters(paging, datatable and searchfield)
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AdvancedSearchQueryModelToConfiguredQueryConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchQueryModelToConfiguredQueryConverter.class);

	private static final String SEARCH_BY_MILESTONE = "searchByMilestone";

	private static final String PROJECT_FILTER_NAME = "project.id";

	private static final List<String> PROJECTIONS_COUNT_OPERATION = Arrays.asList("test-case-milestone-nb",
		"test-case-requirement-nb", "test-case-teststep-nb", "test-case-iteration-nb",
		"test-case-attachment-nb", "requirement-milestone-nb", "requirement-version-nb",
		"requirement-testcase-nb", "requirement-attachment-nb", "itpi-datasets", "itpi-testsuites");

	private Map<String, String> columnPrototypeMapping = new HashMap<>();

	@Inject
	private ColumnPrototypeDao columnPrototypeDao;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private UserAccountService userAccountService;

	private AdvancedSearchQueryModel advancedSearchQueryModel;

	private List<QueryColumnPrototype> prototypes = new ArrayList<>();

	public AdvancedSearchQueryModelToConfiguredQueryConverter configureModel(AdvancedSearchQueryModel advancedSearchQueryModel) {
		this.advancedSearchQueryModel = advancedSearchQueryModel;
		return this;
	}

	public AdvancedSearchQueryModelToConfiguredQueryConverter configureMapping(Map<String, String> columnPrototypeMapping) {
		this.columnPrototypeMapping = columnPrototypeMapping;
		return this;
	}

	public ConfiguredQuery convert() {

		// Find all ColumnPrototypes
		prototypes = columnPrototypeDao.findAll();

		// Create queryModel
		QueryModel query = createBaseQueryModel();

		// Configure the Query
		ConfiguredQuery configuredQuery = new ConfiguredQuery();
		configuredQuery.setQueryModel(query);
		configuredQuery.setPaging(advancedSearchQueryModel.getPageable());
		return configuredQuery;
	}

	private QueryModel createBaseQueryModel() {
		QueryModel query = new QueryModel();
		query.setStrategy(QueryStrategy.MAIN);
		query.setJoinStyle(NaturalJoinStyle.INNER_JOIN);

		// In the search field, projectids are not always set so we must secure this point on projects that the user can read.
		secureProjectFilter(advancedSearchQueryModel.getModel());

		// create the projections and add them to the query
		List<QueryProjectionColumn> projections = extractProjections();
		query.setProjectionColumns(projections);

		// create the filters
		List<QueryFilterColumn> filters = extractFilters();
		query.setFilterColumns(filters);

		// create the ordering
		List<QueryOrderingColumn> orders = extractOrders();
		query.setOrderingColumns(orders);

		return query;
	}

	/**
	 * Extract the projections from the AdvancedSearchQueryModel. The operator is different if
	 * we get the value of the property or if we calculate a value.
	 * @return
	 */
	private List<QueryProjectionColumn> extractProjections() {
		List<QueryProjectionColumn> projections = new ArrayList<>();
		Map<Integer, Object> data = advancedSearchQueryModel.getmDataProp();
		for (Map.Entry<Integer, Object> entry : data.entrySet()) {
			QueryProjectionColumn projection = new QueryProjectionColumn();
			projection.setColumnPrototype(getColumnPrototype(columnPrototypeMapping.get(entry.getValue().toString())));

			Operation operation = Operation.NONE;

			if (PROJECTIONS_COUNT_OPERATION.contains(entry.getValue().toString())) {
				operation = Operation.COUNT;
			}

			projection.setOperation(operation);
			projections.add(projection);
		}
		return projections;
	}

	/**
	 * Extract filters from the AdvancedSearchQueryModel.
	 * If SEARCH_BY_MILESTONE is not null we delete this field because it is not use by the engine.
	 * The advancedSearchQueryModel contains a AdvancedSearchModel that define search fields.
	 * These fields are different type so we must treat them differently.
	 * @return
	 */
	private List<QueryFilterColumn> extractFilters() {
		List<QueryFilterColumn> filters = new ArrayList<>();

		AdvancedSearchModel model = advancedSearchQueryModel.getModel();
		AdvancedSearchSingleFieldModel searchByMilestone = (AdvancedSearchSingleFieldModel) model
			.getFields().get(SEARCH_BY_MILESTONE);
		if (searchByMilestone != null && "true".equals(searchByMilestone.getValue())) {
			model.getFields().remove(SEARCH_BY_MILESTONE);
		}
		Set<String> keys = model.getFields().keySet();
		for (String key : keys) {
			AdvancedSearchFieldModel fieldModel = model.getFields().get(key);
			AdvancedSearchFieldModelType fieldType = fieldModel.getType();
			QueryFilterColumn filter = getFilterColumn(fieldModel, fieldType, key);
			if (filter != null) {
				filters.add(filter);
			}
		}
		return filters;
	}

	private QueryFilterColumn getFilterColumn(AdvancedSearchFieldModel fieldModel,
											  AdvancedSearchFieldModelType fieldType, String key) {
		QueryFilterColumn queryFilterColumn = null;
		String columnLabel = columnPrototypeMapping.get(key);
		switch (fieldType) {
			case TIME_INTERVAL:
				queryFilterColumn = createFilterToTimeInterval(fieldModel, false, columnLabel);
				break;
			case CF_TIME_INTERVAL:
				queryFilterColumn = createFilterToTimeInterval(fieldModel, true, key);
				break;
			case CF_LIST:
				queryFilterColumn = createFilterToList(fieldModel, true, key);
				break;
			case CF_CHECKBOX:
				queryFilterColumn = createFilterToCheckBox(fieldModel, key);
				break;
			case CF_NUMERIC_RANGE:
				queryFilterColumn = createFilterToNumeric(fieldModel, true, key);
				break;
			case NUMERIC_RANGE:
				queryFilterColumn = createFilterToNumeric(fieldModel, false, columnLabel);
				break;
			case MULTILIST:
				queryFilterColumn = createFilterToMultiList(fieldModel, columnLabel);
				break;
			case RANGE:
				queryFilterColumn = createFilterToRange(fieldModel, columnLabel);
				break;
			case TEXT:
				queryFilterColumn = createFilterToText(fieldModel);
				break;
			case TAGS:
				queryFilterColumn = createFilterToTags(fieldModel, key);
				break;
			case LIST:
				queryFilterColumn = createFilterToList(fieldModel, false, columnLabel);
				break;
			case SINGLE:
				queryFilterColumn = createFilterToSingle(fieldModel, false, columnLabel);
				break;
			case CF_SINGLE:
				queryFilterColumn = createFilterToSingle(fieldModel, true, key);
				break;
			default:
				break;
		}

		return queryFilterColumn;
	}

	/**
	 * The character '*' is a wildcard.
	 * @param model
	 * @param isCuf if field is a CustomField
	 * @param key Label of the QueryColumnPrototype of id of the CustomField
	 * @return
	 */
	private QueryFilterColumn createFilterToSingle(AdvancedSearchFieldModel model, boolean isCuf, String key) {

		QueryFilterColumn filter = new QueryFilterColumn();
		AdvancedSearchSingleFieldModel singleFieldModel = (AdvancedSearchSingleFieldModel) model;
		String value = singleFieldModel.getValue();
		if ("".equals(value)) {
			return null;
		}

		if (value.contains("*")) {
			filter.setOperation(Operation.LIKE);
			value = value.replace("*", "%");
		} else {
			filter.setOperation(Operation.EQUALS);
		}
		filter.addValues(Collections.singletonList(value));

		if (isCuf) {
			filter.setColumnPrototype(getColumnPrototype(columnPrototypeMapping.get(QueryCufLabel.CF_SINGLE.name())));
			filter.setCufId(Long.parseLong(key));
		} else {
			filter.setColumnPrototype(getColumnPrototype(key));
		}

		return filter;
	}

	/**
	 *
	 * @param model
	 * @param cufId
	 * @return
	 */
	private QueryFilterColumn createFilterToTags(AdvancedSearchFieldModel model, String cufId) {
		QueryFilterColumn filter = new QueryFilterColumn();
		AdvancedSearchTagsFieldModel fieldModel = (AdvancedSearchTagsFieldModel) model;
		List<String> tags = fieldModel.getTags();
		filter.addValues(tags);
		QueryColumnPrototype queryColumnPrototype = getColumnPrototype(columnPrototypeMapping.get(QueryCufLabel.TAGS));
		filter.setColumnPrototype(queryColumnPrototype);
		filter.setCufId(Long.parseLong(cufId));
		//TODO find the good operator for these cases
		if (AdvancedSearchTagsFieldModel.Operation.AND.equals(fieldModel.getOperation())) {
			filter.setOperation(Operation.EQUALS);
		} else {
			filter.setOperation(Operation.EQUALS);
		}
		return filter;
	}

	/**
	 * For this case we should change the operator if startDate and/or endDate are null.
	 * @param model
	 * @param isCuf if field is a CustomField
	 * @param key Label of the QueryColumnPrototype of id of the CustomField
	 * @return
	 */
	private QueryFilterColumn createFilterToTimeInterval(AdvancedSearchFieldModel model, boolean isCuf, String key) {
		QueryFilterColumn filter = new QueryFilterColumn();

		AdvancedSearchTimeIntervalFieldModel intervalFieldModel = (AdvancedSearchTimeIntervalFieldModel) model;
		Date startDate = intervalFieldModel.getStartDate();
		Date endDate = intervalFieldModel.getEndDate();

		if (startDate != null && endDate != null) {
			filter.setOperation(Operation.BETWEEN);
			filter.addValues(Arrays.asList(String.valueOf(startDate), String.valueOf(endDate)));
		} else if (startDate != null) {
			filter.setOperation(Operation.GREATER_EQUAL);
			filter.addValues(Collections.singletonList(String.valueOf(startDate)));
		} else if (endDate != null) {
			filter.setOperation(Operation.LOWER_EQUAL);
			filter.addValues(Collections.singletonList(String.valueOf(endDate)));
		} else {
			return null;
		}

		if (isCuf) {
			String columnPrototypeLabel = columnPrototypeMapping.get(QueryCufLabel.CF_TIME_INTERVAL);
			filter.setColumnPrototype(getColumnPrototype(columnPrototypeLabel));
			filter.setCufId(Long.parseLong(key));
		} else {
			filter.setColumnPrototype(getColumnPrototype(key));
		}
		return filter;
	}

	/**
	 * For this case we should change the operator if minValue and/or maxValue are null.
	 * @param model
	 * @param isCuf if field is a CustomField
	 * @param key Label of the QueryColumnPrototype of id of the CustomField
	 * @return
	 */
	private QueryFilterColumn createFilterToNumeric(AdvancedSearchFieldModel model, boolean isCuf,
													String key) {
		QueryFilterColumn filter = new QueryFilterColumn();
		AdvancedSearchNumericRangeFieldModel numericRangeFieldModel = (AdvancedSearchNumericRangeFieldModel) model;
		String minValue = numericRangeFieldModel.getMinValue();
		String maxValue = numericRangeFieldModel.getMaxValue();

		if (minValue != null && maxValue != null) {
			filter.setOperation(Operation.BETWEEN);
		} else if (minValue != null) {
			filter.setOperation(Operation.GREATER_EQUAL);
		} else if (maxValue != null) {
			filter.setOperation(Operation.LOWER_EQUAL);
		} else {
			return null;
		}

		if (isCuf) {
			filter.setColumnPrototype(getColumnPrototype(columnPrototypeMapping.get(QueryCufLabel.CF_NUMERIC)));
			filter.setCufId(Long.parseLong(key));
		} else {
			filter.setColumnPrototype(getColumnPrototype(key));
		}

		return filter;
	}

	/**
	 *
	 * @param model
	 * @param isCuf
	 * @param key
	 * @return
	 */
	private QueryFilterColumn createFilterToList(AdvancedSearchFieldModel model, boolean isCuf,
												 String key) {
		QueryFilterColumn queryFilterColumn = new QueryFilterColumn();

		AdvancedSearchListFieldModel listModel = (AdvancedSearchListFieldModel) model;

		List<String> values = listModel.getValues();
		if (values == null || values.isEmpty()) {
			return null;
		}

		if (isCuf) {
			queryFilterColumn.setColumnPrototype(getColumnPrototype(columnPrototypeMapping.get(QueryCufLabel.CF_LIST)));
			queryFilterColumn.setCufId(Long.parseLong(key));
		} else {
			queryFilterColumn.setColumnPrototype(getColumnPrototype(key));
		}
		queryFilterColumn.addValues(values);
		queryFilterColumn.setOperation(Operation.IN);

		return queryFilterColumn;
	}

	// TODO We can delete this method because the engine can't use fields of this type
	private QueryFilterColumn createFilterToMultiList(AdvancedSearchFieldModel model, String key) {
		QueryFilterColumn queryFilterColumn = new QueryFilterColumn();

		AdvancedSearchMultiListFieldModel listModel = (AdvancedSearchMultiListFieldModel) model;

		List<String> values = listModel.getValues();
		if (values == null || values.isEmpty()) {
			return null;
		}

		Integer minValue = listModel.getMinValue();
		Integer maxValue = listModel.getMaxValue();

		queryFilterColumn.addValues(values);

		queryFilterColumn.setOperation(Operation.IN);
		queryFilterColumn.setColumnPrototype(getColumnPrototype(key));
		return queryFilterColumn;
	}

	/**
	 * For this case we should change the operator if minValue and/or maxValue are null.
	 * @param model
	 * @param key
	 * @return
	 */
	private QueryFilterColumn createFilterToRange(AdvancedSearchFieldModel model, String key) {

		AdvancedSearchRangeFieldModel rangeField = (AdvancedSearchRangeFieldModel) model;
		Integer minValue = rangeField.getMinValue();
		Integer maxValue = rangeField.getMaxValue();
		QueryFilterColumn queryFilterColumn = new QueryFilterColumn();
		if (minValue != null && maxValue != null) {
			queryFilterColumn.setOperation(Operation.BETWEEN);
			queryFilterColumn.addValues(Arrays.asList(minValue.toString(), maxValue.toString()));
		} else if (minValue != null) {
			queryFilterColumn.setOperation(Operation.GREATER_EQUAL);
			queryFilterColumn.addValues(Collections.singletonList(minValue.toString()));
		} else if (maxValue != null) {
			queryFilterColumn.setOperation(Operation.LOWER_EQUAL);
			queryFilterColumn.addValues(Collections.singletonList(maxValue.toString()));
		} else {
			return null;
		}
		queryFilterColumn.setColumnPrototype(getColumnPrototype(key));

		return queryFilterColumn;
	}

	// TODO create the filter for fulltext search
	private QueryFilterColumn createFilterToText(AdvancedSearchFieldModel model) {
		return null;
	}

	/**
	 * This case configure filter to checkbox CustomField.
	 * @param model
	 * @param key
	 * @return
	 */
	private QueryFilterColumn createFilterToCheckBox(AdvancedSearchFieldModel model, String key) {
		QueryFilterColumn filter = new QueryFilterColumn();
		SearchCustomFieldCheckBoxFieldModel checkModel = (SearchCustomFieldCheckBoxFieldModel) model;
		filter.addValues(checkModel.getValues());
		filter.setOperation(Operation.EQUALS);
		filter.setCufId(Long.parseLong(key));
		return filter;
	}

	private List<QueryOrderingColumn> extractOrders() {

		Pageable pageable = advancedSearchQueryModel.getPageable();
		List<QueryOrderingColumn> orders = new ArrayList<>();
		Sort sort = pageable.getSort();

		if (sort != null) {
			orders = sort.stream().map(this::convertToOrder).collect(Collectors.toList());
		}
		return orders;
	}

	private QueryOrderingColumn convertToOrder(Sort.Order specifier) {
		QueryOrderingColumn queryOrderingColumn = new QueryOrderingColumn();

		String property = specifier.getProperty();
		property = formatSortedFieldName(property);
		String columnPrototypeLabel = columnPrototypeMapping.get(property);
		QueryColumnPrototype column = getColumnPrototype(columnPrototypeLabel);

		Order order = specifier.isAscending() ? Order.ASC : Order.DESC;

		queryOrderingColumn.setOrder(order);
		queryOrderingColumn.setColumnPrototype(column);
		return queryOrderingColumn;
	}

	private String formatSortedFieldName(String propertyName) {
		String result = propertyName;
		if (propertyName.startsWith("RequirementVersion.")) {
			result = propertyName.replaceFirst("RequirementVersion.", "");
		} else if (propertyName.startsWith("Requirement.")) {
			result = propertyName.replaceFirst("Requirement.", "requirement.");
		} else if (propertyName.startsWith("Project.")) {
			result = propertyName.replaceFirst("Project.", "project-");
		} else if (propertyName.startsWith("TestCase.")) {
			result = propertyName.replaceFirst("TestCase.", "");
		} else if (propertyName.startsWith("AutomationRequest.")) {
			result = propertyName.replaceFirst("AutomationRequest.", "automationRequest.");
		} else if (propertyName.startsWith("IterationTestPlanItem.")) {
			result = propertyName.replaceFirst("IterationTestPlanItem.", "");
		}
		return result;
	}

	private QueryColumnPrototype getColumnPrototype(String property) {
		Optional<QueryColumnPrototype> optionalColumn = prototypes.stream()
			.filter(col -> col.getLabel().equals(property))
			.findFirst();
		QueryColumnPrototype queryColumnPrototype = null;
		if (optionalColumn.isPresent()) {
			queryColumnPrototype = optionalColumn.get();
		}
		return queryColumnPrototype;
	}

	private void secureProjectFilter(AdvancedSearchModel model) {
		// Issue #5079 again
		// first task is to locate which name has the project criteria because it may differ depending on the interface
		// (test case, requirement, test-case-through-requirements
		String key = null;
		Set<String> keys = model.getFields().keySet();
		for (String k : keys) {
			if (k.contains(PROJECT_FILTER_NAME)) {
				key = k;
				break;
			}
		}
		// if no projectFilter was set -> nothing to do
		if (key == null) {
			return;
		}

		AdvancedSearchListFieldModel projectFilter = (AdvancedSearchListFieldModel) model.getFields().get(key);

		List<String> approvedIds;
		List<String> selectedIds = projectFilter.getValues();

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

		projectFilter.setValues(approvedIds);
	}

	public List<Long> findAllReadablesId() {
		UserDto currentUser = userAccountService.findCurrentUserDto();
		List<Long> readableProjectIds = projectFinder.findAllReadableIds(currentUser);
		return readableProjectIds;
	}
}
