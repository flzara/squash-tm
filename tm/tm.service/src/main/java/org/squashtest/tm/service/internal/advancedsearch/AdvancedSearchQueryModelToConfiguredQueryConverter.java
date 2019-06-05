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
import com.google.common.collect.Sets;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Order;
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
import org.squashtest.tm.domain.search.AdvancedSearchNumericRangeFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.AdvancedSearchRangeFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTagsFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTextFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTimeIntervalFieldModel;
import org.squashtest.tm.domain.search.SearchCustomFieldCheckBoxFieldModel;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings.ColumnMapping;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings.SpecialHandler;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.repository.ColumnPrototypeDao;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.query.ConfiguredQuery;
import org.squashtest.tm.service.query.QueryProcessingService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.UserAccountService;

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

	private static final Set<AdvancedSearchFieldModelType> CUSTOM_FIELD_TYPES = Sets.newHashSet(
		AdvancedSearchFieldModelType.CF_SINGLE,
		AdvancedSearchFieldModelType.CF_TIME_INTERVAL,
		AdvancedSearchFieldModelType.CF_LIST,
		AdvancedSearchFieldModelType.CF_CHECKBOX,
		AdvancedSearchFieldModelType.TAGS,
		AdvancedSearchFieldModelType.CF_NUMERIC_RANGE
	);


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

		QueryColumnPrototype proto = lookupColumnPrototypeByResultSetKey(idKey);

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

			if (fieldModel.isSet()){
				QueryFilterColumn filter = createFilterColumn(fieldModel, key);
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
												 String key) {

		if (! fieldModel.isSet()){
			throw new IllegalArgumentException("attempted apply form key '"+key+"' but the field model doesn't hold any value");
		}

		AdvancedSearchFieldModelType fieldType = fieldModel.getType();

		QueryFilterColumn queryFilterColumn = initFilterColumn(key, fieldType);


		switch (fieldType) {

			case TIME_INTERVAL:
			case CF_TIME_INTERVAL:
				filterByTimeInterval(queryFilterColumn, fieldModel);
				break;

			case SINGLE:
			case CF_SINGLE:
				filterByPlainText(queryFilterColumn, fieldModel);
				break;

			case TAGS:
				filterByTags(queryFilterColumn, fieldModel);
				break;

			case NUMERIC_RANGE:
			case CF_NUMERIC_RANGE:
				filterByNumericRange(queryFilterColumn, fieldModel);
				break;

			case RANGE:
				filterByIntegerRange(queryFilterColumn, fieldModel);
				break;

			case TEXT:
				filterByFullText(queryFilterColumn, fieldModel);
				break;

			case CF_CHECKBOX:
				filterByCheckbox(queryFilterColumn, fieldModel);
				break;

			case LIST:
			case CF_LIST:
				filterByList(queryFilterColumn, fieldModel);
				break;



			case MULTILIST:
				// TODO : indeed that one requires a special handler.
				throw new UnsupportedOperationException("input type '"+fieldType+"' is not supported yet ");


			default:
				throw new RuntimeException("Programming error : FieldType '" + fieldType + "' unknown, couldn't create filter for search form attribute '" + key + "'");

		}
		

		return queryFilterColumn;
	}


	/**
	 * Creates a QueryColumnFilter for the given form attribute key. The key is implicitly assumed to be
	 * an attribute of the search form, and is actually mapped to a column prototype (ie is not a specially
	 * hanlded column). Aside from that either custom fields or regular attributes are accepted.
	 *
	 * @param formAttributeKey
	 * @param type
	 * @return
	 */
	private QueryFilterColumn initFilterColumn(String formAttributeKey, AdvancedSearchFieldModelType type){

		QueryFilterColumn filter = new QueryFilterColumn();

		boolean isCustomfield = CUSTOM_FIELD_TYPES.contains(type);

		if (isCustomfield){
			long cufId = Long.parseLong(formAttributeKey);
			QueryColumnPrototype prototype = lookupColumnPrototypeByCufType(type);

			filter.setColumnPrototype(prototype);
			filter.setCufId(cufId);
		}
		else{
			QueryColumnPrototype prototype = lookupColumnPrototypeByFormKey(formAttributeKey);
			filter.setColumnPrototype(prototype);
		}

		return filter;

	}


	private void filterByPlainText(QueryFilterColumn filterColumn, AdvancedSearchFieldModel model) {

		AdvancedSearchSingleFieldModel singleFieldModel = (AdvancedSearchSingleFieldModel) model;

		String value = singleFieldModel.getValue();

		filterColumn.setOperation(Operation.MATCHES);
		filterColumn.getValues().add(value);

	}


	private void filterByTags(QueryFilterColumn filterColumn, AdvancedSearchFieldModel model) {

		AdvancedSearchTagsFieldModel fieldModel = (AdvancedSearchTagsFieldModel) model;
		List<String> tags = fieldModel.getTags();

		filterColumn.addValues(tags);

		if (AdvancedSearchTagsFieldModel.Operation.AND.equals(fieldModel.getOperation())) {
			filterColumn.setOperation(Operation.IN);
		}
		else {
			filterColumn.setOperation(Operation.IN);
		}

	}


	private void filterByTimeInterval(QueryFilterColumn filterColumn, AdvancedSearchFieldModel fieldModel) {


		AdvancedSearchTimeIntervalFieldModel intervalFieldModel = (AdvancedSearchTimeIntervalFieldModel) fieldModel;

		Date startDate = intervalFieldModel.getStartDate();
		Date endDate = intervalFieldModel.getEndDate();

		if (startDate != null && endDate != null) {
			filterColumn.setOperation(Operation.BETWEEN);
			filterColumn.addValues(Arrays.asList(toIso(startDate), toIso(endDate)));
		}
		else if (startDate != null) {
			filterColumn.setOperation(Operation.GREATER_EQUAL);
			filterColumn.getValues().add(toIso(startDate));
		}
		else if (endDate != null) {
			filterColumn.setOperation(Operation.LOWER_EQUAL);
			filterColumn.getValues().add(toIso(endDate));
		}

	}


	private void filterByNumericRange(QueryFilterColumn filterColumn, AdvancedSearchFieldModel fieldModel) {

		AdvancedSearchNumericRangeFieldModel numericRangeFieldModel = (AdvancedSearchNumericRangeFieldModel) fieldModel;

		boolean hasMin = numericRangeFieldModel.hasMinValue();
		boolean hasMax = numericRangeFieldModel.hasMaxValue();

		List<String> filterParameters = new ArrayList<>();


		if (hasMin){
			double min = numericRangeFieldModel.getLocaleAgnosticMinValue();
			filterParameters.add(String.valueOf(min));
		}

		if (hasMax){
			double max = numericRangeFieldModel.getLocaleAgnosticMaxValue();
			filterParameters.add(String.valueOf(max));
		}

		Operation operation = (hasMin && hasMax) ?
								  Operation.BETWEEN :
							  (hasMin) ?
								  Operation.GREATER_EQUAL :
								  Operation.LOWER_EQUAL;

		filterColumn.getValues().addAll(filterParameters);
		filterColumn.setOperation(operation);

	}


	private void filterByList(QueryFilterColumn filterColumn, AdvancedSearchFieldModel fieldModel){
		AdvancedSearchListFieldModel listModel = (AdvancedSearchListFieldModel) fieldModel;

		List<String> values = listModel.getValues();

		filterColumn.addValues(values);
		filterColumn.setOperation(Operation.IN);
	}



	private void filterByIntegerRange(QueryFilterColumn filterColumn, AdvancedSearchFieldModel fieldModel) {

		AdvancedSearchRangeFieldModel rangeField = (AdvancedSearchRangeFieldModel) fieldModel;

		boolean hasMin = rangeField.hasMinValue();
		boolean hasMax = rangeField.hasMaxValue();

		List<String> filterParameters = new ArrayList<>();


		if (hasMin){
			int min = rangeField.getMinValue();
			filterParameters.add(String.valueOf(min));
		}

		if (hasMax){
			int max = rangeField.getMaxValue();
			filterParameters.add(String.valueOf(max));
		}

		Operation operation = (hasMin && hasMax) ?
								  Operation.BETWEEN :
								  (hasMin) ?
									  Operation.GREATER_EQUAL :
									  Operation.LOWER_EQUAL;

		filterColumn.getValues().addAll(filterParameters);
		filterColumn.setOperation(operation);
	}

	// TODO create the filter for fulltext search
	private void filterByFullText(QueryFilterColumn filterColumn, AdvancedSearchFieldModel fieldModel) {

		AdvancedSearchTextFieldModel textFieldModel = (AdvancedSearchTextFieldModel)fieldModel;

		// TODO: use the fulltext operator
		filterColumn.setOperation(Operation.LIKE);

		String value = textFieldModel.getValue();
		filterColumn.getValues().add("%"+value+"%");
	}


	private void filterByCheckbox(QueryFilterColumn filterColumn, AdvancedSearchFieldModel fieldModel) {

		SearchCustomFieldCheckBoxFieldModel checkModel = (SearchCustomFieldCheckBoxFieldModel) fieldModel;

		filterColumn.addValues(checkModel.getValues());
		filterColumn.setOperation(Operation.EQUALS);
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

		String key = specifier.getProperty();


		QueryColumnPrototype column = lookupColumnPrototypeByResultSetKey(key);

		Order order = specifier.isAscending() ? Order.ASC : Order.DESC;

		queryOrderingColumn.setOrder(order);
		queryOrderingColumn.setColumnPrototype(column);
		queryOrderingColumn.setOperation(Operation.NONE);
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


	/**
	 * Retrieves a column prototype for the given form attribute key. It won't work if the
	 * key doesn't reference an actual column prototype or doesn't belong to the search form.
	 *
	 * @param formAttributeKey
	 * @return
	 */
	private QueryColumnPrototype lookupColumnPrototypeByFormKey(String formAttributeKey){
		return internalLookupColumnPrototypeByKey(mappings.getFormMapping(), formAttributeKey);
	}

	private QueryColumnPrototype lookupColumnPrototypeByResultSetKey(String rsAttributeKey){
		return internalLookupColumnPrototypeByKey(mappings.getResultMapping(), rsAttributeKey);
	}

	private QueryColumnPrototype lookupColumnPrototypeByCufType(AdvancedSearchFieldModelType cufType){
		if  (! CUSTOM_FIELD_TYPES.contains(cufType)){
			throw new IllegalArgumentException("unknown CUF type '"+cufType+"'");
		}
		else{
			return internalLookupColumnPrototypeByKey(mappings.getCufMapping(), cufType.toString());
		}
	}



	private QueryColumnPrototype internalLookupColumnPrototypeByKey(ColumnMapping mapping, String key){

		if (! mapping.isMappedKey(key)){
			throw new IllegalArgumentException("attribute key '"+key+"' is unmapped");
		}

		String mappedColumnLabel = mapping.findColumnPrototypeLabel(key);

		return prototypesByLabel.computeIfAbsent(mappedColumnLabel,
			(label) -> {
				throw new IllegalArgumentException("column '"+label+"' is unknown (unmapped)");
			});

	}



	private final String toIso(Date date){
		return DateUtils.formatIso8601Date(date);
	}

}
