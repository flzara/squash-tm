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
package org.squashtest.tm.service.internal.testcase;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.ParameterAssignationMode;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.exception.CyclicStepCallException;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryNodeDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.testcase.CallStepManagerService;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.TestCaseCyclicCallChecker;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

@Service("squashtest.tm.service.CallStepManagerService")
@Transactional
public class CallStepManagerServiceImpl implements CallStepManagerService, TestCaseCyclicCallChecker {

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private TestStepDao testStepDao;
	
	@Inject
	private TestCaseLibraryNodeDao testCaseLibraryNodeDao;

	@Inject
	private TestCaseCallTreeFinder callTreeFinder;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private DatasetModificationService datasetModificationService;
	
	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Override
	@PreAuthorize("(hasPermission(#parentTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') "
			+ "and hasPermission(#calledTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')) "
			+ OR_HAS_ROLE_ADMIN)
	public void addCallTestStep(long parentTestCaseId, long calledTestCaseId) {

		checkAddCallTestStep(parentTestCaseId, calledTestCaseId);

		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);
		TestCase calledTestCase = testCaseDao.findById(calledTestCaseId);

		CallTestStep newStep = new CallTestStep();
		newStep.setCalledTestCase(calledTestCase);

		testStepDao.persist(newStep);

		parentTestCase.addStep(newStep);

