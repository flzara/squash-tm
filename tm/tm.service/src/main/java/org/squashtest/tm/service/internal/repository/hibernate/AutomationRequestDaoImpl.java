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
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.core.foundation.collection.SimpleColumnFiltering;
import org.squashtest.tm.domain.IdCollector;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQueryFactory;
import org.squashtest.tm.domain.project.QProject;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCaseAutomatable;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.tf.automationrequest.QAutomationRequest;
import org.squashtest.tm.domain.users.QUser;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.tf.IllegalAutomationRequestStatusException;
import org.squashtest.tm.service.internal.repository.CustomAutomationRequestDao;
import org.squashtest.tm.service.internal.repository.UserDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;

import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.service.internal.helper.PagingToQueryDsl.*;


public class AutomationRequestDaoImpl implements CustomAutomationRequestDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomationRequestDaoImpl.class);

	private static final String ILLEGAL_STATUS = "One or more AutomationRequest do not have the expected status";

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private DSLContext DSL;

	@Inject
	private UserDao userDao;

	@Override
	public Page<AutomationRequest> findAll(Pageable pageable, Collection<Long> inProjectIds) {

		LOGGER.debug("searching for automation requests, paged");

		return innerFindAll(pageable, new SimpleColumnFiltering(), null, inProjectIds);

	}

	@Override
	public Page<AutomationRequest> findAll(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds) {

		LOGGER.debug("searching for automation requests, paged and filtered");

		return innerFindAll(pageable, filtering, null, inProjectIds);

	}

	@Override
	public Page<AutomationRequest> findAllForAssignee(String username, Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds) {
		LOGGER.debug("searching for automation requests, paged and filtered for user : '{}'", username);

		ColumnFiltering filterWithAssignee = new SimpleColumnFiltering(filtering)
												 .addFilter("assignedTo.login", username)
												.addFilter("requestStatus", AutomationRequestStatus.WORK_IN_PROGRESS.toString());

		return innerFindAll(pageable, filterWithAssignee, (converter) -> {
			// force equality comparison for the assigned user login
			converter.compare("assignedTo.login, requestStatus").withEquality();
		}, inProjectIds);
	}

	@Override
	public Page<AutomationRequest> findAllForTraitment(Pageable pageable, ColumnFiltering columnFiltering, Collection<Long> inProjectIds) {
		ColumnFiltering filterWithTraitment = new SimpleColumnFiltering(columnFiltering).addFilter("requestStatus", AutomationRequestStatus.TRANSMITTED.toString());
		return innerFindAll(pageable, filterWithTraitment, (converter) -> {
			converter.compare("requestStatus").withEquality();
		}, inProjectIds);
	}

	@Override
	public Page<AutomationRequest> findAllForGlobal(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds) {
		LOGGER.debug("searching for automation requests, paged and filtered");

		ColumnFiltering filterWithAssignee;

		if (filtering.getFilter("requestStatus").isEmpty()) {
			filterWithAssignee = new SimpleColumnFiltering(filtering).addFilter("requestStatus", AutomationRequestStatus.WORK_IN_PROGRESS.toString() + ";"
				+ AutomationRequestStatus.TRANSMITTED.toString() + ";" + AutomationRequestStatus.EXECUTABLE.toString());
			return innerFindAll(pageable, filterWithAssignee, (converter) -> {
				converter.compare("requestStatus").withIn();
			}, inProjectIds);
		} else  {
			filterWithAssignee = new SimpleColumnFiltering(filtering);
			return innerFindAll(pageable, filterWithAssignee, (converter) -> {
				converter.compare("requestStatus").withEquality();
			}, inProjectIds);
		}
	}

	@Override
	public Page<AutomationRequest> findAllValid(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds) {
		ColumnFiltering filterWithAssignee = new SimpleColumnFiltering(filtering)
			.addFilter("requestStatus", AutomationRequestStatus.VALID.toString());

		return innerFindAll(pageable, filterWithAssignee, (converter) -> {
			// force equality comparison for the assigned user login
			converter.compare("requestStatus").withEquality();
		}, inProjectIds);
	}

	@Override
	public Page<AutomationRequest> findAllToValidate(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds) {
		ColumnFiltering filter;
		if (filtering.getFilter("requestStatus").isEmpty()) {
			filter = new SimpleColumnFiltering(filtering).addFilter("requestStatus", AutomationRequestStatus.OBSOLETE.toString() + ";"
				+ AutomationRequestStatus.TO_VALIDATE.toString() + ";" + AutomationRequestStatus.NOT_AUTOMATABLE.toString());
			return innerFindAll(pageable, filter, (converter) -> {
				converter.compare("requestStatus").withIn();
			}, inProjectIds);
		} else  {
			filter = new SimpleColumnFiltering(filtering);
			return innerFindAll(pageable, filter, (converter) -> {
				converter.compare("requestStatus").withEquality();
			}, inProjectIds);
		}

	}


	@Override
	public Integer countAutomationRequestForCurrentUser(Long idUser) {

		return DSL.selectCount()
			.from(AUTOMATION_REQUEST)
			.innerJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(AUTOMATION_REQUEST.TEST_CASE_ID))
			.innerJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE.TCLN_ID.eq(TEST_CASE_LIBRARY_NODE.TCLN_ID))
			.innerJoin(PROJECT).on(PROJECT.PROJECT_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
			.where(AUTOMATION_REQUEST.ASSIGNED_TO.eq(idUser))
			.and(AUTOMATION_REQUEST.REQUEST_STATUS.eq(AutomationRequestStatus.WORK_IN_PROGRESS.toString()))
			.and(PROJECT.ALLOW_AUTOMATION_WORKFLOW.isTrue())
			.and(TEST_CASE.AUTOMATABLE.eq(TestCaseAutomatable.Y.name()))
			.fetchOne().value1();
	}

	@Override
	public Map<Long, String> getTransmittedByForCurrentUser(Long idUser, List<String> requestStatus) {

		Condition condition = org.jooq.impl.DSL.trueCondition();
		if(idUser != null) {
			condition = AUTOMATION_REQUEST.ASSIGNED_TO.eq(idUser);
		}

		return DSL.selectDistinct(CORE_USER.PARTY_ID, CORE_USER.LOGIN).from(CORE_USER)
			.innerJoin(TEST_CASE_LIBRARY_NODE).on(CORE_USER.LOGIN.eq(TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_BY))
			.innerJoin(AUTOMATION_REQUEST).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(AUTOMATION_REQUEST.TEST_CASE_ID))
			.where(condition)
			.and(AUTOMATION_REQUEST.REQUEST_STATUS.in(requestStatus))
			.fetch().intoMap(CORE_USER.PARTY_ID, CORE_USER.LOGIN);

	}

	@Override
	public Map<Long, String> getAssignedToForAutomationRequests() {

		return DSL.selectDistinct(CORE_USER.PARTY_ID, CORE_USER.LOGIN).from(CORE_USER)
					.innerJoin(AUTOMATION_REQUEST).on(CORE_USER.PARTY_ID.eq(AUTOMATION_REQUEST.ASSIGNED_TO))
					.fetch().intoMap(CORE_USER.PARTY_ID, CORE_USER.LOGIN);
	}

	@Override
	public void updateAutomationRequestToAssigned(User user, List<Long> ids) {
		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest req SET req.requestStatus = :reqStatus," +
			" req.assignedTo = :user, req.assignmentDate = :assignedOn WHERE req.id in :ids and req.requestStatus in :reqStatusInitial")
			.setParameter("reqStatus", AutomationRequestStatus.WORK_IN_PROGRESS)
			.setParameter("reqStatusInitial", Arrays.asList(AutomationRequestStatus.TRANSMITTED, AutomationRequestStatus.WORK_IN_PROGRESS, AutomationRequestStatus.EXECUTABLE))
			.setParameter("user", user)
			.setParameter("assignedOn", new Timestamp(new Date().getTime()))
			.setParameter("ids", ids).executeUpdate();

		if(ids.size() != automationRequestUpdates) {
			throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
		}
	}

	@Override
	public void updateAutomationRequestNotAutomatable(List<Long> ids) {
		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest req SET req.requestStatus = :reqStatus WHERE req.id in :ids and req.requestStatus = :reqStatusInitial")
			.setParameter("reqStatus", AutomationRequestStatus.NOT_AUTOMATABLE)
			.setParameter("reqStatusInitial", AutomationRequestStatus.TRANSMITTED)
			.setParameter("ids", ids)
			.executeUpdate();

		if(ids.size() != automationRequestUpdates) {
			throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
		}
	}

	@Override
	public void updateStatusToExecutable(List<Long> ids) {
		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest req SET req.requestStatus = :reqStatus," +
			" req.assignmentDate = NULL, req.assignedTo = NULL WHERE req.id in :ids and req.requestStatus = :reqStatusInitial")
			.setParameter("reqStatus", AutomationRequestStatus.EXECUTABLE)
			.setParameter("reqStatusInitial", AutomationRequestStatus.WORK_IN_PROGRESS)
			.setParameter("ids", ids)
			.executeUpdate();

		if(ids.size() != automationRequestUpdates) {
			throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
		}
	}

	@Override
	public void updatePriority(List<Long> tcIds, Integer priority) {
		entityManager.createQuery("UPDATE AutomationRequest req SET req.automationPriority = :priority WHERE req.testCase.id in :tcIds")
			.setParameter("priority", priority)
			.setParameter("tcIds", tcIds)
			.executeUpdate();
	}

	@Override
	public void unassignRequests(List<Long> requestIds) {
		entityManager.createQuery("update AutomationRequest ar set ar.assignedTo = NULL, ar.assignmentDate = NULL," +
			" ar.requestStatus = :requestStatus where ar.id in :requestIds")
			.setParameter("requestStatus", AutomationRequestStatus.TRANSMITTED)
		    .setParameter("requestIds", requestIds)
			.executeUpdate();
	}

	@Override
	public List<Long> getReqIdsByTcIds(List<Long> tcIds) {
		return DSL.selectDistinct(AUTOMATION_REQUEST.AUTOMATION_REQUEST_ID)
			.from(AUTOMATION_REQUEST)
			.where(AUTOMATION_REQUEST.TEST_CASE_ID.in(tcIds))
			.fetch(AUTOMATION_REQUEST.AUTOMATION_REQUEST_ID);
	}

	@Override
	public void updateStatusToTransmitted(List<Long> reqIds, User transmittedBy) {
		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest ar SET ar.transmissionDate = :transmittedOn, " +
			"ar.requestStatus = :requestStatus, ar.transmittedBy = :transmittedBy where ar.id in :requestIds")
			.setParameter("transmittedOn", new Timestamp(new Date().getTime()))
			.setParameter("requestStatus", AutomationRequestStatus.TRANSMITTED)
			.setParameter("transmittedBy", transmittedBy)
			.setParameter("requestIds", reqIds).executeUpdate();

			if(reqIds.size() != automationRequestUpdates) {
				throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
			}
	}

	@Override
	public void updateStatusToValidate(List<Long> reqIds) {
		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest ar SET ar.transmissionDate = NULL, " +
			"ar.requestStatus = :requestStatus, ar.transmittedBy = NULL where ar.id in :requestIds and ar.requestStatus = :requestInitialStatus")
			.setParameter("requestStatus", AutomationRequestStatus.TO_VALIDATE)
			.setParameter("requestInitialStatus", AutomationRequestStatus.VALID)
			.setParameter("requestIds", reqIds).executeUpdate();

		if(reqIds.size() != automationRequestUpdates) {
			throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
		}
	}

	private Page<AutomationRequest> innerFindAll(Pageable pageable, ColumnFiltering filtering, FilterOverride filterOverride, Collection<Long> inProjectIds){


		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("searching in projects : {}", inProjectIds);
			LOGGER.trace("page size : {}, page num : {}, filter by : {}",
				pageable.getPageSize(),
				pageable.getPageNumber(),
				filtering.getFilteredAttributes());
		}

		//create the base query
		HibernateQuery<AutomationRequest> baseQuery = createFindAllBaseQuery(inProjectIds);

		// apply the filter
		Predicate predicate = toQueryDslPredicate(filtering, filterOverride);

		baseQuery.where(predicate);


		LOGGER.trace("fetching automation requests");
		List<AutomationRequest> requests = findRequests(baseQuery, pageable);

		LOGGER.trace("counting automation requests");
		long count = countRequests(baseQuery);

		return new PageImpl<AutomationRequest>(requests, pageable, count);
	}

	@Override
	public void updateStatusToValide(List<Long> reqIds) {
		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest ar set ar.requestStatus = :reqStatus where ar.id in :reqIds and ar.requestStatus in :reqStatusInitial")
			.setParameter("reqStatus", AutomationRequestStatus.VALID)
			.setParameter("reqStatusInitial", Arrays.asList(AutomationRequestStatus.TO_VALIDATE, AutomationRequestStatus.TRANSMITTED))
			.setParameter("reqIds", reqIds)
			.executeUpdate();
		if(reqIds.size() != automationRequestUpdates) {
			throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
		}
	}

	@Override
	public void updateStatusToObsolete(List<Long> reqIds) {
		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest ar set ar.requestStatus = :reqStatus where ar.id in :reqIds and ar.requestStatus in :reqStatusInitial")
			.setParameter("reqStatus", AutomationRequestStatus.OBSOLETE)
			.setParameter("reqStatusInitial", Arrays.asList(AutomationRequestStatus.EXECUTABLE, AutomationRequestStatus.WORK_IN_PROGRESS))
			.setParameter("reqIds", reqIds)
			.executeUpdate();
		if(reqIds.size() != automationRequestUpdates) {
			throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
		}

	}

	@Override
	public Integer countAutomationRequestValid() {
		return DSL.selectCount()
			.from(AUTOMATION_REQUEST)
			.innerJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(AUTOMATION_REQUEST.TEST_CASE_ID))
			.innerJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE.TCLN_ID.eq(TEST_CASE_LIBRARY_NODE.TCLN_ID))
			.innerJoin(PROJECT).on(PROJECT.PROJECT_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
			.where(AUTOMATION_REQUEST.REQUEST_STATUS.eq(AutomationRequestStatus.VALID.name()))
			.and(PROJECT.ALLOW_AUTOMATION_WORKFLOW.isTrue())
			.and(TEST_CASE.AUTOMATABLE.eq(TestCaseAutomatable.Y.name()))
			.fetchOne()
			.value1();
	}

	// *************** boilerplate ****************

	private List<AutomationRequest> findRequests(HibernateQuery<AutomationRequest> baseQuery, Pageable pageable){

		// first, clone the baseQuery to make sure we won't alter the original
		HibernateQuery<AutomationRequest> fetchRequest = baseQuery.clone();

		// apply paging and sorting
		fetchRequest.offset(pageable.getOffset()).limit(pageable.getPageSize());

		OrderSpecifier<?>[] orderSpecifiers = toQueryDslSorting(pageable.getSort());
		fetchRequest.orderBy(orderSpecifiers);


		List<AutomationRequest> requests = fetchRequest.fetch();

		if (LOGGER.isTraceEnabled()) {
			List<Long> ids = IdCollector.collect(requests);
			LOGGER.trace("found {} automation requests, ids are : {}", requests.size(), ids);
		}

		return requests;

	}



	private long countRequests(HibernateQuery<AutomationRequest> baseQuery){

		// clone the base query
		HibernateQuery<AutomationRequest> countRequest = baseQuery.clone();

		// counting
		long count = countRequest.fetchCount();

		return count;
	}


	private HibernateQuery<AutomationRequest> createFindAllBaseQuery(Collection<Long> inProjectIds){

		QAutomationRequest request = QAutomationRequest.automationRequest;
		QTestCase testCase = QTestCase.testCase;
		QProject project = QProject.project1;
		QUser assignedTo = new QUser("assignedTo");
		QUser transmittedBy = new QUser("transmittedBy");
		QUser createdBy = new QUser("createdBy");

		HibernateQuery<AutomationRequest> querydslRequest = (HibernateQuery<AutomationRequest>) new ExtendedHibernateQueryFactory(getSession())
					.from(request)
					.leftJoin(request.testCase, testCase)
					.leftJoin(testCase.project, project)
					.leftJoin(request.assignedTo, assignedTo)
					.leftJoin(request.transmittedBy, transmittedBy)
					.leftJoin(request.createdBy, createdBy)
					.where(project.id.in(inProjectIds)
						.and(request.testCase.automatable.eq(TestCaseAutomatable.Y))
						.and(project.allowAutomationWorkflow.isTrue()));


		return querydslRequest;
	}




	private Predicate toQueryDslPredicate(ColumnFiltering filtering, FilterOverride override) {
		ColumnFilteringConverter converter = filterConverter(AutomationRequest.class)
				   .from(filtering)
				   // types not mentioned here are considered as String
				   .typeFor("requestStatus").isClass(AutomationRequestStatus.class)
				   .typeFor("testCase.kind").isClass(TestCaseKind.class)
				   .typeFor("id", "automationPriority").isClass(Long.class)
			       .typeFor("testCase.id").isClass(Long.class)
			       .typeFor("createdBy", "transmittedBy", "assignedTo").isClass(Long.class)
				   // filter operation not mentioned here are considered as Equality (or Like if the property is a String)
				   .compare(
					   "transmissionDate",
					   "assignmentDate")
				   .withDates()
					.compare("id", "automationPriority").withEquality();

		// override if necessary
		if (override != null){
			override.accept(converter);
		}

		return converter.build();

	}

	private OrderSpecifier<?>[] toQueryDslSorting(Sort sort) {
		return sortConverter(AutomationRequest.class)
				   .from(sort)
				   .typeFor("requestStatus").isClass(AutomationRequestStatus.class)
				   .build();
	}




	private Session getSession(){
		return entityManager.unwrap(Session.class);
	}


	private static interface FilterOverride extends Consumer<ColumnFilteringConverter>{};

}
