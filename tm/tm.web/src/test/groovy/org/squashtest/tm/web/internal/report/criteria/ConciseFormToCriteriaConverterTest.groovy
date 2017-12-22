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
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.CheckboxInput;
import org.squashtest.tm.api.report.form.CheckboxesGroup;
import org.squashtest.tm.api.report.form.DropdownList;
import org.squashtest.tm.api.report.form.Input;
import org.squashtest.tm.api.report.form.InputType;
import org.squashtest.tm.api.report.form.OptionInput;
import org.squashtest.tm.api.report.form.RadioButtonsGroup;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.web.internal.report.criteria.FormToCriteriaConverter;
import org.apache.commons.lang3.time.DateUtils;

import spock.lang.Specification;

/**
 * @author Gregory Fouquet
 *
 */
class ConciseFormToCriteriaConverterTest extends Specification {
	Report report
	ConciseFormToCriteriaConverter converter

	def setup() {
		report = Mock()
		report.form >> []
		converter = new ConciseFormToCriteriaConverter(report, [])
	}

	def "should build string criteria"() {
		when:
		Map expanded = converter.expand([batman: [val: "leatherpants", type: "TEXT"]])

		then:
		expanded == [batman: [value: "leatherpants", type: "TEXT"]]
	}

	def "should build checkbox criteria"() {
		when:
		Map expanded = converter.expand([cbx: [val: true,  type: "CHECKBOX"]])

		then:
		expanded == [cbx: [value: "", selected: true,  type: "CHECKBOX"]]
	}

	def "should build radio criteria"() {
		given:
		def opts = []
		OptionInput opt = Mock()
		opt.value >> "spandex tights"
		opts << opt

		opt = Mock()
		opt.value >> "leatherpants"
		opts << opt

		and:
		RadioButtonsGroup input = new RadioButtonsGroup()
		input.setName("outfit")
		input.setOptions(opts)
		report = Mock()
		report.form >> [input]

		and:
		converter = new ConciseFormToCriteriaConverter(report, [])

		when:
		Map expanded = converter.expand([outfit: [val: "spandex tights", type: "RADIO_BUTTONS_GROUP"]])

		then:
		expanded == [outfit: [
				[value: "spandex tights", type: "RADIO_BUTTONS_GROUP", selected: true],
				[value: "leatherpants", type: "RADIO_BUTTONS_GROUP", selected: false]
			]]
	}
	def "should build dropdown criteria"() {
		given:
		def opts = []
		OptionInput opt = Mock()
		opt.value >> "spandex tights"
		opts << opt

		opt = Mock()
		opt.value >> "leatherpants"
		opts << opt

		and:
		DropdownList input = new DropdownList()
		input.setName("outfit")
		input.setOptions(opts)
		report = Mock()
		report.form >> [input]

		and:
		converter = new ConciseFormToCriteriaConverter(report, [])

		when:
		Map expanded = converter.expand([outfit:
			[val: "spandex tights", type: "DROPDOWN_LIST"],
		])

		then:
		expanded == [outfit: [
				[value: "spandex tights", type: "DROPDOWN_LIST", selected: true],
				[value: "leatherpants", type: "DROPDOWN_LIST", selected: false]
			]]
	}
	def "should build checkboxes group criteria"() {
		given:
		def opts = []
		OptionInput opt = Mock()
		opt.value >> "batarang"
		opts << opt

		opt = Mock()
		opt.value >> "webshooters"
		opts << opt

		opt = Mock()
		opt.value >> "utility-belt"
		opts << opt

		and:
		CheckboxesGroup input = new CheckboxesGroup()
		input.setName("equipment")
		input.setOptions(opts)
		report = Mock()
		report.form >> [input]

		and:
		converter = new ConciseFormToCriteriaConverter(report, [])

		when:
		Map expanded = converter.expand([equipment:
			[val: ["batarang", "utility-belt"], type: "CHECKBOXES_GROUP"]
		])

		then:
		expanded == [equipment: [
				[value: "batarang", type: "CHECKBOXES_GROUP", selected: true],
				[value: "webshooters", type: "CHECKBOXES_GROUP", selected: false],
				[value: "utility-belt", type: "CHECKBOXES_GROUP", selected: true],
			]]
	}

	def "should build date criteria"() {
		when:
		Map expanded = converter.expand([batman: [val: "2014-02-01", type: "DATE"]])

		then:
		expanded == [batman: [value: "2014-02-01", type: "DATE"]]
	}

	def "should build no date criteria"() {
		when:
		Map expanded = converter.expand([batman: [val: "--", type: "DATE"]])

		then:
		expanded == [batman: [value: "--", type: "DATE"]]
	}

	def "should build nodes criteria"() {
		when:
		Map expanded = converter.expand([nodes:
			[val: [
					[resid: 10, restype: "campaigns"],
					[resid: 20, restype: "folders"],
					[resid: 30, restype: "folders"]
				], type: "TREE_PICKER"]
		])

		then:
		expanded == [nodes: [
				[value: 10, nodeType: "campaigns", type: "TREE_PICKER"],
				[value: 20, nodeType: "folders", type: "TREE_PICKER"],
				[value: 30, nodeType: "folders", type: "TREE_PICKER"]
			]]
	}

	def "should not build nodes criteria when no nodes selected"() {
		when:
		Map expanded = converter.expand([nodes:
			[val: [], type: "TREE_PICKER"]
		])

		then:
		expanded == [:]
	}

	def "should build projects criteria"() {
		given:
		List projects = []

		Project p = Mock()
		p.id >> 10L
		projects << p

		p = Mock()
		p.id >> 20L
		projects << p

		p = Mock()
		p.id >> 30L
		projects << p

		and:
		converter = new ConciseFormToCriteriaConverter(report, projects)

		when:
		Map expanded = converter.expand([projects:
			[val: ["10", "30"], type: "PROJECT_PICKER"]
		])

		then:
		expanded == [projects: [
				[value: "10", selected: true, type: "PROJECT_PICKER"],
				[value: "20", selected: false, type: "PROJECT_PICKER"],
				[value: "30", selected: true, type: "PROJECT_PICKER"]
			]]
	}
}
