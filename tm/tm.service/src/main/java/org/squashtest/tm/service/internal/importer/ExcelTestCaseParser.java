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

import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.SheetCorruptedException;

interface ExcelTestCaseParser {
	

	String DESCRIPTION_TAG = "Description";
	String IMPORTANCE_TAG = "Importance";
	String NATURE_TAG = "Nature";
	String TYPE_TAG = "Type";
	String STATUS_TAG = "Status";
	String CREATED_ON_TAG = "Created_on";
	String CREATED_BY_TAG = "Created_by";
	String PREREQUISITE_TAG = "Prerequisite";
	String ACTION_STEP_TAG = "Action_step";

	TestCase parseFile(Workbook workbook, ImportSummaryImpl summary) throws SheetCorruptedException;
	
	TestCase parseFile(InputStream stream, ImportSummaryImpl summary) throws SheetCorruptedException;
	
	String stripFileExtension(String fullName);
}
