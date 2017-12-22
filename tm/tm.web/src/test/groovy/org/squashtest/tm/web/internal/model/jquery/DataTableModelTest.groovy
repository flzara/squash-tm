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
package org.squashtest.tm.web.internal.model.jquery

import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

import spock.lang.Specification

class DataTableModelTest extends Specification {
	DataTableModel dataTableModel = new DataTableModel()
	
	def "Column names should be appended in a comma-separated list"() {
		when:
		dataTableModel.comumnNames = ['foo', 'bar', 'baz']
		
		then:
		dataTableModel.getsColumns() == 'foo,bar,baz'
	}
	
	def "Should add a row to the data"() {
		given:
		def row =  ['foo', 'bar', 'baz']
		
		when:
		dataTableModel.addRow row
		
		then:
		dataTableModel.aaData.contains(row)
	}

	def "Should display all the rows"() {
		given:
		dataTableModel.aaData << ['foo', 'bar', 'baz']
		
		when:
		dataTableModel.displayAllRows()
		
		then:
		dataTableModel.iTotalRecords == 1
		dataTableModel.iTotalDisplayRecords == 1
	}

	def "Should display 5 row of 5"() {
		given:
		dataTableModel.aaData << ['foo', 'bar', 'baz']
		
		when:
		dataTableModel.displayRowsFromTotalOf(5)
		
		then:
		dataTableModel.iTotalRecords == 5
		dataTableModel.iTotalDisplayRecords == 5
	}
}
