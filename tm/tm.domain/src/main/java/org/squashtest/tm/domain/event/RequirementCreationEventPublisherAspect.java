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
package org.squashtest.tm.domain.event;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * This aspect advises a RequirementVersion's state change from transient to persistent and raises a creation event.
 *
 * FIXME probably doesn't work since jpa / spring data migration
 *
 * @author Gregory Fouquet
 * @since 1.4.0  11/04/16 (port from .aj file)
 */
@Aspect
public class RequirementCreationEventPublisherAspect extends AbstractRequirementEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementCreationEventPublisherAspect.class);

	@Pointcut("call(public void org.hibernate.Session+.persist(Object)) && args(requirement)")
	private void callRequirementPersister(Requirement requirement) {
		// NOOP
	}

	@Pointcut("call(public void org.hibernate.Session+.persist(Object)) && args(requirementVersion)")
	private void callRequirementVersionPersister(RequirementVersion requirementVersion) {
		// NOOP
	}

	@After("callRequirementPersister(requirement)")
	public void listenRequirementCreation(Requirement requirement) {
		if (aspectIsEnabled()) {
			RequirementCreation event = new RequirementCreation(requirement.getCurrentVersion(), currentUser());
			publish(event);
			LOGGER.trace("Creation event raised for current version");
		}
	}

	@After("callRequirementVersionPersister(requirementVersion)")
	public void listenRequirementVersionCreation(RequirementVersion requirementVersion) {
		if (aspectIsEnabled()) {
			RequirementCreation event = new RequirementCreation(requirementVersion, currentUser());
			publish(event);
			LOGGER.trace("Creation event raised for version");
		}
	}
}
