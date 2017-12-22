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
package org.squashtest.tm.web.internal.controller.search.advanced.searchinterface;

import javax.inject.Provider

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.domain.requirement.RequirementCriticality
import org.squashtest.tm.web.internal.controller.search.advanced.searchinterface.SearchInterfaceDescription.OptionBuilder
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper

import spock.lang.Specification


/**
 * @author Gregory Fouquet
 *
 */
class SearchInterfaceDescriptionTest extends Specification {
	SearchInterfaceDescription searchInterfaceDescription = new SearchInterfaceDescription() {}
	LevelLabelFormatter levelFormatter = Mock()
	Provider levelFormatterProvider = Mock()
	InternationalizableLabelFormatter internationalizableFormatter = Mock()
	Provider internationalizableFormatterProvider = Mock()
	InternationalizationHelper messageSource = Mock()

	def "should create level combo intermediate model"() {
		given:
		def expectedKeys = RequirementCriticality.values().collect { it.level + "-" + it.name() } as Set

		and:
		levelFormatter.formatLabel(_) >> "formatted label"
		levelFormatter._ >> levelFormatter
		levelFormatterProvider.get() >> levelFormatter

		use(ReflectionCategory) {
			SearchInterfaceDescription.set field: "levelLabelFormatter", of: searchInterfaceDescription, to: levelFormatterProvider
		}

		when:
		def model = searchInterfaceDescription.delegateLevelComboBuilder(RequirementCriticality.values()).useLocale(Locale.JAPANESE).buildMap()

		then:
		model.keySet() == expectedKeys
	}

	def "should create level combo model"() {
		given:
		def sortedCrits = RequirementCriticality.values().sort({a, b -> a.level - b.level})
		def expectedKeys = sortedCrits.collect { it.level + "-" + it.name() }
		def expectedLabels = sortedCrits.collect { it.name() }

		and:
		levelFormatter.formatLabel(_) >> { Enum it ->it.name() }
		levelFormatter._ >> levelFormatter
		levelFormatterProvider.get() >> levelFormatter

		use(ReflectionCategory) {
			SearchInterfaceDescription.set field: "levelLabelFormatter", of: searchInterfaceDescription, to: levelFormatterProvider
		}

		when:
		def model = searchInterfaceDescription.levelComboBuilder(RequirementCriticality.values()).useLocale(Locale.JAPANESE).build()

		then:
		model*.code == expectedKeys
		model*.value == expectedLabels
	}

	def "should create internationalizable combo model"() {
		given:
		def expectedKeys = RequirementCriticality.values().sort({ a, b -> a.name().compareTo(b.name()) }).collect { it.name() }

		and:
		internationalizableFormatter.formatLabel(_) >> { Enum it -> it.name().toLowerCase() }
		internationalizableFormatter._ >> internationalizableFormatter
		internationalizableFormatterProvider.get() >> internationalizableFormatter

		use(ReflectionCategory) {
			SearchInterfaceDescription.set field: "internationalizableLabelFormatter", of: searchInterfaceDescription, to: internationalizableFormatterProvider
		}

		when:
		def model = searchInterfaceDescription.internationalizableComboBuilder(RequirementCriticality.values()).useLocale(Locale.JAPANESE).build()

		then:
		model*.code == expectedKeys
		model*.value == expectedKeys.collect { it.toLowerCase() }
	}

	def "should build option"() {
		given:
		OptionBuilder optionBuilder = searchInterfaceDescription.optionBuilder(Locale.JAPANESE);

		when:
		def foo = optionBuilder.label("foo").optionKey("F").selected().build()
		def bar = optionBuilder.label("bar").optionKey("B").build()

		then:
		foo.value == "foo"
		foo.code == "F"
		foo.selected
		bar.value == "bar"
		bar.code == "B"
		!bar.selected
	}
}
