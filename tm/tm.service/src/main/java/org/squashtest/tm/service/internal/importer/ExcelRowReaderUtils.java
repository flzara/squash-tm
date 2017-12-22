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
package org.squashtest.tm.service.internal.importer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*package private*/final class ExcelRowReaderUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelRowReaderUtils.class);

	private ExcelRowReaderUtils(){

	}

	/**
	 * If the cell is of numeric type : will find the cell of the given header tag and will, get it's numerical value.<br>
	 * If the cell is of String type, will try to parse the string value of the cell and return <code>null</code> if parsing failed.
	 *
	* @param row : the concerned row
	 * @param columnsMapping : a map linking the header name to the column index
	 * @param tag : the column header
	 * @return a <code>Double</code> or <code>null</code> it the cell is not numerical or a text that can be parsed in double
	 */
	public static Double readNumericField(Row row, Map<String, Integer> columnsMapping, String tag) {
		Cell cell = accessToCell(row, columnsMapping, tag);
		Double toReturn = null;
		if (cell != null) {
			int type = cell.getCellType();
			if (type == Cell.CELL_TYPE_NUMERIC) {
				toReturn = cell.getNumericCellValue();
			} else {
				if (type == Cell.CELL_TYPE_STRING) {
					String value2s = cell.getStringCellValue();
					try {
						toReturn = Double.parseDouble(value2s);
					} catch (NumberFormatException nfe) {
						LOGGER.warn(nfe.getMessage());
					}
				}
			}
		}
		return toReturn;
	}

	/**
	 * If the cell is of numeric type : will read the Date value of the cell. <br>
	 * If the cell is of String type : will try to parse the date with the format "dd/MM/yyyy" and return null if the parsing failed
	 * @param row : the concerned row
	 * @param columnsMapping : a map linking the header name to the column index
	 * @param tag : the column header
	 * @return a <code>Date</code> or <code>null</code> it the cell is not numerical or a text that can be parsed into a date
	 */
	public static Date readDateField(Row row, Map<String, Integer> columnsMapping, String tag) {
		Cell cell = accessToCell(row, columnsMapping, tag);
		Date toReturn = null;
		if (cell != null) {
			int type = cell.getCellType();
			if (type == Cell.CELL_TYPE_NUMERIC) {
				toReturn = cell.getDateCellValue();

			} else {
				if (type == Cell.CELL_TYPE_STRING) {
					String dateS = cell.getStringCellValue();

					try {
						toReturn = new SimpleDateFormat("dd/MM/yyyy").parse(dateS);
					} catch (ParseException e) {
						LOGGER.warn(e.getMessage());
					}
				}
			}
		}
		return toReturn;
	}

	/**
	 * If the cell is of String type : will return it's value if not empty<br>
	 * If the cell is of numeric type : will return the number as String (Integer (if round) or Double value)
	 * @param row : the concerned row
	 * @param columnsMapping : a map linking the header name to the column index
	 * @param tag : the column header
	 * @return
	 */
	public static String readTextField(Row row, Map<String, Integer> columnsMapping, String tag) {
		Cell cell = accessToCell(row, columnsMapping, tag);
		String toReturn = null;
		if (cell != null) {
			int type = cell.getCellType();
			if (type == Cell.CELL_TYPE_STRING) {
				String value = cell.getStringCellValue();
				if (notEmpty(value)) {
					toReturn = value;
				}
			} else {
				if (type == Cell.CELL_TYPE_NUMERIC) {
					Double doubleVal = cell.getNumericCellValue();
					if(doubleVal - doubleVal.intValue() == 0){
						toReturn = String.valueOf(doubleVal.intValue());
					}else{
					toReturn = doubleVal.toString();
					}
				}
			}
		}
		return toReturn;
	}

	private static boolean notEmpty(String string) {
		return string != null && !string.isEmpty();
	}

	private static Cell accessToCell(Row row, Map<String, Integer> columnsMapping, String tag) {
		Integer columnIndex = columnsMapping.get(tag);
		if (columnIndex != null && columnIndex >= 0) {
			return row.getCell(columnIndex);
		}
		return null;
	}

	public static Map<String, Integer>  mapColumns (Sheet sheet) {
		Map<String, Integer> columnsMapping = new HashMap<>();

		Row firstRow = sheet.getRow(0);
		for(int c = 0 ; c < firstRow.getLastCellNum(); c++){
			Cell headerCell =  firstRow.getCell(c);
			if(headerCell != null){
			String headerTag = headerCell.getStringCellValue();
			columnsMapping.put(headerTag.toUpperCase(), c);
			}
		}
		return columnsMapping;

	}
}
