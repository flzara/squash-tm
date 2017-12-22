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

import org.apache.poi.ss.usermodel.Cell;
import org.junit.Test;

import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class OptionalIntegerCellCoercerTest extends Specification {
	Cell cell = Mock()

	@Unroll
	def "should coerce numeric cell value #cellVal to #expectedVal"() {
		given:
		cell.getCellType() >> Cell.CELL_TYPE_NUMERIC
		cell.getNumericCellValue() >> cellVal

		expect:
		OptionalIntegerCellCoercer.INSTANCE.coerce(cell) == expectedVal

		where:
		cellVal | expectedVal
		30.0    | 30
		30.0001 | 30
		29.9996 | 30
		36.348  | 36
	}

	@Unroll
	def "should coerce string cell value #cellVal to #expectedVal"() {
		given:
		cell.getCellType() >> Cell.CELL_TYPE_STRING
		cell.getNumericCellValue() >> {throw new RuntimeException() }
		cell.getStringCellValue() >> cellVal

		expect:
		OptionalIntegerCellCoercer.INSTANCE.coerce(cell) == expectedVal

		where:
		cellVal   | expectedVal
		"30"      | 30
		"30.0001" | 30
		"29.9996" | 30
		"36.348"  | 36
	}

	@Unroll
	def "should throw exception on non parseable #cellVal string cell"() {
		given:
		cell.getCellType() >> Cell.CELL_TYPE_STRING
		cell.getNumericCellValue() >> {throw new RuntimeException() }
		cell.getStringCellValue() >> cellVal

		when:
		OptionalIntegerCellCoercer.INSTANCE.coerce(cell)

		then:
		thrown(RuntimeException)

		where:
		cellVal << ["", "  " , "not a number"]
	}

	@Unroll
	def "should throw exception on funky cell type #type"() {
		given:
		cell.getCellType() >> type

		when:
		OptionalIntegerCellCoercer.INSTANCE.coerce(cell)

		then:
		thrown(CannotCoerceException)

		where:
		type << [
			Cell.CELL_TYPE_BOOLEAN,
			Cell.CELL_TYPE_ERROR,
			Cell.CELL_TYPE_FORMULA
		]
	}

	def "should coerce empty cells to null"() {
		given:
		cell.getCellType() >> Cell.CELL_TYPE_BLANK

		expect:
		OptionalIntegerCellCoercer.INSTANCE.coerce(cell) == null
	}
}
