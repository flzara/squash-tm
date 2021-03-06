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
package org.squashtest.tm.service.scmserver

import org.springframework.transaction.annotation.Transactional
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
class ScmRepositoryManagerServiceIT {

	@Inject
	ScmRepositoryManagerService scmRepositoryManagerService

	@DataSet("ScmRepositoryManagerServiceIT.xml")
	def "#findMatchingScmRepositoriesUrl(String) - Nominal - Should find matching scm repository's URL"() {
		given:
		String searchInput = "http://"

		when:
		List<String> result = scmRepositoryManagerService.findMatchingScmRepositoriesUrl(searchInput)
		then:
		result.size() == 2
		result.contains("http://github.com/toto/FirstRepository")
		result.contains("http://bitbucket.com/toto/SecondRepository")
	}

	@DataSet("ScmRepositoryManagerServiceIT.xml")
	def "#findMatchingScmRepositoriesUrl(String) - Empty - Should find no matching scm repository's URL"() {
		given:
		String searchInput = "https://"

		when:
		List<String> result = scmRepositoryManagerService.findMatchingScmRepositoriesUrl(searchInput)
		then:
		result.size() == 0
	}
}
