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
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException
import org.squashtest.tm.core.scm.spi.ScmConnector
import org.squashtest.tm.domain.scm.ScmRepository
import org.squashtest.tm.domain.scm.ScmServer
import org.squashtest.tm.domain.servers.AuthenticationProtocol
import org.squashtest.tm.domain.servers.Credentials
import org.squashtest.tm.exception.NameAlreadyInUseException
import org.squashtest.tm.service.internal.repository.ScmRepositoryDao
import org.squashtest.tm.service.internal.repository.ScmServerDao
import org.squashtest.tm.service.servers.CredentialsProvider
import spock.lang.Specification

class ScmRepositoryManagerServiceTest extends Specification {

	private ScmRepositoryManagerServiceImpl scmRepositoryManagerService = new ScmRepositoryManagerServiceImpl()
	private ScmConnectorRegistry scmRegistry = Mock()
	private ScmRepositoryDao scmRepositoryDao = Mock()
	private ScmServerDao scmServerDao = Mock()
	private CredentialsProvider credentialsProvider = Mock()

	def setup() {
		scmRepositoryManagerService.scmRepositoryDao = scmRepositoryDao
		scmRepositoryManagerService.scmServerDao = scmServerDao
		scmRepositoryManagerService.scmRegistry = scmRegistry
		scmRepositoryManagerService.credentialsProvider = credentialsProvider
	}

	def "#findByScmServerOrderByPath(Long) - [Nominal] Should find all ScmRepositories ordered by path"() {
		given: "Mock server"
			long serverId = 3
			ScmServer server = Mock(ScmServer){getId() >> serverId}
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
			ScmServer server = Mock(ScmServer){getId() >> serverId}

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
		and: "Mock server"
			long serverId = 12
			ScmServer server = Mock()
			server.getId() >> serverId
		and: "Mock credentials"
			Credentials credentials = Mock()
			AuthenticationProtocol protocol = GroovyMock()
			credentials.getImplementedProtocol() >> protocol
			Optional<Credentials> maybeCredentials = Optional.of(credentials)
		and: "Mock connector"
			ScmConnector connector = Mock()
			connector.supports(protocol) >> true
		and: "Mock Dao methods"
			scmRepositoryDao.isRepositoryNameAlreadyInUse(serverId, repo.name) >> false
			scmServerDao.getOne(serverId) >> server
			1 * scmRepositoryDao.save(repo) >> repo
		and:
			credentialsProvider.getAppLevelCredentials(server) >> maybeCredentials
			scmRegistry.createConnector(repo) >> connector
		when:
			scmRepositoryManagerService.createNewScmRepository(serverId, repo)
		then:
			1 * connector.createRepository(credentials)
			1 * connector.prepareRepository(credentials)
	}

	def "#createNewScmRepository(ScmRepository) - [Exception] Should try to create a new ScmRepository with a name already used and throw a NameAlreadyInUseException"() {
		given:
			String repoName = "My_Project"
			ScmRepository repo = new ScmRepository()
			repo.name = repoName
		and:
			long serverId = 12
			ScmServer server = Mock()
			server.getId() >> serverId
		and:
			scmRepositoryDao.isRepositoryNameAlreadyInUse(serverId, repoName) >> true
		when:
			scmRepositoryManagerService.createNewScmRepository(serverId, repo)
		then:
			thrown NameAlreadyInUseException
	}

	def "#createNewScmRepository(ScmRepository) - [Exception] Should try to create a new ScmRepository with no ScmServer credentials and throw an Exception"() {
		given:
			ScmRepository repo = new ScmRepository()
			repo.name = "One_Repository"
		and:
			ScmServer server = Mock()
			long serverId = 2
			server.getId() >> serverId
		and:
			scmRepositoryDao.isRepositoryNameAlreadyInUse(serverId, repo.name) >> false
			scmServerDao.getOne(serverId) >> server
			scmRepositoryDao.save(repo) >> repo
			credentialsProvider.getAppLevelCredentials(server) >> Optional.empty()
		when:
			scmRepositoryManagerService.createNewScmRepository(serverId, repo)
		then:
			thrown BugTrackerNoCredentialsException
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
			repo.workingFolderPath = "/resources/features"
		and: "Expected result"
			String newFolderPath = "/resources/gherkin/features"
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultFolderPath = scmRepositoryManagerService.updateFolder(repoId, newFolderPath)
		then:
			repoId == repoId
			repo.workingFolderPath == newFolderPath
			1 * scmRepositoryDao.save(repo)
			resultFolderPath == newFolderPath
	}

