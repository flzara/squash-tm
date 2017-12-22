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
package org.squashtest.tm.service.internal.batchexport;

import java.util.List;

import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Row;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel.RequirementModel;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;

@Component
@Scope("prototype")
public class SearchRequirementExcelExporter extends RequirementExcelExporter{

	@Inject
	private RequirementLibraryNavigationService nav;
	
	@Inject
	private RequirementVersionManagerService requirementVersionManagerService;
	
	private static final RequirementSheetColumn[] SEARCH_REQ_COLUMNS = {
		RequirementSheetColumn.REQ_VERSIONS,
	};
	
	private static final RequirementSheetColumn[] MILESTONE_SEARCH_REQ_COLUMNS = {
		RequirementSheetColumn.REQ_VERSION_NB_MILESTONE
	};
	
	@Inject
	public SearchRequirementExcelExporter(FeatureManager featureManager,
			MessageSource messageSource) {
		super(featureManager, messageSource);
	}
	
	@Override
	protected int doOptionalCreateSheetHeader(Row h, int cIdx) {
		int columnIndexOptional = cIdx;
		if (milestonesEnabled) {
			for (TemplateColumn t : MILESTONE_SEARCH_REQ_COLUMNS){
				h.createCell(columnIndexOptional++).setCellValue(t.getHeader());
			}
		}
		for (TemplateColumn t : SEARCH_REQ_COLUMNS){
			h.createCell(columnIndexOptional++).setCellValue(t.getHeader());
		}
		return columnIndexOptional;
	}

	@Override
	protected int doOptionnalAppendRequirement(Row row, int colIndex,
			RequirementModel reqModel) {
		int columnIndexOptional = colIndex;
		Requirement req = nav.findRequirement(reqModel.getRequirementId());
		List<RequirementVersion> requirementVersions = req.getRequirementVersions();
		
		if (milestonesEnabled) {
			RequirementVersion requirementVersion = requirementVersionManagerService.findById(reqModel.getId());
			row.createCell(columnIndexOptional++).setCellValue(requirementVersion.getMilestones().size());
		}
		
		row.createCell(columnIndexOptional++).setCellValue(requirementVersions.size());
		return columnIndexOptional;
	}
}
