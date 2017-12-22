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
package org.squashtest.tm.service.security;

import static org.junit.Assert.*

import javax.inject.Inject

import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.it.config.EnabledAclSpecConfig
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@ContextHierarchy([
	// enabling the ACL management that was disabled in DbunitServiceSpecification 
	@ContextConfiguration(name="aclcontext", classes = [EnabledAclSpecConfig], inheritLocations=false)	
])
class OAuth2ClientServiceIT extends DbunitServiceSpecification {

	@Inject
	OAuth2ClientService oAuth2ClientService;

	def "should create a client"(){
		when:
		oAuth2ClientService.addClientDetails("client1", "secret");

		then:
		def clientList = oAuth2ClientService.findClientDetailsList();
		clientList.size() == 1
	}

	@DataSet("DatasetOAuth2ClientServiceIT.xml")
	def "should find the list of all clients"(){
		when:
		def clientList = oAuth2ClientService.findClientDetailsList();

		then:
		clientList.size() == 3
	}

	@DataSet("DatasetOAuth2ClientServiceIT.xml")
	def "should remove a client"(){
		when:
		oAuth2ClientService.removeClientDetails("client1");
		def clientList = oAuth2ClientService.findClientDetailsList();

		then:
		clientList.size() == 2
	}
}
