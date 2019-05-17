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

import com.google.common.collect.ImmutableMap;
import com.querydsl.core.types.Order;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
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
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.AdvancedSearchSingleFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTagsFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchTextFieldModel;
import org.squashtest.tm.service.internal.repository.ColumnPrototypeDao;
import org.squashtest.tm.service.query.ConfiguredQuery;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AdvancedSearchQueryModelToConfiguredQueryConverter {

	private Map<String, String> columnPrototypeMapping = new HashMap<>();

	@Inject
	private ColumnPrototypeDao columnPrototypeDao;

	private AdvancedSearchQueryModel advancedSearchQueryModel;

	private List<QueryColumnPrototype> prototypes = new ArrayList<>();

	public AdvancedSearchQueryModelToConfiguredQueryConverter(AdvancedSearchQueryModel advancedSearchQueryModel, Map<String, String> columnPrototypeMapping) {
		this.advancedSearchQueryModel = advancedSearchQueryModel;
		this.columnPrototypeMapping = columnPrototypeMapping;
	}

	public ConfiguredQuery convert() {

		prototypes = columnPrototypeDao.findAll();

		QueryModel query = createBaseQueryModel();



		ConfiguredQuery configuredQuery = new ConfiguredQuery();
		configuredQuery.setQueryModel(query);
		configuredQuery.setPaging(advancedSearchQueryModel.getPageable());
		return configuredQuery;
	}

	private QueryModel createBaseQueryModel() {
		QueryModel query = new QueryModel();
		query.setStrategy(QueryStrategy.MAIN);
		query.setJoinStyle(NaturalJoinStyle.INNER_JOIN);

		List<QueryProjectionColumn> projections = extractProjections();
		query.setProjectionColumns(projections);

		List<QueryFilterColumn> filters = extractFilters();
		query.setFilterColumns(filters);

		List<QueryOrderingColumn> orders = extractOrders();
		query.setOrderingColumns(orders);

		return query;
	}

	private List<QueryProjectionColumn> extractProjections() {
		List<QueryProjectionColumn> projections = new ArrayList<>();
		return projections;
	}

	private List<QueryFilterColumn> extractFilters() {
		List<QueryFilterColumn> filters = new ArrayList<>();

		AdvancedSearchModel model = advancedSearchQueryModel.getModel();
		Set<String> keys = model.getFields().keySet();
		for (String key: keys) {
		    AdvancedSearchFieldModel fieldModel = model.getFields().get(key);
		    AdvancedSearchFieldModelType fieldType = fieldModel.getType();
		    filters.add(getFilterColumn(fieldModel, fieldType, key));
		}
		return filters;
	}

	private QueryFilterColumn getFilterColumn(AdvancedSearchFieldModel fieldModel,
											  AdvancedSearchFieldModelType fieldType, String key) {
		QueryFilterColumn queryFilterColumn = null;

		switch (fieldType) {
			case TIME_INTERVAL:
				break;
			case CF_TIME_INTERVAL:
				break;
			case CF_LIST:
				break;
			case CF_TEXT:
				break;
			case CF_SINGLE:
				break;
			case CF_CHECKBOX:
				break;
			case CF_NUMERIC_RANGE:
				break;
			case NUMERIC_RANGE:
				break;
			case MULTILIST:
				break;
			case RANGE:
				break;
			case TEXT:
				break;
			case TAGS:
				queryFilterColumn = createFilterToTags(fieldModel);
				break;
			case LIST:
				break;
			case SINGLE:
				queryFilterColumn = createFilterToSingle(fieldModel);
				break;
			default:
				break;
		}
		return queryFilterColumn;
	}

	private QueryFilterColumn createFilterToSingle(AdvancedSearchFieldModel model) {

		QueryFilterColumn filter = new QueryFilterColumn();
		AdvancedSearchSingleFieldModel singleFieldModel = (AdvancedSearchSingleFieldModel) model;
		String value = singleFieldModel.getValue();
		filter.addValues(Collections.singletonList(value));
		filter.setOperation(Operation.EQUALS);
		String columnLabel = columnPrototypeMapping.get(singleFieldModel.toString());
		filter.setColumnPrototype(getColumnPrototype(columnLabel));
		return filter;
	}

	private QueryFilterColumn createFilterToTags(AdvancedSearchFieldModel model) {
		QueryFilterColumn filter = new QueryFilterColumn();
		AdvancedSearchTagsFieldModel fieldModel = (AdvancedSearchTagsFieldModel) model;
		List<String> tags = fieldModel.getTags();
		filter.addValues(tags);
		QueryColumnPrototype queryColumnPrototype = getColumnPrototype(columnPrototypeMapping.get("CUSTOM_FIELD_TAGS"));
		filter.setColumnPrototype(queryColumnPrototype);
		filter.setCufId(Long.parseLong(fieldModel.toString()));
		if(AdvancedSearchTagsFieldModel.Operation.AND.equals(fieldModel.getOperation())) {
			filter.setOperation(Operation.IN);
		} else {
			//TODO create the good operation
		}
		return filter;
	}

	private List<QueryOrderingColumn> extractOrders() {

		Pageable pageable = advancedSearchQueryModel.getPageable();

		Sort sort = pageable.getSort();
		List<QueryOrderingColumn> orders = sort.stream().map(this::convertToOrder).collect(Collectors.toList());

		return orders;
	}

	private QueryOrderingColumn convertToOrder(Sort.Order specifier) {
		QueryOrderingColumn queryOrderingColumn = new QueryOrderingColumn();

		String property = specifier.getProperty();
		String columnPrototypeLabel = columnPrototypeMapping.get(property);
		QueryColumnPrototype column = getColumnPrototype(columnPrototypeLabel);

		Order order = specifier.isAscending() ? Order.ASC : Order.DESC;

		queryOrderingColumn.setOrder(order);
		queryOrderingColumn.setColumnPrototype(column);
		return queryOrderingColumn;
	}

	private QueryColumnPrototype getColumnPrototype(String property) {
		QueryColumnPrototype column = prototypes.stream()
			.filter(col -> col.getLabel().equals(property))
			.findFirst().get();
		return column;
	}
}
