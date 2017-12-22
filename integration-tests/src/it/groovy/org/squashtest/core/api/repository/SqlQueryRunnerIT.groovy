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
package org.squashtest.core.api.repository

import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.ContextHierarchy;
import org.squashtest.it.basespecs.DatasourceDependantSpecification;
import org.squashtest.it.utils.SkipAll
import org.squashtest.tm.api.repository.SqlQueryRunner
import org.squashtest.tm.service.internal.api.repository.HibernateSqlQueryRunner;
import org.unitils.database.annotations.Transactional
import org.unitils.database.util.TransactionMode
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Specification
import spock.unitils.UnitilsSupport

import javax.inject.Inject

/**
 * Nore : isolation=Isolation.READ_UNCOMMITTED because SqlQueryRunner explicitely opens a tx and does not contribute to theone opened by test method. Which means it is not able to see injected data otherwise.
 *
 * @author Gregory Fouquet
 *
 */
@Rollback
@org.springframework.transaction.annotation.Transactional
@Transactional(TransactionMode.DISABLED)
@UnitilsSupport
@DataSet("SqlQueryRunnerIT.should select all active core users.xml")
class SqlQueryRunnerIT extends DatasourceDependantSpecification {
	@Inject
	SqlQueryRunner runner

	def "should select all active core user logins"() {
		when:

		def res = runner.executeSelect("select LOGIN from CORE_USER where ACTIVE = true")

		then:
		res.containsAll(["daniel.bryan", "chris.jericho"])
		res.size() == 2
	}

	def "should select all active core user logins and names"() {
		when:
		def res = runner.executeSelect("select LOGIN, LAST_NAME from CORE_USER where ACTIVE = true")

		then:
		res.find { it[0] == "daniel.bryan" }[1] == "bryan"
		res.find { it[0] == "chris.jericho" }[1] == "jericho"
		res.size() == 2
	}

	def "should select all active core user aliased logins and names"() {
		when:
		def res = runner.executeSelect('select LOGIN "logname", LAST_NAME "name" from CORE_USER where ACTIVE = true')

		then:
		res.find { it[0] == "daniel.bryan" }[1] == "bryan"
		res.find { it[0] == "chris.jericho" }[1] == "jericho"
		res.size() == 2
	}

	def "should select single inactive core user"() {
		when:
		def res = runner.executeUniqueSelect("select LOGIN from CORE_USER where ACTIVE = false")

		then:
		res == "shawn.michaels"
	}

	def "should select core user by last_name named parameter"() {
		when:
		def res = runner.executeSelect("select LOGIN from CORE_USER where LAST_NAME = :name", [name: "bryan"])

		then:
		res == ["daniel.bryan"]
	}

	def "should select core user by last_name named list parameter"() {
		when:
		def res = runner.executeSelect("select LOGIN from CORE_USER where LAST_NAME in ( :names )", [names: ["bryan", "jericho"]])

		then:
		res.containsAll(["daniel.bryan", "chris.jericho"])
	}

	def "should select unique core user by last_name named parameter"() {
		when:
		def res = runner.executeUniqueSelect("select LOGIN from CORE_USER where LAST_NAME = :name", [name: "bryan"])

		then:
		res == "daniel.bryan"
	}
}
