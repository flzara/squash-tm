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
package org.squashtest.tm.api.report;

import java.util.Map;

import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.Input;

/**
 * Defines the interface of a Report.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface Report {

	/**
	 * This method should return a non null category. Categories are used to regroup reports.
	 * 
	 * @return the category
	 */
	StandardReportCategory getCategory();

	/**
	 * This method should return a non null type. Types are used to give more information about the report but are not
	 * used to regroup reports.
	 * 
	 * @return the type
	 */
	StandardReportType getType();

	/**
	 * Should return the key to an existing i18n entry which describes the report.
	 * 
	 * @return the descriptionKey
	 */
	String getDescriptionKey();

	/**
	 * Should return a non null, non empty array of views
	 * 
	 * @return the views
	 */
	ReportView[] getViews();

	/**
	 * Should return an internationalized description of the report.
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * Should return a non null array of {@link Input} describing this report's form. It will be used to generate the
	 * GUI form which the user will use to describe / parameterize the report.
	 * 
	 * @return the form
	 */
	Input[] getForm();

	/**
	 * Should return the key to an existing i18n entry representing the report's label (name).
	 * 
	 * @return the descriptionKey
	 */
	String getLabelKey();

	/**
	 * Should return an internationalized label of the report.
	 * 
	 * @return
	 */
	String getLabel();

	/**
	 * This method should return a ModelAndView initialized with the view matching the given index and format, and a
	 * dataset matching the given criteria.
	 * 
	 * @param viewIndex
	 *            index of the {@link ReportView} which is to be returned as a Spring MVC view
	 * @param format
	 *            name of the format (pdf, xls...) which is to be produced
	 * @param criteria
	 *            map of criteria which are to be used to generate the dataset
	 * @return a non nul, initialized ModelAndView object.
	 */
	ModelAndView buildModelAndView(int viewIndex, String format, Map<String, Criteria> criteria);
	
}