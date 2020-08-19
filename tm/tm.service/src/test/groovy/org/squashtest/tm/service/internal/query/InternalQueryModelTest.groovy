package org.squashtest.tm.service.internal.query

import org.squashtest.tm.domain.EntityType

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

import org.squashtest.tm.domain.query.ColumnType
import org.squashtest.tm.domain.query.QueryAggregationColumn
import org.squashtest.tm.domain.query.QueryColumnPrototype
import org.squashtest.tm.domain.query.QueryFilterColumn
import org.squashtest.tm.domain.query.QueryModel
import org.squashtest.tm.domain.query.QueryOrderingColumn
import org.squashtest.tm.domain.query.QueryProjectionColumn
import org.squashtest.tm.domain.query.QueryStrategy
import org.squashtest.tm.domain.query.SpecializedEntityType
import org.squashtest.tm.service.query.ConfiguredQuery
import spock.lang.Specification
import spock.lang.Unroll

import static org.squashtest.tm.domain.query.ColumnType.ATTRIBUTE
import static org.squashtest.tm.domain.query.ColumnType.CALCULATED
import static org.squashtest.tm.domain.query.ColumnType.CUF
import static org.squashtest.tm.domain.query.QueryStrategy.INLINED
import static org.squashtest.tm.domain.query.QueryStrategy.MAIN
import static org.squashtest.tm.domain.query.QueryStrategy.SUBQUERY
import static org.squashtest.tm.service.internal.query.InternalEntityType.CAMPAIGN
import static org.squashtest.tm.service.internal.query.InternalEntityType.EXECUTION
import static org.squashtest.tm.service.internal.query.InternalEntityType.REQUIREMENT
import static org.squashtest.tm.service.internal.query.InternalEntityType.TEST_CASE
import static org.squashtest.tm.service.internal.query.InternalEntityType.TEST_CASE_MILESTONE
import static org.squashtest.tm.service.internal.query.InternalEntityType.TEST_CASE_STEP

class InternalQueryModelTest extends Specification{

	def "should detect the subqueries for what they are"(){

		given :
		QueryModel parent = new QueryModel(
			projectionColumns : [
					mockColumn(ATTRIBUTE, MAIN, "projection", "meas attribute"),
					mockColumn(CALCULATED, SUBQUERY, "projection", "measure calculated subquery"),
					mockColumn(CUF, INLINED, "projection", "meas cuf"),
					mockColumn(CALCULATED, INLINED, "projection", "measure calculated inlined")
				],
			aggregationColumns : [
					mockColumn(ATTRIBUTE, MAIN, "aggregation", "axis attribute"),
					mockColumn(CALCULATED, SUBQUERY, "aggregation", "axis calculated subquery"),
					mockColumn(CUF, INLINED, "aggregation", "axis cuf"),
					mockColumn(CALCULATED, INLINED, "aggregation", "axis calculated inlined")
				],
			filterColumns: [
					mockColumn(ATTRIBUTE, MAIN, "filter", "filter attribute"),
					mockColumn(CALCULATED, SUBQUERY, "filter", "filter calculated subquery"),
					mockColumn(CUF, INLINED, "filter", "filter cuf"),
					mockColumn(CALCULATED, INLINED, "filter", "filter calculated inlined")
				]
				)

		and :
		InternalQueryModel detailed = new InternalQueryModel(new ConfiguredQuery(parent))

		when :
		def inlined = detailed.getInlinedColumns();
		def subcolumns = detailed.getSubqueryColumns();

		then :
		inlined.collect{it.column.label} as Set == ["measure calculated inlined", "filter calculated inlined", "axis calculated inlined"] as Set
		subcolumns.collect{it.column.label} as Set == ["measure calculated subquery", "filter calculated subquery", "axis calculated subquery"] as Set
	}


