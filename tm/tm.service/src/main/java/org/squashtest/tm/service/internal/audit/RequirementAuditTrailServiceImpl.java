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
package org.squashtest.tm.service.internal.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.event.RequirementLargePropertyChange;
import org.squashtest.tm.service.audit.RequirementAuditTrailService;
import org.squashtest.tm.service.internal.repository.RequirementAuditEventDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Gregory Fouquet
 *
 */
@Service("squashtest.tm.service.audit.RequirementAuditTrailService")
public class RequirementAuditTrailServiceImpl implements RequirementAuditTrailService {
	@Inject
	private RequirementAuditEventDao auditEventDao;


	@PersistenceContext
	private EntityManager em;

	/**
	 * @see org.squashtest.tm.service.audit.RequirementAuditTrailService#findAllByRequirementVersionIdOrderedByDate(long,
	 *      org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<RequirementAuditEvent> findAllByRequirementVersionIdOrderedByDate(
		long requirementVersionId, Pageable pageable) {

		return auditEventDao.findAllByRequirementVersionIdOrderByDateDesc(
			requirementVersionId, pageable);
	}

	@Override
	public Page<RequirementAuditEvent> findAllByRequirementVersionIdOrderedByDate(long requirementVersionId) {
            Pageable pageable = new PageRequest(0, Integer.MAX_VALUE);
            return findAllByRequirementVersionIdOrderedByDate(requirementVersionId, pageable);
	}

	/**
	 * @see org.squashtest.tm.service.audit.RequirementAuditTrailService#findLargePropertyChangeById(long)
	 */
	@Override
	public RequirementLargePropertyChange findLargePropertyChangeById(long eventId) {
		return em.find(RequirementLargePropertyChange.class, eventId);
	}


}
