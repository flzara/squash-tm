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
package org.squashtest.tm.service.internal.batchimport.excel;

import static org.junit.Assert.*;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;
import org.squashtest.tm.core.foundation.lang.DateUtils;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory Fouquet
 *
 */
class OptionalDateCellCoercerTest extends Specification {
	@Unroll
	def "should coerce cell of type #cellType and value #cellValue into #date"() {
		given:
		Cell cell = Mock()
		cell.getStringCellValue() >> cellValue
		cell.getNumericCellValue() >> cellValue
		cell.getDateCellValue() >> cellValue
		cell.getCellType() >> cellType

		expect:
		OptionalDateCellCoercer.INSTANCE.coerce(cell) == date

		where:
		cellType 				| cellValue										| date
		Cell.CELL_TYPE_BLANK	| ""											| null
		Cell.CELL_TYPE_STRING	| "2012-05-08"									| DateUtils.parseIso8601Date("2012-05-08");
		Cell.CELL_TYPE_NUMERIC	| DateUtils.parseIso8601Date("2017-05-18")	 	| DateUtils.parseIso8601Date("2017-05-18")
	}
}
