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

import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.testcase.TestCaseStatus

import static org.squashtest.tm.domain.EntityType.*
import org.squashtest.tm.domain.query.ColumnType
import org.squashtest.tm.domain.query.DataType
import org.squashtest.tm.domain.query.QueryColumnPrototype
import org.squashtest.tm.domain.query.SpecializedEntityType
import org.squashtest.tm.domain.testcase.TestCaseImportance
import org.squashtest.tm.domain.testcase.TestCaseKind
import spock.lang.Specification
import spock.lang.Unroll


class EnumHelperTest extends Specification{

	@Unroll
	def "should retrieve enum values"(){

		when:
		def helper = new EnumHelper(column)

		then :
		helper.valueOf(value) == constant

		where :
		value			|	constant                            | 	column

		"VERY_HIGH"		|	TestCaseImportance.VERY_HIGH        |	proto(TEST_CASE, "importance")
		"STANDARD"		|	TestCaseKind.STANDARD               |	proto(TEST_CASE, "kind")
		"OBSOLETE"		|	TestCaseStatus.OBSOLETE             |	proto(TEST_CASE, "status")
		"APPROVED"		|	RequirementStatus.APPROVED          |	proto(REQUIREMENT_VERSION, "status")
		"MINOR"			|	RequirementCriticality.MINOR 		|	proto(REQUIREMENT_VERSION, "criticality")
		"MAJOR"			|	RequirementCriticality.MAJOR 		|	proto(REQUIREMENT, "resource.criticality")

	}

	def "should fail when the column is not an attribute column"(){

		given:
		def col = Mock(QueryColumnPrototype) { getColumnType() >> ColumnType.CALCULATED }

		when:
		new EnumHelper(col)

		then:
		thrown IllegalArgumentException

	}

	def "should fail when the column datatype is not levelenum"(){

		given:
		def col = Mock(QueryColumnPrototype) { getColumnType() >> ColumnType.ATTRIBUTE; getDataType() >> DataType.STRING }

		when:
		new EnumHelper(col)

		then:
		thrown IllegalArgumentException

	}



	def proto(entityType, attrName){
		def specType = new SpecializedEntityType(entityType, null)
		Mock(QueryColumnPrototype){
			getSpecializedType() >> specType
			getDataType() >>  DataType.LEVEL_ENUM
			getColumnType() >> ColumnType.ATTRIBUTE
			getAttributeName() >> attrName
		}
	}


}