	def "#updateFolder(long, String) - [Nothing] Should try to update the folder path of a ScmRepository with the same path and do nothing"() {
		given: "Mock data"
			long repoId = 1
			String repoFolder = "/resources/features"
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.workingFolderPath = repoFolder
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultFolderPath = scmRepositoryManagerService.updateFolder(repoId, repoFolder)
		then:
			repo.id == repoId
			repo.workingFolderPath == repoFolder
			0 * scmRepositoryDao.save(repo)
			resultFolderPath == repoFolder
	}

	def "#updateBranch(long, String) - [Nominal] Should update the working branch of the ScmRepository"() {
		given: "Mock data"
			ScmServer server = Mock()
		and:
			long repoId = 7
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.workingBranch = "master"
			repo.scmServer = server
		and:
			Credentials credentials = Mock()
			AuthenticationProtocol protocol = GroovyMock()
			credentials.getImplementedProtocol() >> protocol
			Optional<Credentials> maybeCredentials = Optional.of(credentials)
		and:
			ScmConnector connector = Mock()
			connector.supports(protocol) >> true
		and: "Expected result"
			String newBranch = "develop"
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
			credentialsProvider.getAppLevelCredentials(server) >> maybeCredentials
			scmRegistry.createConnector(repo) >> connector
		when:
			String resultBranch = scmRepositoryManagerService.updateBranch(repoId, newBranch)
		then:
			repoId == repoId
			repo.workingBranch == newBranch
			1 * scmRepositoryDao.save(repo)
			1 * connector.prepareRepository(credentials)
			resultBranch == newBranch
	}

	def "#updateBranch(long, String) - [Nothing] Should try to update the working branch with the same branch and do nothing"() {
		given: "Mock data"
			long repoId = 7
			String repoBranch = "develop"
			ScmRepository repo = new ScmRepository()
			repo.id = repoId
			repo.workingBranch = repoBranch
		and: "Mock Dao method"
			scmRepositoryDao.getOne(repoId) >> repo
		when:
			String resultFolderPath = scmRepositoryManagerService.updateBranch(repoId, repoBranch)
		then:
			repo.id == repoId
			repo.workingBranch == repoBranch
			0 * scmRepositoryDao.save(repo)
			resultFolderPath == repoBranch
	}

	def "#updateBranch(long, String) - [Exception] Should try to update the working branch with no ScmServer credentials and thrown an Exception"() {
		given:
			ScmServer server = Mock()
		and:
			ScmRepository repo = new ScmRepository()
			repo.id = 7
			repo.workingBranch = "master"
			repo.scmServer = server
		and:
			scmRepositoryDao.getOne(repo.id) >> repo
			credentialsProvider.getAppLevelCredentials(server) >> Optional.empty()
		when:
			scmRepositoryManagerService.updateBranch(repo.id, "develop")
		then:
			1 * scmRepositoryDao.save(repo)
			thrown BugTrackerNoCredentialsException
	}

	def "#isOneRepositoryBoundToProject(Collection<Long>) - [Yes] Should verify that at least one ScmRepository is bound to a Project and return true"() {
		given: "Mock data"
			Collection<Long> repoIds = [2, 3]
		and: "Mock Dao method"
			scmRepositoryDao.isOneRepositoryBoundToProject(repoIds) >> true
		when:
			boolean result = scmRepositoryManagerService.isOneRepositoryBoundToProject(repoIds)
		then:
			result == true
	}

	def "#isOneRepositoryBoundToProject(Collection<Long>) - [No] Should verify that none of the ScmRepository is bound to a Project and return false"() {
		given: "Mock data"
			Collection<Long> repoIds = [2, 3]
		and: "Mock Dao method"
			scmRepositoryDao.isOneRepositoryBoundToProject(repoIds) >> false
		when:
			boolean result = scmRepositoryManagerService.isOneRepositoryBoundToProject(repoIds)
		then:
			result == false
	}

	def "#deleteScmrepositories(Collection<Long>) - [Nominal] - Should delete several ScmRepositories"() {
		given: "Mock data"
			Collection<Long> repoIds = [14, 5, 9]
		when:
			scmRepositoryManagerService.deleteScmRepositories(repoIds)
		then:
			1 * scmRepositoryDao.releaseScmRepositoriesFromProjects(repoIds)
			1 * scmRepositoryDao.deleteByIds(repoIds)
	}
}
