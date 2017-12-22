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
package org.squashtest.tm.service.internal.repository.hibernate

import javax.inject.Inject

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.requirement.RequirementCategory
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.domain.requirement.VerificationCriterion
import org.squashtest.tm.service.internal.repository.RequirementDao
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
class HibernateRequirementDaoIT extends DbunitDaoSpecification {
	@Inject RequirementDao requirementDao


	/**
	 * Dataset explained :
	 *
	 * <ul><li>Project1 #-1
	 * <ul><li>Requirement-Library #-1
	 * <ol><li>RequirementFolder #-30<ol><li>RequirementFolder #-70</li><ol><li>Requirement #-10<ol><li>Requirement #-50</li><ol><li>Requirement #-60</li></oL></oL></ol></li></ol></li>
	 * <li>Requirement #-40<ol><li>Requirement #-20</li></ol></li>
	 * </ol></li></ul></li></ul>
	 */
	@DataSet("HibernateRequirementDaoIT.should find requirements to export.xml")
	def "should find requirements to export from nodes"(){
		given :
		def selectedNodesIds = [-10L, -40L]

		when:
		def reqs = requirementDao.findRequirementToExportFromNodes(selectedNodesIds)

		then :
		reqs.size() == 5
		def req3 = reqs[0]
		req3.id == -40
		req3.folderName == ""
		req3.requirementParentPath == ""
		def req4 = reqs[1]
		req4.id == -20
		req4.folderName == ""
		req4.requirementParentPath == "req 40"
		def req1 = reqs[2]
		req1.id == -10
		req1.folderName == "folder 30/folder 40"
		req1.requirementParentPath == ""
		def req2 = reqs[3]
		req2.id == -50
		req2.folderName == "folder 30/folder 40"
		req2.requirementParentPath == "req 10"
		def req5 = reqs[4]
		req5.id == -60
		req5.folderName == "folder 30/folder 40"
		req5.requirementParentPath == "req 10/req 50"
	}

	/**
	 * see method "should find requirements to export from nodes" for dataset explanation
	 */
	@DataSet("HibernateRequirementDaoIT.should find requirements to export.xml")
	def "should find requirements to export from library"(){
		given :
		def libraryIds = [-1L]

		when:
		def reqs = requirementDao.findRequirementToExportFromLibrary(libraryIds)

		then :
		reqs.size() == 5
		def req3 = reqs[0]
		req3.id == -40
		req3.folderName == ""
		req3.requirementParentPath == ""
		def req4 = reqs[1]
		req4.id == -20
		req4.folderName == ""
		req4.requirementParentPath == "req 40"
		def req1 = reqs[2]
		req1.id == -10
		req1.folderName == "folder 30/folder 40"
		req1.requirementParentPath == ""
		def req2 = reqs[3]
		req2.id == -50
		req2.folderName == "folder 30/folder 40"
		req2.requirementParentPath == "req 10"
		def req5 = reqs[4]
		req5.id == -60
		req5.folderName == "folder 30/folder 40"
		req5.requirementParentPath == "req 10/req 50"
	}
}
