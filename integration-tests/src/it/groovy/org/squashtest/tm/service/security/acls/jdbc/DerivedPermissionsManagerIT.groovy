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
package org.squashtest.tm.service.security.acls.jdbc

import javax.inject.Inject;

import org.spockframework.util.NotThreadSafe;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.it.basespecs.DbunitServiceSpecification;
import org.unitils.dbunit.annotation.DataSet;
import org.squashtest.tm.domain.project.Project;

import spock.lang.Unroll
import spock.unitils.UnitilsSupport;


/*
 * The dataset in this test is the following :
 *
 * project : {
 *  id:14
 * }
 *
 * team{
 * 	id:38
 *  clearance: project manager
 * }
 *
 * user{
 * 	id:39
 * 	team:none
 * 	clearance:none
 * 	remarks : totally unrelated.
 * }
 *
 * user{
 * 	id : 34
 team : none
 *  clearance :  tester
 * }
 *
 * user{
 *  id : 35
 *  team : none
 *  clearance : project manager
 * }
 *
 * user{
 * 	id:36
 *  team:38
 *  clearance : none
 *  team clearance : project manager
 * }
 *
 * user{
 *  id:37
 *  team : 38
 *  clearance : project manager
 *  team clearance : project manager
 * }
 *
 *
 *
 */

@NotThreadSafe
@UnitilsSupport
@Transactional
class DerivedPermissionsManagerIT extends DbunitServiceSpecification {

	private static final String PROJECT_CLASSNAME = Project.class.getName()
	private static final ObjectIdentity PROJECT_IDENTITY = new ObjectIdentityImpl(PROJECT_CLASSNAME, -14L)

	static final long TEAM_ID = -38L

	@Inject
	private DerivedPermissionsManager manager


	// ***************** low level methods ***************************


	@Unroll("should decide that it should #yesorno handle object of class #type")
	def "should decide that it should handle this or that"(){

		expect :
		ObjectIdentity id = new ObjectIdentityImpl(type, -0L)
		response == manager.isSortOfProject(id)

		where :

		type 						|		response		| yesorno
		Project.class.getName()		|		true			| ""
		'some.other.class'			|		false			| "not"
	}


	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	@Unroll("should find that this given target object identity #yesorno exist")
	def "should find that this or that target object identity exist or dont"(){

		expect :
		expectation == manager.doesExist(id)

		where :
		expectation	|	yesorno		| id
		true		|	"does"		| PROJECT_IDENTITY
		false		|	"does not"	| new ObjectIdentityImpl("georges.what.else", -2L)
	}


	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	@Unroll("should find that this given party #yesorno exist")
	def "should find that this or that given party exist/dont"(){

		expect :
		expectation == manager.doesExist(id)

		where :
		expectation	|	yesorno		| id
		true		|	"does"		| -38L
		false		|	"does not"	| -999L
	}


	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should find the members of the team"(){

		expect :
		manager.findMembers(TEAM_ID) as Set == [-36L, -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should find out that the said team is actually a user and returns it"(){

		expect :
		manager.findMembers(-36L) == [-36L]

	}


	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should find all the users that participate to a project, either directly or via a team"(){

		expect :
		manager.findUsers(PROJECT_IDENTITY) as Set == [-34L, -35L, -36L, -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should find all the users, no restrictions"(){

		expect :
		manager.findAllUsers() as Set == [-34L, -35L, -36L, -37L, -39L] as Set
	}


	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should remove project manager authorities from the given users"(){

		when :
		manager.removeProjectManagerAuthorities([-35L, -37L])

