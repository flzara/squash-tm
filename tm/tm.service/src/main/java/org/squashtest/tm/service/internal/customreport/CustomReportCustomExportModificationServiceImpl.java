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

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.customreport.CustomReportCustomExport;
import org.squashtest.tm.service.customreport.CustomReportCustomExportModificationService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;

import javax.inject.Inject;
import javax.transaction.Transactional;

@Service
@Transactional
public class CustomReportCustomExportModificationServiceImpl implements CustomReportCustomExportModificationService {

	@Inject
	private CustomReportLibraryNodeService reportLibraryNodeService;

	@Override
	public void updateCustomExport(Long customExportId, CustomReportCustomExport updatedCustomExport) {
		CustomReportCustomExport customExport = reportLibraryNodeService.findCustomExportByNodeId(customExportId);
		if(!customExport.getName().equals(updatedCustomExport.getName())) {
			reportLibraryNodeService.renameNode(customExportId, updatedCustomExport.getName());
		}
		customExport.setScope(updatedCustomExport.getScope());
		customExport.setColumns(updatedCustomExport.getColumns());
	}
}
