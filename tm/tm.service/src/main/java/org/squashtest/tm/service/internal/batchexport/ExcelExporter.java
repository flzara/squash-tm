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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CoverageModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CustomField;
import org.squashtest.tm.service.internal.batchexport.ExportModel.DatasetModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.ParameterModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestStepModel;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.CoverageSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.DatasetSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.ParameterSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.StepSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TestCaseSheetColumn;
import org.squashtest.tm.service.internal.dto.NumericCufHelper;

/**
 * @author bsiri
 *
 */
@Component
@Scope("prototype")
class ExcelExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelExporter.class);

	private static final String DS_SHEET = TemplateWorksheet.DATASETS_SHEET.sheetName;
	private static final String PRM_SHEET = TemplateWorksheet.PARAMETERS_SHEET.sheetName;
	private static final String ST_SHEET = TemplateWorksheet.STEPS_SHEET.sheetName;
	protected static final String TC_SHEET = TemplateWorksheet.TEST_CASES_SHEET.sheetName;

	private static final String COV_SHEET = TemplateWorksheet.COVERAGE_SHEET.sheetName;
	// that map will remember which column index is
	private Map<String, Integer> cufColumnsByCode = new HashMap<>();

	protected Workbook workbook;

	protected boolean milestonesEnabled;

	private MessageSource messageSource;

	private String errorCellTooLargeMessage;


	private static final List<CoverageSheetColumn> COVERAGE_COLUMNS = Arrays.asList(
		CoverageSheetColumn.REQ_PATH,
		CoverageSheetColumn.REQ_VERSION_NUM,
		CoverageSheetColumn.TC_PATH);

	private static final List<DatasetSheetColumn> DS_COLUMNS = Arrays.asList(
		DatasetSheetColumn.TC_OWNER_PATH,
		DatasetSheetColumn.TC_OWNER_ID,
		DatasetSheetColumn.TC_DATASET_ID,
		DatasetSheetColumn.TC_DATASET_NAME,
		DatasetSheetColumn.TC_PARAM_OWNER_PATH,
		DatasetSheetColumn.TC_PARAM_OWNER_ID,
		DatasetSheetColumn.TC_DATASET_PARAM_NAME,
		DatasetSheetColumn.TC_DATASET_PARAM_VALUE);

	private static final List<ParameterSheetColumn> PRM_COLUMNS = Arrays.asList(
		ParameterSheetColumn.TC_OWNER_PATH,
		ParameterSheetColumn.TC_OWNER_ID,
		ParameterSheetColumn.TC_PARAM_ID,
		ParameterSheetColumn.TC_PARAM_NAME,
		ParameterSheetColumn.TC_PARAM_DESCRIPTION);

	private static final List<StepSheetColumn> ST_COLUMNS = Arrays.asList(StepSheetColumn.TC_OWNER_PATH,
		StepSheetColumn.TC_OWNER_ID,
		StepSheetColumn.TC_STEP_ID,
		StepSheetColumn.TC_STEP_NUM,
		StepSheetColumn.TC_STEP_IS_CALL_STEP,
		StepSheetColumn.TC_STEP_CALL_DATASET,
		StepSheetColumn.TC_STEP_ACTION,
		StepSheetColumn.TC_STEP_EXPECTED_RESULT,
		StepSheetColumn.TC_STEP_NB_REQ,
		StepSheetColumn.TC_STEP_NB_ATTACHMENT);


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
		TestCaseSheetColumn.TC_LAST_MODIFIED_BY};

	private static final List<TestCaseSheetColumn> TC_COLUMNS_MILESTONES = new ArrayList<>(Arrays.asList(ArrayUtils.add(BASIC_TC_COLUMNS, 7, TestCaseSheetColumn.TC_MILESTONE)));

	private static final List<TestCaseSheetColumn> TC_COLUMNS = Arrays.asList(BASIC_TC_COLUMNS);


	@Inject
	public ExcelExporter(FeatureManager featureManager, MessageSource messageSource) {
		super();
		milestonesEnabled = featureManager.isEnabled(Feature.MILESTONE);

		createWorkbook();
		createHeaders();

	}

	void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
		errorCellTooLargeMessage = this.messageSource.getMessage("test-case.export.errors.celltoolarge", null, LocaleContextHolder.getLocale());
	}

	public void appendToWorkbook(ExportModel model, boolean keepRteFormat) {

		if (!keepRteFormat) {
			removeRteFormat(model);
		}
		appendTestCases(model);
		appendTestSteps(model);
		appendParameters(model);
		appendDatasets(model);
		appendCoverage(model);
	}

	private void removeRteFormat(ExportModel model) {
		removeRteFormatFromParameters(model.getParameters());
		removeRteFormatFromTestCases(model.getTestCases());
		removeRteFormatFromTestSteps(model.getTestSteps());
	}

	private void removeRteFormatFromTestSteps(List<TestStepModel> testSteps) {
		for (TestStepModel ts : testSteps) {
			ts.setAction(removeHtml(ts.getAction()));
			ts.setResult(removeHtml(ts.getResult()));
		}
	}

	private void removeRteFormatFromTestCases(List<TestCaseModel> testCases) {
		for (TestCaseModel tc : testCases) {
			tc.setDescription(removeHtml(tc.getDescription()));
			tc.setPrerequisite(removeHtml(tc.getPrerequisite()));
		}
	}

	private void removeRteFormatFromParameters(List<ParameterModel> parameters) {
		for (ParameterModel param : parameters) {
			param.setDescription(removeHtml(param.getDescription()));
		}
	}

	private String removeHtml(String html) {
		if(StringUtils.isBlank(html)){
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

	private void appendTestCases(ExportModel model) {
		List<TestCaseModel> models = model.getTestCases();
		Sheet tcSheet = workbook.getSheet(TC_SHEET);
		Row r;
		int rIdx = tcSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (TestCaseModel tcm : models) {

			r = tcSheet.createRow(rIdx);

			try {
				r.createCell(cIdx++).setCellValue(tcm.getProjectId());
				r.createCell(cIdx++).setCellValue(tcm.getProjectName());
				r.createCell(cIdx++).setCellValue(tcm.getPath());
				r.createCell(cIdx++).setCellValue(tcm.getOrder());
				r.createCell(cIdx++).setCellValue(tcm.getId());
				r.createCell(cIdx++).setCellValue(tcm.getReference());
				r.createCell(cIdx++).setCellValue(tcm.getName());
				if (milestonesEnabled) {
					r.createCell(cIdx++).setCellValue(tcm.getMilestone());
				}
				r.createCell(cIdx++).setCellValue(tcm.getWeightAuto());
				r.createCell(cIdx++).setCellValue(tcm.getWeight().toString());
				r.createCell(cIdx++).setCellValue(tcm.getNature().getCode());
				r.createCell(cIdx++).setCellValue(tcm.getType().getCode());
				r.createCell(cIdx++).setCellValue(tcm.getStatus().toString());
				r.createCell(cIdx++).setCellValue(HtmlUtils.htmlUnescape(tcm.getDescription()));
				r.createCell(cIdx++).setCellValue(HtmlUtils.htmlUnescape(tcm.getPrerequisite()));
				r.createCell(cIdx++).setCellValue(tcm.getNbReq());
				r.createCell(cIdx++).setCellValue(tcm.getNbCaller());
				r.createCell(cIdx++).setCellValue(tcm.getNbAttachments());
				r.createCell(cIdx++).setCellValue(format(tcm.getCreatedOn()));
				r.createCell(cIdx++).setCellValue(tcm.getCreatedBy());
				r.createCell(cIdx++).setCellValue(format(tcm.getLastModifiedOn()));
				r.createCell(cIdx++).setCellValue(tcm.getLastModifiedBy());

				appendCustomFields(r, "TC_CUF_", tcm.getCufs());
				cIdx = doOptionnalAppendTestCases(r, cIdx, tcm);

			} catch (IllegalArgumentException wtf) {
				if (LOGGER.isWarnEnabled()){
					LOGGER.warn("cannot export content for test case '"+tcm.getId()+"' : some data exceed the maximum size of an excel cell");
				}
				if (LOGGER.isTraceEnabled()){
					LOGGER.trace("",wtf);
				}
				tcSheet.removeRow(r);
				r = tcSheet.createRow(rIdx);

				r.createCell(0).setCellValue(errorCellTooLargeMessage);

			}
			rIdx++;
			cIdx = 0;
		}
	}

	protected int doOptionnalAppendTestCases(Row r, int cIdx, TestCaseModel tcm) {
		//extension point for optional columns
		return cIdx;
	}

	private void appendTestSteps(ExportModel model) {

		List<TestStepModel> models = model.getTestSteps();
		Sheet stSheet = workbook.getSheet(ST_SHEET);

		Row r;
		int rIdx = stSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (TestStepModel tsm : models) {

			r = stSheet.createRow(rIdx);

			try {
				r.createCell(cIdx++).setCellValue(tsm.getTcOwnerPath());
				r.createCell(cIdx++).setCellValue(tsm.getTcOwnerId());
				r.createCell(cIdx++).setCellValue(tsm.getId());
				r.createCell(cIdx++).setCellValue(tsm.getOrder());
				r.createCell(cIdx++).setCellValue(tsm.getIsCallStep());
				r.createCell(cIdx++).setCellValue(tsm.getDsName());
				r.createCell(cIdx++).setCellValue(HtmlUtils.htmlUnescape(tsm.getAction()));
				r.createCell(cIdx++).setCellValue(HtmlUtils.htmlUnescape(tsm.getResult()));
				r.createCell(cIdx++).setCellValue(tsm.getNbReq());
				r.createCell(cIdx++).setCellValue(tsm.getNbAttach());

				appendCustomFields(r, "TC_STEP_CUF_", tsm.getCufs());
			} catch (IllegalArgumentException wtf) {
				if (LOGGER.isWarnEnabled()){
					LOGGER.warn("cannot export content for test step '"+tsm.getId()+"' : some data exceed the maximum size of an excel cell");
				}
				if (LOGGER.isTraceEnabled()){
					LOGGER.trace("",wtf);
				}
				stSheet.removeRow(r);
				r = stSheet.createRow(rIdx);

				r.createCell(0).setCellValue(errorCellTooLargeMessage);

			}

			rIdx++;
			cIdx = 0;
		}
	}

	private void appendParameters(ExportModel model) {

		List<ParameterModel> models = model.getParameters();
		Sheet pSheet = workbook.getSheet(PRM_SHEET);

		Row r;
		int rIdx = pSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (ParameterModel pm : models) {
			r = pSheet.createRow(rIdx);

			try {
				r.createCell(cIdx++).setCellValue(pm.getTcOwnerPath());
				r.createCell(cIdx++).setCellValue(pm.getTcOwnerId());
				r.createCell(cIdx++).setCellValue(pm.getId());
				r.createCell(cIdx++).setCellValue(pm.getName());
				r.createCell(cIdx++).setCellValue(HtmlUtils.htmlUnescape(pm.getDescription()));
			} catch (IllegalArgumentException wtf) {

				if (LOGGER.isWarnEnabled()){
					LOGGER.warn("cannot export content for parameter '"+pm.getId()+"' : some data exceed the maximum size of an excel cell");
				}
				if (LOGGER.isTraceEnabled()){
					LOGGER.trace("",wtf);
				}
				pSheet.removeRow(r);
				r = pSheet.createRow(rIdx);

				r.createCell(0).setCellValue(errorCellTooLargeMessage);

			}

			rIdx++;
			cIdx = 0;
		}
	}

	private void appendDatasets(ExportModel model) {

		List<DatasetModel> models = model.getDatasets();
		Sheet dsSheet = workbook.getSheet(DS_SHEET);

		Row r;
		int rIdx = dsSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (DatasetModel dm : models) {
			r = dsSheet.createRow(rIdx);

			try {
				r.createCell(cIdx++).setCellValue(dm.getTcOwnerPath());
				r.createCell(cIdx++).setCellValue(dm.getOwnerId());
				r.createCell(cIdx++).setCellValue(dm.getId());
				r.createCell(cIdx++).setCellValue(dm.getName());
				r.createCell(cIdx++).setCellValue(dm.getParamOwnerPath());
				r.createCell(cIdx++).setCellValue(dm.getParamOwnerId());
				r.createCell(cIdx++).setCellValue(dm.getParamName());
				r.createCell(cIdx++).setCellValue(dm.getParamValue());
			} catch (IllegalArgumentException wtf) {
				if (LOGGER.isWarnEnabled()){
					LOGGER.warn("cannot export content for dataset '"+dm.getId()+"' : some data exceed the maximum size of an excel cell");
				}
				if (LOGGER.isTraceEnabled()){
					LOGGER.trace("",wtf);
				}
				dsSheet.removeRow(r);
				r = dsSheet.createRow(rIdx);

				r.createCell(0).setCellValue(errorCellTooLargeMessage);

			}

			rIdx++;
			cIdx = 0;
		}

	}

	private void appendCustomFields(Row r, String codePrefix, List<CustomField> cufs) {

		for (CustomField cuf : cufs) {

			String code = codePrefix + cuf.getCode();
			Integer idx = cufColumnsByCode.get(code);

			// if unknown : register it
			if (idx == null) {
				idx = registerCuf(r.getSheet(), code);
			}

			Cell c = r.createCell(idx);
			String value = nullSafeValue(cuf);
			if (cuf.getType().equals(InputType.NUMERIC)){
				value = NumericCufHelper.formatOutputNumericCufValue(value);
			}
			c.setCellValue(value);
		}
	}


	private void appendCoverage(ExportModel model) {
		List<CoverageModel> models = model.getCoverages();
		Sheet covSheet = workbook.getSheet(COV_SHEET);

		Row r;
		int rIdx = covSheet.getLastRowNum() + 1;
		int cIdx = 0;

		for (CoverageModel cm : models) {
			r = covSheet.createRow(rIdx);

			r.createCell(cIdx++).setCellValue(cm.getReqPath());
			r.createCell(cIdx++).setCellValue(cm.getReqVersion());
			r.createCell(cIdx++).setCellValue(cm.getTcPath());

			rIdx++;
			cIdx = 0;
		}

	}


	private String nullSafeValue(CustomField customField) {
		String value = customField.getValue();
		return value == null ? "" : value;
	}

	private int registerCuf(Sheet sheet, String code) {

		Row headers = sheet.getRow(0);
		int nextIdx = headers.getLastCellNum();
		headers.createCell(nextIdx).setCellValue(code);

		cufColumnsByCode.put(code, nextIdx);

		return nextIdx;
	}

	private String format(Date date) {
		if (date == null) {
			return "";
		} else {
			return DateUtils.formatIso8601Date(date);
		}
	}

	// for now we care only of Excel 2003
	private void createWorkbook() {
		Workbook wb = new HSSFWorkbook();
		wb.createSheet(TC_SHEET);
		wb.createSheet(ST_SHEET);
		wb.createSheet(PRM_SHEET);
		wb.createSheet(DS_SHEET);
		wb.createSheet(COV_SHEET);
		this.workbook = wb;
	}

	private void createHeaders() {

		createTestCaseSheetHeaders();
		createStepSheetHeaders();
		createParameterSheetHeaders();
		createDatasetSheetHeaders();
		createCoverageSheetHeaders();

	}

	private void createCoverageSheetHeaders() {
		createSheetHeaders(COV_SHEET, COVERAGE_COLUMNS);
	}

	private void createSheetHeaders(String sheetName, List<? extends TemplateColumn> cols) {
		Sheet dsSheet = workbook.getSheet(sheetName);
		Row h = dsSheet.createRow(0);
		int cIdx = 0;
		for (TemplateColumn t : cols) {
			h.createCell(cIdx++).setCellValue(t.getHeader());
		}
	}

	private void createDatasetSheetHeaders() {
		createSheetHeaders(DS_SHEET, DS_COLUMNS);
	}

	private void createParameterSheetHeaders() {
		createSheetHeaders(PRM_SHEET, PRM_COLUMNS);
	}

	private void createStepSheetHeaders() {
		createSheetHeaders(ST_SHEET, ST_COLUMNS);
	}

	private void createTestCaseSheetHeaders() {

		List<TestCaseSheetColumn> columns = milestonesEnabled ? TC_COLUMNS_MILESTONES : TC_COLUMNS;
		createSheetHeaders(TC_SHEET, columns);
		createOptionalTestCaseSheetHeaders();
	}

	protected void createOptionalTestCaseSheetHeaders() {
		//extension point for optionnal columns
	}

}
