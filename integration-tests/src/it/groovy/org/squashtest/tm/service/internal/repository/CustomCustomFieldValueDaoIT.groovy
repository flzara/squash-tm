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
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.service.internal.repository.hibernate.HibernateExecutionStepDao
import spock.unitils.UnitilsSupport

import org.unitils.dbunit.annotation.DataSet
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
}
