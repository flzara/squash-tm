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
package org.squashtest.tm.service.internal.customfield

import org.spockframework.util.NotThreadSafe
import org.springframework.transaction.annotation.Transactional
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Unroll
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
@Transactional
@NotThreadSafe
@DataSet("CustomFieldModelService.sandbox.xml")
class CustomFieldModelServiceIT extends DbunitServiceSpecification {

	@Inject
	private CustomFieldModelServiceImpl customFieldModelService

	def "should find cuf ids"() {
		when:
		def ids = customFieldModelService.findUsedCustomFieldIds(readableProjectIds)

		then:
		ids.sort() == expectdInfolistIds.sort()

		where:
		readableProjectIds   || expectdInfolistIds
		[]                   || []
		[-1L]                || [-1L, -3L]
		[-1L, -2L]           || [-1L, -2L, -3L]
		[-1L, -2L, -3L, -4L] || [-1L, -2L, -3L]
	}


	def "should fetch correct number of cuf models and options"() {
		when:
		def cufMap = customFieldModelService.findCufMap([-1L, -2L, -3L])

		then:
		cufMap.size() == 3
		CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel2 = cufMap.get(-2L)
		singleSelectFieldModel2.options.size() == 4

		CustomFieldModelFactory.SingleSelectFieldModel singleSelectFieldModel3 = cufMap.get(-3L)
		singleSelectFieldModel3.options.size() == 2
	}

	@Unroll
	def "should fetch correct single value cuf models"() {
		when:
		def cufMap = customFieldModelService.findCufMap([-1L, -2L, -3L, -4L, -5L])

		then:
		def customFieldModel = cufMap.get(cufId)
		customFieldModel.id == cufId
		customFieldModel.code == code
		customFieldModel.label == label
		customFieldModel.defaultValue == defaultValue
		customFieldModel.isOptional() == isOptionnal
		customFieldModel.class == expectedClass

		where:

		cufId || code   | label        | name   | defaultValue          | isOptionnal | expectedClass
		-1L   || "LOT"  | "Lot Label"  | "Lot"  | null                  | true        | CustomFieldModelFactory.SingleValuedCustomFieldModel.class
		-4L   || "RICH" | "Rich Label" | "Rich" | "large default value" | false       | CustomFieldModelFactory.SingleValuedCustomFieldModel.class
		-5L   || "DATE" | "Date Label" | "Date" | "2017-09-18"          | true        | CustomFieldModelFactory.DatePickerFieldModel.class

	}

	@Unroll
	def "should fetch correct SSF cuf models "() {
		when:
		def cufMap = customFieldModelService.findCufMap([-1L, -2L, -3L, -4L, -5L, -6L])

		then:
		CustomFieldModelFactory.SingleSelectFieldModel customFieldModel = cufMap.get(cufId) as CustomFieldModelFactory.SingleSelectFieldModel
		customFieldModel.id == cufId
		customFieldModel.code == code
		customFieldModel.label == label
		customFieldModel.defaultValue == defaultValue
		customFieldModel.isOptional() == isOptionnal
		customFieldModel.class == expectedClass
		customFieldModel.options.collect { it.code }.sort() == optionCodes.sort()
		customFieldModel.options.collect { it.label }.sort() == optionLabels.sort()

		where:

		cufId || code      | label           | name      | defaultValue | isOptionnal | optionCodes                                  | optionLabels                                 | expectedClass
		-2L   || "LISTE"   | "Liste Label"   | "Liste"   | "Option1"    | false       | ["OPTION1", "OPTION2", "OPTION3", "OPTION4"] | ["Option1", "Option2", "Option3", "Option4"] | CustomFieldModelFactory.SingleSelectFieldModel.class
		-3L   || "LISTE_2" | "Liste Label 2" | "Liste 2" | "Option2"    | true        | ["OPTION1", "OPTION2"]                       | ["Option1", "Option2"]                       | CustomFieldModelFactory.SingleSelectFieldModel.class

	}

	def "should fetch correct MSF cuf models "() {
		when:
		def cufMap = customFieldModelService.findCufMap([-1L, -2L, -3L, -4L, -5L, -6L])

		then:
		CustomFieldModelFactory.MultiSelectFieldModel customFieldModel = cufMap.get(cufId) as CustomFieldModelFactory.MultiSelectFieldModel
		customFieldModel.id == cufId
		customFieldModel.code == code
		customFieldModel.label == label
		customFieldModel.defaultValue.sort() as Set == defaultValue.sort() as Set
		customFieldModel.isOptional() == isOptionnal
		customFieldModel.class == expectedClass
		customFieldModel.options.collect { it.code }.sort() == optionCodes.sort()
		customFieldModel.options.collect { it.label }.sort() == optionLabels.sort()

		where:

		cufId || code  | label       | name   | defaultValue    | isOptionnal | optionCodes  | optionLabels            | expectedClass
		-6L   || "TAG" | "Tag Label" | "Tags" | ["lol", "titi"] | false       | ["", "", ""] | ["lol", "toto", "titi"] | CustomFieldModelFactory.MultiSelectFieldModel.class

	}

	def "should find custom field binding by projects"() {
		when:
		def bindingsByProject = customFieldModelService.findCustomFieldsBindingsByProject([-1L,-2L,-3L])

		then:
		bindingsByProject.get(-1L).get("TEST_CASE").collect{it.id}.sort() == [-1L]
		bindingsByProject.get(-1L).get("TEST_CASE").find {it.id == -1L}.renderingLocations.collect{it.enumName}.sort() == []
		bindingsByProject.get(-1L).get("REQUIREMENT_VERSION").collect{it.id}.sort() == [-3L,-2L]
		bindingsByProject.get(-1L).get("TEST_STEP").collect{it.id}.sort() == [-5L,-4L]
		bindingsByProject.get(-1L).get("TEST_STEP").find {it.id == -4L}.renderingLocations.collect{it.enumName}.sort() == ["STEP_TABLE","TEST_PLAN"]
		bindingsByProject.get(-1L).get("TEST_STEP").find {it.id == -5L}.renderingLocations.collect{it.enumName}.sort() == ["STEP_TABLE"]

		bindingsByProject.get(-2L).get("TEST_CASE").collect{it.id}.sort() == [-7L]
		bindingsByProject.get(-2L).get("REQUIREMENT_VERSION").collect{it.id}.sort() == [-6L]
		bindingsByProject.get(-2L).get("TEST_STEP").collect{it.id}.sort() == []

		bindingsByProject.get(-3L).get("TEST_CASE").collect{it.id}.sort() == []
		bindingsByProject.get(-3L).get("REQUIREMENT_VERSION").collect{it.id}.sort() == []
		bindingsByProject.get(-3L).get("TEST_STEP").collect{it.id}.sort() == []

	}

}
