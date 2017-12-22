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
package org.squashtest.tm.web.internal.report.criteria;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.spockframework.compiler.model.Spec;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.InputType;
import org.squashtest.tm.web.internal.report.criteria.FormToCriteriaConverter;
import org.apache.commons.lang3.time.DateUtils;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class FormToCriteriaConverterTest extends Specification {
	FormToCriteriaConverter converter = new FormToCriteriaConverter()
	
	def "should build string criteria"() {
		when:
		Map criteria = converter.convert([batman: [value: "leatherpants", type: "TEXT"]])
		
		then:
		criteria.batman.name == "batman"
		criteria.batman.value == "leatherpants"
		criteria.batman.sourceInput == InputType.TEXT
	} 
	def "should build checkbox criteria"() {
		when:
		Map criteria = converter.convert([cbx: [value: "", selected: true,  type: "CHECKBOX"]])
		
		then:
		criteria.cbx.name == "cbx"
		criteria.cbx.value == true
		criteria.cbx.sourceInput == InputType.CHECKBOX
	} 
	def "should build radio criteria"() {
		when:
		Map criteria = converter.convert([outfit: [[value: "spandex tights", type: "RADIO_BUTTONS_GROUP", selected: true], [value: "leatherpants", type: "RADIO_BUTTONS_GROUP", selected: false]]])
		
		then:
		criteria.outfit.name == "outfit"
		criteria.outfit.value == "spandex tights"
		criteria.outfit.sourceInput == InputType.RADIO_BUTTONS_GROUP
	} 
	def "should build dropdown criteria"() {
		when:
		Map criteria = converter.convert([outfit: [[value: "spandex tights", type: "DROPDOWN_LIST", selected: true], [value: "leatherpants", type: "DROPDOWN_LIST", selected: false]]])
		
		then:
		criteria.outfit.name == "outfit"
		criteria.outfit.value == "spandex tights"
		criteria.outfit.sourceInput == InputType.DROPDOWN_LIST
	} 
	def "should build checkboxes group criteria"() {
		when:
		Map criteria = converter.convert([equipment: [
			[value: "batarang", type: "CHECKBOXES_GROUP", selected: true], 
			[value: "webshooters", type: "CHECKBOXES_GROUP", selected: false], 
			[value: "utility-belt", type: "CHECKBOXES_GROUP", selected: true], 
		]])
		
		then:
		criteria.equipment.name == "equipment"
		criteria.equipment.value == ["batarang", "utility-belt"]
		
		criteria.equipment.sourceInput == InputType.CHECKBOXES_GROUP
		criteria.equipment.isSelected("batarang") == true
		criteria.equipment.isSelected("webshooters") == false
		criteria.equipment.isSelected("utility-belt") == true
	} 
	
	def "should build date criteria"() {
		given: 
		Calendar c = Calendar.instance
		c.clear()
		c.set(1974,06,31)
		
		when:
		Map criteria = converter.convert([batman: [value: "1974-07-31", type: "DATE"]])
		
		then:
		criteria.batman.name == "batman"
		DateUtils.truncate(criteria.batman.value, Calendar.DATE) == DateUtils.truncate(c.time, Calendar.DATE)
		criteria.batman.sourceInput == InputType.DATE
	} 
	
	def "should build no date criteria"() {
		when:
		Map criteria = converter.convert([batman: [value: "--", type: "DATE"]])
		
		then:
		criteria.batman.name == "batman"
		criteria.batman.value == Criteria.NO_VALUE
		criteria.batman.sourceInput == InputType.DATE
	} 
	
	def "should build nodes criteria"() {
		when:
		Map criteria = converter.convert([nodes: [
			[value: 10, nodeType: "campaigns", type: "TREE_PICKER"], 
			[value: 20, nodeType: "folders", type: "TREE_PICKER"],
			[value: 30, nodeType: "folders", type: "TREE_PICKER"]
		]])
		
		then:
		criteria.nodes.name == "nodes"
		criteria.nodes.value.campaigns == [10]
		criteria.nodes.value.folders == [20,30]
		criteria.nodes.sourceInput == InputType.TREE_PICKER
	} 
	
	def "should build projects criteria"() {
		when:
		Map criteria = converter.convert([projects: [
			[value: 10, selected: true, type: "PROJECT_PICKER"], 
			[value: 20, selected: false, type: "PROJECT_PICKER"],
			[value: 30, selected: true, type: "PROJECT_PICKER"]
		]])
		
		then:
		criteria.projects.name == "projects"
		criteria.projects.selectedOptions == [10, 30]
		criteria.projects.sourceInput == InputType.PROJECT_PICKER
	} 
}
