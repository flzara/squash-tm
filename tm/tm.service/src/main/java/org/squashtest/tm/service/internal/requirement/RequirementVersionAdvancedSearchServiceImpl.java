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
import com.querydsl.jpa.hibernate.HibernateQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.jpql.ExtHQLTemplates;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchModel;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.QueryCufLabel;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchQueryModelToConfiguredQueryConverter;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.query.QueryProcessingServiceImpl;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.query.ConfiguredQuery;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.*;

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
import java.util.stream.Collectors;


@Transactional(readOnly = true)
@Service("squashtest.tm.service.RequirementVersionAdvancedSearchService")
public class RequirementVersionAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements RequirementVersionAdvancedSearchService {

	/**
	 * This is initialized in a static block at the end of the class definition
	 */
	private static final AdvancedSearchColumnMappings MAPPINGS = new AdvancedSearchColumnMappings("requirement-id");


	private static final String IS_CURRENT_VERSION = "isCurrentVersion";
	private static final String LINKS = "links";
	private static final String LINK_TYPE = "link-type";
	private static final String PARENT = "parent";
	private static final String REQUIREMENT_CHILDREN = "requirement.children";


	private static final List<String> PARAMS_NOT_PROJECTIONS = Arrays.asList("entity-index",
		"empty-openinterface2-holder", "empty-opentree-holder", "editable", "project-name", "links");

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

		Session session = entityManager.unwrap(Session.class);
		// prepare the query

		AdvancedSearchQueryModelToConfiguredQueryConverter converter = converterProvider.get();

		converter.configureModel(model).configureMapping(MAPPINGS);

		HibernateQuery<Tuple> query = converter.prepare();
		
		// attach a session
		query = query.clone(session);
		

		// round 1 : find our paged requirement version ids

		List<Tuple> tuples = query.fetch();
		List<Long> ids = tuples.stream().map(tuple -> tuple.get(0, Long.class)).collect(Collectors.toList());

		// round 2 : get the total count (remove the paging)

		HibernateQuery<?> noPagingQuery = query.clone(session);
		noPagingQuery.limit(Long.MAX_VALUE);
		noPagingQuery.offset(0);
		long count = noPagingQuery.fetchCount();

		// round 3 : now get the actual requirement versions

		List<RequirementVersion> result = requirementVersionDao.findAllById(ids);


		return new PageImpl(result, sorting, count);
	}


	// ******************* column mappings  *******************************************************


	static {

		/* **************************************************
		 *
		 * 		Input form columns registry
		 *
		 *
		 *****************************************************/
		MAPPINGS.getFormMapping()
				.map("requirement.project.id", REQUIREMENT_PROJECT_ID)
				.map("requirement.id", REQUIREMENT_ID)
				.map("reference", REQUIREMENT_VERSION_REFERENCE)
				.map("name", REQUIREMENT_VERSION_NAME)
				.map("description", REQUIREMENT_VERSION_DESCRIPTION)
				.map("criticality", REQUIREMENT_VERSION_CRITICALITY)
				.map("category", REQUIREMENT_VERSION_CATEGORY)
				.map("status", REQUIREMENT_VERSION_STATUS)
				.map("milestone.label", REQUIREMENT_VERSION_MILESTONE_LABEL)
				.map("milestone.status", REQUIREMENT_VERSION_MILESTONE_STATUS)
				.map("milestone.endDate", REQUIREMENT_VERSION_MILESTONE_END_DATE)
				.map("testcases", REQUIREMENT_VERSION_TCCOUNT);


			/*	TODOs :
				.mapHandler("requirement.children", null)
				.mapHandler("parent", null)
				.mapHandler("link-type", null);
			*/



		/* **************************************************
		 *
		 * 		Result Table columns registry
		 *
		 *
		 *****************************************************/
		MAPPINGS.getResultMapping()
				.map("project-name", REQUIREMENT_PROJECT_NAME)
				.map("requirement-id", REQUIREMENT_VERSION_ID)
				.map("requirement-reference", REQUIREMENT_VERSION_REFERENCE)
				.map("requirement-label", REQUIREMENT_VERSION_NAME)
				.map("requirement-criticality", REQUIREMENT_VERSION_CRITICALITY)
				.map("requirement-category", REQUIREMENT_VERSION_CATEGORY)
				.map("requirement-status", REQUIREMENT_VERSION_STATUS)
				.map("requirement-milestone-nb", REQUIREMENT_VERSION_MILCOUNT)
				.map("requirement-version", REQUIREMENT_VERSION_VERS_NUM)
				.map("requirement-version-nb", REQUIREMENT_NB_VERSIONS)
				.map("requirement-testcase-nb", REQUIREMENT_VERSION_TCCOUNT)
				.map("requirement-attachment-nb", REQUIREMENT_VERSION_ATTCOUNT)
				.map("requirement-created-by", REQUIREMENT_VERSION_CREATED_BY)
				.map("requirement-modified-by", REQUIREMENT_VERSION_MODIFIED_BY);

				/*
				*TODOs
				*
				.mapHandler("link", null);
			 	*/


		/* **************************************************
		 *
		 * 		Custom fields columns registry
		 *
		 *
		 *****************************************************/
		MAPPINGS.getCufMapping()
				.map(QueryCufLabel.TAGS.toString(), REQUIREMENT_VERSION_CUF_TAG)
				.map(QueryCufLabel.CF_LIST.toString(), REQUIREMENT_VERSION_CUF_LIST)
				.map(QueryCufLabel.CF_SINGLE.toString(), REQUIREMENT_VERSION_CUF_TEXT)
				.map(QueryCufLabel.CF_TIME_INTERVAL.toString(), REQUIREMENT_VERSION_CUF_DATE)
				.map(QueryCufLabel.CF_NUMERIC.toString(), REQUIREMENT_VERSION_CUF_NUMERIC)
				.map(QueryCufLabel.CF_CHECKBOX.toString(), REQUIREMENT_VERSION_CUF_CHECKBOX);

	}


	
	

}
