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

import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.core.foundation.lang.DateUtils
import org.squashtest.tm.domain.bdd.ActionWord
import org.squashtest.tm.domain.bdd.BddImplementationTechnology
import org.squashtest.tm.service.internal.repository.ActionWordDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject
import java.sql.Timestamp
import java.time.LocalDateTime

@DataSet
@UnitilsSupport
class ActionWordDaoIT extends DbunitDaoSpecification {

	@Inject
	ActionWordDao actionWordDao

	def "ActionWords implementation information should be updated after their TestCases are Transmitted"() {
		given:
			def updatedCucumberActionWords = [-1L, -2L, -3L, -4L]
			def updatedRobotActionWords = [-5L, -6L, -7L]
			def notUpdatedActionWords = [-8L, -9L]
		when:
			actionWordDao.updateActionWordImplInfoFromAutomRequestIds([-1L, -2L, -3L, -4L, -5L])
		and:
			List<ActionWord> resCucumberActionWords = actionWordDao.findAllById(updatedCucumberActionWords)
			List<ActionWord> resRobotActionWords = actionWordDao.findAllById(updatedRobotActionWords)
			List<ActionWord> restNotUpdatedActionWords = actionWordDao.findAllById(notUpdatedActionWords)
		then:
			def currentIso9601Date = DateUtils.formatIso8601Date(new Date())
			resCucumberActionWords.each {
				assert DateUtils.formatIso8601Date(it.lastImplementationDate) == currentIso9601Date
				assert it.lastImplementationTechnology == BddImplementationTechnology.CUCUMBER
			}
		and:
			resRobotActionWords.each {
				assert DateUtils.formatIso8601Date(it.lastImplementationDate) == currentIso9601Date
				assert it.lastImplementationTechnology == BddImplementationTechnology.ROBOT
			}
		and:
			restNotUpdatedActionWords.each {
				assert DateUtils.formatIso8601Date(it.lastImplementationDate) == "2020-01-01"
				assert it.lastImplementationTechnology == null
			}
	}
}
