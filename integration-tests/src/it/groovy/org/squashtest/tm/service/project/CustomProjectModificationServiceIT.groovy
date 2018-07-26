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
import org.squashtest.tm.service.internal.dto.UserDto
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
class CustomProjectModificationServiceIT extends DbunitServiceSpecification {

	@Inject
	CustomProjectModificationService service



	@DataSet("CustomProjectModificationService.sandbox.xml")
	def "should find projects models"() {
		given:
		UserDto user = new UserDto("robert", -2L, [-100L, -300L], false)

		when:
		def jsonProjects = service.findAllProjects([-14L, -15L, -16L, -19L, -21L], user)

		then:
		jsonProjects.size() == 4
		jsonProjects.collect { it.name }.sort() == ["Projet 1", "Projet 2", "Projet 5", "Test Project-1"]

		def jsonProject15 = jsonProjects.getAt(2)
		jsonProject15.getId() == -15L
		jsonProject15.getName().equals("Projet 1")
		jsonProject15.getRequirementCategories().id == -1L
		jsonProject15.getTestCaseNatures().id == -2L
		jsonProject15.getTestCaseTypes().id == -3L

		def customFieldBindings = jsonProject15.getCustomFieldBindings()
		customFieldBindings.size() == 14
		def customFieldBindingModels = customFieldBindings.get("CAMPAIGN")
		customFieldBindingModels.size() == 3
		customFieldBindingModels.collect { it.id }.sort() == [-3L, -2L, -1L]
		customFieldBindingModels.collect { it.customField.id }.sort() == [-3L, -2L, -1L]
		customFieldBindingModels.collect { it.customField.name }.sort() == ["Liste", "Liste 2", "Lot"]

		def jsonMilestones = jsonProject15.getMilestones()
		jsonMilestones.size() == 2
		jsonMilestones.collect { it.label }.sort() == ["Jalon 1", "Jalon 2"]
	}
}
