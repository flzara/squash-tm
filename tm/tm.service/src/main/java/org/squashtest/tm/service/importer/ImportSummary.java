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
package org.squashtest.tm.service.importer;

public interface ImportSummary {
	int getTotal(); 		//total test cases read in the archive
	int getSuccess();		//total test cases successfully imported
	int getRenamed();		//total test cases or folders that were renammed
	int getModified();		//total test cases modifications due to incorrect/incomplete data
	int getFailures();		//total test cases that could not be imported
	int getRejected(); 		//total test cases whose extension is neither xsl or xslx
	/**
	 * adds the result of an import summary to this import summary 
	 */
	void add(ImportSummary summary);
	int getMilestoneFailures();
	int getMilestoneNotActivatedFailures();
}

