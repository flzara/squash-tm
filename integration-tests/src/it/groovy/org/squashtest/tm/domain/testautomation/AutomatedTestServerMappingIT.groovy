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
package org.squashtest.tm.domain.testautomation

import org.squashtest.it.basespecs.DbunitMappingSpecification
;

class AutomatedTestServerMappingIT extends DbunitMappingSpecification {

	def "should persist a new AutomatedTestServer"(){

		given :
		URL baseUrl = new URL("http://www.squashtest.com/")

		and :
		TestAutomationServer server = new TestAutomationServer("myserver", baseUrl, "bob", "robert", "jenkins")

		when :
		persistFixture server
		def server2 = doInTransaction({it.get(TestAutomationServer.class, server.id)})

		then :
		server2.baseURL.equals(server.baseURL)

		cleanup :
		deleteFixture server

	}

}
