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
package org.squashtest.tm.service.internal.query

import spock.lang.Specification

import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.createInternalModel

import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkAggr
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkFilter
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkOrder
import static org.squashtest.tm.service.internal.query.QueryEngineTestUtils.mkProj

class ProjectionPlannerTest extends Specification {

	def utils = new QuerydslToolbox()


	// **************** ColumnAliasing tests ******************

	def "ColumnAliasing should assign aliases as 'col_x_0_', with x an int incremented in sequence"(){

		given :
		def model = createInternalModel(
			mkProj("attribute, numeric, none, test_case, id"),
			mkProj("attribute, level_enum, none, test_case, importance"),
			mkProj("attribute, text, none, test_case, label"),
			mkProj("attribute, numeric, none, test_case, version")
		)

		and :
		def aliasing = new ProjectionPlanner.ColumnAliasing(utils : utils)

		when :
		aliasing.planProjections(model)

		then:
		aliasing.projectedColumns
			.collect { [it.columnInstance.column.attributeName, it.alias]} == [
		    ["id", "col_0_0_"],
			["importance", "col_1_0_"],
			["label", "col_2_0_"],
			["version", "col_3_0_"]
		]

	}

	def "ColumnAliasing should plan projections by inspecting the Projection columns of a query model"(){

		given:
		def projId = mkProj("attribute, numeric, none, test_case, id")
		def projLabel = mkProj("attribute, text, none, test_case, label")

		and :

		def model = createInternalModel(
			projId,
			projLabel,
			mkAggr("attribute, numeric, none, test_case, id"),
			mkFilter("attribute, level_enum, none, test_case, importance"),
			mkOrder("attribute, text, none, test_case, label")
		)

		and :
		def aliasing = new ProjectionPlanner.ColumnAliasing(utils : utils)

		when:
		aliasing.planProjections(model)

		then:
		// cant test directly the content because the groovy method reference utils.&someMethod
		// would not return the same type than a java method reference

		// so we compare only parts of them instead

		aliasing.projectedColumns.collect { [it.columnInstance, it.alias ] } == [
			[projId, "col_0_0_"],
			[projLabel, "col_1_0_"]
		]


	}


	def "ColumnAliasing should also plan extra projection columns if it detects level_enum-based columns in the Order clause"(){


		given:
		// regular projection
		def projId = mkProj("attribute, numeric, none, test_case, id")
		// extra projection, because sorting on a level_enum
		def sortImportance = mkOrder("attribute, level_enum, none, test_case, importance")

		and :

		def model = createInternalModel(
			projId,
			mkAggr("attribute, numeric, none, test_case, id"),
			mkFilter("attribute, level_enum, none, test_case, importance"),
			sortImportance
		)

		and :
		def aliasing = new ProjectionPlanner.ColumnAliasing(utils : utils)

		when:
		aliasing.planProjections(model)

		then:
		// cant test directly the content because the groovy method reference utils.&someMethod
		// would not return the same type than a java method reference

		// so we compare only parts of them instead

		aliasing.projectedColumns.collect { [it.columnInstance, it.alias ] } == [
			[projId, "col_0_0_"],
			[sortImportance, "col_1_0_"]
		]

	}



	def "a column planed in a ColumnAliasing as a Projection should render as a Projection (normal and case-when)"(){
		given:
		// regular projection
		def projId = mkProj("attribute, numeric, none, test_case, id")

		// extra projection, because sorting on a level_enum
		def sortImportance = mkOrder("attribute, level_enum, none, test_case, importance")

		def renderedId =  utils.createAsSelect(projId).as("col_0_0_")
		def renderedImportance = utils.createAsCaseWhen(sortImportance).as('col_1_0_')

		and :
		def model = createInternalModel(
			projId,
			sortImportance
		)

		and :
		def aliasing = new ProjectionPlanner.ColumnAliasing(utils : utils)

		when:
		aliasing.planProjections(model)

		then:
		aliasing.projectedColumns[0].renderAsAliasedSelect() == renderedId
		aliasing.projectedColumns[1].renderAsAliasedSelect() == renderedImportance

	}



}

