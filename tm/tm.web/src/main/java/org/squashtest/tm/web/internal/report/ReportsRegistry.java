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
package org.squashtest.tm.web.internal.report;

import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.report.BasicReport;
import org.squashtest.tm.api.report.Report;
import org.squashtest.tm.api.report.ReportPlugin;
import org.squashtest.tm.api.report.StandardReportCategory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class registers / unregisters {@link BasicReport} and their {@link StandardReportCategory} when
 * {@link ReportPlugin} services are started / stopped.
 *
 * @author Gregory Fouquet
 *
 */
@Component
public class ReportsRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportsRegistry.class);

	private final MultiValueMap reportsByCategory = new MultiValueMap();
	private final Map<ReportIdentifier, IdentifiedReportDecorator> reportByIdentifier = new ConcurrentHashMap<>();

	/**
	 * Collection of known ReportPligins. @Inject is not suitable because it doesn't handle empty collections.
	 */
	@Autowired(required = false)
	private Collection<ReportPlugin> plugins = Collections.emptyList();

	@Inject
	private MessageSource i18nHelper;

	/**
	 * OSGi context should be configured to call this method when a {@link ReportPlugin} service is started.
	 */
	@PostConstruct
	public void registerReports() {
		for (ReportPlugin plugin : plugins) {
			Report[] reports = plugin.getReports();

			for (Report report : reports) {
				String pluginNamespace = report.getLabelKey();
				StandardReportCategory category = report.getCategory();
				IdentifiedReportDecorator identifiedReport = createIdentifiedReport(report, pluginNamespace);

				reportsByCategory.put(category, identifiedReport);
				reportByIdentifier.put(identifiedReport.getIdentifier(), identifiedReport);

				LOGGER.info("Registered report [{}] under Category [{}] along with Namespace [{}]", report, category.getI18nKey(), pluginNamespace);
			}

		}
	}

	private IdentifiedReportDecorator createIdentifiedReport(Report report, String pluginNamespace) {
		return new IdentifiedReportDecorator(report, pluginNamespace);
	}


	@SuppressWarnings("unchecked")
	public Set<StandardReportCategory> getCategories() {
		return reportsByCategory.keySet();
	}


	public List<StandardReportCategory> getSortedCategories() {
		List<StandardReportCategory> sortedCategories = new ArrayList<>(getCategories());
		Collections.sort(sortedCategories, new CategorySorter(i18nHelper));
		return sortedCategories;
	}

	@SuppressWarnings("unchecked")
	public Collection<IdentifiedReportDecorator> findReports(StandardReportCategory category) {
		Collection<IdentifiedReportDecorator> res = (Collection<IdentifiedReportDecorator>) reportsByCategory.get(category);
		return res == null ? Collections.<IdentifiedReportDecorator>emptyList() : res;
	}

	@SuppressWarnings("unchecked")
	public Map<StandardReportCategory, Collection<BasicReport>> getReportsByCategory() {
		return reportsByCategory;
	}

	@SuppressWarnings("unchecked")
	public Map<StandardReportCategory, Collection<BasicReport>> getSortedReportsByCategory() {

		Map<StandardReportCategory, Collection<BasicReport>> sortedMap = new HashMap<>(reportsByCategory.size());

		for (StandardReportCategory categ : (Iterable<StandardReportCategory>) reportsByCategory.keySet()) {
			List<BasicReport> sortedReports = new ArrayList<>(reportsByCategory.getCollection(categ));
			Collections.sort(sortedReports, new ReportSorter());
			sortedMap.put(categ, sortedReports);
		}

		return sortedMap;
	}

	/**
	 * @param namespace
	 * @return
	 */
	public Report findReport(String namespace) {
		return reportByIdentifier.get(new ReportIdentifier(namespace));
	}


	// ****************************** boilerplate *****************************

	private static class CategorySorter implements Comparator<StandardReportCategory> {

		private MessageSource i18nHelper;

		CategorySorter(MessageSource helper) {
			this.i18nHelper = helper;
		}

		@Override
		public int compare(StandardReportCategory category1, StandardReportCategory category2) {
			Locale locale = LocaleContextHolder.getLocale();
			String name1 = i18nHelper.getMessage(category1.getI18nKey(), null, locale);
			String name2 = i18nHelper.getMessage(category2.getI18nKey(), null, locale);
			return name1.compareTo(name2);
		}

	}

	private static class ReportSorter implements Comparator<Report> {
		@Override
		public int compare(Report report1, Report report2) {
			String name1 = report1.getLabel();
			String name2 = report2.getLabel();
			return name1.compareTo(name2);
		}

	}
}
