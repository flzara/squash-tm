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
package org.squashtest.tm.service.internal.customreport;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.service.customreport.CustomReportFolderService;
import org.squashtest.tm.service.internal.repository.CustomReportFolderDao;

@Service("org.squashtest.tm.service.customreport.CustomReportFolderService")
public class CustomReportFolderServiceImpl implements
		CustomReportFolderService {
	
	@Inject
	private CustomReportFolderDao folderDao;
	
	@Override
	@PreAuthorize("hasPermission(#entityId, 'org.squashtest.tm.domain.customreport.CustomReportFolder' ,'WRITE') "
			+ OR_HAS_ROLE_ADMIN)
	public void updateDescription(Long entityId, String newDescription) {
		CustomReportFolder crf = folderDao.findById(entityId);
		crf.setDescription(newDescription);
	}
}
