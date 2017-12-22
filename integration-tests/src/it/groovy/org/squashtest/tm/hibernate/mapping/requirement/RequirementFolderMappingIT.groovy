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


import org.hibernate.Hibernate
import org.hibernate.Session
import org.hibernate.exception.GenericJDBCException

import javax.validation.ConstraintViolationException

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.it.basespecs.DbunitMappingSpecification;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.Requirement
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.requirement.RequirementVersion

class RequirementFolderMappingIT extends DbunitMappingSpecification {
	def "should persist and retrieve a requirement folder"() {
		given:
		def f = new RequirementFolder(name:"folder mapping test")
		persistFixture f

		when:
		def res = doInTransaction({Session s -> s.get(RequirementFolder, f.id)
		})

		then:
		res != null

		cleanup:
		deleteFixture f
	}

	def "should not persist folder without name"() {
		given:
		def f = new RequirementFolder()

		when:
		persistFixture f

		then:
		thrown(ConstraintViolationException)
	}


	def "should persist and retrieve folders"(){
		given :
		def folder = new RequirementFolder(name : "folder")
		def tosave = new RequirementFolder(name : "tosave")



		folder.addContent tosave

		persistFixture folder


		when :
		def obj1
		def contentIds = doInTransaction {
			obj1 = it.get(RequirementFolder, folder.id)
			obj1.content.collect { it.id
			}
		}

		then :
		contentIds.size() == 1
		contentIds.containsAll([tosave.id])

		cleanup :

		deleteFixture folder, tosave


	}

}