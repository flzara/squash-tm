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
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Order
import org.squashtest.tm.domain.scm.ScmServer
import org.squashtest.tm.exception.NameAlreadyInUseException
import org.squashtest.tm.service.internal.repository.ScmServerDao
import spock.lang.Specification

class ScmServerManagerServiceTest extends Specification {

	private ScmServerManagerServiceImpl scmServerManagerService = new ScmServerManagerServiceImpl()
	private ScmServerDao scmServerDao = Mock()

	def setup() {
		scmServerManagerService.scmServerDao = scmServerDao
	}

	def "#findAllOrderByName() - [Nominal] Should find all the ScmServers ordered by name"() {
		given: "Mock data"
			ScmServer s1 = Mock()
			s1.name = "GitHub"
			ScmServer s2 = Mock()
			s2.name = "BitBucket"
			ScmServer s3 = Mock()
			s3.name = "Assembla"
		and: "Expected result"
			List<ScmServer> expectedList = [s3, s2, s1] as List
		and: "Mock Dao method"
			scmServerDao.findAllByOrderByNameAsc() >> expectedList
		when:
			List<ScmServer> resultList = scmServerManagerService.findAllOrderByName()
		then:
			resultList == expectedList
	}

	def "#findAllOrderByName() - [Empty] Should find no ScmServer"() {
		given: "Expected result"
			List<ScmServer> expectedList = [] as List
		and: "Mock Dao method"
			scmServerDao.findAllByOrderByNameAsc() >> expectedList
		when:
			List<ScmServer> resultList = scmServerManagerService.findAllOrderByName()
		then:
			resultList == expectedList
	}

	def "#findAllSortedScmServers(Pageable) - [Nominal] Should find all the ScmServers sorted by name"() {
		given: "Mock servers"
			ScmServer s1 = Mock()
			s1.name = "BitBucket"
			ScmServer s2 = Mock()
			s2.name = "GitHub"
			ScmServer s3 = Mock()
			s3.name = "Assembla"
		and: "Mock pageable"
			Order order = new Order(Direction.DESC, "name")
			Sort sort = new Sort(order)
			Pageable p = Mock()
			p.getSort() >> sort
		and: "Expected result"
			Page<ScmServer> expectedPage = [s2, s1, s3] as Page
		and: "Mock Dao method"
			scmServerDao.findAll(p) >> expectedPage
		when:
			Page<ScmServer> resultPage = scmServerManagerService.findAllSortedScmServers(p)
		then:
			resultPage == expectedPage
	}

	def "#findAllSortedScmServers(Pageable) - [Nominal] Should find all the ScmServers sorted by kind"() {
		given: "Mock servers"
			ScmServer s1 = Mock()
			s1.kind = "git"
			ScmServer s2 = Mock()
			s2.kind = "mercurial"
			ScmServer s3 = Mock()
			s3.kind = "subversion"
		and: "Mock pageable"
			Order order = new Order(Direction.DESC, "kind")
			Sort sort = new Sort(order)
			Pageable p = Mock()
			p.getSort() >> sort
		and: "Expected result"
			Page<ScmServer> expectedPage = [s3, s2, s1] as Page
		and: "Mock Dao method"
			scmServerDao.findAll(p) >> expectedPage
		when:
			Page<ScmServer> resultPage = scmServerManagerService.findAllSortedScmServers(p)
		then:
			resultPage == expectedPage
	}

	def "#findAllSortedScmServers(Pageable) - [Empty] Should find no ScmServer"() {
		given: "Mock data"
			Pageable p = Mock()
		and: "Expected result"
			Page<ScmServer> expectedPage = [] as Page
		and: "Mock Dao method"
			scmServerDao.findAll(p) >> expectedPage
		when:
			Page<ScmServer> resultPage = scmServerManagerService.findAllSortedScmServers(p)
		then:
			resultPage == expectedPage
	}

	def "#findScmServer(long) - [Nominal] Should find a ScmServer"() {
		given: "Mock data"
			ScmServer expectedServer = Mock()
			long serverId = 4
		and: "Mock Dao method"
			scmServerDao.getOne(serverId) >> expectedServer
		when:
			ScmServer foundServer = scmServerManagerService.findScmServer(serverId)
		then:
			foundServer == expectedServer
	}

	def "#createNewScmServer(ScmServer) - [Nominal] Should create a new ScmServer"() {
		given: "Mock data"
			ScmServer newScmServer = Mock()
		and: "Mock expected result"
			ScmServer expectedScmServer = Mock()
		and: "Mock Dao methods"
			scmServerDao.isServerNameAlreadyInUse(newScmServer) >> false
			scmServerDao.save(newScmServer) >> expectedScmServer
		when:
			ScmServer createdScmServer = scmServerManagerService.createNewScmServer(newScmServer)
		then:
			createdScmServer == expectedScmServer
	}

	def "#createNewScmServer(ScmServer) - [Exception] Should try to create a new ScmServer with a name already used and throw a NameAlreadyInUseException"() {
		given: "Mock data"
			ScmServer newScmServer = Mock()
			newScmServer.getName() >> "Github_Server"
		and: "Mock Dao method"
			scmServerDao.isServerNameAlreadyInUse(newScmServer.getName()) >> true
		when:
			scmServerManagerService.createNewScmServer(newScmServer)
		then:
			thrown NameAlreadyInUseException
	}

