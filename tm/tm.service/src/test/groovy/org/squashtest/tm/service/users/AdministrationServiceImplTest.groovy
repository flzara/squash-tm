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
package org.squashtest.tm.service.users

import org.squashtest.tm.domain.users.Team
import org.squashtest.tm.domain.users.User
import org.squashtest.tm.domain.users.UsersGroup
import org.squashtest.tm.exception.user.LoginAlreadyExistsException
import org.squashtest.tm.service.configuration.ConfigurationService
import org.squashtest.tm.service.feature.FeatureManager
import org.squashtest.tm.service.internal.repository.*
import org.squashtest.tm.service.internal.user.AdministrationServiceImpl
import org.squashtest.tm.service.security.AdministratorAuthenticationService
import org.squashtest.tm.service.security.acls.model.ObjectAclService
import org.squashtest.tm.service.user.UserAccountService
import spock.lang.Specification

class AdministrationServiceImplTest extends Specification {

	AdministrationServiceImpl service = new AdministrationServiceImpl()

	UserAccountService userAccountService = Mock()
	ProjectDao projectDao = Mock()
	UserDao userDao = Mock()
	UsersGroupDao groupDao = Mock()
	AdministrationDao adminDao = Mock()
	ConfigurationService configurationService = Mock()
	TeamDao teamDao = Mock()
	AdministratorAuthenticationService adminAuthentService = Mock()
	FeatureManager features = Mock()
	ObjectAclService aclService = Mock()


	def setup(){
		service.userAccountService = userAccountService
		service.projectDao = projectDao
		service.userDao = userDao
		service.groupDao = groupDao
		service.adminDao = adminDao
		service.configurationService = configurationService
		service.teamDao = teamDao
		service.adminAuthentService = adminAuthentService
		service.features = features
		service.aclService = aclService

		features.isEnabled(_) >> false
	}

	def "should associate user to team"(){
		given :
		User user = Mock()
		Team team = Mock()
		def teams = [team]
		userDao.findOne(1L)>>user
		teamDao.findAll([2L])>> teams

		//aclService.updateDerivedPermissions(1L) >> void

		when :
		service.associateToTeams(1L, [2L])
		then :
		1* user.addTeam(team)
		1* team.addMember(user)
	}

	def "should deassociate team from user"(){
		given :
		User user = Mock()
		def teamIds = [2L]
		userDao.findOne(1L) >> user
		when :
		service.deassociateTeams(1L, [2L])
		then :
		1*user.removeTeams([2L])
	}

	def "should create stub user from principal"() {
		given:
		User persisted

		and:
		UsersGroup defaultGroup = Mock()
		groupDao.findByQualifiedName(UsersGroup.USER) >> defaultGroup

		when:
		User res = service.createUserFromLogin("chris.jericho")

		then:
		res.login == "chris.jericho"
		res.lastName == "chris.jericho"
		res.firstName == ""
		res.active
		res.group == defaultGroup
		// we check something was persisted and we capture it
		1 * userDao.save({ persisted = it })
		res == persisted
	}

	def "should fail to create existing user from principal"() {
		given:
		userDao.findUserByLogin("chris.jericho") >> Mock(User)

		when:
		service.createUserFromLogin("chris.jericho")

		then:
		thrown LoginAlreadyExistsException
	}

	def "should create user"() {
		given:
		User newUser = Mock()
		newUser.login >> "chris.jericho"
		newUser.active >> true

		and:
		UsersGroup defaultGroup = Mock()
		groupDao.findOne(10L) >> defaultGroup

		when:
		service.addUser(newUser, 10L, "y2j")

		then:
		1 * userDao.save(newUser)
		1 * newUser.setGroup(defaultGroup)
		1 * adminAuthentService.createNewUserPassword("chris.jericho", "y2j", true, true, true, true, _)
	}

	def "should create user without credentials"() {
		given:
		User newUser = Mock()
		newUser.login >> "chris.jericho"

		and:
		UsersGroup defaultGroup = Mock()
		groupDao.findOne(10L) >> defaultGroup

		when:
		service.createUserWithoutCredentials(newUser, 10L)

		then:
		1 * userDao.save(newUser)
		1 * newUser.setGroup(defaultGroup)
		0 * adminAuthentService.createNewUserPassword(_, _, _, _, _, _, _)
	}

	def "should create authentication data"() {
		given:
		User user = Mock()
		user.login >> "chris.jericho"
		user.active >> true

		and:
		userDao.findOne(10L) >> user

		and:
		adminAuthentService.userExists("chris.jericho") >> false

		when:
		service.createAuthentication(10L, "y2j")

		then:
		1 * adminAuthentService.createUser({
			it.password == "y2j"
		})

	}

	def "should not create authentication data for existing user"() {
		given:
		User user = Mock()
		user.login >> "chris.jericho"
		user.active >> true

		and:
		userDao.findOne(10L) >> user

		and:
		adminAuthentService.userExists("chris.jericho") >> true

		when:
		service.createAuthentication(10L, "y2j")

		then:
		thrown LoginAlreadyExistsException

	}
}
