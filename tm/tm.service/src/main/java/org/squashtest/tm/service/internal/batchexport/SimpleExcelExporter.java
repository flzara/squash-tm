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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.service.campaign.IterationModificationService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.internal.batchexport.ExportModel.*;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.*;
import org.squashtest.tm.service.internal.dto.NumericCufHelper;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author bflessel
 */
@Component
@Scope("prototype")
class SimpleExcelExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExcelExporter.class);

	private static final String DATA_EXCEED_MAX_CELL_SIZE_MESSAGE = "' : some data exceed the maximum size of an excel cell";

	protected static final String TC_SHEET = TemplateWorksheet.TEST_CASES_SHEET.sheetName;
	protected static final String STATUS = "requirement.status.";
	protected static final String IMPORTANCE = "test-case.importance.";


	protected Workbook workbook;

	protected boolean milestonesEnabled;

	private MessageSource messageSource;

	private String errorCellTooLargeMessage;

	private static final TestCaseSheetColumn[] BASIC_TC_COLUMNS = {TestCaseSheetColumn.PROJECT_ID,
		TestCaseSheetColumn.PROJECT_NAME,
		TestCaseSheetColumn.TC_PATH,
		TestCaseSheetColumn.TC_NUM,
		TestCaseSheetColumn.TC_ID,
		TestCaseSheetColumn.TC_REFERENCE,
		TestCaseSheetColumn.TC_NAME,
		TestCaseSheetColumn.TC_WEIGHT_AUTO,
		TestCaseSheetColumn.TC_WEIGHT,
		TestCaseSheetColumn.TC_NATURE,
		TestCaseSheetColumn.TC_TYPE,
		TestCaseSheetColumn.TC_STATUS,
		TestCaseSheetColumn.TC_DESCRIPTION,
		TestCaseSheetColumn.TC_PRE_REQUISITE,
		TestCaseSheetColumn.TC_NB_REQ,
		TestCaseSheetColumn.TC_NB_CALLED_BY,
		TestCaseSheetColumn.TC_NB_ATTACHMENT,
		TestCaseSheetColumn.TC_CREATED_ON,
		TestCaseSheetColumn.TC_CREATED_BY,
		TestCaseSheetColumn.TC_LAST_MODIFIED_ON,
		TestCaseSheetColumn.TC_LAST_MODIFIED_BY,
		TestCaseSheetColumn.TC_KIND,
		TestCaseSheetColumn.TC_SCRIPTING_LANGUAGE,
		TestCaseSheetColumn.TC_SCRIPT
	};



	@Inject
	public SimpleExcelExporter(FeatureManager featureManager, MessageSource messageSource) {
		super();
		milestonesEnabled = featureManager.isEnabled(Feature.MILESTONE);
		getMessageSource(messageSource);

	}

	@PostConstruct // So these methods are not called directly by constructor
	public void init() {
		createWorkbook();
		createHeaders();
	}

	void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
		errorCellTooLargeMessage = this.messageSource.getMessage("test-case.export.errors.celltoolarge", null, LocaleContextHolder.getLocale());
	}

	public void simpleAppendToWorkbook(ExportModel model, boolean keepRteFormat) {

		if (!keepRteFormat) {
			removeRteSimpleFormat(model);
		}
		appendSimpleTestCases(model);
	}

	private void removeRteSimpleFormat(ExportModel model) {
		removeRteFormatFromTestCases(model.getTestCases());
	}

	private void removeRteFormatFromTestCases(List<TestCaseModel> testCases) {
		for (TestCaseModel tc : testCases) {
			tc.setDescription(removeHtml(tc.getDescription()));
			tc.setPrerequisite(removeHtml(tc.getPrerequisite()));
			for (CustomField cf : tc.getCufs()) {
				cf.setValue(removeHtml(cf.getValue()));
			}
		}
	}

	private String removeHtml(String html) {
		if (StringUtils.isBlank(html)) {
			return "";
		}
		return html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
	}

	public File print() {
		try {
			File temp = File.createTempFile("tc_export_", "xls");
			temp.deleteOnExit();

			FileOutputStream fos = new FileOutputStream(temp);
			workbook.write(fos);
			fos.close();

			return temp;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}


	private void appendSimpleTestCases(ExportModel model) {
		List<TestCaseModel> models = model.getTestCases();
		Sheet tcSheet = workbook.getSheet(TC_SHEET);
		Row r;
		int rIdx = tcSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (TestCaseModel tcm : models) {

			r = tcSheet.createRow(rIdx);

			try {
				r.createCell(cIdx++).setCellValue(tcm.getProjectName());
				r.createCell(cIdx++).setCellValue(tcm.getId());
				r.createCell(cIdx++).setCellValue(tcm.getReference());
				r.createCell(cIdx++).setCellValue(tcm.getName());
				r.createCell(cIdx++).setCellValue(IMPORTANCE+tcm.getWeight().toString());
				r.createCell(cIdx++).setCellValue(getMessage(tcm.getNature().getLabel()));
				r.createCell(cIdx++).setCellValue(getMessage(tcm.getType().getLabel()));
				r.createCell(cIdx++).setCellValue(STATUS+tcm.getStatus().toString());
				if (milestonesEnabled) {
					r.createCell(cIdx++).setCellValue(tcm.getMilestone());
				}
				r.createCell(cIdx++).setCellValue(tcm.getNbReq());
				r.createCell(cIdx++).setCellValue(tcm.getNbCaller());
				r.createCell(cIdx++).setCellValue(tcm.getNbAttachments());
				r.createCell(cIdx++).setCellValue(tcm.getNbIterations());
				r.createCell(cIdx++).setCellValue(tcm.getCreatedBy());
				r.createCell(cIdx++).setCellValue(tcm.getLastModifiedBy());


			} catch (IllegalArgumentException wtf) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("cannot export content for test case '" + tcm.getId() + DATA_EXCEED_MAX_CELL_SIZE_MESSAGE);
				}
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("", wtf);
				}
				tcSheet.removeRow(r);
				r = tcSheet.createRow(rIdx);

				r.createCell(0).setCellValue(errorCellTooLargeMessage);

			}
			rIdx++;
			cIdx = 0;
		}
	}



	// for now we care only of Excel 2003
	private void createWorkbook() {
		Workbook wb = new HSSFWorkbook();
		wb.createSheet(TC_SHEET);
		this.workbook = wb;
	}

	private void createHeaders() {

		createTcSimpleSheetHeaders(TC_SHEET);


	}

	private void createTcSimpleSheetHeaders(String sheetName) {
		Sheet dsSheet = workbook.getSheet(sheetName);
		Row h = dsSheet.createRow(0);
		int cIdx = 0;
		h.createCell(cIdx++).setCellValue(getMessage("label.project"));
		h.createCell(cIdx++).setCellValue("ID");
		h.createCell(cIdx++).setCellValue(getMessage("label.reference"));
		h.createCell(cIdx++).setCellValue(getMessage("label.Label"));
		h.createCell(cIdx++).setCellValue(getMessage("test-case.importance.label"));
		h.createCell(cIdx++).setCellValue(getMessage("test-case.nature.label"));
		h.createCell(cIdx++).setCellValue(getMessage("test-case.type.label"));
		h.createCell(cIdx++).setCellValue(getMessage("test-case.status.label"));
		if (milestonesEnabled) {
			h.createCell(cIdx++).setCellValue(getMessage("label.milestoneNb"));
		}
		h.createCell(cIdx++).setCellValue(getMessage("label.numberOfAssociatedRequirements"));
		h.createCell(cIdx++).setCellValue(getMessage("label.numberOfTestSteps"));
		h.createCell(cIdx++).setCellValue(getMessage("label.numberOfAssociatedIterations"));
		h.createCell(cIdx++).setCellValue(getMessage("label.numberOfAttachments"));
		h.createCell(cIdx++).setCellValue(getMessage("label.createdBy"));
		h.createCell(cIdx++).setCellValue(getMessage("label.modifiedBy"));

	}


	public void getMessageSource(MessageSource source){
		this.messageSource = source;

	}

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}
}