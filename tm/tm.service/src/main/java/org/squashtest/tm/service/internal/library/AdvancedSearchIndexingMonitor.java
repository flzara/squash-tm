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
package org.squashtest.tm.service.internal.library;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.IndexMonitor;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.internal.advancedsearch.IndexationServiceImpl;


public class AdvancedSearchIndexingMonitor implements MassIndexerProgressMonitor {


	private static final Map<Class<?>, String> dateKeys = new HashMap<>();
	private static final Map<Class<?>, String> versionKeys = new HashMap<>();

	static {
		dateKeys.put(RequirementVersion.class, IndexationServiceImpl.REQUIREMENT_INDEXING_DATE_KEY);
		versionKeys.put(RequirementVersion.class, IndexationServiceImpl.REQUIREMENT_INDEXING_VERSION_KEY);

		dateKeys.put(TestCase.class, IndexationServiceImpl.TESTCASE_INDEXING_DATE_KEY);
		versionKeys.put(TestCase.class, IndexationServiceImpl.TESTCASE_INDEXING_VERSION_KEY);

		dateKeys.put(IterationTestPlanItem.class, IndexationServiceImpl.CAMPAIGN_INDEXING_DATE_KEY);
		versionKeys.put(IterationTestPlanItem.class, IndexationServiceImpl.CAMPAIGN_INDEXING_VERSION_KEY);
	}

	private ConfigurationService configurationService;
	private List<Class<?>> indexedDomains;
	private Class<?> indexedClass;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	public AdvancedSearchIndexingMonitor(List<Class<?>> classes, ConfigurationService configurationService) {
		this.configurationService = configurationService;
		this.indexedDomains = classes;

		if (multipleIndex()) {
			IndexMonitor.resetTotal();
		} else {
			indexedClass = indexedDomains.get(0);
			IndexMonitor.monitors.put(indexedClass, new IndexMonitor());
		}

	}


	private boolean multipleIndex() {
		return indexedDomains.size() > 1;
	}

	private IndexMonitor getCurrentMonitor() {

		if (multipleIndex()) {
			return IndexMonitor.total;
		} else {
			return IndexMonitor.monitors.get(indexedClass);
		}

	}

	@Override
	public void addToTotalCount(long arg0) {
		getCurrentMonitor().addToTotalCount(arg0);
	}

	@Override
	public void documentsBuilt(int arg0) {
		getCurrentMonitor().addToDocumentsBuilded(arg0);

	}


	@Override
	public void indexingCompleted() {

		Date indexingDate = new Date();
		String currentVersion = this.configurationService.findConfiguration(IndexationServiceImpl.SQUASH_VERSION_KEY);

		for (Class<?> c : indexedDomains) {
			updateIndexingDateAndVersion(c, indexingDate, currentVersion);
		}

	}

	private void updateIndexingDateAndVersion(Class<?> c, Date indexingDate, String currentVersion) {

		this.configurationService.updateConfiguration(dateKeys.get(c), dateFormat.format(indexingDate));
		this.configurationService.updateConfiguration(versionKeys.get(c), currentVersion);
	}


	@Override
	public void documentsAdded(long arg0) {
		// don't care

	}

	@Override
	public void entitiesLoaded(int arg0) {
		// don't care

	}
}