	def "#updateName(long, String) - [Nominal] Should update the name of a ScmServer"() {
		given: "Mock data"
			long serverId = 90
			ScmServer server = new ScmServer()
			server.id = serverId
			server.name = "GitHub Server"
		and:
			String newName = "GitLab Server"
		and: "Mock Dao methods"
			scmServerDao.getOne(serverId) >> server
			scmServerDao.isServerNameAlreadyInUse(newName) >> false
		when:
			String resultName = scmServerManagerService.updateName(serverId, newName)
		then:
			server.id == serverId
			server.name == newName
			1 * scmServerDao.save(server)
			resultName == newName
	}

	def "#updateName(long, String) - [Nothing] Should try to update the name of a ScmServer with the same name and do nothing"() {
		given: "Mock data"
			long serverId = 90
			String serverName = "GitHub Server"
			ScmServer server = new ScmServer()
			server.id = serverId
			server.name = serverName
		and: "Mock Dao method"
			scmServerDao.getOne(serverId) >> server
		when:
			String resultName = scmServerManagerService.updateName(serverId, serverName)
		then:
			server.id == serverId
			server.name == serverName
			0 * scmServerDao.isServerNameAlreadyInUse()
			0 * scmServerDao.save(server)
			resultName == serverName
	}

	def "#updateName(long, String) - [Exception] Should try to update the name of a ScmServer with an already used name and throw a NameAlreadyInUseException"() {
		given: "Mock data"
			long serverId = 90
			String serverName = "GitHub Server"
			ScmServer server = new ScmServer()
			server.id = serverId
			server.name = serverName
		and:
			String newName ="GitLab Server"
		and: "Mock Dao methods"
			scmServerDao.getOne(serverId) >> server
			scmServerDao.isServerNameAlreadyInUse(newName) >> true
		when:
			scmServerManagerService.updateName(serverId, newName)
		then:
			server.id == serverId
			server.name == serverName
			0 * scmServerDao.isServerNameAlreadyInUse()
			0 * scmServerDao.save(server)
			thrown NameAlreadyInUseException
	}

	def "#updateUrl(long, String) - [Nominal] Should update the Url of a ScmServer"() {
		given: "Mock data"
			long serverId = 50
			ScmServer server = new ScmServer()
			server.id = serverId
			server.url = "http://github.com"
		and:
			String newUrl = "http://gitlab.com"
		and: "Mock Dao method"
			scmServerDao.getOne(serverId) >> server
		when:
			String resultUrl = scmServerManagerService.updateUrl(serverId, newUrl)
		then:
			server.id == serverId
			server.url == newUrl
			1 * scmServerDao.save(server)
			resultUrl == newUrl
	}

	def "#updateUrl(long, String) - [Nothing] Should try to update the Url of a ScmServer with the same Url and do nothing"() {
		given: "Mock data"
			long serverId = 50
			String serverUrl = "http://github.com"
			ScmServer server = new ScmServer()
			server.id = serverId
			server.url = serverUrl
		and: "Mock Dao method"
			scmServerDao.getOne(serverId) >> server
		when:
			String resultUrl = scmServerManagerService.updateUrl(serverId, serverUrl)
		then:
			server.id == serverId
			server.url == serverUrl
			0 * scmServerDao.save(server)
			resultUrl == serverUrl
	}

	def "#updateUrl(long, String) - [Exception] Should try to update the Url of a ScmServer with an malformed Url and throw an Exception"() {
		given: "Mock data"
			long serverId = 50
			String serverUrl = "http://github.com"
			ScmServer server = new ScmServer()
			server.id = serverId
			server.url = serverUrl
		and:
			String malformedUrl = "malformedUrl"
		and: "Mock Dao methods"
			scmServerDao.getOne(serverId) >> server
			scmServerDao.save(server) >> { throw new Exception() }
		when:
			scmServerManagerService.updateUrl(serverId, malformedUrl)
		then:
			thrown Exception
	}

	def "isOneServerBoundToProject(Collection<Long>) - [True] Should verify that the ScmServers contain a ScmRepository bound to a Project and return true"() {
		given:
			Collection<Long> serverIds = [5, 27]
		and:
			scmServerDao.isOneServerBoundToProject(serverIds) >> true
		when:
			boolean result = scmServerManagerService.isOneServerBoundToProject(serverIds)
		then:
			result == true
	}

	def "isOneServerBoundToProject(Collection<Long>) - [False] Should verify that the ScmServers do not contain any ScmRepository bound to a Project and return false"() {
		given:
			Collection<Long> serverIds = [5, 27]
		and:
			scmServerDao.isOneServerBoundToProject(serverIds) >> false
		when:
			boolean result = scmServerManagerService.isOneServerBoundToProject(serverIds)
		then:
			result == false
	}

	def '#deleteScmServers(Collection<Long>) - [Nominal] Should delete several ScmServers'() {
		given: "Mock data"
			Collection<Long> serverIds = [1L, 5L, 15L]
		and:
			ScmServer s1 = Mock()
			ScmServer s2 = Mock()
			ScmServer s3 = Mock()
		and: "Mock Dao methods"
			scmServerDao.getOne(1L) >> s1
			scmServerDao.getOne(5L) >> s2
			scmServerDao.getOne(15L) >> s3
		when:
			scmServerManagerService.deleteScmServers(serverIds)
		then:
			1 * scmServerDao.releaseContainedScmRepositoriesFromProjects(serverIds)
			1 * scmServerDao.delete(s1)
			1 * scmServerDao.delete(s2)
			1 * scmServerDao.delete(s3)
	}
}
