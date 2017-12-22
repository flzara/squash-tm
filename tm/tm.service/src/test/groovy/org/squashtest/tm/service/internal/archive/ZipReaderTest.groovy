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
package org.squashtest.tm.service.internal.archive

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import spock.lang.Specification

class ZipReaderTest extends Specification{
	
	def "should browse zip and bring names"(){
		
		given :
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("binaries/xls.zip");
		
		and : 
			def reader = new ZipReader(stream, "UTF8")
		
		when :
			def names = []
			def entry;
			while(reader.hasNext()){
				entry = reader.next();
				names << [ entry.getName(), entry.getShortName(), entry.getParent(), entry.isFile() ]
			}

		
		then :
			def res = names.collect{[ it[0], it[1],  it[3] ]}
			res.containsAll([
					["/test1.xlsx", "test1.xlsx", true],
					["/test2.xlsx", "test2.xlsx", true],
					["/folder", "folder",  false],
					["/folder/test3.xlsx", "test3.xlsx",  true]
				
				])
			
			names.collect{it[2].getName()}.containsAll([ "/", "/", "/folder", "/"]) 
	}

	

	
	def "should create valid workbook"(){
		given :
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("binaries/xls.zip");
		
		and :
			def reader = new ZipReader(stream, "UTF8")
			
		when :
			def books = [];
			def entry;
			while(reader.hasNext()){
				entry = reader.next();
				if (entry.isFile()){
					Workbook workbook = WorkbookFactory.create(entry.getStream());
					books << workbook
				}
			}
			
			
		then :
			books.size() == 3
			books.collect{ it.getSheetAt(0).getRow(0).getCell(0).getStringCellValue() } == [ "qsdqs", "rty", "azer" ]
	}

}
