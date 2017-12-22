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
package org.squashtest.tm.hibernate.mapping.requirement


import javax.validation.ConstraintViolationException;

import org.hibernate.JDBCException
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.it.basespecs.DbunitMappingSpecification;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementSyncExtender;
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage
import org.squashtest.tm.domain.testcase.TestCase
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException

class RequirementMappingIT extends DbunitMappingSpecification {


	def "should persist a new Requirement"(){
		given :
		def version = new RequirementVersion(name: "req 1", description: "this is a new requirement")
		def requirement = new Requirement(version)
		def categ = doInTransaction({it.get(InfoListItem.class, 1l)})
		requirement.category = categ

		when :
		persistFixture requirement
		def obj = doInTransaction({session -> session.get(Requirement, requirement.id) })

		then :
		obj != null
		obj.id!=null
		obj.name == "req 1"
		obj.description == "this is a new requirement"
		obj.createdBy != null
		obj.createdOn !=null
		obj.lastModifiedOn ==null
		cleanup :
		deleteFixture requirement
	}

	def "hibernate should bypass the setters and successfuly load a requirement with status obsolete"(){

		given :
		def version = new RequirementVersion(name: "req 2", description: "this is an obsolete requirement")
		def requirement = new Requirement(version)
		def categ = doInTransaction({it.get(InfoListItem.class, 1l)})
		requirement.category = categ
		requirement.setStatus(RequirementStatus.OBSOLETE)

		when :
		persistFixture requirement
		def refetch = doInTransaction({session -> session.get(Requirement, requirement.id)})

		then :
		notThrown IllegalRequirementModificationException
		refetch.name =="req 2"
		refetch.description == "this is an obsolete requirement"
		refetch.status == RequirementStatus.OBSOLETE

		cleanup :
		deleteFixture requirement
	}


	def "should not persist a nameless requirement"(){
		given :
		def requirement = new Requirement(new RequirementVersion())

		when :
		persistFixture requirement

		then :
		thrown (ConstraintViolationException)
	}


}
