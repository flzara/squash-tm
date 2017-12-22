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
package org.squashtest.tm.service.audit

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.squashtest.tm.core.foundation.collection.Paging
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.event.RequirementCreation
import org.squashtest.tm.service.internal.audit.RequirementAuditTrailServiceImpl
import org.squashtest.tm.service.internal.repository.RequirementAuditEventDao

import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class RequirementAuditTrailServiceImplTest extends Specification {
	RequirementAuditTrailServiceImpl service = new RequirementAuditTrailServiceImpl()
	RequirementAuditEventDao dao = Mock()
	
	def setup() {
		service.auditEventDao = dao
	}
	
	def "should return an correctly paged resultset"() {
		given:
		Paging paging = Mock() 
		paging.firstItemIndex >> 0
		paging.pageSize >> 2
		
		and :
		PageRequest request = new PageRequest(0,2)
		
		and : 
		def events = [new RequirementCreation(), new RequirementCreation()] 
		Page<RequirementAuditEvent> pageres = Mock()
		pageres.getNumber() >> 0
		pageres.getSize() >> 2
		pageres.getTotalElements() >> 20L
		pageres.getContent() >> events
		
		and:
		dao.findAllByRequirementVersionIdOrderByDateDesc(10L, request) >> pageres
		
		
		when:
		def res = service.findAllByRequirementVersionIdOrderedByDate(10L, request)
		
		then:
                // note : this test becomes more irrelevant now that Spring Data pagination API is 
                // in use. Perhaps we should just remove that test altogether.
		res == pageres
		
	}
	
}
