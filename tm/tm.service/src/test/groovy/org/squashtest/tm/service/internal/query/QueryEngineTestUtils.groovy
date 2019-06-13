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

import org.squashtest.tm.domain.query.ColumnType
import org.squashtest.tm.domain.query.DataType
import org.squashtest.tm.domain.query.Operation
import org.squashtest.tm.domain.query.QueryAggregationColumn
import org.squashtest.tm.domain.query.QueryColumnPrototype
import org.squashtest.tm.domain.query.QueryFilterColumn
import org.squashtest.tm.domain.query.QueryProjectionColumn
import org.squashtest.tm.domain.query.SpecializedEntityType
import org.squashtest.tm.domain.testcase.QTestCase
import spock.lang.Specification

public class QueryEngineTestUtils extends Specification{

	public static QTestCase tc = QTestCase.testCase
	public static QRequirementVersionCoverage cov = QRequirementVersionCoverage.requirementVersionCoverage
	public static QRequirementVersion v = QRequirementVersion.requirementVersion
	public static QRequirement r = QRequirement.requirement
	public static QIterationTestPlanItem itp = QIterationTestPlanItem.iterationTestPlanItem
	public static QIteration ite = QIteration.iteration
	public static QCampaign cp = QCampaign.campaign
	public static QExecution exec = QExecution.execution
	public static QIssue iss = QIssue.issue

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

}
