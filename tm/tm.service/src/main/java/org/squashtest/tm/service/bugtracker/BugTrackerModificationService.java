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
package org.squashtest.tm.service.bugtracker;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;

@Transactional
@DynamicManager(name = "squashtest.tm.service.BugTrackerModificationService", entity = BugTracker.class)
public interface BugTrackerModificationService extends CustomBugTrackerModificationService {


	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeIframeFriendly(long bugtrackerId, boolean isIframeFriendly);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeKind(long bugtrackerId, String kind);

	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeUrl(long bugtrackerId, String url);
	
	@PreAuthorize(HAS_ROLE_ADMIN)
	void changeAuthenticationPolicy(long bugtrackerId, AuthenticationPolicy policy);

}
