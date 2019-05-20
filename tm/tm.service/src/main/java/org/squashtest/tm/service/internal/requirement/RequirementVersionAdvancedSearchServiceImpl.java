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

import com.querydsl.core.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.QueryCufLabel;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchQueryModelToConfiguredQueryConverter;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.query.QueryProcessingServiceImpl;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.query.ConfiguredQuery;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service("squashtest.tm.service.RequirementVersionAdvancedSearchService")
public class RequirementVersionAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements
	RequirementVersionAdvancedSearchService {

	private static final Map<String, String> COLUMN_PROTOTYPE_MAPPING = new HashMap() {{
		put("project-name", "REQUIREMENT_PROJECT_LABEL");
		put("requirement-id", "REQUIREMENT_ID");
		put("requirement-reference", "REQUIREMENT_VERSION_REFERENCE");
		put("requirement-label", "REQUIREMENT_VERSION_NAME");
		put("requirement-criticality", "REQUIREMENT_VERSION_CRITICALITY");
		put("requirement-category", "REQUIREMENT_VERSION_CATEGORY");
		put("requirement-status", "REQUIREMENT_VERSION_STATUS");
		put("requirement-milestone-nb", "REQUIREMENT_VERSION_MILCOUNT");
		put("requirement-version", "REQUIREMENT_VERSION_VERS_NUM");
		put("requirement-version-nb", "REQUIREMENT_NB_VERSIONS");
		put("requirement-attachment-nb", "REQUIREMENT_VERSION_ATTCOUNT");
		put("requirement-created-by", "REQUIREMENT_VERSION_CREATED_BY");
		put("requirement-modified-by", "REQUIREMENT_VERSION_MODIFIED_BY");
		//TODO create columnPrototype
		put("links", "");
		put("attachments", "REQUIREMENT_VERSION_ATTCOUNT");
		put("category", "REQUIREMENT_VERSION_CATEGORY");
		put("createdBy", "REQUIREMENT_VERSION_CREATED_BY");
		put("createdOn", "REQUIREMENT_VERSION_CREATED_ON");
		put("criticality", "REQUIREMENT_VERSION_CRITICALITY");
		put("description", "REQUIREMENT_VERSION_DESCRIPTION");
		put("hasDescription", "REQUIREMENT_VERSION_DESCRIPTION");
		put("lastModifiedBy", "REQUIREMENT_VERSION_MODIFIED_BY");
		put("lastModifiedOn", "REQUIREMENT_VERSION_MODIFIED_ON");
		put("link-type", "REQUIREMENT_LINK_LINK_TYPE");
		put("name", "REQUIREMENT_VERSION_NAME");
		//TODO create columnPrototype
		put("parent", "");
		put("reference", "REQUIREMENT_VERSION_REFERENCE");
		//TODO create columnPrototype
		put("requirement.children", "");
		put("requirement.id", "REQUIREMENT_ID");
		put("requirement.project.id", "REQUIREMENT_PROJECT");
		put("testcases", "REQUIREMENT_VERSION_TCCOUNT");
		put(QueryCufLabel.TAGS, "REQUIREMENT_VERSION_CUF_TAG");
		put(QueryCufLabel.CF_LIST, "REQUIREMENT_VERSION_CUF_LIST");
		put(QueryCufLabel.CF_SINGLE, "REQUIREMENT_VERSION_CUF_TEXT");
		put(QueryCufLabel.CF_TIME_INTERVAL, "REQUIREMENT_VERSION_CUF_DATE");
		put(QueryCufLabel.CF_NUMERIC, "REQUIREMENT_VERSION_CUF_NUMERIC");
		put(QueryCufLabel.CF_CHECKBOX, "REQUIREMENT_VERSION_CUF_CHECKBOX");
	}};

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementVersionAdvancedSearchServiceImpl.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private Provider<AdvancedSearchQueryModelToConfiguredQueryConverter> converterProvider;

	@Inject
	private QueryProcessingServiceImpl dataFinder;

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

	@SuppressWarnings("unchecked")
	@Override
	public Page<RequirementVersion> searchForRequirementVersions(AdvancedSearchQueryModel model,
																 Pageable sorting, MessageSource source, Locale locale) {

		AdvancedSearchQueryModelToConfiguredQueryConverter converter = converterProvider.get();
		ConfiguredQuery configuredQuery = converter.configureModel(model).configureMapping(COLUMN_PROTOTYPE_MAPPING).convert();
		List<Tuple> tuples = dataFinder.executeQuery(configuredQuery);
		tuples.size();
		List<RequirementVersion> result = Collections.emptyList();
		int countAll = 0;

		return new PageImpl(result, sorting, countAll);
	}

}
