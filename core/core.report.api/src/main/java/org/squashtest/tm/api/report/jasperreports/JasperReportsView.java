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
package org.squashtest.tm.api.report.jasperreports;

import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRParameter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.util.Assert;
import org.springframework.web.servlet.View;
import org.squashtest.tm.api.report.ReportView;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.query.ReportQuery;
import org.squashtest.tm.core.foundation.i18n.Labelled;

/**
 * @author bsiri
 * @author Gregory Fouquet
 *
 */
public final class JasperReportsView extends Labelled implements ReportView, InitializingBean {
	private String[] formats;
	private View springView;
	private ReportQuery query;
	private MessageSource messageSource;

	public JasperReportsView() {
		super();
	}

	/**
	 * @see org.squashtest.tm.api.report.ReportView#getFormats()
	 */
	@Override
	public String[] getFormats() {
		return formats;
	}

	/**
	 * @param formats
	 *            the formats to set
	 */
	public void setFormats(String[] formats) {
		this.formats = formats;
	}

	/**
	 * @see org.squashtest.tm.api.report.ReportView#buildViewModel(java.lang.String, java.util.Map)
	 */
	@Override
	public Map<String, Object> buildViewModel(String format, Map<String, Criteria> criteria) {
		Map<String, Object> res = new HashMap<>();

		query.executeQuery(criteria, res);

		res.put("format", format);
		res.put(JRParameter.REPORT_RESOURCE_BUNDLE,
				new MessageSourceResourceBundle(messageSource, LocaleContextHolder.getLocale()));

		return res;
	}

	/**
	 * Sets the Spring MVC View bean. It should be a JasperReportMultiFormatView.
	 *
	 * @param springView
	 *            the springView to set
	 */
	public void setSpringView(View viewBean) {
		this.springView = viewBean;
	}

	/**
	 * @return the springView
	 */
	@Override
	public View getSpringView() {
		return springView;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public void setQuery(ReportQuery query) {
		this.query = query;
	}

	/**
	 * Checks the state of this bean.
	 *
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		Assert.notNull(springView, "springView must not be null");
		Assert.notNull(query, "query must not be null");

	}

	/**
	 * @see org.squashtest.tm.core.foundation.i18n.ContextBasedInternationalized#initializeMessageSource(org.springframework.context.MessageSource)
	 */
	@Override
	protected void initializeMessageSource(MessageSource messageSource) {
		super.initializeMessageSource(messageSource);
		this.messageSource = messageSource;
	}

}
