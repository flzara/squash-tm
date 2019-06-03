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

import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Order;
import static org.apache.commons.lang3.StringUtils.*;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.hibernate.HibernateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
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
import org.squashtest.tm.domain.search.AdvancedSearchTextFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTimeIntervalFieldModel;
import org.squashtest.tm.domain.search.QueryCufLabel;
import org.squashtest.tm.domain.search.SearchCustomFieldCheckBoxFieldModel;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.repository.ColumnPrototypeDao;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.query.ConfiguredQuery;
import org.squashtest.tm.service.query.QueryProcessingService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings.ColumnMapping;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings.SpecialHandler;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This converter is used to create a ConfiguredQuery with some parameters(paging, datatable and searchfield).
 *
 * The goal is to build a query which select only the ids of the entity that match all the criteria. The calling
 * service must then fetch the entities using those ids.
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AdvancedSearchQueryModelToConfiguredQueryConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdvancedSearchQueryModelToConfiguredQueryConverter.class);

	private static final String PROJECT_FILTER_NAME = "project.id";


	private AdvancedSearchColumnMappings mappings;

	@Inject
	private ColumnPrototypeDao columnPrototypeDao;

	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private QueryProcessingService queryService;



	private AdvancedSearchQueryModel advancedSearchQueryModel;

	private Map<String, QueryColumnPrototype> prototypesByLabel = new HashMap<>();

	public AdvancedSearchQueryModelToConfiguredQueryConverter configureModel(AdvancedSearchQueryModel advancedSearchQueryModel) {
		this.advancedSearchQueryModel = advancedSearchQueryModel;
		return this;
	}

	public AdvancedSearchQueryModelToConfiguredQueryConverter configureMapping(AdvancedSearchColumnMappings mappings) {
		this.mappings = mappings;
		return this;
	}

	public <T> HibernateQuery<T> prepare() {

		// Find all ColumnPrototypes
		prototypesByLabel = columnPrototypeDao.findAll().stream().collect(Collectors.toMap(
			col -> col.getLabel(),
			col -> col
		));

		// Create queryModel
		QueryModel query = createBaseQueryModel();

		// Configure the Query
		ConfiguredQuery configuredQuery = new ConfiguredQuery();
		configuredQuery.setQueryModel(query);
		configuredQuery.setPaging(advancedSearchQueryModel.getPageable());


		// generate the ExtendedHibernateQuery
		ExtendedHibernateQuery<T> hibQuery = queryService.prepareQuery(configuredQuery);

		// now append the special handlers
		applyAllSpecialHandlers(hibQuery);

		return hibQuery;

	}

	// *************** Bulk query creation methods ************************


	private QueryModel createBaseQueryModel() {
		QueryModel query = new QueryModel();
		query.setStrategy(QueryStrategy.MAIN);
		query.setJoinStyle(NaturalJoinStyle.INNER_JOIN);

		// In the search field, projectids are not always set so we must secure this point on projects that the user can read.
		secureProjectFilter(advancedSearchQueryModel.getModel());

		// create the select
		List<QueryProjectionColumn> projections = createMappedProjections();
		query.setProjectionColumns(projections);

		// create the filters
		List<QueryFilterColumn> filters = createMappedFilters();
		query.setFilterColumns(filters);

		// create the ordering
		List<QueryOrderingColumn> orders = createMappedOrders();
		query.setOrderingColumns(orders);

		return query;
	}


	private void applyAllSpecialHandlers(ExtendedHibernateQuery<?> query){

		applySpeciallyHandledFilters(query);
		applySpeciallyHandlerOrders(query);

	}

	// ********************** Projection creations ***********************************


	/**
	 *	Create the query column projections for the mapped attribute, ie where an attribute of the desired result set
	 * can actually be mapped to a QueryColumnPrototype.
	 *
	 * For now we simplify the problem and only project on the entity id.
	 */
	private List<QueryProjectionColumn> createMappedProjections() {

		QueryProjectionColumn projection = new QueryProjectionColumn();

		String idKey = mappings.getIdKey();
		String columnLabel = mappings.getResultMapping().findColumnPrototypeLabel(idKey);

		QueryColumnPrototype proto = lookupColumnPrototype(columnLabel);
		projection.setColumnPrototype(proto);
		projection.setOperation(Operation.NONE);

		return ImmutableList.of(projection);

	}



	/*
		ON HOLD :
		that method is suspended : indeed for now the projection returns an entity, not tuples.
		Delete before release if that method was not needed after all.

	private List<QueryProjectionColumn> createMappedProjections() {

		ColumnMapping resultMappings = mappings.getResultMapping();

		// the keys to be processed, ie only those that exist as a "mappedKey"
		List<String> processableKeys = advancedSearchQueryModel.getSearchResultColumns();
		processableKeys.retainAll(resultMappings.getMappedKeys());


		List<QueryProjectionColumn> projections = new ArrayList<>();


		// now build the columns
		for (String key : processableKeys) {

			QueryProjectionColumn projection = new QueryProjectionColumn();

			String colLabel = resultMappings.findColumnPrototypeLabel(key);

			projection.setColumnPrototype(lookupColumnPrototype(colLabel));

			Operation operation = Operation.NONE;

			projection.setOperation(operation);
			projections.add(projection);
		}


		return projections;
	}
	*/

	/**
	 * For the expected result keys, apply the missing select clauses
	 *
	 */
	/*
		ON HOLD :
		that method is suspended : indeed for now the projection returns an entity, not tuples.
		Delete before release if that method was not needed after all.

	private void applySpeciallyHandledProjections(ExtendedHibernateQuery<?> query){


		// TODO : the trick here will be to add the select clauses at the expected position,
		// which might not be possible using the public query builder API. Hmm.

		ColumnMapping resultMappings = mappings.getResultMapping();

		// the keys to be processed, ie only those that exist as a "specialHandlers"
		List<String> processableKeys = advancedSearchQueryModel.getSearchResultColumns();
		processableKeys.retainAll(resultMappings.getSpecialKeys());


		// now build the columns
		for (String key: processableKeys){
			SpecialHandler handler = resultMappings.findHandler(key);

			EntityPath<?> attributePath = handler.getAttributePath.get();

			query.select(attributePath);
		}

	}
	*/

	// ********************** Filters creation  ***********************************


	/**
	 * Extract filters from the AdvancedSearchQueryModel. Only the form keys
	 * that are mapped to existing columns prototypes (eg, mappings.getFormMappings().isMappedKey(key) == true),
	 * or the custom fields, will be processed. The others will be processed in the second pass (the special handlings).
	 *
	 * @return
	 */
	private List<QueryFilterColumn> createMappedFilters() {

		List<QueryFilterColumn> filters = new ArrayList<>();

		ColumnMapping formMapping = mappings.getFormMapping();

		AdvancedSearchModel model = advancedSearchQueryModel.getModel();
		Set<String> processableKeys = model.getFields().keySet();


		/*
		 * Exclude the specially handled keys. This will leave us with
		 * the mapped keys, and custom field keys.
		 */
		processableKeys.removeAll(formMapping.getSpecialKeys());

		// now process
		for (String key : processableKeys) {

			AdvancedSearchFieldModel fieldModel = model.getFields().get(key);
			AdvancedSearchFieldModelType fieldType = fieldModel.getType();

			QueryFilterColumn filter = createFilterColumn(fieldModel, fieldType, key);

			if (filter != null) {
				filters.add(filter);
			}
		}

		return filters;
	}


	/*
	 * Once the query is available, we can apply special handlers for filters
	 */

	private void applySpeciallyHandledFilters(ExtendedHibernateQuery<?> query){

		AdvancedSearchModel model = advancedSearchQueryModel.getModel();

		ColumnMapping formMapping = mappings.getFormMapping();

		Set<String> processableKeys =  model.getFields().keySet();

		// process only the specially handled filters this time
		processableKeys.retainAll(formMapping.getSpecialKeys());

		for (String key: processableKeys){
			SpecialHandler handler = formMapping.findHandler(key);
			AdvancedSearchFieldModel searchField = model.getFields().get(key);
			handler.applyFilter.accept(query, searchField);
		}

	}




	private QueryFilterColumn createFilterColumn(AdvancedSearchFieldModel fieldModel,
												 AdvancedSearchFieldModelType fieldType,
												 String key) {

		ColumnMapping resultMapping = mappings.getFormMapping();

		QueryFilterColumn queryFilterColumn = null;

		// retrieve the query column label, if the column is mapped.
		// if not, leave the label as the key itself and the custom field column
		// will be looked up differently.
		String columnLabel = null;
		if (resultMapping.isMappedKey(key)){
			columnLabel = resultMapping.findColumnPrototypeLabel(key);
		}
		else{
			columnLabel = key;
		}


		switch (fieldType) {
			// regular fields (most of them at least)
			case TIME_INTERVAL:
				queryFilterColumn = createFilterToTimeInterval(fieldModel, false, columnLabel);
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
				queryFilterColumn = createFilterToText(fieldModel, key);
				break;
			case LIST:
				queryFilterColumn = createFilterToList(fieldModel, false, columnLabel);
				break;
			case SINGLE:
				queryFilterColumn = createFilterToSingle(fieldModel, false, columnLabel);
				break;

			// custom fields
			case CF_SINGLE:
				queryFilterColumn = createFilterToSingle(fieldModel, true, key);
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
			case TAGS:
				queryFilterColumn = createFilterToTags(fieldModel, key);
				break;
			case CF_NUMERIC_RANGE:
				queryFilterColumn = createFilterToNumeric(fieldModel, true, key);
				break;
			default:
				throw new RuntimeException("Programming error : FieldType '"+fieldType+"' unknown f, couldn't create filter for search form attribute '"+key+"'");

		}

		return queryFilterColumn;
	}

	/**
	 * @param model
	 * @param isCuf if field is a CustomField
	 * @param key Label of the QueryColumnPrototype of id of the CustomField
	 * @return
	 */
	private QueryFilterColumn createFilterToSingle(AdvancedSearchFieldModel model, boolean isCuf, String key) {

		QueryFilterColumn filter = new QueryFilterColumn();

		AdvancedSearchSingleFieldModel singleFieldModel = (AdvancedSearchSingleFieldModel) model;

		String value = singleFieldModel.getValue();

		if (isBlank(value)) {
			return null;
		}

		filter.setOperation(Operation.MATCHES);
		filter.getValues().add(value);

		if (isCuf) {

			String colLabel = lookupCufColumnLabel(QueryCufLabel.CF_SINGLE);
			filter.setColumnPrototype(lookupColumnPrototype(colLabel));
			filter.setCufId(Long.parseLong(key));

		}
		else {
			filter.setColumnPrototype(lookupColumnPrototype(key));
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

		String label = lookupCufColumnLabel(QueryCufLabel.TAGS);
		QueryColumnPrototype queryColumnPrototype = lookupColumnPrototype(label);

		filter.setColumnPrototype(queryColumnPrototype);
		filter.setCufId(Long.parseLong(cufId));

		//TODO update the engine to handle the 'AND' case
		if (AdvancedSearchTagsFieldModel.Operation.AND.equals(fieldModel.getOperation())) {
			filter.setOperation(Operation.IN);
		}
		else {
			filter.setOperation(Operation.IN);
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
			filter.addValues(Arrays.asList(toIso(startDate), toIso(endDate)));
		}
		else if (startDate != null) {
			filter.setOperation(Operation.GREATER_EQUAL);
			filter.getValues().add(toIso(startDate));
		}
		else if (endDate != null) {
			filter.setOperation(Operation.LOWER_EQUAL);
			filter.getValues().add(toIso(endDate));
		}
		else {
			return null;
		}

		if (isCuf) {

			String columnPrototypeLabel = lookupCufColumnLabel(QueryCufLabel.CF_TIME_INTERVAL);
			filter.setColumnPrototype(lookupColumnPrototype(columnPrototypeLabel));
			filter.setCufId(Long.parseLong(key));

		}
		else {

			filter.setColumnPrototype(lookupColumnPrototype(key));

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

		if (isBlank(minValue) && isBlank(maxValue)) {
			filter.setOperation(Operation.BETWEEN);
			filter.getValues().add(minValue);
			filter.getValues().add(maxValue);
		}
		else if (isBlank(minValue)) {
			filter.setOperation(Operation.GREATER_EQUAL);
			filter.getValues().add(minValue);
		}
		else if (isBlank(maxValue)) {
			filter.setOperation(Operation.LOWER_EQUAL);
			filter.getValues().add(maxValue);
		}
		else {
			return null;
		}

		if (isCuf) {

			String label = lookupCufColumnLabel(QueryCufLabel.CF_NUMERIC);
			filter.setColumnPrototype(lookupColumnPrototype(label));
			filter.setCufId(Long.parseLong(key));

		} else {

			filter.setColumnPrototype(lookupColumnPrototype(key));

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
			String label = lookupCufColumnLabel(QueryCufLabel.CF_LIST);
			queryFilterColumn.setColumnPrototype(lookupColumnPrototype(label));
			queryFilterColumn.setCufId(Long.parseLong(key));
		}
		else {
			queryFilterColumn.setColumnPrototype(lookupColumnPrototype(key));
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


		queryFilterColumn.addValues(values);

		queryFilterColumn.setOperation(Operation.IN);
		queryFilterColumn.setColumnPrototype(lookupColumnPrototype(key));
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
			queryFilterColumn.getValues().add(minValue.toString());
			queryFilterColumn.getValues().add(maxValue.toString());
		}
		else if (minValue != null) {
			queryFilterColumn.setOperation(Operation.GREATER_EQUAL);
			queryFilterColumn.getValues().add(minValue.toString());
		}
		else if (maxValue != null) {
			queryFilterColumn.setOperation(Operation.LOWER_EQUAL);
			queryFilterColumn.getValues().add(maxValue.toString());
		}
		else {
			return null;
		}

		queryFilterColumn.setColumnPrototype(lookupColumnPrototype(key));

		return queryFilterColumn;
	}

	// TODO create the filter for fulltext search
	private QueryFilterColumn createFilterToText(AdvancedSearchFieldModel model, String key) {

		QueryFilterColumn filter = new QueryFilterColumn();

		AdvancedSearchTextFieldModel textFieldModel = (AdvancedSearchTextFieldModel)model;

		// TODO: something better
		filter.setOperation(Operation.LIKE);

		String value = textFieldModel.getValue();
		filter.getValues().add("%"+value+"%");

		filter.setColumnPrototype(lookupColumnPrototype(key));
		return filter;
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

		filter.setColumnPrototype(lookupColumnPrototype(key));

		return filter;
	}


	// ****************************** Order creation **************************


	private List<QueryOrderingColumn> createMappedOrders() {

		ColumnMapping resultMapping = mappings.getResultMapping();

		Pageable pageable = advancedSearchQueryModel.getPageable();
		List<QueryOrderingColumn> orders = new ArrayList<>();
		Sort sort = pageable.getSort();

		if (sort != null) {
			orders = sort.stream()
						 .filter( order -> resultMapping.isMappedKey(order.getProperty()))
						 .map(this::convertToOrder)
						 .collect(Collectors.toList());
		}
		return orders;
	}


	private void applySpeciallyHandlerOrders(ExtendedHibernateQuery<?> query){

		// TODO : add the order by clauses in the correct order, which might be difficult,
		// see #applySpeciallyHandledProjections

		ColumnMapping resultMapping = mappings.getResultMapping();

		Pageable pageable = advancedSearchQueryModel.getPageable();
		Sort sort = pageable.getSort();

		if (sort != null) {
			sort.stream()
				 .filter( order -> resultMapping.isSpecialKey(order.getProperty()))
				 .forEach(order -> applySpecialOrder(order, query));
		}

	}


	private QueryOrderingColumn convertToOrder(Sort.Order specifier) {

		QueryOrderingColumn queryOrderingColumn = new QueryOrderingColumn();

		String property = specifier.getProperty();
		//property = formatSortedFieldName(property);

		String columnPrototypeLabel = mappings.getResultMapping().findColumnPrototypeLabel(property);

		QueryColumnPrototype column = lookupColumnPrototype(columnPrototypeLabel);

		Order order = specifier.isAscending() ? Order.ASC : Order.DESC;

		queryOrderingColumn.setOrder(order);
		queryOrderingColumn.setColumnPrototype(column);
		return queryOrderingColumn;

	}

	private void applySpecialOrder(Sort.Order specifier, ExtendedHibernateQuery<?> query){

		String property = specifier.getProperty();

		SpecialHandler handler = mappings.getResultMapping().findHandler(property);
		EntityPath<?> attributePath = handler.getAttributePath.get();
		Order order = specifier.isAscending() ? Order.ASC : Order.DESC;

		query.orderBy(new OrderSpecifier(order, attributePath));

	}

/*
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
*/


	// **************** utility methods **************************************

	private final QueryColumnPrototype lookupColumnPrototype(String columnLabel) {
		return prototypesByLabel.computeIfAbsent(columnLabel, (label) -> { throw new IllegalArgumentException("column '"+label+"' is unknown (unmapped)"); });
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




	private String lookupCufColumnLabel(QueryCufLabel cufLabel){
		return mappings.getCufMapping().findColumnPrototypeLabel(cufLabel.toString());
	}

	private final String toIso(Date date){
		return DateUtils.formatIso8601Date(date);
	}

	// this remove the rank indicator which is prefixed in the
	private String stripLevelEnumRankPrefix(String original){
		int prefix = original.indexOf('-');
		return original.substring(prefix);
	}
}
