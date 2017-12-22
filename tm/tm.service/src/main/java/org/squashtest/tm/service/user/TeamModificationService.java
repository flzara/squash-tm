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
package org.squashtest.tm.service.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.service.security.Authorizations;

/**
 * Dynamically generated methods for {@link Team} modification.
 * @author mpagnon
 *
 */
@Transactional
@PreAuthorize(Authorizations.HAS_ROLE_ADMIN)
@DynamicManager(name = "squashtest.tm.service.TeamModificationService", entity = Team.class)
public interface TeamModificationService extends CustomTeamModificationService, TeamFinderService {

	/**
	 * Simply updates the description of the team of the given id
	 *
	 * @param teamId
	 * @param description
	 */
	void changeDescription(long teamId, String description);

}
