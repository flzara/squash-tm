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

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification
import org.squashtest.tm.service.internal.repository.MilestoneDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@DataSet
@Transactional
@UnitilsSupport
class MilestoneDaoIT extends DbunitDaoSpecification {

	@Inject
	MilestoneDao milestoneDao

	def "isTestStepBoundToLockedMilestone(long) - Should not find any milestone blocking the test step modification"() {
		expect:
		!milestoneDao.isTestStepBoundToLockedMilestone(-1L)
	}
	/* The TestStep belongs to a TestCase bound to a Planned|Locked Milestone */
	def "isTestStepBoundToLockedMilestone(long) - Should find a locked milestone directly blocking the test step modification"() {
		expect:
		milestoneDao.isTestStepBoundToLockedMilestone(-2L)
	}
	/* The TestStep belongs to a TestCase verifying a RequirementVersion bound to a Planned|Locked Milestone */
	def "isTestStepBoundToLockedMilestone(long) - Should find a locked milestone indirectly blocking the test step modification"() {
		expect:
		milestoneDao.isTestStepBoundToLockedMilestone(-3L)

	}

	def "isParameterBoundToLockedMilestone(long) - Should not find any milestone blocking the parameter modification"() {
		expect:
		!milestoneDao.isParameterBoundToLockedMilestone(-1L)
	}
	/* The Parameter belongs to a TestCase bound to a Planned|Locked Milestone */
	def "isParameterBoundToLockedMilestone(long) - Should find a locked milestone directly blocking the parameter modification"() {
		expect:
		milestoneDao.isParameterBoundToLockedMilestone(-2L)
	}
	/* The Parameter belongs to a TestCase verifying a RequirementVersion bound to a Planned|Locked Milestone */
	def "isParameterBoundToLockedMilestone(long) - Should find a locked milestone indirectly blocking the parameter modification"() {
		expect:
		milestoneDao.isParameterBoundToLockedMilestone(-3L)
	}

	def "isDatasetBoundToLockedMilestone(long) - Should not find any milestone blocking the dataset modification"() {
		expect:
		!milestoneDao.isDatasetBoundToLockedMilestone(-1L)
	}
	/* The Dataset belongs to a TestCase bound to a Planned|Locked Milestone */
	def "isDatasetBoundToLockedMilestone(long) - Should find a locked milestone directly blocking the dataset modification"() {
		expect:
		milestoneDao.isDatasetBoundToLockedMilestone(-2L)
	}
	/* The Dataset belongs to a TestCase verifying a RequirementVersion bound to a Planned|Locked Milestone */
	def "isDatasetBoundToLockedMilestone(long) - Should find a locked milestone indirectly blocking the dataset modification"() {
		expect:
		milestoneDao.isDatasetBoundToLockedMilestone(-3L)
	}

	def "isDatasetParamValueBoundToLockedMilestone(long) - Should not find any milestone blocking the dataset parameter value modification"() {
		expect:
		!milestoneDao.isDatasetParamValueBoundToLockedMilestone(-1L)
	}
	/* The DatasetParamValue belongs to a TestCase bound to a Planned|Locked Milestone */
	def "isDatasetParamValueBoundToLockedMilestone(long) - Should find a locked milestone directly blocking the dataset parameter value modification"() {
		expect:
		milestoneDao.isDatasetParamValueBoundToLockedMilestone(-2L)
	}
	/* The DatasetParamValue belongs to a TestCase verifying a RequirementVersion bound to a Planned|Locked Milestone */
	def "isDatasetParamValueBoundToLockedMilestone(long) - Should find a locked milestone indirectly blocking the dataset parameter value modification"() {
		expect:
		milestoneDao.isDatasetParamValueBoundToLockedMilestone(-3L)
	}

}
