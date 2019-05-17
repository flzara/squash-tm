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

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("squashtest.tm.service.RequirementVersionAdvancedSearchService")
public class RequirementVersionAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	RequirementVersionAdvancedSearchService {

	private Map<String, String> COLUMN_PROTOTYPE_MAPPING = Stream.of(
		//TODO create columnPrototype
		new AbstractMap.SimpleImmutableEntry<>("project-name", ""),
		new AbstractMap.SimpleImmutableEntry<>("requirement-id", "REQUIREMENT_ID"),
		new AbstractMap.SimpleImmutableEntry<>("requirement-reference", "REQUIREMENT_VERSION_REFERENCE"),
		//TODO create columnPrototype
		new AbstractMap.SimpleImmutableEntry<>("requirement-label", ""),
		new AbstractMap.SimpleImmutableEntry<>("requirement-criticality", "REQUIREMENT_VERSION_CRITICALITY"),
		new AbstractMap.SimpleImmutableEntry<>("requirement-category", "REQUIREMENT_VERSION_CATEGORY"),
		new AbstractMap.SimpleImmutableEntry<>("requirement-status", "REQUIREMENT_VERSION_STATUS"),
		new AbstractMap.SimpleImmutableEntry<>("requirement-milestone-nb", "REQUIREMENT_VERSION_MILCOUNT"),
		new AbstractMap.SimpleImmutableEntry<>("requirement-version", "REQUIREMENT_VERSION_VERS_NUM"),
		new AbstractMap.SimpleImmutableEntry<>("requirement-version-nb", "REQUIREMENT_NB_VERSIONS"),
		//TODO create columnPrototype
		new AbstractMap.SimpleImmutableEntry<>("requirement-attachment-nb", ""),
		new AbstractMap.SimpleImmutableEntry<>("requirement-created-by", "REQUIREMENT_VERSION_CREATED_BY"),
		new AbstractMap.SimpleImmutableEntry<>("requirement-modified-by", "REQUIREMENT_VERSION_MODIFIED_BY"),
		//TODO create columnPrototype
		new AbstractMap.SimpleImmutableEntry<>("links", ""))
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	Map<String, String> articles
		= ImmutableMap.of("project-name", "My New Article", "requirement-reference", "Second Article"
		, );
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
	public Page<RequirementVersion> searchForRequirementVersions(AdvancedSearchModel model,
																 Pageable sorting, MessageSource source, Locale locale) {


		List<RequirementVersion> result = Collections.emptyList();
		int countAll = 0;

		return new PageImpl(result, sorting, countAll);
	}

}
