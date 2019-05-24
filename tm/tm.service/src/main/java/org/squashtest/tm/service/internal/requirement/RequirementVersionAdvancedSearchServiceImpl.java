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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.AdvancedSearchRangeFieldModel;
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
		put("project-name", "REQUIREMENT_PROJECT_LABEL"); put("requirement-id", "REQUIREMENT_ID");
		put("requirement-reference", "REQUIREMENT_VERSION_REFERENCE"); put("requirement-label", "REQUIREMENT_VERSION_NAME");
		put("requirement-criticality", "REQUIREMENT_VERSION_CRITICALITY"); put("requirement-category", "REQUIREMENT_VERSION_CATEGORY");
		put("requirement-status", "REQUIREMENT_VERSION_STATUS"); put("requirement-milestone-nb", "REQUIREMENT_VERSION_MILCOUNT");
		put("requirement-version", "REQUIREMENT_VERSION_VERS_NUM");	put("requirement-version-nb", "REQUIREMENT_NB_VERSIONS");
		put("requirement-testcase-nb", "REQUIREMENT_VERSION_TCCOUNT"); put("requirement-attachment-nb", "REQUIREMENT_VERSION_ATTCOUNT");
		put("requirement-created-by", "REQUIREMENT_VERSION_CREATED_BY"); put("requirement-modified-by", "REQUIREMENT_VERSION_MODIFIED_BY");
		put("attachments", "REQUIREMENT_VERSION_ATTCOUNT");	put("category", "REQUIREMENT_VERSION_CATEGORY");
		put("createdBy", "REQUIREMENT_VERSION_CREATED_BY");	put("createdOn", "REQUIREMENT_VERSION_CREATED_ON");
		put("criticality", "REQUIREMENT_VERSION_CRITICALITY"); put("description", "REQUIREMENT_VERSION_DESCRIPTION");
		put("hasDescription", "REQUIREMENT_VERSION_DESCRIPTION"); put("lastModifiedBy", "REQUIREMENT_VERSION_MODIFIED_BY");
		put("lastModifiedOn", "REQUIREMENT_VERSION_MODIFIED_ON"); put("link-type", "REQUIREMENT_LINK_LINK_TYPE");
		put("name", "REQUIREMENT_VERSION_NAME"); put("reference", "REQUIREMENT_VERSION_REFERENCE");
		put("requirement.id", "REQUIREMENT_ID"); put("requirement.project.id", "REQUIREMENT_PROJECT");
		put("testcases", "REQUIREMENT_VERSION_TCCOUNT"); put(QueryCufLabel.TAGS, "REQUIREMENT_VERSION_CUF_TAG");
		put(QueryCufLabel.CF_LIST, "REQUIREMENT_VERSION_CUF_LIST");	put(QueryCufLabel.CF_SINGLE, "REQUIREMENT_VERSION_CUF_TEXT");
		put(QueryCufLabel.CF_TIME_INTERVAL, "REQUIREMENT_VERSION_CUF_DATE"); put(QueryCufLabel.CF_NUMERIC, "REQUIREMENT_VERSION_CUF_NUMERIC");
		put(QueryCufLabel.CF_CHECKBOX, "REQUIREMENT_VERSION_CUF_CHECKBOX");	}};

	private static final String IS_CURRENT_VERSION = "isCurrentVersion";
	private static final String LINKS = "links";
	private static final String LINK_TYPE = "link-type";
	private static final String PARENT = "parent";
	private static final String REQUIREMENT_CHILDREN = "requirement.children";


	private static final List<String> PARAMS_NOT_PROJECTIONS = Arrays.asList("entity-index",
		"empty-openinterface2-holder", "empty-opentree-holder", "editable", "project-name", "links");

	private static final List<String> NOT_QUERYING_FILTERS =  Arrays.asList(IS_CURRENT_VERSION, LINKS, LINK_TYPE,
		PARENT, REQUIREMENT_CHILDREN);

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
	public List<RequirementVersion> searchForRequirementVersions(AdvancedSearchQueryModel model, Locale locale) {

		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<RequirementVersion> searchForRequirementVersions(AdvancedSearchQueryModel model,
																 Pageable sorting, MessageSource source, Locale locale) {

		AdvancedSearchQueryModelToConfiguredQueryConverter converter = converterProvider.get();

		Map<String, AdvancedSearchFieldModel> fieldsNotQuerying = collectFieldsNotQuerying(model.getModel());

		ConfiguredQuery configuredQuery = converter.configureModel(model).configureMapping(COLUMN_PROTOTYPE_MAPPING).convert();

		ExtendedHibernateQuery query = dataFinder.prepareQuery(configuredQuery);

		addFilterOnNotQueryingField(query, fieldsNotQuerying);

		List<RequirementVersion> result = Collections.emptyList();
		int countAll = 0;

		return new PageImpl(result, sorting, countAll);
	}

	private Map<String, AdvancedSearchFieldModel> collectFieldsNotQuerying(AdvancedSearchModel model) {
		Map<String, AdvancedSearchFieldModel> modelMap = new HashMap<>();
		for (String key : NOT_QUERYING_FILTERS) {
			if (model.getFields().containsKey(key)) {
				AdvancedSearchFieldModel fieldModel = model.getFields().get(key);
				modelMap.put(key, fieldModel);
				model.getFields().remove(key);
			}

		}
		return modelMap;
	}

	/**
	 * The engine is not able to express some predicates. We must add them to the query.
	 * @param query
	 * @param filters
	 */
	private void addFilterOnNotQueryingField(ExtendedHibernateQuery query, Map<String, AdvancedSearchFieldModel> filters) {

		for (Map.Entry<String, AdvancedSearchFieldModel> fieldModelEntry: filters.entrySet()) {
			AdvancedSearchFieldModel model = fieldModelEntry.getValue();
			String key = fieldModelEntry.getKey();

			if (key.equals(IS_CURRENT_VERSION)) {
				addCurrentVersionFilter(query, model);
			} else if (key.equals(LINKS)) {
				addLinksFilter(query, model);
			} else if (key.equals(LINK_TYPE)) {
				addLinkTypeFilter(query, model);
			} else if (key.equals(PARENT)) {
				addParentFilter(query, model);
			} else if (key.equals(REQUIREMENT_CHILDREN)) {
				addRequirementChildrenFilter(query, model);
			}

		}
	}

	private void addCurrentVersionFilter(ExtendedHibernateQuery query, AdvancedSearchFieldModel fieldModel) {

	}

	private void addLinksFilter(ExtendedHibernateQuery query, AdvancedSearchFieldModel fieldModel) {

	}

	private void addLinkTypeFilter(ExtendedHibernateQuery query, AdvancedSearchFieldModel fieldModel) {

	}

	private void addParentFilter(ExtendedHibernateQuery query, AdvancedSearchFieldModel fieldModel) {

	}

	private void addRequirementChildrenFilter(ExtendedHibernateQuery query, AdvancedSearchFieldModel fieldModel) {

	}
}
