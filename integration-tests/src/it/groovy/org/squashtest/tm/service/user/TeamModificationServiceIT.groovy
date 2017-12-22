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
package org.squashtest.tm.service.user

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.users.Team
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.exception.NameAlreadyInUseException
import org.squashtest.tm.service.SecurityConfig;
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.it.config.EnabledAclSpecConfig;
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

/**
 * @author mpagnon
 *
 */
@UnitilsSupport
@Transactional
@ContextHierarchy([
	// enabling the ACL management that was disabled in DbunitServiceSpecification 
	@ContextConfiguration(name="aclcontext", classes = [EnabledAclSpecConfig], inheritLocations=false)	
])
class TeamModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	TeamModificationService service

	def "should persist a team"(){
		given : 
		Team team = new Team()
		team.name = "team1"
		
		when:
		service.persist(team)

		then:
		 def result = findAll("Team")
		 result.any ({it.name == "team1"})
	}
	
	@DataSet("TeamModificationServiceIT.should delete team with acls.xml")
	def"should delete a team along with it's acls"(){
		given:
		def teamId = -10L
		when:
		service.deleteTeam(teamId)
		then:
		!found(Team.class, -10L)
		!found("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID", -240L)		
	}
	
	@DataSet("TeamModificationServiceIT.should not persist team homonyme.xml")
	def"should not persist team homonyme"(){
		given : 
		Team team = new Team()
		team.name = "team1"
		when:
		service.persist(team)
		then:
		thrown(NameAlreadyInUseException)
	}
	
	
	@DataSet("TeamModificationServiceIT.should delete team but no user.xml")
	def"should delete a team but no user"(){
		given:
		def teamId = -10L
		when:
		service.deleteTeam(teamId)
		then:
		!found(Team.class, -10L)
		found(User.class, -20L)
		found(User.class, -30L)
		found("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID", -241L)
		found("ACL_RESPONSIBILITY_SCOPE_ENTRY", "ID", -242L)
	}
	
	@DataSet("TeamModificationServiceIT.should delete team having a core group.xml")
	def"should delete team having a core group"(){
		given:
		def teamId = -10L
		when:
		service.deleteTeam(teamId)
		then:
		!found(Team.class, -10L)
		
	}
}
