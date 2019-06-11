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
import com.querydsl.core.types.dsl.Expressions;
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

import static org.squashtest.tm.domain.requirement.QRequirementVersion.requirementVersion;

import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;
import org.squashtest.tm.domain.requirement.QRequirement;
import org.squashtest.tm.domain.requirement.QRequirementPathEdge;
import org.squashtest.tm.domain.requirement.QRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModel;
import org.squashtest.tm.domain.search.AdvancedSearchFieldModelType;
import org.squashtest.tm.domain.search.AdvancedSearchQueryModel;
import org.squashtest.tm.domain.search.AdvancedSearchRangeFieldModel;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchColumnMappings.SpecialHandler;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchQueryModelToConfiguredQueryConverter;
import org.squashtest.tm.service.internal.advancedsearch.AdvancedSearchServiceImpl;
import org.squashtest.tm.service.internal.query.QueryProcessingServiceImpl;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.requirement.RequirementVersionAdvancedSearchService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_NB_VERSIONS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_PROJECT_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_PROJECT_NAME;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_ATTCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CATEGORY;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CREATED_BY;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CRITICALITY;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CUF_CHECKBOX;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CUF_DATE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CUF_LIST;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CUF_NUMERIC;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CUF_TAG;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_CUF_TEXT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_DESCRIPTION;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_ID;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_MILCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_MILESTONE_END_DATE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_MILESTONE_LABEL;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_MILESTONE_STATUS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_MODIFIED_BY;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_NAME;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_REFERENCE;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_STATUS;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_TCCOUNT;
import static org.squashtest.tm.domain.query.QueryColumnPrototypeReference.REQUIREMENT_VERSION_VERS_NUM;







@Transactional(readOnly = true)
@Service("squashtest.tm.service.RequirementVersionAdvancedSearchService")
public class RequirementVersionAdvancedSearchServiceImpl extends AdvancedSearchServiceImpl implements RequirementVersionAdvancedSearchService {

	/**
	 * This is initialized in a static block at the end of the class definition
	 */
	private static final AdvancedSearchColumnMappings MAPPINGS = new AdvancedSearchColumnMappings(requirementVersion);


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

		
		// round 1 : find our paged requirement versions
		HibernateQuery<RequirementVersion> query = converter.prepareFetchQuery();
		query = query.clone(session);		
		List<RequirementVersion> versions = query.fetch();

		// round 2 : get the total count (remove the paging)
		HibernateQuery<Long> countQuery = converter.prepareCountQuery();
		countQuery = countQuery.clone(session);
		long count = countQuery.fetchCount();

		return new PageImpl(versions, sorting, count);

	}
	

	
	// ******************* special handling *******************************************************
	

	// tests that the requirement has children by a subquery of the form 'select 1 from where <predicate>'
	// this form is less performant but has a good isolation from the main query, which makes its robusts
	// to alteration of the outer query.
	private static void createFilterHaveChildren(ExtendedHibernateQuery<?> query, AdvancedSearchFieldModel model) {

		AdvancedSearchRangeFieldModel range = (AdvancedSearchRangeFieldModel) model;

		QRequirement parentRequirement = new QRequirement("parentRequirement");
		QRequirementVersion parentVersion = new QRequirementVersion("parentVersion");

		// This is the column on which we join with the outer query. We already know that the engine will
		// select the default alias for requirementVersion.
		QRequirementVersion outerVersion = requirementVersion;


		HibernateQuery<Integer> subquery = new ExtendedHibernateQuery<>()
												 .select(Expressions.ONE)
												 .from(parentRequirement)
												 .join(parentRequirement.versions, parentVersion)
												 .where(parentVersion.id.eq(outerVersion.id))
													.groupBy(parentRequirement.id);

		// now check if we need to verify that at least one relation exist, or at most zero :
		if (range.hasMaxValue()){
			subquery.having(parentRequirement.children.size().eq(0));
		}
		else{
			subquery.having(parentRequirement.children.size().gt(0));
		}

		// append to the superquery
		query.where(subquery.exists());


	}


	// same remark than for createFilterHaveChildren
	private static void createFilterHaveParent(ExtendedHibernateQuery<?> query, AdvancedSearchFieldModel model) {

		AdvancedSearchRangeFieldModel range = (AdvancedSearchRangeFieldModel) model;

		
		QRequirement parentRequirement = new QRequirement("parentRequirement");
		QRequirement childRequirement = new QRequirement("childRequirement");
		QRequirementVersion childVersion = new QRequirementVersion("childVersion");


		// This is the column on which we join with the outer query. We already know that the engine will
		// select the default alias for requirementVersion.
		QRequirementVersion outerVersion = requirementVersion;

		HibernateQuery<Integer> subquery = new ExtendedHibernateQuery<>()
												.select(Expressions.ONE)
												.from(parentRequirement)
												.join(parentRequirement.children, childRequirement)
												.join(childRequirement.versions, childVersion)
												.where(childVersion.id.eq(outerVersion.id));


		// now check if we need to verify that at least one relation exist, or at most zero :
		if (range.hasMaxValue()){
			query.where(subquery.notExists());
		}
		else{
			query.where(subquery.exists());
		}


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
				.map("testcases", REQUIREMENT_VERSION_TCCOUNT)


				
				.mapHandler("requirement.children", new SpecialHandler(RequirementVersionAdvancedSearchServiceImpl::createFilterHaveChildren))				
				.mapHandler("parent", new SpecialHandler(RequirementVersionAdvancedSearchServiceImpl::createFilterHaveParent));
				/*
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
				.map(AdvancedSearchFieldModelType.TAGS.toString(), REQUIREMENT_VERSION_CUF_TAG)
				.map(AdvancedSearchFieldModelType.CF_LIST.toString(), REQUIREMENT_VERSION_CUF_LIST)
				.map(AdvancedSearchFieldModelType.CF_SINGLE.toString(), REQUIREMENT_VERSION_CUF_TEXT)
				.map(AdvancedSearchFieldModelType.CF_TIME_INTERVAL.toString(), REQUIREMENT_VERSION_CUF_DATE)
				.map(AdvancedSearchFieldModelType.CF_NUMERIC_RANGE.toString(), REQUIREMENT_VERSION_CUF_NUMERIC)
				.map(AdvancedSearchFieldModelType.CF_CHECKBOX.toString(), REQUIREMENT_VERSION_CUF_CHECKBOX);

	}


	
	

}