		then :

		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) == [-36L]

	}


	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should grant project manager authority to user"(){

		when :
		manager.grantProjectManagerAuthorities([-34L])

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
						where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """).containsAll([-34L])
	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should filter out users that don't manage anything"(){

		expect :
		manager.retainUsersManagingAnything([-34L, -35L, -36L, -37L]) as Set == [-35L, -36L, -37L] as Set

	}


	// ***************** high level methods ***************************



	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	@Unroll("should fix messed up personal authorities (#loopnum)")
	def "should fix the personal authorities regardless of their current (and possibly wrong) state"(){

		given :
		def ids = [-34L, -35L, -36L, -37L, -39L]
		messUpAuthorities( ids )

		when :
		manager.updateAuthsForThoseUsers([-34L, -35L, -36L, -37L, -39L])

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
				where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-35L, -36L, -37L] as Set

		where :
		a						|	loopnum
		"dummy"					|	"(1)"
		"dummy"					|	"(2)"
		"dummy"					|	"(3)"
		"dummy"					|	"(4)"
		"dummy"					|	"(5)"

	}


	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should update the authorities of a new project manager on the project"(){

		given :
		executeSQL(""" update ACL_RESPONSIBILITY_SCOPE_ENTRY
						set ACL_GROUP_ID=-5 where PARTY_ID=-34 """)

		when :
		manager.updateDerivedPermissions(-34L)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-34L, -35L, -36L, -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should update the authorities of a new team member"(){

		given :
		executeSQL(" insert into CORE_TEAM_MEMBER values(-38, -34) ")

		when :
		manager.updateDerivedPermissions(-34L)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-34L, -35L, -36L, -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should remove the authorities of a demoted project manager"(){

		given :
		executeSQL(""" update ACL_RESPONSIBILITY_SCOPE_ENTRY
						set ACL_GROUP_ID=-2 where PARTY_ID=-35 """)

		when :
		manager.updateDerivedPermissions(-35L)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-36L, -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should remove the authorities of a member removed of a team of project leaders and who doesn't manage anything else"(){

		given :
		executeSQL("delete from CORE_TEAM_MEMBER where USER_ID=-36 ")

		when :
		manager.updateDerivedPermissions(-36L)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-35L, -37L] as Set

	}


	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should maintain the authorities of a removed project leader because he still belongs to the team of project leaders"(){

		given :
		executeSQL("delete from ACL_RESPONSIBILITY_SCOPE_ENTRY where PARTY_ID=-37 ")

		when :
		manager.updateDerivedPermissions(-37L)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-35L, -36L, -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "should maintain the authorities of a guy removed from the team of project leader because he still has direct manager clearances"(){

		given :
		executeSQL(" delete from CORE_TEAM_MEMBER where USER_ID=-37 ")

		when :
		manager.updateDerivedPermissions(-37L)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
				where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-35L, -36L, -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "once the team of project leaders is removed from the project, one user is demoted but not the other one"(){

		given :
		executeSQL(" delete from ACL_RESPONSIBILITY_SCOPE_ENTRY where  PARTY_ID=" +TEAM_ID)

		when :
		manager.updateDerivedPermissions(TEAM_ID)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
				where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-35L,  -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "once the team of project leaders is demoted, one user is demoted but not the other one"(){

		given :
		executeSQL(""" update ACL_RESPONSIBILITY_SCOPE_ENTRY
						set ACL_GROUP_ID=-2 where PARTY_ID=""" +TEAM_ID)

		when :
		manager.updateDerivedPermissions(TEAM_ID)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) as Set == [-35L,  -37L] as Set

	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "project is nuked ! authorities updated accordingly"(){

		given :
		executeSQL(""" delete from ACL_RESPONSIBILITY_SCOPE_ENTRY
					where OBJECT_IDENTITY_ID in (-14, -15, -16, -17)""")
		executeSQL(" delete from ACL_OBJECT_IDENTITY ")

		when :
		manager.updateDerivedPermissions(PROJECT_IDENTITY)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) == []
	}

	@DataSet("DerivedPermissionsManagerIT.setup.xml")
	def "the project leader team is nuked ! authorities updated accordingly"(){

		given :
		executeSQL(" delete from ACL_RESPONSIBILITY_SCOPE_ENTRY where  PARTY_ID=" +TEAM_ID)
		executeSQL(" delete from CORE_TEAM_MEMBER ")
		executeSQL(" delete from CORE_TEAM ")
		executeSQL(" delete from CORE_PARTY where PARTY_ID = "+TEAM_ID)

		when :
		manager.updateDerivedPermissions(TEAM_ID)

		then :
		executeSQL(""" select PARTY_ID from CORE_PARTY_AUTHORITY
					where AUTHORITY='ROLE_TM_PROJECT_MANAGER' """) == [-35L, -37L ]

	}



	// ******************************* utilities *******************************

	def messUpAuthorities(ids){

		def random = new Random()

		executeSQL(" delete from CORE_PARTY_AUTHORITY ")

		ids.each{
			def ismanager = random.nextBoolean()
			if (ismanager){
				newSQLQuery(" insert into CORE_PARTY_AUTHORITY values ($it, 'ROLE_TM_PROJECT_MANAGER')")
			}
		}
	}

}
