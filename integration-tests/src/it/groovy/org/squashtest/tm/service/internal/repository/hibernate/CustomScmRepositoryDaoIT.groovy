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
import org.squashtest.tm.service.internal.repository.ScmRepositoryDao
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
class CustomScmRepositoryDaoIT extends DbunitDaoSpecification {
	@Inject
	ScmRepositoryDao scmRepositoryDao

	@DataSet("CustomScmRepositoryDaoIT.xml")
	def "should find all scripted and keyword test cases by id then group them by their repository"(){
		given:
		def testcaseIds = [-1L,-2L,-3L,-4L,-5L,-6L,-7L,-8L,-9L,-10L,-11L,-12L,-13L,-14L,-15L]
		when:
		def res = scmRepositoryDao.findScriptedAndKeywordTestCasesGroupedByRepoById(testcaseIds)
		then:
		res != null
		res.size() == 2
		def entrySet = res.entrySet()
		def entry1 = entrySet.find{it.getKey().getId() == -1L}
		def entry2 = entrySet.find{it.getKey().getId() == -2L}
		and:
			entry1 != null
			def repo1 = entry1.getKey()
			repo1 != null
			repo1.getId() == -1L
			repo1.getName() == "First Repository"
			def repo1TestCases = entry1.getValue()
			repo1TestCases != null
			repo1TestCases.size() == 5
			def repo1TcIds = repo1TestCases.collect{it.id}
			repo1TcIds.containsAll([-3L, -4L, -5L, -10L, -11L])
		and:
			entry2 != null
			def repo2 = entry2.getKey()
			repo2 != null
			repo2.getId() == -2L
			repo2.getName() == "Second Repository"
			def repo2TestCases = entry2.getValue()
			repo2TestCases != null
			repo2TestCases.size() == 3
			def repo2TcIds = repo2TestCases.collect{it.id}
			repo2TcIds.containsAll([-12L, -13L, -14L])

	}

}
