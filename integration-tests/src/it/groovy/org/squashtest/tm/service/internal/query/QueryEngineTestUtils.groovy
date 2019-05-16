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

import com.querydsl.core.types.Order
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.bugtracker.QIssue
import org.squashtest.tm.domain.campaign.QCampaign
import org.squashtest.tm.domain.campaign.QIteration
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem
import org.squashtest.tm.domain.query.*
import org.squashtest.tm.domain.execution.QExecution
import org.squashtest.tm.domain.requirement.QRequirement
import org.squashtest.tm.domain.requirement.QRequirementVersion
import org.squashtest.tm.domain.testautomation.QAutomatedTest
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage
import org.squashtest.tm.domain.testcase.QTestCase
import org.squashtest.tm.service.query.ConfiguredQuery

public class QueryEngineTestUtils {

	public static QTestCase tc = QTestCase.testCase
	public static QRequirementVersionCoverage cov = QRequirementVersionCoverage.requirementVersionCoverage
	public static QRequirementVersion v = QRequirementVersion.requirementVersion
	public static QRequirement r = QRequirement.requirement
	public static QIterationTestPlanItem itp = QIterationTestPlanItem.iterationTestPlanItem
	public static QIteration ite = QIteration.iteration
	public static QCampaign cp = QCampaign.campaign
	public static QExecution exec = QExecution.execution
	public static QIssue iss = QIssue.issue
	public static QAutomatedTest tatest = QAutomatedTest.automatedTest

	public static QueryProjectionColumn mkProj(ColumnType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName){
		def specType = new SpecializedEntityType(entityType : eType)
		def proto = new QueryColumnPrototype(specializedType : specType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def meas = new QueryProjectionColumn(columnPrototype : proto, operation : operation)

		return meas

	}

	public static QueryAggregationColumn mkAggr(ColumnType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName){
		def specType = new SpecializedEntityType(entityType : eType)
		def proto = new QueryColumnPrototype(specializedType : specType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def axe = new QueryAggregationColumn(columnPrototype : proto, operation : operation)

		return axe

	}

	public static QueryFilterColumn mkFilter(ColumnType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName, List<String> values){
		def specType = new SpecializedEntityType(entityType : eType)
		def proto = new QueryColumnPrototype(specializedType : specType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def filter = new QueryFilterColumn(columnPrototype : proto, operation : operation, values : values)

		return filter
	}


	public static QueryOrderingColumn mkOrder(ColumnType attrType, DataType datatype, EntityType eType, String attributeName, Order dir){
		def specType = new SpecializedEntityType(entityType : eType)
		def proto = new QueryColumnPrototype(specializedType : specType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def order = new QueryOrderingColumn(columnPrototype : proto, order: dir)

		return order
	}

	
	public static InternalQueryModel createInternalModel(QueryColumnPrototypeInstance... columns){
		def queryModel = new QueryModel()
		queryModel.projectionColumns = columns.findAll { it instanceof QueryProjectionColumn }
		queryModel.aggregationColumns = columns.findAll { it instanceof QueryAggregationColumn }
		queryModel.filterColumns = columns.findAll { it instanceof QueryFilterColumn }
		queryModel.orderingColumns = columns.findAll { it instanceof QueryOrderingColumn }

		return new InternalQueryModel(new ConfiguredQuery(queryModel))

	}

}
