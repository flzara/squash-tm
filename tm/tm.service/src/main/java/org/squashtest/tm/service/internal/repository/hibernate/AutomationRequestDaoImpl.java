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
package org.squashtest.tm.service.internal.repository.hibernate;


import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.hibernate.HibernateQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.domain.IdCollector;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQueryFactory;
import org.squashtest.tm.domain.project.QProject;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.tf.automationrequest.QAutomationRequest;
import org.squashtest.tm.domain.users.QUser;
import org.squashtest.tm.service.internal.repository.CustomAutomationRequestDao;
import static org.squashtest.tm.service.internal.api.repository.PagingToQueryDsl.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


public class AutomationRequestDaoImpl implements CustomAutomationRequestDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomationRequestDaoImpl.class);

	@PersistenceContext
	private EntityManager entityManager;


	@Override
	public Page<AutomationRequest> findAll(Pageable pageable, ColumnFiltering filtering) {

		LOGGER.debug("searching for automation requests, paged and filtered");

		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("page size : {}, page num : {}, filter by : {}",
				pageable.getPageSize(),
				pageable.getPageNumber(),
				filtering.getFilteredAttributes());
		}

		LOGGER.trace("fetching automation requests");
		List<AutomationRequest> requests = findRequests(pageable, filtering);

		LOGGER.trace("counting automation requests");
		long count = countRequests(filtering);

		return new PageImpl<AutomationRequest>(requests, pageable, count);

	}






	// *************** boilerplate ****************


	private List<AutomationRequest> findRequests(Pageable pageable, ColumnFiltering filtering){

		// create base query
		HibernateQuery<AutomationRequest> fetchRequest = createFindAllBaseQuery();

		// apply paging
		fetchRequest.offset(pageable.getOffset()).limit(pageable.getPageSize());

		// apply sorting
		OrderSpecifier<?>[] orderSpecifiers = toQueryDslSorting(pageable.getSort());

		fetchRequest.orderBy(orderSpecifiers);

		// apply filter
		Predicate predicate = toQueryDslPredicate(filtering);

		fetchRequest.where(predicate);

		// fetch
		List<AutomationRequest> requests = fetchRequest.fetch();

		if (LOGGER.isTraceEnabled()) {
			List<Long> ids = IdCollector.collect(requests);
			LOGGER.trace("found {} automation requests, ids are : {}", requests.size(), ids);
		}

		return requests;

	}

	private Predicate toQueryDslPredicate(ColumnFiltering filtering) {
		return filterConverter(AutomationRequest.class)
				   .from(filtering)
				   		// types not mentioned here are considered as String
				   		.typeFor("requestStatus").isClass(AutomationRequestStatus.class)
				   		.typeFor("kind").isClass(TestCaseKind.class)
				   		.typeFor("id", "automationPriority").isClass(Long.class)

				   		// filter operation not mentioned here are considered as Equality (or Like if the property is a String)
				   		.compare(
				   			"transmissionDate",
							"assignmentDate",
							"transmittedBy")
				   		.withBetweenDates()
				   .build();
	}

	private OrderSpecifier<?>[] toQueryDslSorting(Sort sort) {
		return sortConverter(AutomationRequest.class)
			  .from(sort)
				   .typeFor("requestStatus")
					.isClass(AutomationRequestStatus.class)
			  .build();
	}

	private long countRequests(ColumnFiltering filtering){

		LOGGER.trace("counting all automation requests, filter by : {}", filtering.getFilteredAttributes());

		// create base query
		HibernateQuery<AutomationRequest> countRequest = createFindAllBaseQuery();

		// apply filter
		Predicate predicate = toQueryDslPredicate(filtering);

		countRequest.where(predicate);

		// counting
		long count = countRequest.fetchCount();

		return count;
	}



	private Session getSession(){
		return entityManager.unwrap(Session.class);
	}


	private HibernateQuery<AutomationRequest> createFindAllBaseQuery(){

		QAutomationRequest request = QAutomationRequest.automationRequest;
		QTestCase testCase = QTestCase.testCase;
		QProject project = QProject.project1;
		QUser assignedTo = new QUser("assignedTo");
		QUser transmittedBy = new QUser("transmittedBy");
		QUser createdBy = new QUser("createdBy");

		return (HibernateQuery<AutomationRequest>) new ExtendedHibernateQueryFactory(getSession())
					.from(request)
					.leftJoin(request.testCase, testCase)
					.leftJoin(testCase.project, project)
					.leftJoin(request.assignedTo, assignedTo)
					.leftJoin(request.transmittedBy, transmittedBy)
					.leftJoin(request.createdBy, createdBy);
	}



}
