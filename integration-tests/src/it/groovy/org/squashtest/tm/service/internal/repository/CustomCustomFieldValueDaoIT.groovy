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
package org.squashtest.tm.service.internal.repository

import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.domain.EntityReference
import org.squashtest.tm.domain.EntityType
import org.squashtest.tm.service.internal.repository.hibernate.HibernateExecutionStepDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@DataSet
@UnitilsSupport
class CustomCustomFieldValueDaoIT extends DbunitDaoSpecification {

	@Inject
	CustomCustomFieldValueDao customFieldValueDao

	@Inject
	HibernateExecutionStepDao executionStepDao

	def "#getCufValuesMapByEntityReference(EntityReference, Map<EntityType, List<Long>>) - Should get the CufValues Map of a Campaign"() {
		given: "scope"
		EntityReference scope = new EntityReference(EntityType.CAMPAIGN, -1L)
		and: "my requested cufs"
		Map<EntityType, List<Long>> requestedCufMap = new HashMap<>()
		requestedCufMap.put(EntityType.CAMPAIGN, [-1L] as List)
		when:
		Map<EntityReference, Map<Long, Object>> resultMap = customFieldValueDao.getCufValuesMapByEntityReference(scope, requestedCufMap)
		then:
		resultMap != null
		Map<Long, Object> campaignCufMap = resultMap.get(scope)
		campaignCufMap != null
		campaignCufMap.get(-1L).toString() == "Hello"
	}

	def "#getCufValuesMapByEntityReference() - Should get the CufValues Map only for a TestStep related ExecutionStep"() {

		given:
		EntityReference scope = new EntityReference(EntityType.CAMPAIGN, -1L)
		and:
		Map<EntityType, List<Long>> requestedCufMap = new HashMap<>()
		requestedCufMap.put(EntityType.TEST_STEP, [-2L] as List)
		and:
		EntityReference testStep = new EntityReference(EntityType.TEST_STEP, -2L)
		when:
		Map<EntityReference, Map<Long, Object>> resultMap =
			customFieldValueDao.getCufValuesMapByEntityReference(scope, requestedCufMap)
		then:
		resultMap != null
		Map<Long, Object> testStepCuf = resultMap.get(testStep)
		testStepCuf != null
		testStepCuf.get(-2L).toString() == "Plop"
	}

	def "#getCufValuesMapByEntityReference() - Should get the CufValues Map only for an ExecutionStep"() {

		given:
			EntityReference scope = new EntityReference(EntityType.CAMPAIGN, -1L)
		and:
			Map<EntityType, List<Long>> requestedCufMap = new HashMap<>()
			requestedCufMap.put(EntityType.EXECUTION_STEP, [-3L] as List)
		and:
			EntityReference executionStep = new EntityReference(EntityType.EXECUTION_STEP, -2L)
		when:
			Map<EntityReference, Map<Long, Object>> resultMap =
				customFieldValueDao.getCufValuesMapByEntityReference(scope, requestedCufMap)
		then:
			resultMap != null
			Map<Long, Object> executionStepCufMap = resultMap.get(executionStep)
			executionStepCufMap != null
			executionStepCufMap.get(-3L).toString() == "Plop"
	}

	def "#getCufValuesMapByEntityReference() - Should get the CufValues of all EntityTypes"() {

		given: "Scope"
			EntityReference scope = new EntityReference(EntityType.CAMPAIGN, -1L)

		and: "Requested Cufs"
			Map<EntityType, List<Long>> requestedCufMap = new HashMap<>()
			requestedCufMap.put(EntityType.CAMPAIGN, [-1L, -4L] as List)
			requestedCufMap.put(EntityType.ITERATION, [-1L] as List)
			requestedCufMap.put(EntityType.TEST_SUITE, [-1L] as List)
			requestedCufMap.put(EntityType.TEST_CASE, [-1L] as List)
			requestedCufMap.put(EntityType.EXECUTION, [-1L] as List)
			requestedCufMap.put(EntityType.TEST_STEP, [-2L] as List)
			requestedCufMap.put(EntityType.EXECUTION_STEP, [-3L] as List)

		and: "All implied EntityReferences"
			EntityReference campaign1 = new EntityReference(EntityType.CAMPAIGN, -1L)
			EntityReference iteration1 = new EntityReference(EntityType.ITERATION, -1L)
			EntityReference testSuite1 = new EntityReference(EntityType.TEST_SUITE, -2L)
			EntityReference testCase1 = new EntityReference(EntityType.TEST_CASE, -1L)
			EntityReference execution1 = new EntityReference(EntityType.EXECUTION, -2L)
			EntityReference executionStep1 = new EntityReference(EntityType.EXECUTION_STEP, -2L)
			EntityReference testStep1 = new EntityReference(EntityType.TEST_STEP, -2L)
		when:
			Map<EntityReference, Map<Long, Object>> resultMap =
				customFieldValueDao.getCufValuesMapByEntityReference(scope, requestedCufMap)

		then:
			resultMap != null

			Map<Long, Object> campaignCufMap = resultMap.get(campaign1)
			campaignCufMap != null
			campaignCufMap.get(-1L).toString() == "Hello"
			def campaignTagCufsArray = campaignCufMap.get(-4L).toString().split('\\s*,\\s*')
			campaignTagCufsArray.length == 3
			Arrays.asList(campaignTagCufsArray).containsAll(["TAG1", "TAG2", "TAG3"])

			Map<Long, Object> iterationCufMap = resultMap.get(iteration1)
			iterationCufMap != null
			iterationCufMap.get(-1L).toString() == "Hola"

			Map<Long, Object> testCaseCufMap = resultMap.get(testCase1)
			testCaseCufMap != null
			testCaseCufMap.get(-1L).toString() == "Ciao!"

			Map<Long, Object> testSuiteCufMap = resultMap.get(testSuite1)
			testSuiteCufMap != null
			testSuiteCufMap.get(-1L).toString() == "Hallo"

			Map<Long, Object> executionCufMap = resultMap.get(execution1)
			executionCufMap != null
			executionCufMap.get(-1L).toString() == "Ola"

			Map<Long, Object> executionStepCufMap = resultMap.get(executionStep1)
			executionStepCufMap != null
			executionStepCufMap.get(-3L).toString() == "Plop"

			Map<Long, Object> testStepCufMap = resultMap.get(testStep1)
			testStepCufMap != null
			testStepCufMap.get(-2L).toString() == "Plop"
	}

}
