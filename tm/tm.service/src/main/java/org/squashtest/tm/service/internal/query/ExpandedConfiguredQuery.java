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
package org.squashtest.tm.service.internal.query;

import org.springframework.data.domain.Pageable;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.query.NaturalJoinStyle;
import org.squashtest.tm.domain.query.QueryColumnPrototypeInstance;
import org.squashtest.tm.domain.query.ColumnType;
import org.squashtest.tm.domain.query.QueryAggregationColumn;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryFilterColumn;
import org.squashtest.tm.domain.query.QueryModel;
import org.squashtest.tm.domain.query.QueryOrderingColumn;
import org.squashtest.tm.domain.query.QueryProjectionColumn;
import org.squashtest.tm.domain.query.QueryStrategy;
import org.squashtest.tm.service.query.ConfiguredQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


/**
 * see javadoc on {@link QueryProcessingServiceImpl}
 *
 * @author bsiri
 *
 */
class ExpandedConfiguredQuery {

	private ConfiguredQuery parent;

	private List<InternalEntityType> targetEntities;

	private InternalEntityType rootEntity;


	// *************** constructors ************************************

	// for testing purposes - do not use
	ExpandedConfiguredQuery(){
		super();
	}


	/**
	 * Constructor that will build a ExpandedConfiguredQuery for the subquery of the given column
	 *
	 * @param column
	 */
	static ExpandedConfiguredQuery createFor(QueryColumnPrototypeInstance column){
		ConfiguredQuery parentQuery = new ConfiguredQuery();
		parentQuery.setQueryModel(column.getColumn().getSubQuery());

		return new ExpandedConfiguredQuery(parentQuery);
	}

	static ExpandedConfiguredQuery createFor(QueryModel queryModel){
		ConfiguredQuery parentQuery = new ConfiguredQuery();
		parentQuery.setQueryModel(queryModel);

		return new ExpandedConfiguredQuery(parentQuery);
	}

	ExpandedConfiguredQuery(ConfiguredQuery parent){

		this.parent = parent;

		// find all the target entities
		computeTargetEntities();

		// init the root entity
		initRootEntity();

	}

	// ********* all getters **********************

	List<QueryProjectionColumn> getProjectionColumns(){
		return parent.getQueryModel().getProjectionColumns();
	}

	List<QueryAggregationColumn> getAggregationColumns(){
		return parent.getQueryModel().getAggregationColumns();
	}

	List<QueryFilterColumn> getFilterColumns(){
		return parent.getQueryModel().getFilterColumns();
	}

	List<QueryOrderingColumn> getOrderingColumns(){
		return parent.getQueryModel().getOrderingColumns();
	}

	NaturalJoinStyle getJoinStyle(){
		return parent.getQueryModel().getJoinStyle();
	}

	QueryStrategy getStrategy(){
		return parent.getQueryModel().getStrategy();
	}

	Collection<EntityReference> getScope(){
		return parent.getScope();
	}

	InternalEntityType getRootEntity() {
		return rootEntity;
	}


	List<InternalEntityType> getTargetEntities() {
		return targetEntities;
	}

	Pageable getPaging(){
		return parent.getPaging();
	}

	Collection<? extends QueryColumnPrototypeInstance> getInlinedColumns(){
		return findSubqueriesForStrategy(QueryStrategy.INLINED);

	}

	Collection<? extends QueryColumnPrototypeInstance> getSubqueryColumns(){
		return findSubqueriesForStrategy(QueryStrategy.SUBQUERY);

	}


	// ********************** init code ********************************


	protected final void computeTargetEntities(){

		List<QueryColumnPrototypeInstance> allColumns = collectAllColumns();

		targetEntities = allColumns.stream()
							 .map(col -> col.getColumn().getSpecializedType())
							 .map(InternalEntityType::fromSpecializedType)
							 .distinct()
							 .collect(Collectors.toList());

	}

	private void initRootEntity(){
		rootEntity = targetEntities.get(0);
	}




	private Collection<QueryColumnPrototypeInstance> findSubqueriesForStrategy(QueryStrategy strategy){
		Collection<QueryColumnPrototypeInstance> found = collectAllColumns().stream()
													.filter( col -> {
														QueryColumnPrototype proto = col.getColumn();
														return proto.getColumnType() == ColumnType.CALCULATED &&
																   proto.getSubQuery().getStrategy() == strategy;
													}).collect(Collectors.toSet());

		return found;
	}


	List<QueryColumnPrototypeInstance> collectAllColumns(){
		QueryModel query = parent.getQueryModel();

		List<QueryColumnPrototypeInstance> allColumns = new ArrayList<>();

		allColumns.addAll(query.getProjectionColumns());
		allColumns.addAll(query.getAggregationColumns());
		allColumns.addAll(query.getOrderingColumns());
		allColumns.addAll(query.getFilterColumns());

		return allColumns;
	}
}
