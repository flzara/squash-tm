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
package org.squashtest.tm.service.internal.chart.engine

import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.domain.bugtracker.QIssue
import org.squashtest.tm.domain.campaign.QCampaign
import org.squashtest.tm.domain.campaign.QIteration
import org.squashtest.tm.domain.campaign.QIterationTestPlanItem
import org.squashtest.tm.domain.chart.*
import org.squashtest.tm.domain.execution.QExecution
import org.squashtest.tm.domain.requirement.QRequirement
import org.squashtest.tm.domain.requirement.QRequirementVersion
import org.squashtest.tm.domain.testautomation.QAutomatedTest
import org.squashtest.tm.domain.testcase.QRequirementVersionCoverage
import org.squashtest.tm.domain.testcase.QTestCase

public class ChartEngineTestUtils{

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

	public static MeasureColumn mkMeasure(ColumnType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName){
		def specType = new SpecializedEntityType(entityType : eType)
		def proto = new ColumnPrototype(specializedType : specType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def meas = new MeasureColumn(column : proto, operation : operation)

		return meas

	}

	public static AxisColumn mkAxe(ColumnType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName){
		def specType = new SpecializedEntityType(entityType : eType)
		def proto = new ColumnPrototype(specializedType : specType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def axe = new AxisColumn(column : proto, operation : operation)

		return axe

	}

	public static Filter mkFilter(ColumnType attrType, DataType datatype, Operation operation, EntityType eType, String attributeName, List<String> values){
		def specType = new SpecializedEntityType(entityType : eType)
		def proto = new ColumnPrototype(specializedType : specType, dataType : datatype, columnType : attrType, attributeName : attributeName)
		def filter = new Filter(column : proto, operation : operation, values : values)

		return filter

	}

}
