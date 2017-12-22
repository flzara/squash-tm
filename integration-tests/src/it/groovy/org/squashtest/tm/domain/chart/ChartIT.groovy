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
package org.squashtest.tm.domain.chart

import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.EntityType;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class ChartIT extends DbunitServiceSpecification{

	@DataSet("charts.xml")
	def "Should find chart definition"(){
		
		when :
		def result = findEntity(ChartDefinition.class, -1L)
		then :
		result.name == "My chart"
		result.owner.login == "lol"
		result.visibility == Visibility.PUBLIC
		result.type == ChartType.PIE
	}
	
	@DataSet("charts.xml")
	def "Should find chart definition filters"(){
		
		when :
		def result = findEntity(ChartDefinition.class, -1L)
		then :
		result.filters.operation == [Operation.LOWER]
		result.filters.values == [["2000-12-12"]]
	}

	
	@DataSet("charts.xml")
	def "Should find chart definition axis"(){
		
		when :
		def result = findEntity(ChartDefinition.class, -1L)
		then :
		result.axis.label == ["my axis"]
		result.axis.operation == [Operation.NONE]
		result.axis.column.dataType == [DataType.NUMERIC]
	}
	
	@DataSet("charts.xml")
	def "Should find chart definition measures columns"(){
		
		when :
		def result = findEntity(ChartDefinition.class, -1L)
		then :
		result.measures.label == (0..5).collect{"my measure " + it}
		result.measures.operation == (0..5).collect{if (it == 1) Operation.BY_MONTH else Operation.BY_DAY }
	}

	
	@DataSet("charts.xml")
	def "Should find chart definition scope"(){
		
		when :
		def result = findEntity(ChartDefinition.class, -1L)
		then :
		result.scope.type ==  (1..4).collect{ if (it == 4) EntityType.PROJECT else EntityType.TEST_CASE } 
		result.scope.id == [-1, -2, -3, -1]
	}
	
}
