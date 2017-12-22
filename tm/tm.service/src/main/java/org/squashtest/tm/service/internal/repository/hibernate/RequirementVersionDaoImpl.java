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
package org.squashtest.tm.service.internal.repository.hibernate;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.internal.repository.CustomRequirementVersionDao;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;

import java.util.Optional;

/**
 * 
 * @author Gregory Fouquet
 * 
 */

public class RequirementVersionDaoImpl implements CustomRequirementVersionDao {
	@PersistenceContext
	private EntityManager em;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	private Session currentSession() {
		return em.unwrap(Session.class);
	}


	@Override
	public Requirement findRequirementById(long requirementId) {
		return (Requirement)currentSession().load(Requirement.class, requirementId);
	}

	@Override
	public RequirementVersion findByRequirementIdAndMilestone(long requirementId) {

		Optional<Milestone> active = activeMilestoneHolder.getActiveMilestone();

		if (active.isPresent()) {
			Query q = currentSession().getNamedQuery("requirementVersion.findVersionByRequirementAndMilestone");
			q.setParameter("requirementId", requirementId);
			q.setParameter("milestoneId", active.get().getId());
			return (RequirementVersion) q.uniqueResult();
		}
		else{
			Query q = currentSession().getNamedQuery("requirementVersion.findLatestRequirementVersion");
			q.setParameter("requirementId", requirementId);
			return (RequirementVersion)q.uniqueResult();
		}
	}


}
