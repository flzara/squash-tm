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

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Gregory Fouquet
 *
 */
class LiberalBooleanCellCoercerTest extends Specification {
	LiberalBooleanCellCoercer coercer = LiberalBooleanCellCoercer.INSTANCE
	Cell cell = Mock()
	
	@Unroll
	def "Should coerce cell #value of type # type to #expected"() {
		given: 
		cell.getCellType() >> type
		cell.getNumericCellValue() >> value
		cell.getStringCellValue() >> value
		cell.getBooleanCellValue() >> value
		
		expect: 
		coercer.coerce(cell) == expected
		
		where:
		value	| expected	| type
		"0"		| false		| Cell.CELL_TYPE_STRING
		0d		| false		| Cell.CELL_TYPE_NUMERIC
		false	| false		| Cell.CELL_TYPE_BOOLEAN
		-0.49d	| false		| Cell.CELL_TYPE_NUMERIC
		0.49d	| false		| Cell.CELL_TYPE_NUMERIC
		"1"		| true		| Cell.CELL_TYPE_STRING
		1d		| true		| Cell.CELL_TYPE_NUMERIC
		true	| true		| Cell.CELL_TYPE_BOOLEAN
		0.5d	| true		| Cell.CELL_TYPE_NUMERIC
		1.49d	| true		| Cell.CELL_TYPE_NUMERIC
	}

	@Unroll
	def "Should not coerce cell #value of type # type"() {
		given: 
		cell.getCellType() >> type
		cell.getNumericCellValue() >> value
		cell.getStringCellValue() >> value
		cell.getBooleanCellValue() >> value
		
		when: 
		coercer.coerce(cell) 
		
		then: thrown(CannotCoerceException)
		
		where:
		value	| type
		""		| Cell.CELL_TYPE_STRING
		"bad"	| Cell.CELL_TYPE_STRING
		"2"		| Cell.CELL_TYPE_STRING
		1		| Cell.CELL_TYPE_FORMULA
		1		| Cell.CELL_TYPE_BLANK
		1		| Cell.CELL_TYPE_ERROR
		2		| Cell.CELL_TYPE_NUMERIC
		-1		| Cell.CELL_TYPE_NUMERIC
	}
}
