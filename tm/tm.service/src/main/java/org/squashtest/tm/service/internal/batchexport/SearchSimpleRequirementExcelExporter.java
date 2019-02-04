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

import org.apache.poi.ss.usermodel.Row;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel.RequirementModel;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn;
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;

import javax.inject.Inject;

/**
 * @author bflessel
 */

@Component
@Scope("prototype")
public class SearchSimpleRequirementExcelExporter extends RequirementSearchExcelExporter{

	@Inject
	private RequirementLibraryNavigationService nav;

	@Inject
	private RequirementVersionManagerService requirementVersionManagerService;


	private static final RequirementSheetColumn[] MILESTONE_SEARCH_REQ_COLUMNS = {
		RequirementSheetColumn.REQ_VERSION_NB_MILESTONE
	};

	@Inject
	public SearchSimpleRequirementExcelExporter(FeatureManager featureManager,
												MessageSource messageSource) {
		super(featureManager, messageSource);
	}
	
	@Override
	protected int doOptionalCreateSheetHeader(Row h, int cIdx) {
		return cIdx;
}

	@Override
	protected int doOptionnalAppendRequirement(Row row, int colIndex,
											   RequirementModel reqModel) {
		return colIndex;
	}

}
