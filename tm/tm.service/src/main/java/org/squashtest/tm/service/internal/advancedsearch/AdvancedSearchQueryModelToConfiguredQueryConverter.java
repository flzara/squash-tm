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

import static java.util.stream.Collectors.toList;

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

import javax.inject.Inject;

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
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.DataType;
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

import com.google.common.collect.Sets;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.hibernate.HibernateQuery;

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

	/**
	 * Returns the query that will list the entities of interest. 
	 * The result is returned as a Tuple, the first element of which are the entities (the rest of 
	 * the tuple exists for technical purposes, see documentation of {@link #createMappedProjections()}).
	 * 
	 * 
	 * Note that the query needs to be given a session.
	 * 
	 * @return
	 */
	public HibernateQuery<Tuple> prepareFetchQuery() {

		ExtendedHibernateQuery<Tuple> hibQuery = basePrepareQuery();
		
		return hibQuery;

	}
	
	/**
	 * Prepares the query as a count query. It removes the pagination
	 * and should be invoked using 'selectCount()'. The query requires to be 
	 * given a session.
	 * 
	 * @return
	 */

	
	public HibernateQuery<Tuple> prepareCountQuery() {

		ExtendedHibernateQuery<Tuple> countQuery = basePrepareQuery();
		
		// neutralize the paging 
		countQuery.limit(Long.MAX_VALUE);
		countQuery.offset(0);

		return countQuery;
		
	}
	
	
	private ExtendedHibernateQuery<Tuple> basePrepareQuery() {

		// Find all ColumnPrototypes
		prototypesByLabel = columnPrototypeDao.findAll().stream().collect(Collectors.toMap(
			col -> col.getLabel(),
			col -> col
		));

		
		// first pass : create the ConfiguredQuery
		ConfiguredQuery configuredQuery = createConfiguredQuery();

		// generate the ExtendedHibernateQuery
		ExtendedHibernateQuery<Tuple> hibQuery = queryService.prepareQuery(configuredQuery);

		// second pass : now process the special handlers
		applyAllSpecialHandlers(hibQuery);
		
		return hibQuery;

	}
	
	private ConfiguredQuery createConfiguredQuery(){
		
		// Create queryModel
		QueryModel query = createBaseQueryModel();

		// Configure the Query
		ConfiguredQuery configuredQuery = new ConfiguredQuery();
		configuredQuery.setQueryModel(query);
		configuredQuery.setPaging(advancedSearchQueryModel.getPageable());
		
		return configuredQuery;
	}
	

	// *************** Bulk query creation methods ************************


	private QueryModel createBaseQueryModel() {
		QueryModel query = new QueryModel();
		query.setStrategy(QueryStrategy.MAIN);
		query.setJoinStyle(NaturalJoinStyle.INNER_JOIN);

		// In the search field, projectids are not always set so we must secure this point on projects that the user can read.
		secureProjectFilter();
		
		// create the projections
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


	private void applyAllSpecialHandlers(ExtendedHibernateQuery<Tuple> query){

		applySpecialProjectionHandlers(query);
		applySpecialFilterHandlers(query);
		applySpecialOrderHandlers(query);

	}

	// ********************** Projection creations ***********************************
	

	/**
	 * <p>
	 * Create the query column projections for the mapped attribute, ie where an attribute of the desired result set
	 * can actually be mapped to a QueryColumnPrototype.
	 *	</p>
	 *
	 * <p>
	 * 	The tuple created this way is organized as follow :
	 * 	<ol>
	 * 		<li>The first item of the tuple is the root entity itself, as referenced by its label {@link AdvancedSearchColumnMappings#getRootEntityColumnLabel()}. This is the expected returned result</li>
	 * 		<li>
	 * 			The next items are any all the columns expected by the sort by clause (required by PostGre).
	 * 			In case the 'sort by' clause is empty and no sorted-on columns was appended, a dummy column will be selected instead to ensure that
	 * 			the final result set will be a Tuple anyway.
	 * 		 </li>
	 * 	</ol>
	 * </p>
	 */

	private List<QueryProjectionColumn> createMappedProjections() {
		
		List<QueryProjectionColumn> projections = new ArrayList<>();
		
		// first round : enqueue the column for the root entity
		String rootColLabel = mappings.getRootEntityColumnLabel();		
		QueryColumnPrototype rootProto = lookupColumnPrototypeByColumnLabel(rootColLabel);

		QueryProjectionColumn entityProjection = toProjectionColumn(rootProto);		
		projections.add(entityProjection);

		// second round : add the sorted-on columns
		List<QueryColumnPrototype> sortByPrototypes = locatePrototypesOfMappedOrderColumns();

		// if empty, add a surrogate column
		if (sortByPrototypes.isEmpty()){
			QueryProjectionColumn dummy = createDummyProjection(entityProjection);
			projections.add(dummy);
		}
		// else append the columns as desired
		else{
			List<QueryProjectionColumn> sortedProjections
				= sortByPrototypes.stream()
									.map(this::toProjectionColumn)
									.collect(toList());

			projections.addAll(sortedProjections);
		}

		
		return Collections.unmodifiableList(projections);

	}


	private List<QueryColumnPrototype> locatePrototypesOfMappedOrderColumns(){

		ColumnMapping resultMapping = mappings.getResultMapping();
		Optional<Sort> maybeSort = extractSort();

		if (maybeSort.isPresent()){
			Sort sort = maybeSort.get();
			return sort.stream()
					   // retain only the result set keys that maps to a column prototype
					   .filter(order -> resultMapping.isMappedKey(order.getProperty()))
					   // resolve the column prototype
					   .map(order -> lookupColumnPrototypeByResultSetKey(order.getProperty()))
					   .collect(toList());
		}
		else{
			return Collections.emptyList();
		}



	}

	
	private QueryProjectionColumn toProjectionColumn(QueryColumnPrototype proto){
		QueryProjectionColumn column = new QueryProjectionColumn();
		column.setColumnPrototype(proto);
		column.setOperation(Operation.NONE);
		
		return column;
	}
	
	private QueryProjectionColumn createDummyProjection(QueryProjectionColumn rootProjection){
		
		QueryColumnPrototype rootProto = rootProjection.getColumn();
		String rootProtoLabel = rootProto.getLabel();
		
		// XXX that is a ugly hack
		String dummyLabel = rootProtoLabel.replace("_ENTITY", "_ID");
		QueryColumnPrototype dummyProto = lookupColumnPrototypeByColumnLabel(dummyLabel);
		
		return toProjectionColumn(dummyProto);
		
	}
	
	private void applySpecialProjectionHandlers(ExtendedHibernateQuery<Tuple> query){
		// for now, it's a no-op
	}



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

		AdvancedSearchModel model = advancedSearchQueryModel.getSearchFormModel();
		Set<String> processableKeys = model.getFieldKeys();


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

	private void applySpecialFilterHandlers(ExtendedHibernateQuery<?> query){

		AdvancedSearchModel model = advancedSearchQueryModel.getSearchFormModel();

		ColumnMapping formMapping = mappings.getFormMapping();

		Set<String> processableKeys =  model.getFieldKeys();

		// process only the specially handled filters this time
		processableKeys.retainAll(formMapping.getSpecialKeys());

		for (String key: processableKeys){
			SpecialHandler handler = formMapping.findHandler(key);
			AdvancedSearchFieldModel searchField = model.getFields().get(key);
			
			handler.applyFilter.accept(query,  searchField);
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

	// TODO : see if one day we support MATCHES
	private void filterByPlainText(QueryFilterColumn filterColumn, AdvancedSearchFieldModel model) {

		AdvancedSearchSingleFieldModel singleFieldModel = (AdvancedSearchSingleFieldModel) model;

		String value = singleFieldModel.getValue();

		if(filterColumn.getDataType().equals(DataType.NUMERIC)) {
			filterColumn.setOperation(Operation.EQUALS);
		} else {
			filterColumn.setOperation(Operation.LIKE);
		}

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

	// TODO create the filter for fulltext_search search
	private void filterByFullText(QueryFilterColumn filterColumn, AdvancedSearchFieldModel fieldModel) {

		AdvancedSearchTextFieldModel textFieldModel = (AdvancedSearchTextFieldModel)fieldModel;

		filterColumn.setOperation(Operation.FULLTEXT);

		String value = textFieldModel.getValue();

		value = value.replace(" ", " | ");

		filterColumn.getValues().add(value);
	}


	private void filterByCheckbox(QueryFilterColumn filterColumn, AdvancedSearchFieldModel fieldModel) {

		SearchCustomFieldCheckBoxFieldModel checkModel = (SearchCustomFieldCheckBoxFieldModel) fieldModel;

		filterColumn.addValues(checkModel.getValues());
		filterColumn.setOperation(Operation.IN);
	}
	
	

	// ****************************** Order creation **************************


	private List<QueryOrderingColumn> createMappedOrders() {

		ColumnMapping resultMapping = mappings.getResultMapping();
		List<QueryOrderingColumn> orders = new ArrayList<>();

		Optional<Sort> maybeSort = extractSort();

		if (maybeSort.isPresent()) {
			Sort sort = maybeSort.get();
			orders = sort.stream()
						 .filter( order -> resultMapping.isMappedKey(order.getProperty()))
						 .map(this::convertToOrder)
						 .collect(toList());
		}
		return orders;
	}


	private void applySpecialOrderHandlers(ExtendedHibernateQuery<?> query){

		// TODO : add the order by clauses in the correct order, which might be difficult,
		// see #applySpeciallyHandledProjections
		ColumnMapping resultMapping = mappings.getResultMapping();

		Optional<Sort> maybeSort = extractSort();

		if (maybeSort.isPresent()) {
			Sort sort = maybeSort.get();
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


	// **************** utility methods **************************************


	private void secureProjectFilter() {
		AdvancedSearchModel formModel = advancedSearchQueryModel.getSearchFormModel();
		
		// Issue #5079 again
		// first task is to locate which name has the project criteria because it may differ depending on the interface
		// (test case, requirement, test-case-through-requirements
		String key = null;
		Set<String> keys = formModel.getFieldKeys();
		for (String k : keys) {
			if (k.contains(PROJECT_FILTER_NAME)) { // assess whether the comparison should be 'equals'
				key = k;
				break;
			}
		}
		// if no projectFilter was set -> nothing to do
		if (key == null) {
			return;
		}

		AdvancedSearchListFieldModel projectFilter = (AdvancedSearchListFieldModel) formModel.getFields().get(key);

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

		return lookupColumnPrototypeByColumnLabel(mappedColumnLabel);
		
	}
	

	private QueryColumnPrototype lookupColumnPrototypeByColumnLabel(String mappedColumnLabel){
		return prototypesByLabel.computeIfAbsent(mappedColumnLabel,
				(label) -> {
					throw new IllegalArgumentException("column '"+label+"' is unknown (unmapped)");
				});

	}


	private Optional<Sort> extractSort(){
		Sort sort = null;
		Pageable pageable = advancedSearchQueryModel.getPageable();
		
		if (pageable != null){
			sort = pageable.getSort();
		}
		
		return Optional.ofNullable(sort);
	}
	


	private final String toIso(Date date){
		return DateUtils.formatIso8601Date(date);
	}

}
