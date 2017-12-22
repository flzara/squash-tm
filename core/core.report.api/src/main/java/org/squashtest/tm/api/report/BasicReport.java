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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.Input;
import org.squashtest.tm.core.foundation.i18n.Labelled;

/**
 * Basic implementation od a {@link Report}. This class should be used in report plugins to describe reports.
 * 
 * @author bsiri
 * @author Gregory Fouquet
 * 
 */
public class BasicReport extends Labelled implements Report, InitializingBean {
	private StandardReportCategory category = StandardReportCategory.VARIOUS;
	private StandardReportType type = StandardReportType.GENERIC;

	private String descriptionKey;
	private ReportView[] views = {};
	private int defaultViewIndex = 0;
	private Input[] form = {};

	/**
	 * @see org.squashtest.tm.api.report.Report#getCategory()
	 */
	@Override
	public StandardReportCategory getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(StandardReportCategory category) {
		this.category = category;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getType()
	 */
	@Override
	public StandardReportType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(StandardReportType type) {
		this.type = type;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getDescriptionKey()
	 */
	@Override
	public String getDescriptionKey() {
		return descriptionKey;
	}

	/**
	 * @param descriptionKey
	 *            the descriptionKey to set
	 */
	public void setDescriptionKey(String descriptionKey) {
		this.descriptionKey = descriptionKey;
	}

	/**
	 * @param views
	 *            the views to set
	 */
	public void setViews(ReportView[] views) {
		this.views = views;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getViews()
	 */
	@Override
	public ReportView[] getViews() {
		return views;
	}

	/**
	 * @param defaultViewIndex
	 *            the defaultViewIndex to set
	 */
	public void setDefaultViewIndex(int defaultViewIndex) {
		this.defaultViewIndex = defaultViewIndex;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getDescription()
	 */
	@Override
	public String getDescription() {
		return getMessage(descriptionKey);
	}

	/**
	 * @param form
	 *            the form to set
	 */
	public void setForm(Input[] form) {
		this.form = form;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#getForm()
	 */
	@Override
	public Input[] getForm() {
		return form;
	}

	/**
	 * @return the defaultViewIndex
	 */
	public int getDefaultViewIndex() {
		return defaultViewIndex;
	}

	/**
	 * @see org.squashtest.tm.api.report.Report#buildModelAndView(int, java.lang.String, java.util.Map)
	 */
	@Override
	public ModelAndView buildModelAndView(int viewIndex, String format, Map<String, Criteria> criteria) {
		ReportView view = views[viewIndex];
		Map<String, Object> model = view.buildViewModel(format, criteria);
		return new ModelAndView(view.getSpringView(), model);
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(category, "Category property should not be null");

		Assert.notNull(type, "Type property should not be null");

		Assert.notNull(form, "Form property should not be null");

		Assert.notNull(descriptionKey, "descriptionKey property should not be null");

		Assert.notNull(getLabelKey(), "labelKey property should not be null");

		Assert.notNull(views, "Views property should not be null");
		Assert.notEmpty(views, "Views property should not be empty");
		Assert.noNullElements(views, "Views property should contain null elements");

	}

}