	def "should collect all the query columns in the correct order (aggregation, then projections, then ordering, then filtering)"(){

		given :
			def proj1 = proj(EntityType.ITERATION)

			def order1 = order(EntityType.CAMPAIGN)

			def agg1 = agg(EntityType.REQUIREMENT)
			def agg2 = agg(EntityType.TEST_CASE_STEP)

			def filter1 = filter(EntityType.EXECUTION)

		and:
		def internalQuery = new InternalQueryModel(new ConfiguredQuery(mockQueryModel(proj1, order1, agg1, agg2, filter1)))

		when :
		def allColumns = internalQuery.collectAllColumns()

		then :
		allColumns == [ agg1, agg2, proj1, order1, filter1]

	}



	@Unroll("should automatically select the root entity as #rootEntity because #humanMsg")
	def "should automatically select the root entity"(){

		expect:
		new InternalQueryModel(new ConfiguredQuery(queryModel)).rootEntity == rootEntity

		where :
		rootEntity	| humanMsg																		|	queryModel
		TEST_CASE	| "it is the type of the first aggregate columns"								|	mockQueryModel(agg(EntityType.TEST_CASE), agg(EntityType.TEST_CASE_STEP), proj(EntityType.REQUIREMENT))
		REQUIREMENT	| "there was no aggregation so we took the type of the first projection column" | 	mockQueryModel(proj(EntityType.REQUIREMENT), proj(EntityType.TEST_CASE_STEP))

	}



	def "if root entity set explicitly, should override the autoselection mechanism"(){

		given:
		def queryModel = mockQueryModel(agg(EntityType.TEST_CASE), proj(EntityType.REQUIREMENT))
		def overrideRootEntity = InternalEntityType.EXECUTION

		def internalModel = new InternalQueryModel(new ConfiguredQuery(queryModel))

		when:
		internalModel.withRootEntity(overrideRootEntity)

		then:
		internalModel.rootEntity == overrideRootEntity

	}


	@Unroll()
	def "the target entities should always list the root entity first (autoselection mode)"(){

		expect:
			def queryModel = mockQueryModel(shuffled)	// see note in the where: clause
			def internalModel = new InternalQueryModel(new ConfiguredQuery(queryModel))


			internalModel.rootEntity == internalModel.targetEntities[0]

		where:
		// don't know why, but shuffled is as an array of list (instead of just a list)
		shuffled << (1..10).collect {
			def columns = [
				agg(EntityType.TEST_CASE),
				proj(EntityType.REQUIREMENT),
				order(EntityType.TEST_CASE_STEP),
				filter(EntityType.EXECUTION),
				proj(EntityType.CAMPAIGN)
			]
			Collections.shuffle(columns)
			return columns as Object[]
		}

	}


	def "the target entities should always include and list the root entity first (user-specified mode)"(){

		given:
		def columns = [
			agg(EntityType.TEST_CASE),
			proj(EntityType.REQUIREMENT),
			order(EntityType.TEST_CASE_STEP),
			filter(EntityType.EXECUTION),
			proj(EntityType.CAMPAIGN)
		] as Object[]
		def queryModel = mockQueryModel(columns)
		def internalModel = new InternalQueryModel(new ConfiguredQuery(queryModel)).withRootEntity(TEST_CASE_MILESTONE)

		when :
		def targetEntities = internalModel.getTargetEntities()

		then :
		targetEntities[0] == TEST_CASE_MILESTONE
		// the rest can be unordered
		targetEntities as Set == [TEST_CASE_MILESTONE, TEST_CASE, REQUIREMENT, TEST_CASE_STEP, EXECUTION, CAMPAIGN] as Set
	}


	def "when an InternalQueryModel has a profile REGULAR_QUERY, all columns from the querymodel should be listed"(){

		given:
			def agg1 = agg(EntityType.TEST_CASE_STEP)
			def proj1 = proj(EntityType.REQUIREMENT)
			def filter1 = filter(EntityType.CAMPAIGN)
			def order1 = order(EntityType.ITERATION)

			def model = mockQueryModel(agg1, proj1, filter1, order1)

		when:
			def internalModel = new InternalQueryModel(new ConfiguredQuery(model))
			internalModel.withProfile(QueryProfile.REGULAR_QUERY)


		then :
			internalModel.getProjectionColumns() == [ proj1 ]
			internalModel.getAggregationColumns() == [ agg1 ]
			internalModel.getFilterColumns() == [ filter1 ]
			internalModel.getOrderingColumns() == [ order1 ]

	}

