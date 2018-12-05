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
import org.squashtest.tm.service.internal.repository.ScmServerDao
import spock.lang.Specification

class ScmRepositoryManagerServiceTest extends Specification {

	private ScmRepositoryManagerServiceImpl scmRepositoryManagerService = new ScmRepositoryManagerServiceImpl()
	private ScmRepositoryDao scmRepositoryDao = Mock()
	private ScmServerDao scmServerDao = Mock()

	def setup() {
		scmRepositoryManagerService.scmRepositoryDao = scmRepositoryDao
		scmRepositoryManagerService.scmServerDao = scmServerDao
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

	def "#createNewScmRepository(ScmRepository) - [Nominal] Should create a new ScmRepository with its attributes"() {
		given: "Mock repository"
			ScmRepository repo = new ScmRepository()
			repo.name = "My_Project"
			repo.repositoryPath = "/home/repositories/repo1"
			repo.folderPath = "resources/features"
			repo.branch = "master"
		and:
			long serverId = 12
			ScmServer server = new ScmServer()
			server.id = serverId
		and: "Mock Dao methods"
			scmServerDao.getOne(serverId) >> server
			scmRepositoryDao.save(repo) >> repo
		when:
			scmRepositoryManagerService.createNewScmRepository(serverId, repo)
		then:
			1 * scmRepositoryDao.save(repo)
	}

	def "#updateName(long, String) - [Nominal] Should update the name of the ScmRepository"() {
		given: "Mock data"
			long repoId = 1
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.name = "SchoolManagementProject"
		and: "Expected result"
			String newName = "ZooManagementProject"
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultName = scmRepositoryManagerService.updateName(repoId, newName)
		then:
			repo.id == repoId
			repo.name == newName
			1 * scmRepositoryDao.save(repo)
			resultName == newName
	}

	def "#updatename(long, String) - [Nothing] Should try to update the name of a ScmRepository with the same name and do nothing"() {
		given: "Mock data"
		long repoId = 1
		String repoName = "SchoolProject"
		ScmRepository repo = new ScmRepository()
		repo.id = repoId
		repo.name = repoName
		and: "Mock Dao method"
		scmRepositoryDao.getOne(repoId) >> repo
		when:
		String resultName = scmRepositoryManagerService.updateName(repoId, repoName)
		then:
		repo.id == repoId
		repo.name == repoName
		0 * scmRepositoryDao.save(repo)
		resultName == repoName
	}

	def "#updatePath(long, String) - [Nominal] Should update the path of the ScmRepository"() {
		given: "Mock data"
			long repoId = 6
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.repositoryPath = "/home/repositories/project"
		and: "Expected result"
			String newPath = "/home/repositories/another_project"
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultPath = scmRepositoryManagerService.updatePath(repoId, newPath)
		then:
			repo.id == repoId
			repo.repositoryPath == newPath
			1 * scmRepositoryDao.save(repo)
			resultPath == newPath
	}

	def "#updatePath(long, String) - [Nothing] Should try to update the path of a ScmRepository with the same path and do nothing"() {
		given: "Mock data"
			long repoId = 6
			String repoPath = "/home/repositories/project"
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.repositoryPath = repoPath
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultPath = scmRepositoryManagerService.updatePath(repoId, repoPath)
		then:
			repo.id == repoId
			repo.repositoryPath == repoPath
			0 * scmRepositoryDao.save(repo)
			resultPath == repoPath
	}

	def "#updateFolder(long, String) - [Nominal] Should update the folder path of the ScmRepository"() {
		given: "Mock data"
			long repoId = 1
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.folderPath = "/resources/features"
		and: "Expected result"
			String newFolderPath = "/resources/gherkin/features"
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultFolderPath = scmRepositoryManagerService.updateFolder(repoId, newFolderPath)
		then:
			repoId == repoId
			repo.folderPath == newFolderPath
			1 * scmRepositoryDao.save(repo)
			resultFolderPath == newFolderPath
	}

	def "#updateFolder(long, String) - [Nothing] Should try to update the folder path of a ScmRepository with the same path and do nothing"() {
		given: "Mock data"
			long repoId = 1
			String repoFolder = "/resources/features"
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.folderPath = repoFolder
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultFolderPath = scmRepositoryManagerService.updateFolder(repoId, repoFolder)
		then:
			repo.id == repoId
			repo.folderPath == repoFolder
			0 * scmRepositoryDao.save(repo)
			resultFolderPath == repoFolder
	}

	def "#updateBranch(long, String) - [Nominal] Should update the working branch of the ScmRepository"() {
		given: "Mock data"
			long repoId = 7
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.branch = "master"
		and: "Expceted result"
			String newBranch = "develop"
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultBranch = scmRepositoryManagerService.updateBranch(repoId, newBranch)
		then:
			repoId == repoId
			repo.branch == newBranch
			1 * scmRepositoryDao.save(repo)
			resultBranch == newBranch
	}

	def "#updateBranch(long, String) - [Nothing] Should try to update the folder path of a ScmRepository with the same path and do nothing"() {
		given: "Mock data"
			long repoId = 7
			String repoBranch = "develop"
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.branch = repoBranch
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultFolderPath = scmRepositoryManagerService.updateBranch(repoId, repoBranch)
		then:
			repo.id == repoId
			repo.branch == repoBranch
			0 * scmRepositoryDao.save(repo)
			resultFolderPath == repoBranch
	}

	def "#deleteScmrepositories(Collection<Long>) - [Nominal] - Should delete several ScmRepositories"() {
		given: "Mock data"
			Collection<Long> repoIds = [14, 5, 9]
		when:
			scmRepositoryManagerService.deleteScmRepositories(repoIds)
		then:
			1 * scmRepositoryDao.deleteByIds(repoIds)
	}
}
