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

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.testcase.*
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.repository.ParameterDao
import org.squashtest.tm.service.internal.repository.TestCaseDao
import org.squashtest.tm.service.testcase.DatasetModificationService
import org.squashtest.tm.service.testcase.ParameterFinder
import org.squashtest.tm.service.testcase.ParameterModificationService
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
class ParameterModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	ParameterModificationService service

	@Inject
	DatasetModificationService datasetService

	@Inject
	ParameterFinder finder

	@Inject
	TestCaseDao testCaseDao

	@Inject
	ParameterDao parameterDao

	@DataSet("ParameterModificationServiceIT.xml")
	def "should return the parameter list for a given test case"() {

		when:
		List<Parameter> params = service.findAllParameters(-100L)
		then:
		params.size() == 1
	}

	@DataSet("ParameterModificationServiceIT.xml")
	def "should return the parameter list for a given test case with call step"() {


		when:
		List<Parameter> params = service.findAllParameters(-101L)
		then:
		params.size() == 3
	}

	@DataSet("ParameterModificationServiceIT.xml")
	def "should change parameter name"() {

		when:
		service.changeName(-10100L, "newName")
		then:
		parameterDao.findById(-10100L).name == "newName"
	}

	@DataSet("ParameterModificationServiceIT.should change parameter name.xml")
	def "should change parameter name and update step"() {
		given: "a test step with one parameter that ocurs once in it's steps"
		long parameterId = -1L
		String newParamName = "newName"
		when:
		service.changeName(parameterId, newParamName)
		then:
		ActionTestStep editedStep = em.getReference(ActionTestStep.class, -1L)
		String newStep = "do this \${newName}"
		editedStep.action.equals(newStep)
	}

	@DataSet("ParameterModificationServiceIT.xml")
	def "should change parameter description"() {

		when:
		service.changeDescription(-10100L, "newDescription")
		then:
		parameterDao.findById(-10100L).description == "newDescription"
	}

	@DataSet("ParameterModificationServiceIT.xml")
	def "should remove parameter"() {

		when:
		TestCase testCase = testCaseDao.findById(-100L)
		Parameter param = parameterDao.findById(-10100L)
		parameterDao.delete(param)
		then:
		em.flush()
		testCase.getParameters().size() == 0
	}

	@DataSet("ParameterModificationServiceIT.xml")
	def "should find parameter in step"() {
		when:
		service.createParamsForStep(-101L)
		then:
		TestCase testCase = em.getReference(TestCase.class, -100L)
		testCase.parameters.collect { it.name }.contains("parameter")

	}

	@Unroll
	@DataSet("ParameterModificationServiceIT.should find if parameter is used.xml")
	def "should find whether a parameter is used in a test case"() {
		given:
		long parameterId = paramId
		when:
		boolean result = service.isUsed(parameterId)
		then:
		result == paramResult
		where:
		paramId | paramResult
		-1L     | true
		-2L     | false
		-3L     | false

	}

	@DataSet("ParameterModificationServiceIT.xml")
	def "should update datasets when a parameter is created"() {
		given: "a test case with a datataset"
		Dataset dataset = new Dataset(name: "dataset2")
		datasetService.persist(dataset, -100L)
		and: "a new parameter"
		Parameter parameter = new Parameter()
		parameter.name = "parameter2"
		when:
		service.addNewParameterToTestCase(parameter, -100L)
		then:
		TestCase testCase = testCaseDao.findById(-100L)
		testCase.getDatasets().size() == 1
		for (Dataset data : testCase.getDatasets()) {
			data.parameterValues.size() == 1
			for (DatasetParamValue param : data.parameterValues) {
				param.parameter.name == "parameter2"
				param.paramValue == ""
			}
		}
	}
}