		/*
		 * Feat 3693 : no need for that anymore : by default a call step doesn't delegate the parameters of the called test case anymore
		 *	datasetModificationService.updateDatasetParameters(parentTestCaseId);
		 */
		testCaseImportanceManagerService.changeImportanceIfCallStepAddedToTestCases(calledTestCase, parentTestCase);
	}
	
	
	
	@Override
	@PreAuthorize("hasPermission(#parentTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'WRITE') " 
			
			+ OR_HAS_ROLE_ADMIN)
	public void addCallTestSteps(long parentTestCaseId, List<Long> calledTestCaseIds) {
		
		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);
		
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIds(calledTestCaseIds);
		
		// check READ on each of those nodes
		// Throws AccessDenied if cannot read one of them
		for (TestCaseLibraryNode node : nodes){
			PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(node, "READ"));
		}
		
		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);
		
		for (TestCase testCase : testCases) {

			checkAddCallTestStep(parentTestCaseId, testCase.getId());
			
			TestCase calledTestCase = testCaseDao.findById(testCase.getId());
			
			CallTestStep newStep = new CallTestStep();
			newStep.setCalledTestCase(calledTestCase);
			testStepDao.persist(newStep);

			parentTestCase. addStep(newStep);
			
			testCaseImportanceManagerService.changeImportanceIfCallStepAddedToTestCases(calledTestCase, parentTestCase);
		}
		
	}

	@Override
	public void addCallTestStep(long parentTestCaseId, long calledTestCaseId,
			int index) {

		checkAddCallTestStep(parentTestCaseId, calledTestCaseId);

		TestCase parentTestCase = testCaseDao.findById(parentTestCaseId);
		TestCase calledTestCase = testCaseDao.findById(calledTestCaseId);

		CallTestStep newStep = new CallTestStep();
		newStep.setCalledTestCase(calledTestCase);

		testStepDao.persist(newStep);

		parentTestCase.addStep(index,newStep);

		testCaseImportanceManagerService.changeImportanceIfCallStepAddedToTestCases(calledTestCase, parentTestCase);

	}
	
	

	private void checkAddCallTestStep(long parentTestCaseId, long calledTestCaseId){
		if (parentTestCaseId == calledTestCaseId) {
			throw new CyclicStepCallException();
		}

		Set<Long> callTree = callTreeFinder.getTestCaseCallTree(calledTestCaseId);

		if (callTree.contains(parentTestCaseId)) {
			throw new CyclicStepCallException();
		}
	}


	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public TestCase findTestCase(long testCaseId) {
		return testCaseDao.findById(testCaseId);
	}


	@Override
	@PreAuthorize("hasPermission(#destinationTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')" + OR_HAS_ROLE_ADMIN)
	public void checkForCyclicStepCallBeforePaste(long destinationTestCaseId, String[] pastedStepId) {
		List<Long> idsAsList = parseLong(pastedStepId);
		checkForCyclicStepCallBeforePaste(destinationTestCaseId, idsAsList);
	}

	@Override
	@PreAuthorize("hasPermission(#destinationTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')" + OR_HAS_ROLE_ADMIN)
	public void checkForCyclicStepCallBeforePaste(long destinationTestCaseId, List<Long> pastedStepId) {
		List<Long> firstCalledTestCasesIds = testCaseDao.findCalledTestCaseOfCallSteps(pastedStepId);

		// 1> check that first called test cases are not the destination one.
		if (firstCalledTestCasesIds.contains(destinationTestCaseId)) {
			throw new CyclicStepCallException();
		}

		// 2> check that each first called test case doesn't have the destination one in it's callTree
		for (Long testCaseId : firstCalledTestCasesIds) {
			Set<Long> callTree = callTreeFinder.getTestCaseCallTree(testCaseId);
			if (callTree.contains(destinationTestCaseId)) {
				throw new CyclicStepCallException();
			}
		}

	}

	@Override
	@PreAuthorize("hasPermission(#destinationTestCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')" + OR_HAS_ROLE_ADMIN)
	public void checkForCyclicStepCallBeforePaste(Long destinationTestCaseId, Long calledTestCaseId) {

		// 1> check that first called test cases are not the destination one.
		if (calledTestCaseId.equals(destinationTestCaseId)) {
			throw new CyclicStepCallException();
		}

		// 2> check that each first called test case doesn't have the destination one in it's callTree
		Set<Long> callTree = callTreeFinder.getTestCaseCallTree(calledTestCaseId);
		if (callTree.contains(destinationTestCaseId)) {
			throw new CyclicStepCallException();
		}
	}


	private List<Long> parseLong(String[] stringArray) {
		List<Long> longList = new ArrayList<>();
		for (String aStringArray : stringArray) {
			longList.add(Long.parseLong(aStringArray));
		}
		return longList;
	}

	@Override
	@Transactional(readOnly = true)
	public void checkNoCyclicCall(TestCase testCase) throws CyclicStepCallException {
		long rootTestCaseId = testCase.getId();

		List<Long> firstCalledTestCasesIds = testCaseDao.findAllDistinctTestCasesIdsCalledByTestCase(rootTestCaseId);
		// 1> find first called test cases and check they are not the parent one
		if (firstCalledTestCasesIds.contains(rootTestCaseId)) {
			throw new CyclicStepCallException();
		}
		// 2> check that each first called test case doesn't have the destination one in it's callTree
		for (Long testCaseId : firstCalledTestCasesIds) {
			Set<Long> callTree = callTreeFinder.getTestCaseCallTree(testCaseId);
			if (callTree.contains(rootTestCaseId)) {
				throw new CyclicStepCallException();
			}
		}
	}


	@Override
	@PreAuthorize("hasPermission(#callStepId, 'org.squashtest.tm.domain.testcase.CallTestStep', 'WRITE')" + OR_HAS_ROLE_ADMIN)
	public void setParameterAssignationMode(long callStepId, ParameterAssignationMode mode, Long datasetId) {

		// a class cast exception would be welcome if the call step id is not appropriate
		// so I let it out of a try block.
		CallTestStep step = (CallTestStep) testStepDao.findById(callStepId);
		Long callerId = step.getTestCase().getId();

		switch(mode){
		case NOTHING :
			step.setCalledDataset(null);
			step.setDelegateParameterValues(false);
			break;

		case DELEGATE :
			step.setCalledDataset(null);
			step.setDelegateParameterValues(true);
			break;

		case CALLED_DATASET :
			if (datasetId == null){
				throw new IllegalArgumentException("attempted to bind no dataset (datasetid is null) to a call step, yet the parameter assignation mode is 'CALLED_DATASET'");
			}

			Dataset ds = datasetModificationService.findById(datasetId);
			step.setCalledDataset(ds);
			step.setDelegateParameterValues(false);
			break;

		default :
			throw new IllegalArgumentException("ParameterAssignationMode '"+mode+"' is not handled here, please find a dev and make him do the job");

		}

		datasetModificationService.cascadeDatasetsUpdate(callerId);

	}

	




}