	def "when an InternalQueryModel has a profile SUBSELECT_QUERY, should only expose the projection and filter"(){

		given:
		def agg1 = agg(EntityType.TEST_CASE_STEP)
		def proj1 = proj(EntityType.REQUIREMENT)
		def filter1 = filter(EntityType.CAMPAIGN)
		def order1 = order(EntityType.ITERATION)

		def model = mockQueryModel(agg1, proj1, filter1, order1)

		when:
		def internalModel = new InternalQueryModel(new ConfiguredQuery(model))
		internalModel.withProfile(QueryProfile.SUBSELECT_QUERY)


		then :
		internalModel.getProjectionColumns() == [ proj1 ]
		internalModel.getAggregationColumns() == [  ]
		internalModel.getFilterColumns() == [ filter1 ]
		internalModel.getOrderingColumns() == [ ]

	}


	def "when an InternalQueryModel has a profile SUBWHERE_QUERY, should only expose the projection, aggregation and filter"(){

		given:
		def agg1 = agg(EntityType.TEST_CASE_STEP)
		def proj1 = proj(EntityType.REQUIREMENT)
		def filter1 = filter(EntityType.CAMPAIGN)
		def order1 = order(EntityType.ITERATION)

		def model = mockQueryModel(agg1, proj1, filter1, order1)

		when:
		def internalModel = new InternalQueryModel(new ConfiguredQuery(model))
		internalModel.withProfile(QueryProfile.SUBWHERE_QUERY)


		then :
		internalModel.getProjectionColumns() == [ proj1 ]
		internalModel.getAggregationColumns() == [ agg1 ]
		internalModel.getFilterColumns() == [ filter1 ]
		internalModel.getOrderingColumns() == [ ]

	}

	// ************ more test code ************************

	def mockQueryModel(...columns){
		Mock(QueryModel){
			getProjectionColumns() >> columns.findAll { it instanceof QueryProjectionColumn }
			getAggregationColumns() >> columns.findAll { it instanceof QueryAggregationColumn }
			getFilterColumns() >> columns.findAll { it instanceof QueryFilterColumn }
			getOrderingColumns() >> columns.findAll { it instanceof QueryOrderingColumn }
		}
	}

	def mockColumn(ColumnType type, QueryStrategy strategy, String columnrole, String label){
		def col;
		QueryColumnPrototype proto = Mock(QueryColumnPrototype);
		QueryModel query = Mock(QueryModel);

		switch (columnrole){
			case "projection" :
					col = Mock(QueryProjectionColumn); break;
			case "aggregation" :
					col = Mock(QueryAggregationColumn); break;
			case "filter" :
					col = Mock(QueryFilterColumn); break;
		}

		col.getSpecializedType() >> new SpecializedEntityType(entityType : EntityType.TEST_CASE)
		col.getColumn() >> proto
		proto.getColumnType() >> type
		proto.getLabel() >> label
		proto.getSubQuery() >> query
		query.getStrategy() >> strategy

		return col
	}

	def proj(entityType, entityRole = null){
		Mock(QueryProjectionColumn){
			getSpecializedType() >> new SpecializedEntityType(entityType, entityRole)
		}
	}

	def agg(entityType, entityRole = null){
		Mock(QueryAggregationColumn){
			getSpecializedType() >> new SpecializedEntityType(entityType, entityRole)
		}
	}
	def filter(entityType, entityRole = null){
		Mock(QueryFilterColumn){
			getSpecializedType() >> new SpecializedEntityType(entityType, entityRole)
		}
	}

	def order(entityType, entityRole = null){
		Mock(QueryOrderingColumn){
			getSpecializedType() >> new SpecializedEntityType(entityType, entityRole)
		}
	}




}
