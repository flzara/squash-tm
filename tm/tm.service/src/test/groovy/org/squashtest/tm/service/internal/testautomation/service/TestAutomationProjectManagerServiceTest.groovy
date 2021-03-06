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
package org.squashtest.tm.service.internal.testautomation.service

import org.squashtest.tm.domain.servers.AuthenticationProtocol
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials
import org.squashtest.tm.domain.testautomation.TestAutomationProject
import org.squashtest.tm.domain.testautomation.TestAutomationServer
import org.squashtest.tm.service.internal.testautomation.TestAutomationConnectorRegistry
import org.squashtest.tm.service.internal.testautomation.TestAutomationProjectManagerServiceImpl
import org.squashtest.tm.service.servers.CredentialsProvider
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector
import spock.lang.Specification

class TestAutomationProjectManagerServiceTest extends Specification {


	TestAutomationConnectorRegistry connectorRegistry

	CredentialsProvider credentialsProvider

	TestAutomationProjectManagerServiceImpl service

	def setup(){
		connectorRegistry = Mock()
		credentialsProvider = Mock()
		service = new TestAutomationProjectManagerServiceImpl()
		service.connectorRegistry = connectorRegistry
		service.credentialsProvider = credentialsProvider
	}

	def "should return a list of projects refering to a server object"(){

		given :
		def proj1 = new TestAutomationProject("proj1")
		def proj2 = new TestAutomationProject("proj2")
		def proj3 = new TestAutomationProject("proj3")

		and :
		TestAutomationConnector connector = Mock()
		connector.listProjectsOnServer(_, _) >> [ proj1, proj2, proj3 ]
		connector.supports(AuthenticationProtocol.BASIC_AUTH) >> true

		and :
		connectorRegistry.getConnectorForKind(_) >> connector

		BasicAuthenticationCredentials credentials = new BasicAuthenticationCredentials("admin", "password".toCharArray())

		credentialsProvider.getAppLevelCredentials(_) >> Optional.of(credentials)

		and :
		def server = new TestAutomationServer("jenkins")
		server.setName("myserver")
		server.setUrl("http://www.toto.com")

		when :
		def res = service.listProjectsOnServer(server)

		then :

		//the collection contains three elements
		res.size()==3

		//all of the elements refer to the same server instance :
		res.collect{it.server}.unique().size() == 1

		//the elements have the specified names :
		res.collect{it.jobName} == ["proj1", "proj2", "proj3"]
	}




}
