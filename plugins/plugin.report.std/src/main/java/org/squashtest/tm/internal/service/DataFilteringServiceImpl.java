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
package org.squashtest.tm.internal.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.ProjectResource;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.plugin.report.std.service.DataFilteringService;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;


@Service("squashtest.tm.service.DataFilteringService")
@Transactional(readOnly = true)
public class DataFilteringServiceImpl implements DataFilteringService {
	@Inject
	private PermissionEvaluationService permissionService;

	@Inject
	private ProjectFilterModificationService userFilterService;

	@Override
	public boolean isFullyAllowed(Object object) {
		return hasReadPermissions(object)
			// TODO extract methot out of that crap so its understandable
			&& (object instanceof ProjectResource ?
			isAllowedByUser((ProjectResource) object)
			: true //will prolly change that later.
		);
	}


	@Override
	public boolean hasReadPermissions(Object object) {
		return permissionService.canRead(object);
	}

	@Override
	public boolean isAllowedByUser(ProjectResource object) {
		ProjectFilter filter = userFilterService.findProjectFilterByUserLogin();

		return !filter.getActivated() || filter.isProjectSelected(object.getProject());

	}

}
