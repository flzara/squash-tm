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
package org.squashtest.csp.core.bugtracker.spi;

import java.util.Locale;


/**
 * That interface will hold the label of the fields corresponding to the specific 
 * implementation of the bugtracker.  
 * 
 * All implementations of a connector should implement that too.
 * 
 * @author bsiri
 *
 */
public interface  BugTrackerInterfaceDescriptor {

	/**
	 * sets the locale for current thread.
	 * @param locale
	 */
	void setLocale(Locale locale);
	
	
	// ***************** basic userful info ************
	
	/**
	 * @returns whether or not the backing bugtracker supports rich formated text for the description field
	 */
	boolean getSupportsRichDescription();
	
	/**
	 * @returns whether or not the backing bugtracker supports rich formated text for the comment field
	 */
	boolean getSupportsRichComment();
	
	
	// ***************** labels for the issue report popup fields *******************
	
	/**
	 * @return the label for the issue priority level dropdown list in the reporting sheet 
	 */
	String getReportPriorityLabel();
	
	/**
	 * @return the label for the project version dropdown list in the reporting sheet
	 */
	String getReportVersionLabel();
	
	/**
	 * @return the label for the assignee dropdown list in the reporting sheet
	 */
	String getReportAssigneeLabel();
	
	
	/**
	 * @return the label for the project category dropdown list in the reporting sheet
	 */
	String getReportCategoryLabel();

	/**
	 * @return the label for the summary field in the reporting sheet
	 */	
	String getReportSummaryLabel();
	
	/**
	 * @return the label for the issue description field in the reporting sheet
	 */
	String getReportDescriptionLabel();
	
	
	/**
	 * @return the label for the issue commentary field in the reporting sheet
	 */
	String getReportCommentLabel();
	

	/**
	 * @return the dummy value to display in the version dropdown list when it's empty and/or unavailable
	 */
	String getEmptyVersionListLabel();
	
	
	/**
	 * @return the dummy value to display in the category dropdown list when it's empty and/or unavailable
	 */
	String getEmptyCategoryListLabel();
	
	
	/**
	 * @return the dummy value to display in the assignee dropdown list when it's empty and/or unavailable
	 */
	String getEmptyAssigneeListLabel();
	
	
	
	// ****************** issue tables labels ***********************


	/**
	 * @return the header for 'issue id' column in issues summary tables
	 */
	String getTableIssueIDHeader();

	/**
	 * @return the header for the 'summary' column in issues summary tables
	 */
	String getTableSummaryHeader();
	
	
	/**
	 * @return the header for the 'priority' column in issues summary tables
	 */
	String getTablePriorityHeader();
	
	/**
	 * @return the header for 'status' column in issues summary tables
	 */
	String getTableStatusHeader();
	
	
	/**
	 * @return the header for the 'description' column in issues summary tables
	 */
	String getTableDescriptionHeader();
	
	
	/**
	 * @return the header for the 'assignee' column in issues summary tables
	 */
	String getTableAssigneeHeader();
	
	/**
	 * @return the header for the 'reported in' column in issues summary tables
	 */
	String getTableReportedInHeader();

	
	/**
	 * @return the label standing for the assignee when none was set
	 */
	String getTableNoAssigneeLabel();
	

}
