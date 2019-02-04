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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CustomField;
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel.RequirementModel;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateColumn;
import org.squashtest.tm.service.internal.batchimport.testcase.excel.TemplateWorksheet;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author bflessel
 */
@Component
@Scope("prototype")
public class RequirementSearchExcelExporter {
	private static final String REQUIREMENT_SHEET = TemplateWorksheet.REQUIREMENT_SHEET.sheetName;
	private static final String CRITICALITY = "requirement.criticality.";
	private static final String CATEGORY = "requirement.category.";
	private static final String STATUS = "requirement.status.";

	private static final RequirementSheetColumn[] BASIC_REQ_COLUMNS = {
		RequirementSheetColumn.PROJECT_NAME,
		RequirementSheetColumn.REQ_ID,
		RequirementSheetColumn.REQ_VERSION_REFERENCE,
		RequirementSheetColumn.REQ_VERSION_NAME,
		RequirementSheetColumn.REQ_VERSION_CRITICALITY,
		RequirementSheetColumn.REQ_VERSION_CATEGORY,
		RequirementSheetColumn.REQ_VERSION_STATUS,
		RequirementSheetColumn.REQ_VERSION_NUM,
		RequirementSheetColumn.REQ_VERSIONS,
		RequirementSheetColumn.REQ_VERSION_NB_TC,
		RequirementSheetColumn.REQ_VERSION_NB_ATTACHEMENT,
		RequirementSheetColumn.REQ_VERSION_CREATED_BY,
		RequirementSheetColumn.REQ_VERSION_LAST_MODIFIED_BY};


	private static final List<RequirementSheetColumn> REQUIREMENT_COLUMNS_MILESTONES = Arrays.asList(ArrayUtils.add(BASIC_REQ_COLUMNS, RequirementSheetColumn.REQ_VERSION_MILESTONE));

	private static final List<RequirementSheetColumn> REQUIREMENT_COLUMNS = Arrays.asList(BASIC_REQ_COLUMNS);
	// that map will remember which column index is
	private Map<String, Integer> cufColumnsByCode = new HashMap<>();

	private Workbook workbook;

	protected boolean milestonesEnabled;

	@Inject
	private MessageSource messageSource;

	private String errorCellTooLargeMessage;

	@Inject
	private RequirementVersionManagerService requirementVersionManagerService;

	@Inject
	public RequirementSearchExcelExporter(FeatureManager featureManager, MessageSource messageSource) {
		super();
		milestonesEnabled = featureManager.isEnabled(Feature.MILESTONE);
		getMessageSource(messageSource);
		createWorkbook();
		createHeaders();

	}

	void setMessageSource(MessageSource messageSource) {

		this.messageSource = messageSource;
		errorCellTooLargeMessage = this.messageSource.getMessage("test-case.export.errors.celltoolarge", null, LocaleContextHolder.getLocale());

	}



	public void appendToWorkbook(RequirementExportModel model, boolean keepRteFormat) {
		if (!keepRteFormat) {
			removeRteFormat(model);
		}
		sort(model);
		appendRequirementModel(model);
	}

	private void sort(RequirementExportModel model) {
		Collections.sort(model.getRequirementsModels(), RequirementModel.COMPARATOR);
	}



	private void appendRequirementModel(RequirementExportModel model) {
		List<RequirementModel> models = model.getRequirementsModels();
		Sheet reqSheet = workbook.getSheet(REQUIREMENT_SHEET);
		int rowIndex = reqSheet.getLastRowNum() + 1;

		for (RequirementModel reqModel : models) {
			appendOneRequirement(reqSheet, rowIndex, reqModel);
			rowIndex++;
		}

	}


	private void createSheetHeaders(String sheetName, List<? extends TemplateColumn> cols) {
		Sheet dsSheet = workbook.getSheet(sheetName);
		Row h = dsSheet.createRow(0);
		int cIdx = 0;
		h.createCell(cIdx++).setCellValue(getMessage("label.project"));
		h.createCell(cIdx++).setCellValue("ID");
		h.createCell(cIdx++).setCellValue(getMessage("label.reference"));
		h.createCell(cIdx++).setCellValue(getMessage("label.Label"));
		h.createCell(cIdx++).setCellValue(getMessage("requirement.criticality.label"));
		h.createCell(cIdx++).setCellValue(getMessage("requirement.category.label"));
		h.createCell(cIdx++).setCellValue(getMessage("label.Status"));
		if (milestonesEnabled) {
			h.createCell(cIdx++).setCellValue(getMessage("label.milestoneNb"));
		}
		h.createCell(cIdx++).setCellValue(getMessage("label.version"));
		h.createCell(cIdx++).setCellValue(getMessage("label.numberOfVersions"));
		h.createCell(cIdx++).setCellValue(getMessage("label.numberOfTestCases"));
		h.createCell(cIdx++).setCellValue(getMessage("label.numberOfAttachments"));
		h.createCell(cIdx++).setCellValue(getMessage("label.createdBy"));
		h.createCell(cIdx++).setCellValue(getMessage("label.modifiedBy"));

		//call extension point and get the new column index in return
		doOptionalCreateSheetHeader(h, cIdx);
	}

