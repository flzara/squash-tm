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
package org.squashtest.tm.web.internal.controller.audittrail;

import org.springframework.context.MessageSource
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.domain.event.RequirementCreation
import org.squashtest.tm.domain.event.RequirementLargePropertyChange
import org.squashtest.tm.service.audit.RequirementAuditTrailService
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters
import org.squashtest.tm.web.internal.model.datatable.DataTableModel
import org.springframework.data.domain.Page

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class RequirementAuditTrailControllerTest extends Specification {
	RequirementAuditTrailController controller = new RequirementAuditTrailController()
	RequirementAuditTrailService requirementAuditTrailService = Mock()
	InternationalizationHelper i18nHelper = Mock()

	def setup() {
		controller.auditTrailService = requirementAuditTrailService
		controller.i18nHelper = i18nHelper
	}

	def "should return an audit event table model for the requested requirement"() {
		given:
		Locale locale = Locale.JAPANESE
		DataTableDrawParameters drawParams = Mock()

		and:
		RequirementCreation event = Mock()
		Page page = Mock()
		page.content >> [event]
		requirementAuditTrailService.findAllByRequirementVersionIdOrderedByDate(10L, _) >> page

		when:
		DataTableModel model =  controller.getEventsTableModel(10L, drawParams, locale)

		then:
		model.aaData.size() == 1
	}
	def "should return an audit event"() {
		given:
		RequirementLargePropertyChange event = Mock()
		event.propertyName >> "shoe size"
		event.oldValue >> "10.5"
		event.newValue >> "13"
		requirementAuditTrailService.findLargePropertyChangeById(10L) >> event

		when:
		def res =  controller.getLargePropertyChangeEvent(10L)

		then:
		res
		res.propertyName == event.propertyName
		res.oldValue == event.oldValue
		res.newValue == event.newValue
	}
}
