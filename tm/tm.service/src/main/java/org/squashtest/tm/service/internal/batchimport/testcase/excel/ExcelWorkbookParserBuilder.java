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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.squashtest.tm.exception.SheetCorruptedException;
import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException;
import org.squashtest.tm.service.batchimport.excel.WorksheetFormatStatus;
import org.squashtest.tm.service.batchimport.excel.WorksheetMismatch;

/**
 * Builds an excel parser. It checks the structure of the excel file and
 * configures the parser accordingly.
 *
 * @author Gregory Fouquet
 *
 */
class ExcelWorkbookParserBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelWorkbookParserBuilder.class);
	private final File xls;

	@Inject
	@Value("${uploadfilter.upload.import.sizeLimitInBytes}")
	private double maxSize;

	public ExcelWorkbookParserBuilder(@NotNull File xls) {
		super();
		this.xls = xls;
	}

	/**
	 * Builds a parser. May throw exceptions when the workbook contains
	 * unrecoverable errors.
	 *
	 * @return
	 * @throws SheetCorruptedException
	 *             when the excel file cannot be read
	 * @throws TemplateMismatchException
	 *             when the workbook does not match the expected template in an
	 *             unrecoverable way.
	 */
	public ExcelWorkbookParser build() throws SheetCorruptedException,
			TemplateMismatchException {

		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(xls));

		Workbook wb = openWorkbook(is);
		List<TemplateMismatchException> mismatches = new ArrayList<>();
		WorkbookMetaData wmd = null;
		try {
			wmd = buildMetaData(wb);
			wmd.validate();
		} catch (TemplateMismatchException tme) {
			mismatches.add(tme);
		}
		if (!mismatches.isEmpty()) {
			TemplateMismatchException tme = new TemplateMismatchException();
			for (TemplateMismatchException mismatch : mismatches) {
				tme.addWorksheetFormatStatus(mismatch.getWorksheetFormatStatuses());
			}
			throw tme;
		}

		LOGGER.trace("Metamodel is built, will create a parser based on the metamodel");

		return new ExcelWorkbookParser(wb, wmd);
		} catch (FileNotFoundException e) {
			throw new SheetCorruptedException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * @param wb
	 * @return
	 */
	private WorkbookMetaData buildMetaData(Workbook wb) throws TemplateMismatchException {
		LOGGER.trace("Building metamodel for workbook");

		WorkbookMetaData wmd = new WorkbookMetaData();
		processSheets(wb, wmd);

		return wmd;
	}

	/**
	 * Reads the workbook's sheets and append {@link WorksheetDef}s to the
	 * {@link WorkbookMetaData} accordingly.
	 *
	 * @param wb
	 * @param wmd
	 */
	private void processSheets(Workbook wb, WorkbookMetaData wmd) {
		List<WorksheetFormatStatus> worksheetKOStatuses = new ArrayList<>();

		for (int iSheet = 0; iSheet < wb.getNumberOfSheets(); iSheet++) {
			processSheet(wb, wmd, worksheetKOStatuses, iSheet);
		}
		if (!worksheetKOStatuses.isEmpty()) {
			throw new TemplateMismatchException(worksheetKOStatuses);
		}
	}

	@SuppressWarnings("rawtypes")
	private void processSheet(Workbook wb, WorkbookMetaData wmd, List<WorksheetFormatStatus> worksheetKOStatuses,
			int iSheet) {
		Sheet ws = wb.getSheetAt(iSheet);
		String sheetName = ws.getSheetName();

		Collection<TemplateWorksheet> sheetTypes = TemplateWorksheet.coerceFromSheetName(sheetName);

		for (TemplateWorksheet sheetType : sheetTypes) {
			if (sheetType != null) {
				LOGGER.trace("Worksheet named '{}' will be added to metamodel as standard worksheet {}", sheetName,
						sheetType);

				WorksheetDef<?> wd = new WorksheetDef(sheetType);
				wmd.addWorksheetDef(wd);
				WorksheetFormatStatus workSheetFormatStatus = populateColumnDefs(wd, ws);
				if (!workSheetFormatStatus.isFormatOk()) {
					worksheetKOStatuses.add(workSheetFormatStatus);
				}
			} else {
				LOGGER.trace("Skipping unrecognized worksheet named '{}'", ws.getSheetName());

			}
		}
	}

	/**
	 * Reads the given sheet and appends {@link ColumnDef} to the
	 * {@link WorksheetDef} accordingly.
	 *
	 * @param wd
	 * @param ws
	 * @return {@link WorksheetFormatStatus}
	 */
	private WorksheetFormatStatus populateColumnDefs(WorksheetDef<?> wd, Sheet ws) {
		Row headerRow = findHeaderRow(ws);
		WorksheetFormatStatus worksheetFormatStatus = new WorksheetFormatStatus(wd.getWorksheetType());

		if (headerRow == null) {
			worksheetFormatStatus.addWorksheetMismatch(WorksheetMismatch.MISSING_HEADER);
			return worksheetFormatStatus;
		}

		for (int iCell = 0; iCell < headerRow.getLastCellNum(); iCell++) {
			Cell cell = headerRow.getCell(iCell);

			if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
				try {
					String header = cell.getStringCellValue();
					wd.addColumnDef(header, iCell);
				} catch (ColumnMismatchException cme) {// NOSONAR this is not an error case, we log it in report
					worksheetFormatStatus.addMismatch(cme.getType(), cme.getColType());
				}
			}
		}

		return worksheetFormatStatus;
	}

	/**
	 *
	 * @param ws
	 * @return header row or <code>null</code>
	 */
	private Row findHeaderRow(Sheet ws) {
		return ws.getRow(0);
	}

	/**
	 * Opens a workbook from a stream. Potential IO errors are converted /
	 * softened into {@link SheetCorruptedException}
	 *
	 * @param is
	 * @return
	 * @throws SheetCorruptedException
	 */
	private Workbook openWorkbook(InputStream is) throws SheetCorruptedException {
		try {
			return WorkbookFactory.create(is);

		} catch (InvalidFormatException | IOException | IllegalArgumentException | POIXMLException e) {
			LOGGER.info(e.getMessage());
			IOUtils.closeQuietly(is);
			throw new SheetCorruptedException(e);
		}
	}

}
