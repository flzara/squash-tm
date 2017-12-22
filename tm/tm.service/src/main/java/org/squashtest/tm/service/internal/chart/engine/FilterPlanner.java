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
package org.squashtest.tm.service.internal.chart.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.squashtest.tm.domain.chart.ColumnPrototype;
import org.squashtest.tm.domain.chart.Filter;
import org.squashtest.tm.domain.chart.Operation;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;


/**
 * This class will apply filters on the query, see doc on ChartDataFinder for rules about
 * logical combination on filters.
 *
 * @author bsiri
 *
 */
class FilterPlanner {

	private DetailedChartQuery definition;

	private QuerydslToolbox utils;

	private ExtendedHibernateQuery<?> query;

	FilterPlanner(DetailedChartQuery definition, ExtendedHibernateQuery<?> query){
		super();
		this.definition = definition;
		this.query= query;
		this.utils = new QuerydslToolbox();
	}

	FilterPlanner(DetailedChartQuery definition, ExtendedHibernateQuery<?> query, QuerydslToolbox utils){
		super();
		this.definition = definition;
		this.query= query;
		this.utils = utils;
	}



	/**
	 * <p>A given column may be filtered multiple time. This is represented by the
	 * multiple {@link Filter} that target the same {@link ColumnPrototype}.</p>
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
		Map<ColumnPrototype, Collection<Filter>> whereFilters = findWhereFilters();
		BooleanBuilder wherebuilder = makeBuilder(whereFilters);

		query.where(wherebuilder);
	}

	private void addHavingClauses(){
		Map<ColumnPrototype, Collection<Filter>> havingFilters = findHavingFilters();

		BooleanBuilder havingbuilder = makeBuilder(havingFilters);

		query.having(havingbuilder);
	}

	private BooleanBuilder makeBuilder(Map<ColumnPrototype, Collection<Filter>> sortedFilters){
		BooleanBuilder mainBuilder = new BooleanBuilder();

		for (Entry<ColumnPrototype, Collection<Filter>> entry : sortedFilters.entrySet()) {

			BooleanBuilder orBuilder = new BooleanBuilder();

			for (Filter filter : entry.getValue()) {

				if (filter.getOperation() != Operation.NONE){
					BooleanExpression comparison = utils.createAsPredicate(filter);

					orBuilder.or(comparison);
				}
			}

			mainBuilder.and(orBuilder);
		}

		return mainBuilder;
	}

	private Map<ColumnPrototype, Collection<Filter>> findWhereFilters(){
		Collection<Filter> filters = new ArrayList<>(definition.getFilters());

		CollectionUtils.filter(filters, new Predicate() {
			@Override
			public boolean evaluate(Object filter) {
				return utils.isWhereClauseComponent((Filter)filter);
			}
		});

		return sortFilters(filters);
	}


	private Map<ColumnPrototype, Collection<Filter>> findHavingFilters(){
		Collection<Filter> filters = new ArrayList<>(definition.getFilters());

		CollectionUtils.filter(filters, new Predicate() {
			@Override
			public boolean evaluate(Object filter) {
				return utils.isHavingClauseComponent((Filter)filter);
			}
		});

		return sortFilters(filters);
	}



	// this will regroup filters by column prototype. Filters grouped that way will be
	// OR'ed together.
	private Map<ColumnPrototype, Collection<Filter>> sortFilters(Collection<Filter> filters){

		Map<ColumnPrototype, Collection<Filter>> res = new HashMap<>();

		for (Filter filter : filters){
			ColumnPrototype prototype = filter.getColumn();

			if (! res.containsKey(prototype)){
				res.put(prototype, new ArrayList<Filter>());
			}

			res.get(prototype).add(filter);

		}

		return res;
	}

}
