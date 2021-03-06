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
package org.squashtest.tm.service.internal.requirement;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.requirement.RequirementVersionResolverService;

import javax.inject.Inject;

import static org.squashtest.tm.service.security.Authorizations.READ_REQUIREMENT_OR_ROLE_ADMIN;

@Transactional
@Service("squashtest.tm.service.RequirementVersionResolverService")
public class RequirementVersionResolverServiceImpl implements RequirementVersionResolverService{

	@Inject
	private RequirementVersionDao versionDao;


	@Override
	@Transactional(readOnly = true)
	@PreAuthorize(READ_REQUIREMENT_OR_ROLE_ADMIN)
	public RequirementVersion resolveByRequirementId(long requirementId) {
		return versionDao.findByRequirementIdAndMilestone(requirementId);
	}

}
