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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.lang.Wrapped;
import org.squashtest.tm.domain.testcase.IsScriptedTestCaseVisitor;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseVisitor;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.service.annotation.CheckLockedMilestone;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.testcase.DatasetModificationService;
import org.squashtest.tm.service.testcase.ParameterModificationService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional
@Service("squashtest.tm.service.ParameterModificationService")
public class ParameterModificationServiceImpl implements ParameterModificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParameterModificationServiceImpl.class);

	@Inject
	private ParameterDao parameterDao;

	@Inject
	private TestStepDao testStepDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private DatasetModificationService datasetModificationService;



	/**
	 * Returns the parameters that belongs to this test case only.
	 *
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Parameter> findOwnParameters(long testCaseId) {
		return parameterDao.findOwnParametersByTestCase(testCaseId);
	}

	/**
	 *
	 * Returns a list of parameters that either belongs to this test case, either belongs to
	 * test cases being called by a call step that uses the parameter delegation mode.
	 *
	 * @see
	 */
	@Override
	public List<Parameter> findAllParameters(long testCaseId) {

		return parameterDao.findAllParametersByTestCase(testCaseId);
	}

	/**
	 * @see ParameterModificationService#addNewParameterToTestCase(Parameter, long)
	 */
	@Override
	@CheckLockedMilestone(entityType = TestCase.class)
	public void addNewParameterToTestCase(Parameter parameter, @Id long testCaseId) {
		TestCase testCase = testCaseDao.findById(testCaseId);
		IsScriptedTestCaseVisitor testCaseVisitor = new IsScriptedTestCaseVisitor();
		if (testCaseVisitor.isScripted()) {
			throw new IllegalArgumentException("Cannot add parameters in a scripted test case.");
		}
		testCase.accept(testCaseVisitor);
		addNewParameterToTestCase(parameter, testCase);
	}

	private void addNewParameterToTestCase(Parameter parameter, TestCase testCase) {
		parameter.setTestCase(testCase);
		datasetModificationService.cascadeDatasetsUpdate(testCase.getId());
	}


	/**
	 * @see ParameterModificationService#addNewParameterToTestCase(Parameter, long)
	 */
	@Override
	public void changeName(long parameterId, String newName) {
		Parameter parameter = parameterDao.getOne(parameterId);
		parameter.setName(newName);
	}

	/**
	 * @see ParameterModificationService#changeDescription(long, String)
	 */
	@Override
	public void changeDescription(long parameterId, String newDescription) {

		Parameter parameter = parameterDao.getOne(parameterId);
		parameter.setDescription(newDescription);
	}

	/**
	 * @see ParameterModificationService#remove(Parameter)
	 */
	@Override
	public void remove(Parameter parameter) {
		this.parameterDao.delete(parameter);
	}

	/**
	 * @see ParameterModificationService#removeAllByTestCaseIds(List)
	 */
	@Override
	public void removeAllByTestCaseIds(List<Long> testCaseIds) {
		// note : hibernate bulk delete don't care of cascade delete so we have to remove the values by ourselves
		this.parameterDao.removeAllValuesByTestCaseIds(testCaseIds);
		this.parameterDao.removeAllByTestCaseIds(testCaseIds);
	}

	/**
	 * @see ParameterModificationService#removeById(long)
	 */
	@Override
	public void removeById(long parameterId) {
		Optional<Parameter> optParameter = parameterDao.findById(parameterId);
		if (optParameter.isPresent()){
			this.parameterDao.delete(optParameter.get());
		}
	}

	/**
	 * @see ParameterModificationService#createParamsForStep(long)
	 */
	@Override
	public void createParamsForStep(long stepId) {
		TestStep step = testStepDao.findById(stepId);
		createParamsForStep(step);
	}

	/**
	 * @see ParameterModificationService#createParamsForStep(TestStep)
	 */
	@Override
	public void createParamsForStep(TestStep step) {
		LOGGER.debug("creating parameters for step #{}", step.getId());
		Set<String> parameterNames = new ParameterNamesFinder().findParametersNamesInActionAndExpectedResult(step);

		LOGGER.trace("discovered parameters in step : {} ", parameterNames);

		for (String name : parameterNames) {
			createParameterIfNotExists(name, step.getTestCase());
		}
	}

	/**
	 * Will first check for a parameter of the given name in the test case. If there is none, will create one. When a
	 * parameter is created, the datasets of the test case and it's calling test cases will be updated in consequence.
	 *
	 * @param name
	 *            : the name of the potential new Parameter
	 * @param testCase
	 *            : the testCase to add the potential new parameter to
	 */
	private void createParameterIfNotExists(String name, TestCase testCase) {
		if (testCase != null) {
			Parameter parameter = testCase.findParameterByName(name);
			if (parameter == null) {
				LOGGER.debug("declaring new parameter '{}' in test case #{}", name, testCase.getId());
				parameter = new Parameter(name);
				addNewParameterToTestCase(parameter, testCase);
			}
		}
	}

	/**
	 * @see ParameterModificationService#isUsed(long)
	 */
	@Override
	public boolean isUsed(long parameterId) {
		Parameter parameter = parameterDao.getOne(parameterId);
		long testCaseId = parameter.getTestCase().getId();
		Wrapped<Boolean> isUsed =  new Wrapped<>();
		TestCaseVisitor visitor = new TestCaseVisitor() {
			@Override
			public void visit(TestCase testCase) {
				isUsed.setValue(testStepDao.stringIsFoundInStepsOfTestCase(parameter.getParamStringAsUsedInStep(), testCaseId));
			}

			@Override
			public void visit(KeywordTestCase keywordTestCase) {
				isUsed.setValue(testStepDao.stringIsFoundInStepsOfKeywordTestCase(parameter.getParamStringAsUsedInStep(), testCaseId));
			}

			@Override
			public void visit(ScriptedTestCase scriptedTestCase) {
				throw new IllegalArgumentException("Scripted Test Case doesn't have any parameter.");
			}
		};

		parameter.getTestCase().accept(visitor);
		return isUsed.getValue();
	}

	/**
	 * @see ParameterModificationService#findById(long)
	 */
	@Override
	public Parameter findById(long parameterId) {
		return parameterDao.getOne(parameterId);
	}

	/**
	 * @see ParameterModificationService#createParamsForTestCaseSteps(TestCase)
	 */
	@Override
	public void createParamsForTestCaseSteps(TestCase testCase) {
		for (TestStep step : testCase.getActionSteps()) {
			createParamsForStep(step);
		}

	}

}
