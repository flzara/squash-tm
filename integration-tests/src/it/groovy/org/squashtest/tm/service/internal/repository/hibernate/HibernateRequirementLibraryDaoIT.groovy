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
package org.squashtest.tm.service.internal.repository.hibernate;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementLibraryDao;
import org.squashtest.it.basespecs.DbunitServiceSpecification;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;

@Transactional
@UnitilsSupport
@DataSet
class HibernateRequirementLibraryDaoIT extends DbunitServiceSpecification {
	@Inject RequirementLibraryDao dao

	def "should find root content of requirement library"() {
		when:
		def content = dao.findAllRootContentById(-110L)

		then:
		content.size() == 1
		content[0].id == -1110L

	}

	def "should find all libraries"() {
		when:
		def libs = dao.findAll()

		then:
		libs.size() == 2
	}

	def "should find library by id"() {
		when:
		def found = dao.findById(-120L)

		then:
		found != null
	}
}
