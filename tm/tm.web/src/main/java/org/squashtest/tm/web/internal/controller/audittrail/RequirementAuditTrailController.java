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

import java.util.Locale;

import javax.inject.Inject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.event.ChangedProperty;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;

/**
 * This controller handles requests related to a requirement's audit trail (ie its collection of
 * {@link RequirementAuditEvent})
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/audit-trail/requirement-versions")
public class RequirementAuditTrailController {
	@Inject
	private RequirementAuditTrailService auditTrailService;
	
	@Inject
	private InternationalizationHelper i18nHelper;


	@RequestMapping(value = "{requirementVersionId}/events-table", params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getEventsTableModel(@PathVariable long requirementVersionId, DataTableDrawParameters drawParams,
			Locale locale) {
            
            Pageable pageable = SpringPagination.pageable(drawParams);
            
            Page<RequirementAuditEvent> auditTrail = auditTrailService
                            .findAllByRequirementVersionIdOrderedByDate(requirementVersionId, pageable);

            RequirementAuditEventTableModelBuilder builder = new RequirementAuditEventTableModelBuilder(locale,
                            i18nHelper);

            return builder.buildDataModel(auditTrail, drawParams.getsEcho());
	}
	
	@RequestMapping(value="fat-prop-change-events/{eventId}") @ResponseBody
	public ChangedProperty getLargePropertyChangeEvent(@PathVariable long eventId) {
		final RequirementLargePropertyChange event = auditTrailService.findLargePropertyChangeById(eventId);
		
		return new ChangedPropertyJsonDecorator(event);
	}
}
