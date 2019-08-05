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

import org.jooq.DSLContext
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.core.foundation.collection.Filtering
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.users.Team
import org.squashtest.tm.jooq.domain.tables.AclClass
import org.squashtest.tm.jooq.domain.tables.AclObjectIdentity
import org.squashtest.tm.jooq.domain.tables.AclResponsibilityScopeEntry
import org.squashtest.tm.jooq.domain.tables.CoreGroupAuthority
import org.squashtest.tm.jooq.domain.tables.CoreGroupMember
import org.squashtest.tm.jooq.domain.tables.CoreTeamMember
import org.squashtest.tm.jooq.domain.tables.CoreUser
import org.unitils.dbunit.annotation.DataSet
import org.unitils.dbunit.annotation.ExpectedDataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.jooq.impl.DSL.countDistinct
import static org.squashtest.tm.api.security.acls.Roles.ROLE_ADMIN
import static org.squashtest.tm.api.security.acls.Roles.ROLE_TM_USER
import static org.squashtest.tm.core.foundation.collection.SortOrder.ASCENDING
import static org.squashtest.tm.core.foundation.collection.SortOrder.DESCENDING
import static org.squashtest.tm.jooq.domain.Tables.ACL_CLASS
import static org.squashtest.tm.jooq.domain.Tables.ACL_OBJECT_IDENTITY
import static org.squashtest.tm.jooq.domain.Tables.ACL_RESPONSIBILITY_SCOPE_ENTRY
import static org.squashtest.tm.jooq.domain.Tables.CORE_GROUP_AUTHORITY
import static org.squashtest.tm.jooq.domain.Tables.CORE_GROUP_MEMBER
import static org.squashtest.tm.jooq.domain.Tables.CORE_TEAM_MEMBER
import static org.squashtest.tm.jooq.domain.Tables.CORE_USER

/**
 * @author mpagnon
 *
 */
@UnitilsSupport
@Transactional
class AdministrationServiceIT extends DbunitServiceSpecification {

	@Inject
	private DSLContext DSL;

	@Inject
	AdministrationService service

	@DataSet("UserModificationServiceIT.should deassociate user to team.xml")
	@ExpectedDataSet("UserModificationServiceIT.should deassociate user to team result.xml")
	def "should deassociate user to team"(){
		given : "the dataset"
		when :
		service.deassociateTeams(-20L, new ArrayList<Long>([-10L]))
		then : "expected dataset is verified"
		getSession().flush();
	}

	@DataSet("UserModificationServiceIT.should associate user to team.xml")
	@ExpectedDataSet("UserModificationServiceIT.should associate user to team result.xml")
	def "should associate user to team"(){
		given : "the dataset"
		when :
		service.associateToTeams(-20L, new ArrayList<Long>([-10L]))
		then : "expected dataset is verified"
		getSession().flush();
	}

	@DataSet("UserModificationServiceIT.should find non associated teams.xml")
	def "should find non associated teams"(){
		given : "the dataset"
		when :
		def result = service.findAllNonAssociatedTeams(-30L)
		then :
		result.size() == 2
		result.find({it.id == -12L}) != null
		result.find({it.id == -11L}) != null
	}

	@DataSet("UserModificationServiceIT.should find sorted associated teams.xml")
	@Unroll
	def "should find sorted associated teams"(){
		given : "the dataset , a paging"
		PagingAndSorting paging = Mock()
		paging.firstItemIndex >> start
		paging.pageSize >> pageSize
		paging.sortedAttribute >> sortAttr
		paging.sortOrder >> sortOrder

		and :"a filtering"
		Filtering filtering = Mock()
		filtering.filter >> filter
		filtering.isDefined()>> filterDefined

		when :
		PagedCollectionHolder<List<Team>> result = service.findSortedAssociatedTeams(1000020L, paging, filtering)

		then :
		result.items.size() == expected.size()
		result.items*.name.containsAll(expected)

		where :
		start | pageSize | sortAttr | sortOrder  | filterDefined |filter | expected
		0     | 4        | "id"     | ASCENDING  |     false     | ""    |["ONE", "TWO", "THREE", "FIVE"]
		0     | 4        | "name"   | DESCENDING |     false     | ""    |["TWO", "THREE", "TEN", "SIX"]
		0     | 4        | "name"   | ASCENDING  |     true      | "T"   |["EIGHT","TEN", "THREE", "TWO"]
		0     | 2        | "id"     | ASCENDING  |     false     | ""    |["ONE", "TWO"]
		2     | 4        | "id"     | ASCENDING  |     false     | ""    |["THREE", "FIVE", "SIX", "SEVEN"]
	}

	@DataSet("AdministrationServiceIT.should count all active users assigned to at least one project.xml")
	def "should count all active users assigned to at least one project"() {
		given: "the dataset"

		when:
		def res = service.countAllActiveUsersAssignedToAtLeastOneProject()

		then:
		res == 5
	}


}
