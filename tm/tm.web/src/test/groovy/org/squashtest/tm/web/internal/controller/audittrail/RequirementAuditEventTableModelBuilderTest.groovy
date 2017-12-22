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
package org.squashtest.tm.web.internal.controller.audittrail

import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.domain.event.RequirementAuditEvent
import org.squashtest.tm.domain.event.RequirementCreation
import org.squashtest.tm.domain.event.RequirementLargePropertyChange
import org.squashtest.tm.domain.event.RequirementPropertyChange
import org.squashtest.tm.domain.requirement.RequirementStatus
import org.squashtest.tm.domain.requirement.RequirementVersion
import org.squashtest.tm.web.internal.helper.LabelFormatter
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel

import spock.lang.Specification
/**
 * @author Gregory Fouquet
 *
 */
class RequirementAuditEventTableModelBuilderTest extends Specification {
	InternationalizationHelper i18nHelper = Mock()
	Locale locale = Locale.JAPANESE
	LabelFormatter statusFormatter = Mock()
	RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(locale, i18nHelper)

	def "should build item for RequirementCreation event"() {
		given:
		RequirementVersion req = Mock()
		RequirementCreation event = new RequirementCreation(req, "chris jericho")
		setIdAndDate(event)

		and:
		PagedCollectionHolder paged = pagedCollection(event)

		and:
		i18nHelper.internationalize(_,locale) >> "Création"

		when:
		DataTableModel model = builder.buildDataModel(paged, "wooo")

		then:
		model.getAaData() == [			
				["event-date" : "31/12/2011 23h55",
				"event-author" : "chris jericho",
				"event-message" : "Cr&eacute;ation",
				"event-type" : "creation",
				"event-id" : "10"]
			
		]
	}

	def "should build item for Requirement small property change event"() {
		given:
		RequirementVersion req = Mock()
		RequirementPropertyChange event = RequirementPropertyChange.builder()
				.setModifiedProperty("reference")
				.setSource(req)
				.setAuthor("peter parker")
				.setOldValue("amazing")
				.setNewValue("astonishing")
				.build()

		setIdAndDate(event)

		and:
		PagedCollectionHolder paged = pagedCollection(event)

		and:
		i18nHelper.getMessage(_,["amazing", "astonishing"],locale) >> "Modification de reference : 'amazing' -> 'astonishing'"

		when:
		DataTableModel model = builder.buildDataModel(paged, "wooo")

		then:
		model.getAaData() == [
				["event-date" : "31/12/2011 23h55",
				"event-author" : "peter parker",
				"event-message" : "Modification de reference : &#39;amazing&#39; -&gt; &#39;astonishing&#39;",
				"event-type" : "simple-prop",
				"event-id" : "10"]
		]
	}

	def "should build item for Requirement fat property change event"() {
		given:
		RequirementVersion req = Mock()
		RequirementLargePropertyChange event = RequirementLargePropertyChange.builder()
				.setModifiedProperty("description")
				.setSource(req)
				.setAuthor("peter parker")
				.setOldValue("amazing")
				.setNewValue("astonishing")
				.build()

		setIdAndDate(event)

		and:
		PagedCollectionHolder paged = pagedCollection(event)


		and:
		i18nHelper.internationalize(_,locale) >> "Modification de la description"

		when:
		DataTableModel model = builder.buildDataModel(paged, "wooo")

		then:
		model.getAaData() == [
				["event-date" : "31/12/2011 23h55",
				"event-author" : "peter parker",
				"event-message" : "Modification de la description",
				"event-type" : "fat-prop",
				"event-id" : "10"]
		]
	}

	def setIdAndDate(def event) {
		use(ReflectionCategory) {
			RequirementAuditEvent.set field: "id", of: event, to: 10L

			Calendar cal = new GregorianCalendar(2011, Calendar.DECEMBER, 31, 23, 55, 00, 00)
			RequirementAuditEvent.set field: "date", of: event, to: cal.time
		}
	}

	def pagedCollection(def event) {
		PagedCollectionHolder paged = Mock()
		paged.pagedItems >> [event]
		paged.firstItemIndex >> 1
		paged.totalNumberOfItems >> 10

		return paged
	}

	def "should build item for Requirement status property change event"() {
		given:
		RequirementVersion req = Mock()
		RequirementPropertyChange event = RequirementPropertyChange.builder()
				.setModifiedProperty("status")
				.setSource(req)
				.setAuthor("peter parker")
				.setOldValue("OBSOLETE")
				.setNewValue("APPROVED")
				.build()

		setIdAndDate(event)

		and:
		PagedCollectionHolder paged = Mock()
		paged.pagedItems >> [event]
		paged.firstItemIndex >> 1
		paged.totalNumberOfItems >> 10

		and:
		i18nHelper.internationalize(RequirementStatus.OBSOLETE, locale) >> "Obs"
		i18nHelper.internationalize(RequirementStatus.APPROVED, locale) >> "App"
		i18nHelper.getMessage(_, ["Obs", "App"], locale) >> "Modification du status : 'Obsolète' -> 'Approuvé'"

		when:
		DataTableModel model = builder.buildDataModel(paged, "wooo")

		then:
		
		model.getAaData() == [
				["event-date" : "31/12/2011 23h55",
				"event-author" : "peter parker",
				"event-message" : "Modification du status : &#39;Obsol&egrave;te&#39; -&gt; &#39;Approuv&eacute;&#39;",
				"event-type" : "simple-prop",
				"event-id" : "10"]
		]
	}
	
	
}
