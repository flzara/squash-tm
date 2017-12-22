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
package org.squashtest.it.stub.milestone;


import javax.persistence.EntityManager
import javax.persistence.PersistenceContext;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import java.util.Optional



public class StubActiveMilestoneHolder implements ActiveMilestoneHolder {

	@PersistenceContext
	EntityManager entityManager

	private Optional<Milestone> activeMilestone = Optional.empty();
	private Optional<JsonMilestone> activeJsonMilestone = Optional.empty();
	@Override
	public Optional<Milestone> getActiveMilestone() {

		return activeMilestone;
	}


	@Override
	public void setActiveMilestone(Long milestoneId) {

		Milestone milestone
		if (milestoneId != null){
			milestone = entityManager.find(Milestone.class, milestoneId)
		}
		activeMilestone = Optional.ofNullable(milestone);
	}

	@Override
	public void clearContext() {
		activeMilestone = activeMilestone.empty();

	}

	@Override
	Optional<Long> getActiveMilestoneId() {
		return null
	}
}
