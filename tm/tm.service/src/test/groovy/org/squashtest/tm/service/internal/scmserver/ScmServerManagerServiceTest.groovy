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

	def "#findAllOrderByName() - [Nominal] Should find all the ScmServers"() {
		given: "Mock data"
			ScmServer s1 = Mock()
			ScmServer s2 = Mock()
			ScmServer s3 = Mock()
		and: "Expected result"
			List<ScmServer> expectedList = [s1, s2, s3] as List
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

	def '#deleteSingleScmServer(long) - [Nominal] Should delete a single ScmServer'() {
		given: "Mock data"
			ScmServer server = Mock()
			server.getId() >> 37
		when:
			scmServerManagerService.deleteSingleScmServer(server.getId())
		then:
			1 * scmServerDao.deleteById(server.getId())
	}
}
