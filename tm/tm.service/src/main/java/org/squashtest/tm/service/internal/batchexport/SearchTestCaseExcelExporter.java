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

import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn;
import org.squashtest.tm.service.testcase.TestCaseFinder;

@Component
@Scope("prototype")
public class SearchTestCaseExcelExporter extends ExcelExporter{

	@Inject
	private TestCaseFinder testCaseFinder;
	
	@Inject
	IterationModificationService iterationFinder;
	
	private static final TestCaseSheetColumn[] SEARCH_TC_COLUMNS = {
		TestCaseSheetColumn.TC_NB_STEPS,
		TestCaseSheetColumn.TC_NB_ITERATION
		};
	
	private static final TestCaseSheetColumn MILESTONE_SEARCH_TC_COLUMNS = 
			TestCaseSheetColumn.TC_NB_MILESTONES;

	
	@Inject
	public SearchTestCaseExcelExporter(FeatureManager featureManager,
			MessageSource messageSource) {
		super(featureManager, messageSource);
	}
	
	@Override
	protected void createOptionalTestCaseSheetHeaders() {
		Sheet dsSheet = workbook.getSheet(TC_SHEET);
		Row h = dsSheet.getRow(0);
		int cIdx = h.getLastCellNum();
		if (milestonesEnabled) {
			h.createCell(cIdx++).setCellValue(MILESTONE_SEARCH_TC_COLUMNS.getHeader());
		}
		
		for (TemplateColumn t : SEARCH_TC_COLUMNS){
			h.createCell(cIdx++).setCellValue(t.getHeader());	
		}
	}
	
	@Override
	protected int doOptionnalAppendTestCases(Row r, int cIdx, TestCaseModel tcm) {
		TestCase tc = testCaseFinder.findById(tcm.getId());
		int nbMilestone = tc.getMilestones().size();
		int nbSteps = tc.getSteps().size();
		int nbIteration = iterationFinder.findIterationContainingTestCase(tcm.getId()).size();
		int cIdxOptional = cIdx;
		if (milestonesEnabled) {
			r.createCell(cIdxOptional++).setCellValue(nbMilestone);
		}
		r.createCell(cIdxOptional++).setCellValue(nbSteps);
		r.createCell(cIdxOptional++).setCellValue(nbIteration);
		return cIdxOptional;
	}

}