	protected int doOptionalCreateSheetHeader(Row h, int cIdx) {//NOSONAR this is an extension point
		// Extension point for additional export columns (example : search columns)
		return cIdx;
	}

	private void createRequirementHeaders() {
		List<RequirementSheetColumn> columns = REQUIREMENT_COLUMNS;
		createSheetHeaders(REQUIREMENT_SHEET, columns);
	}

	private void appendOneRequirement(Sheet reqSheet, int rowIndex,
									  RequirementModel reqModel) {
		Row row = reqSheet.createRow(rowIndex);
		int colIndex = 0;

		try {
			String criticality = handleMessages(CRITICALITY+reqModel.getCriticality().toString());
			String category = handleMessages(CATEGORY+reqModel.getCategoryCode());
			String status = handleMessages(STATUS+reqModel.getStatus().toString());
			row.createCell(colIndex++).setCellValue(reqModel.getProjectName());
			row.createCell(colIndex++).setCellValue(reqModel.getRequirementId());
			row.createCell(colIndex++).setCellValue(reqModel.getReference());
			row.createCell(colIndex++).setCellValue(reqModel.getName());
			row.createCell(colIndex++).setCellValue(criticality);
			row.createCell(colIndex++).setCellValue(category);
			row.createCell(colIndex++).setCellValue(status);
			if (milestonesEnabled) {
				RequirementVersion requirementVersion = requirementVersionManagerService.findById(reqModel.getId());
				row.createCell(colIndex++).setCellValue(requirementVersion.getMilestones().size());
			}
			row.createCell(colIndex++).setCellValue(reqModel.getRequirementVersionNumber());
			row.createCell(colIndex++).setCellValue(reqModel.getRequirementVersionNumberSize());
			row.createCell(colIndex++).setCellValue(reqModel.getRequirementVersionCoveragesSize());
			row.createCell(colIndex++).setCellValue(reqModel.getAttachmentListSize());
			row.createCell(colIndex++).setCellValue(reqModel.getCreatedBy());
			row.createCell(colIndex++).setCellValue(reqModel.getLastModifiedBy());

			//call extension point and get the new column index in return
			colIndex = doOptionnalAppendRequirement(row, colIndex, reqModel);
		} catch (IllegalArgumentException wtf) {
			reqSheet.removeRow(row);
			row = reqSheet.createRow(rowIndex);
			row.createCell(0).setCellValue(errorCellTooLargeMessage);
		}
	}

	protected int doOptionnalAppendRequirement(Row row, int colIndex, RequirementModel reqModel) {//NOSONAR this is an extension point
		// Extension point for additional export columns (example : search columns)
		return colIndex;
	}

	private void removeRteFormat(RequirementExportModel model) {
		removeRteFormatFromRequirement(model.getRequirementsModels());
	}

	private void removeRteFormatFromRequirement(
		List<RequirementModel> requirementsModels) {
		for (RequirementModel requirementModel : requirementsModels) {
			requirementModel.setDescription(removeHtml(requirementModel.getDescription()));
			for (CustomField cf : requirementModel.getCufs()) {
				cf.setValue(removeHtml(cf.getValue()));
			}
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
			File temp = File.createTempFile("req_export_", "xls");
			temp.deleteOnExit();

			FileOutputStream fos = new FileOutputStream(temp);
			workbook.write(fos);
			fos.close();

			return temp;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	// for now we care only of Excel 2003
	private void createWorkbook() {
		Workbook wb = new HSSFWorkbook();
		wb.createSheet(REQUIREMENT_SHEET);


		this.workbook = wb;
	}

	private void createHeaders() {
		createRequirementHeaders();

	}


	// ***************** other things ******************************

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}

	public void getMessageSource(MessageSource source){
		this.messageSource = source;

	}

	private String handleMessages(String key) {
		try {
			return getMessage(key);
		} catch (NoSuchMessageException e) {
			return key;
		}
	}


}
