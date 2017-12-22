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
package org.squashtest.tm.service.internal.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.event.*;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.event.RequirementAuditor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Audits Requirement events and persists them according to the Requirement's
 * status.
 *
 * @author Gregory Fouquet
 *
 */
@Service
public class StatusBasedRequirementAuditor implements RequirementAuditor,
	RequirementAuditEventVisitor {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatusBasedRequirementAuditor.class);

	/**
	 *  we do not use a dao here because :
	 *  1. we only need SessionFactory.persist(...)
	 *  2. injecting a dao might induce circular refs through the usage of aspects
	 */
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public void notify(RequirementAuditEvent event) {
		event.accept(this);
	}

	@Override
	public void visit(RequirementCreation event) {
		entityManager.persist(event);
		logEvent(event);

	}

	private void logEvent(RequirementCreation event) {
		LOGGER.trace("Requirement was created");
	}

	@Override
	public void visit(RequirementPropertyChange event) {
		if (shouldAuditModification(event)) {
			entityManager.persist(event);
			logEvent(event);
		}

	}

	private void logEvent(RequirementVersionModification event) {
		LOGGER.trace("Requirement was modified");
	}

	private boolean shouldAuditModification(RequirementVersionModification event) {
		if ("status".equals(event.getPropertyName())) {
			return true;
		}
		return RequirementStatus.UNDER_REVIEW == event.getRequirementVersion().getStatus();
	}

	@Override
	public void visit(RequirementLargePropertyChange event) {
		if (shouldAuditModification(event)) {
			entityManager.persist(event);
			logEvent(event);
		}
	}


	@Override
	public void visit(SyncRequirementCreation event) {
		// NOOP
	}

	@Override
	public void visit(SyncRequirementUpdate event) {
		// NOOP
	}
}
