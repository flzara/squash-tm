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
package org.squashtest.tm.service.internal.requirement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service("squashtest.tm.service.RequirementVersionAdvancedSearchService")
public class RequirementVersionAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	RequirementVersionAdvancedSearchService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementVersionAdvancedSearchServiceImpl.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	private static final List<String> LONG_SORTABLE_FIELDS = Arrays.asList();
	private static final List<String> INT_SORTABLE_FIELDS = Arrays.asList("versionNumber");

	private static final String FAKE_REQUIREMENT_VERSION_ID = "-9000";

	@Override
	public List<String> findAllUsersWhoCreatedRequirementVersions(List<Long> idList) {
		return projectDao.findUsersWhoCreatedRequirementVersions(idList);
	}

	@Override
	public List<String> findAllUsersWhoModifiedRequirementVersions(List<Long> idList) {
		return projectDao.findUsersWhoModifiedRequirementVersions(idList);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> searchForRequirementVersions(AdvancedSearchModel model, Locale locale) {


		return new ArrayList<>();
	}


	private String formatSortedFieldName(String fieldName) {
		String result = fieldName;
		if (fieldName.startsWith("RequirementVersion.")) {
			result = fieldName.replaceFirst("RequirementVersion.", "");
		} else if (fieldName.startsWith("Requirement.")) {
			result = fieldName.replaceFirst("Requirement.", "requirement.");
		} else if (fieldName.startsWith("Project.")) {
			result = fieldName.replaceFirst("Project.", "requirement.project.");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PagedCollectionHolder<List<RequirementVersion>> searchForRequirementVersions(AdvancedSearchModel model,
		PagingAndMultiSorting sorting, MessageSource source, Locale locale) {


		List<RequirementVersion> result = Collections.emptyList();
		int countAll = 0;

		return new PagingBackedPagedCollectionHolder<>(sorting, countAll, result);
	}

}
