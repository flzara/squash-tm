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

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.query.Operation;
import org.squashtest.tm.domain.query.QueryColumnPrototype;
import org.squashtest.tm.domain.query.QueryColumnPrototypeReference;
import org.squashtest.tm.domain.query.QueryFilterColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.CAMPAIGN_ID;


/**
 * This class will apply filters on the query, see doc on QueryProcessingServiceImpl for rules about
 * logical combination on filters.
 *
 * @author bsiri
 *
 */
class FilterPlanner {

	private InternalQueryModel definition;

	private QuerydslToolbox utils;

	private ExtendedHibernateQuery<?> query;

	FilterPlanner(InternalQueryModel definition, ExtendedHibernateQuery<?> query){
		super();
		this.definition = definition;
		this.query= query;
		this.utils = new QuerydslToolbox();
	}

	FilterPlanner(InternalQueryModel definition, ExtendedHibernateQuery<?> query, QuerydslToolbox utils){
		super();
		this.definition = definition;
		this.query= query;
		this.utils = utils;
	}



	/**
	 * <p>A given column may be filtered multiple time. This is represented by the
	 * multiple {@link Filter} that target the same {@link QueryColumnPrototype}.</p>
	 *
	 * <p>All filters for a given prototype are ORed together,
	 * then the ORed expressions are ANDed together.</p>
	 *
	 */
	void modifyQuery(){

		addWhereClauses();
		addHavingClauses();
	}


	private void addWhereClauses(){
		Map<QueryColumnPrototype, Collection<QueryFilterColumn>> whereFilters = findWhereFilters();
		BooleanBuilder wherebuilder = makeBuilder(whereFilters);

		query.where(wherebuilder);
	}

	private void addHavingClauses(){
		Map<QueryColumnPrototype, Collection<QueryFilterColumn>> havingFilters = findHavingFilters();

		BooleanBuilder havingbuilder = makeBuilder(havingFilters);

		query.having(havingbuilder);
	}

	private BooleanBuilder makeBuilder(Map<QueryColumnPrototype, Collection<QueryFilterColumn>> sortedFilters){
		BooleanBuilder mainBuilder = new BooleanBuilder();

		for (Entry<QueryColumnPrototype, Collection<QueryFilterColumn>> entry : sortedFilters.entrySet()) {

			BooleanBuilder orBuilder = new BooleanBuilder();

			for (QueryFilterColumn filter : entry.getValue()) {

				if (filter.getOperation() != Operation.NONE){
					BooleanExpression comparison = utils.createAsPredicate(filter);

					orBuilder.or(comparison);
				}
			}

			mainBuilder.and(orBuilder);
		}

		return mainBuilder;
	}

	private Map<QueryColumnPrototype, Collection<QueryFilterColumn>> findWhereFilters(){
		//SQUASH-2181 - NPE on CAMPAIGN_ID
		Collection<QueryFilterColumn> filters =
			definition.getFilterColumns().stream()
				.filter(x-> !CAMPAIGN_ID.equals(x.getColumn().getLabel())
					|| (CAMPAIGN_ID.equals(x.getColumn().getLabel()) && x.getValues() != null && !x.getValues().isEmpty() && x.getValues().get(0) != null))
				.collect(Collectors.toList());

		CollectionUtils.filter(filters, new Predicate() {
			@Override
			public boolean evaluate(Object filter) {
				return utils.isWhereClauseComponent((QueryFilterColumn)filter);
			}
		});

		return sortFilters(filters);
	}


	private Map<QueryColumnPrototype, Collection<QueryFilterColumn>> findHavingFilters(){
		Collection<QueryFilterColumn> filters = new ArrayList<>(definition.getFilterColumns());

		CollectionUtils.filter(filters, new Predicate() {
			@Override
			public boolean evaluate(Object filter) {
				return utils.isHavingClauseComponent((QueryFilterColumn)filter);
			}
		});

		return sortFilters(filters);
	}



	// this will regroup filters by column prototype. Filters grouped that way will be
	// OR'ed together.
	private Map<QueryColumnPrototype, Collection<QueryFilterColumn>> sortFilters(Collection<QueryFilterColumn> filters){

		Map<QueryColumnPrototype, Collection<QueryFilterColumn>> res = new HashMap<>();

		for (QueryFilterColumn filter : filters){
			QueryColumnPrototype prototype = filter.getColumn();

			if (! res.containsKey(prototype)){
				res.put(prototype, new ArrayList<QueryFilterColumn>());
			}

			res.get(prototype).add(filter);

		}

		return res;
	}

}
