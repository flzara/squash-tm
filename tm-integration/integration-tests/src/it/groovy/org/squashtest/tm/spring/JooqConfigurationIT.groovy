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
package org.squashtest.tm.spring

import org.jooq.DSLContext
import org.junit.runner.RunWith
import org.spockframework.runtime.Sputnik
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.lang.IgnoreIf
import spock.unitils.UnitilsSupport

import javax.inject.Inject

import static org.squashtest.tm.jooq.domain.tables.Project.PROJECT

@UnitilsSupport
@Transactional
@RunWith(Sputnik)
class JooqConfigurationIT extends DbunitServiceSpecification {

	@Inject
	DSLContext dslContext

	static boolean isPostgres() {
		return System.properties['jooq.sql.dialect'] == 'POSTGRES'
	}

	@DataSet("JooqConfigurationIT.xml")
	def "should execute jooq query"() {
		when:
		def records = dslContext.select(PROJECT.PROJECT_ID).from(PROJECT).where(PROJECT.PROJECT_ID.eq(-1L)).fetch()

		then:
		records.size().equals(1)
	}

	@DataSet("JooqConfigurationIT.xml")
	@IgnoreIf({ return JooqConfigurationIT.isPostgres() })
	def "should generate request with good case for different databases"() {
		when:
		def request = dslContext.select(PROJECT.PROJECT_ID).from(PROJECT).where(PROJECT.PROJECT_ID.eq(-1L)).getSQL();

		then:
		if(isPostgres()){
			request.contains("project")
			!request.contains("PROJECT")
			!request.contains("Project")
		} else {
			request.contains("PROJECT")
			!request.contains("project")
			!request.contains("Project")
		}

	}


}
