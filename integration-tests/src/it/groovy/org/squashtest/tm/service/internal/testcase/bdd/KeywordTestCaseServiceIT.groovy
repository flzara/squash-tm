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
package org.squashtest.tm.service.internal.testcase.bdd

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification

import org.squashtest.tm.service.testcase.bdd.KeywordTestCaseService
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@DataSet
class KeywordTestCaseServiceIT extends DbunitServiceSpecification {

	@Inject
	KeywordTestCaseService keywordTestCaseService

	def "Should generate a Gherkin script without test steps from a KeywordTestCase"() {
		expect:
		keywordTestCaseService.writeScriptFromTestCase(-6L) ==
			"# language: en\n" +
			"Feature: empty test"
	}

	def "Should generate a Gherkin script from a KeywordTestCase"() {
		expect:
		keywordTestCaseService.writeScriptFromTestCase(-4L) ==
"""# language: en
Feature: Disconnection test

	Scenario: Disconnection test
		GIVEN I am connected
		WHEN I sign oùt
		THEN Je suis déconnecté"""
	}
}
