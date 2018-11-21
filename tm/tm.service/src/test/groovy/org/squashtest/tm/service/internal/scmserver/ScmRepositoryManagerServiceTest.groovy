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
package org.squashtest.tm.service.internal.scmserver

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.scm.ScmServer
import org.squashtest.tm.service.internal.repository.ScmRepositoryDao
import spock.lang.Specification

class ScmRepositoryManagerServiceTest extends Specification {

	private ScmRepositoryManagerServiceImpl scmRepositoryManagerService = new ScmRepositoryManagerServiceImpl()
	private ScmRepositoryDao scmRepositoryDao = Mock()

	def setup() {
		scmRepositoryManagerService.scmRepositoryDao = scmRepositoryDao
	}

	def "#findByScmServerOrderByPath(Long) - [Nominal] Should find all ScmRepositories ordered by path"() {
		given: "Mock server"
			long serverId = 3
			ScmServer server = new ScmServer()
			server.id = serverId
		and: "Mock data"
			ScmRepository repo1 = new ScmRepository()
			repo1.repositoryPath = "/home/repositories/repo1"
			ScmRepository repo2 = new ScmRepository()
			repo2.repositoryPath = "/home/repositories/repo2"
			ScmRepository repo3 = new ScmRepository()
			repo3.repositoryPath = "/home/repositories/repo3"
		and: "Expected result"
			List<ScmRepository> expectedList = [repo1, repo2, repo3] as List
		and: "Mock Dao method"
			scmRepositoryDao.findByScmServerIdOrderByRepositoryPathAsc(serverId) >> expectedList
		when:
			List<ScmRepository> resultList = scmRepositoryManagerService.findByScmServerOrderByPath(serverId)
		then:
			resultList == expectedList
	}

	def "#findByScmServerOrderByPath(Long) - [Empty] Should find no ScmRepository"() {
		given: "Mock server"
			long serverId = 3
			ScmServer server = new ScmServer()
			server.id = serverId
		and: "Expected result"
			List<ScmRepository> expectedList = [] as List
		and: "Mock Dao method"
			scmRepositoryDao.findByScmServerIdOrderByRepositoryPathAsc(serverId) >> expectedList
		when:
			List<ScmRepository> resultList = scmRepositoryManagerService.findByScmServerOrderByPath(serverId)
		then:
			resultList == expectedList
	}


	def "#findPagedScmRepositoriesByScmServer(Long, Pageable) - [Nominal] Should find all the ScmRepositories sorted by path"() {
		given: "Mock server"
			long serverId = 5
		and: "Mock data"
			ScmRepository r1 = Mock()
			r1.repositoryPath = "/home/repositories/repo1"
			ScmRepository r2 = Mock()
			r2.repositoryPath = "/home/repositories/repo2"
			ScmRepository r3 = Mock()
			r3.repositoryPath = "/home/repositories/repo3"
		and: "Mock pageable"
			Sort.Order order = new Sort.Order(Sort.Direction.DESC, "path")
			Sort sort = new Sort(order)
			Pageable pageable = Mock()
			pageable.getSort() >> sort
		and: "Expected result"
			Page<ScmServer> expectedPage = [r3, r2, r1] as Page
		and: "Mock Dao method"
			scmRepositoryDao.findByScmServerId(serverId, pageable) >> expectedPage
		when:
			Page<ScmServer> resultPage = scmRepositoryManagerService.findPagedScmRepositoriesByScmServer(serverId, pageable)
		then:
			resultPage == expectedPage
	}

	def "#findPagedScmRepositoriesByScmServer(Long, Pageable) - [Empty] Should find no ScmRepositories"() {
		given: "Mock data"
			long serverId = 5
			Pageable pageable = Mock()
		and: "Expected result"
			Page<ScmRepository> expectedPage = [] as Page
		and: "Mock Dao method"
			scmRepositoryDao.findByScmServerId(serverId, pageable) >> expectedPage
		when:
			Page<ScmServer> resultPage = scmRepositoryManagerService.findPagedScmRepositoriesByScmServer(serverId, pageable)
		then:
			resultPage == expectedPage
	}

}
