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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.internal.repository.TestStepDao
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Unroll;
import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateTestStepDaoIT extends DbunitDaoSpecification {
	
	@Inject TestStepDao stepDao
	

	@DataSet("HibernateTestCaseDaoIT.should find filtered steps by test case id.xml")
	def "should load a step with its test case"() {
		when :
			ActionTestStep st = stepDao.findById(-200L)
			
		then :
			st.testCase.id == -10L
	}
	

	
	@DataSet("HibernateTestStepDaoIT.should find string in tc steps.xml")
	@Unroll("should find string in tc steps for test case nb #testCaseId")
	def "should find string in tc steps for test case "() {
		
		given : "a string to find and different test cases with steps in dataset"
		String stringToFind = "string to find"
		
		when :
			boolean result = stepDao.stringIsFoundInStepsOfTestCase(stringToFind, testCaseId);
			
		then :
			result == resultForTestCase
			
		where : 
			testCaseId | resultForTestCase
			-10L        | true
			-20L        | true 
			-30L        | false
			-40L        | true
			-50L        | false
		
	}
	
	
	@DataSet("HibernateTestCaseDaoIT.should find filtered steps by test case id.xml")
	def "should find the index of a step"(){
		
		when :
			def index = stepDao.findPositionOfStep(-300L)
			
		then :
			index == 2
		
		
	}
	
}
