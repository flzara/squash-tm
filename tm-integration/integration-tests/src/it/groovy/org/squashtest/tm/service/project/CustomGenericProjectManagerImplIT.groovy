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
package org.squashtest.tm.service.project

import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.domain.bdd.BddImplementationTechnology
import org.squashtest.tm.domain.bdd.BddScriptLanguage
import org.squashtest.tm.domain.project.GenericProject
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.exception.NameAlreadyInUseException
import org.squashtest.tm.service.internal.project.WrongLifetimeFormatException
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@DataSet
@Transactional
@UnitilsSupport
class CustomGenericProjectManagerImplIT extends DbunitServiceSpecification {

	@Inject
	GenericProjectFinder genericProjectFinder

	@Inject
	CustomGenericProjectManager customGenericProjectManager

	def "should persist a project"() {
		given:
			def project = new Project()
			project.name = "new project"
		when:
			customGenericProjectManager.persist(project)
		then:
			project.testCaseLibrary != null
			project.testCaseLibrary.id != null

			project.requirementLibrary != null
			project.requirementLibrary.id != null

			project.campaignLibrary != null
			project.campaignLibrary.id != null

			project.customReportLibrary != null
			project.customReportLibrary.id != null

			project.automationRequestLibrary != null
			project.automationRequestLibrary.id != null

			project.actionWordLibrary != null
			project.actionWordLibrary.id != null

			project.requirementCategories != null
			project.testCaseNatures != null
			project.testCaseTypes != null
	}

	def "should not persist a project with a name already in use"() {
		given:
			def project = new Project()
			project.name = "project 1"
		when:
			customGenericProjectManager.persist(project)
		then:
			thrown NameAlreadyInUseException
	}

	def "#changeBddImplementationTechnology(long, String) - Should change the project Bdd Implementation Technology"() {
		when:
			"setup"
		then:
			GenericProject project = genericProjectFinder.findById(-1L)
			project.bddImplementationTechnology == BddImplementationTechnology.CUCUMBER
			project.bddScriptLanguage == BddScriptLanguage.FRENCH
		when:
			customGenericProjectManager.changeBddImplTechnology(-1L, "ROBOT")
		then:
			GenericProject reloadedProject = genericProjectFinder.findById(-1L)
			reloadedProject.bddImplementationTechnology == BddImplementationTechnology.ROBOT
			// check if choosing Robot set language to English
			reloadedProject.bddScriptLanguage == BddScriptLanguage.ENGLISH
		when:
			customGenericProjectManager.changeBddImplTechnology(-1L, "CUCUMBER")
		then:
			genericProjectFinder.findById(-1L).bddImplementationTechnology == BddImplementationTechnology.CUCUMBER

	}

	def "#changeBddScriptLanguage(long, String) - Should change the project Bdd Script Language"() {
		when:
			"setup"
		then:
			genericProjectFinder.findById(-1L).bddScriptLanguage == BddScriptLanguage.FRENCH
		when:
			customGenericProjectManager.changeBddScriptLanguage(-1L, "GERMAN")
		then:
			genericProjectFinder.findById(-1L).bddScriptLanguage == BddScriptLanguage.GERMAN
		when:
			customGenericProjectManager.changeBddScriptLanguage(-1L, "SPANISH")
		then:
			genericProjectFinder.findById(-1L).bddScriptLanguage == BddScriptLanguage.SPANISH
	}

	def "#changeBddScriptLanguage(long, String) - Should throw an IllegalArgumentException because a Robot project cannot be configured in another language than English"() {
		given:
			def project = genericProjectFinder.findById(-1L)
			project.setBddScriptLanguage(BddScriptLanguage.ENGLISH)
			project.setBddImplementationTechnology(BddImplementationTechnology.ROBOT)
		when:
			customGenericProjectManager.changeBddScriptLanguage(-1L, "GERMAN")
		then:
			thrown IllegalArgumentException
	}

	@Unroll
	def "#changeAutomatedSuitesLifetime(long, String) - Should change the project AutomatedSuitesLifetime"() {
		when:
			"setup"
		then:
			genericProjectFinder.findById(-1L).automatedSuitesLifetime == 0
		when:
			customGenericProjectManager.changeAutomatedSuitesLifetime(-1L, lifetime) == expectedResult
		then:
			genericProjectFinder.findById(-1L).automatedSuitesLifetime == expectedResult
		where:
			lifetime | expectedResult
			""		 | null
			"365"	 | 365
	}

	@Unroll
	def "#changeAutomatedSuitesLifetime(long, String) - Should throw an IllegalArgumentException because the input value is not a positive integer"() {
		when:
			customGenericProjectManager.changeAutomatedSuitesLifetime(-1L, lifetime)
		then:
			thrown WrongLifetimeFormatException
		where:
			lifetime << ["-4125", "999888777666", "lifetime"]

	}
}
