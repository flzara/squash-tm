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
package org.squashtest.tm.service.internal.testcase

import org.squashtest.tm.domain.testcase.CallTestStep
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.CyclicStepCallException
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao
import org.squashtest.tm.service.internal.repository.TestStepDao
import org.squashtest.tm.service.project.ProjectFilterModificationService
import org.squashtest.tm.service.testcase.DatasetModificationService

import spock.lang.Specification

class CallStepManagerServiceImplTest extends Specification {

	CallStepManagerServiceImpl service = new CallStepManagerServiceImpl();
	TestCaseDao testCaseDao = Mock()
	TestStepDao testStepDao = Mock()
	TestCaseLibraryDao testCaseLibraryDao = Mock()
	ProjectFilterModificationService filterService = Mock();
	TestCaseImportanceManagerServiceImpl testCaseImportanceManagerServiceImpl = Mock();
	TestCaseCallTreeFinder callTreeFinder = Mock()
	DatasetModificationService datasetModificationService = Mock()

	def setup(){
		service.testCaseDao = testCaseDao;
		service.testStepDao = testStepDao;
		service.testCaseImportanceManagerService = testCaseImportanceManagerServiceImpl
		service.callTreeFinder = callTreeFinder
		service.datasetModificationService = datasetModificationService
		
	}

	def "should deny step call creation because the caller and calling test cases are the same"(){

		when :
		service.addCallTestStep(1l, 1l);

		then :
		thrown(CyclicStepCallException);
	}


	def "should deny step call creation because the caller is somewhere in the test case call tree of the called test case"(){

		given :
		callTreeFinder.getTestCaseCallTree(_) >> [1L]
		
		when :
		service.addCallTestStep(1L, 2L);

		then :
		thrown(CyclicStepCallException);
	}

	def "should successfully create a call step"(){

		given : "linked test cases definition"

		TestCase caller = Mock();
		TestCase called = Mock();

		testCaseDao.findById(1l) >> caller;
		testCaseDao.findById(2l) >> called;

		and : "acyclic test case call tree"
		callTreeFinder.getTestCaseCallTree(_) >> [3L, 4L, 5L, 6L]

		when :
		service.addCallTestStep(1l, 2l)

		then :
		1 * caller.addStep( { it.calledTestCase  == called && it instanceof CallTestStep});

		1 * testStepDao.persist (_ )
	}
}
