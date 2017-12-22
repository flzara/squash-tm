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
package org.squashtest.csp.core.bugtracker.internal.mantis;



import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;


@Component
public class MantisInterfaceDescriptor implements BugTrackerInterfaceDescriptor {

	private static final String REPORT_PRIORITY_LABEL  	 = "interface.report.priority.label";
	private static final String REPORT_VERSION_LABEL  	 = "interface.report.version.label";
	private static final String REPORT_ASSIGNEE_LABEL 	 = "interface.report.assignee.label";
	private static final String REPORT_CATEGORY_LABEL 	 = "interface.report.category.label";
	private static final String REPORT_SUMMARY_LABEL 	 = "interface.report.summary.label";
	private static final String REPORT_DESCRIPTION_LABEL = "interface.report.description.label";
	private static final String REPORT_COMMENT_LABEL 	 = "interface.report.comment.label";

	private static final String REPORT_EMPTY_VERSION	 = "interface.report.lists.emptyversion.label";
	private static final String REPORT_EMPTY_CATEGORY	 = "interface.report.lists.emptycategory.label";
	private static final String REPORT_EMPTY_ASSIGNEE	 = "interface.report.lists.emptyassignee.label";

	private static final String TABLE_ID_HEADER			 = "interface.table.issueid.header";
	private static final String TABLE_SUMMARY_HEADER	 = "interface.table.summary.header";
	private static final String TABLE_PRIORITY_HEADER	 = "interface.table.priority.header";
	private static final String TABLE_STATUS_HEADER		 = "interface.table.status.header";
	private static final String TABLE_ASSIGNEE_HEADER	 = "interface.table.assignee.header";
	private static final String TABLE_REPORTEDIN_HEADER	 = "interface.table.reportedin.header";
	private static final String TABLE_DESCRIPTION_HEADER = "interface.table.description.header";

	private static final String TABLE_EMPTY_ASSIGNEE	 = "interface.table.null.assignee.label";



	private final ThreadLocal<Locale> threadLocalLocale = new ThreadLocal<>();

	@Inject @Named("mantisConnectorMessageSource")
	private MessageSource messageSource;


	public MantisInterfaceDescriptor(){
		threadLocalLocale.set(LocaleContextHolder.getLocale());
	}


	@Override
	public void setLocale(Locale locale){
		threadLocalLocale.set(locale);
	}


	// ***************** basic userful info ************

	@Override
	public boolean getSupportsRichDescription(){
		return false;
	}

	@Override
	public boolean getSupportsRichComment(){
		return false;
	}


	// ***************** labels for the issue report popup fields *******************

	@Override
	public String getReportPriorityLabel() {
		return getValue(REPORT_PRIORITY_LABEL);
	}

	@Override
	public String getReportVersionLabel() {
		return getValue(REPORT_VERSION_LABEL);
	}

	@Override
	public String getReportAssigneeLabel() {
		return getValue(REPORT_ASSIGNEE_LABEL);
	}

	@Override
	public String getReportCategoryLabel() {
		return getValue(REPORT_CATEGORY_LABEL);
	}

	@Override
	public String getReportSummaryLabel() {
		return getValue(REPORT_SUMMARY_LABEL);
	}


	@Override
	public String getReportDescriptionLabel() {
		return getValue(REPORT_DESCRIPTION_LABEL);
	}

	@Override
	public String getReportCommentLabel() {
		return getValue(REPORT_COMMENT_LABEL);
	}


	@Override
	public String getEmptyVersionListLabel() {
		return getValue(REPORT_EMPTY_VERSION);
	}

	@Override
	public String getEmptyCategoryListLabel() {
		return getValue(REPORT_EMPTY_CATEGORY);
	}

	@Override
	public String getEmptyAssigneeListLabel(){
		return getValue(REPORT_EMPTY_ASSIGNEE);
	}



	// ****************** issue tables labels ***********************


	@Override
	public String getTableIssueIDHeader() {
		return getValue(TABLE_ID_HEADER);
	}


	@Override
	public String getTableSummaryHeader() {
		return getValue(TABLE_SUMMARY_HEADER);
	}


	@Override
	public String getTablePriorityHeader() {
		return getValue(TABLE_PRIORITY_HEADER);
	}


	@Override
	public String getTableStatusHeader() {
		return getValue(TABLE_STATUS_HEADER);
	}

	@Override
	public String getTableAssigneeHeader() {
		return getValue(TABLE_ASSIGNEE_HEADER);
	}


	@Override
	public String getTableReportedInHeader() {
		return getValue(TABLE_REPORTEDIN_HEADER);
	}

	@Override
	public String getTableDescriptionHeader() {
		return getValue(TABLE_DESCRIPTION_HEADER);
	}


	@Override
	public String getTableNoAssigneeLabel() {
		return getValue(TABLE_EMPTY_ASSIGNEE);
	}



	/* *************************** private stuffs ************************* */
	private String getValue(String key){
		return messageSource.getMessage(key, null, threadLocalLocale.get());
	}


}
