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
import org.squashtest.tm.domain.project.AutomationWorkflowType;
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
import org.squashtest.tm.service.internal.helper.PagingToQueryDsl;
import org.squashtest.tm.service.internal.repository.CustomAutomationRequestDao;
import org.squashtest.tm.service.internal.repository.UserDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.AUTOMATED;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.AUTOMATION_IN_PROGRESS;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.READY_TO_TRANSMIT;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.REJECTED;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.SUSPENDED;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.TRANSMITTED;
import static org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus.WORK_IN_PROGRESS;
import static org.squashtest.tm.jooq.domain.Tables.AUTOMATION_REQUEST;
import static org.squashtest.tm.jooq.domain.Tables.CORE_USER;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_LIBRARY_NODE;
import static org.squashtest.tm.service.internal.helper.PagingToQueryDsl.ColumnFilteringConverter;
import static org.squashtest.tm.service.internal.helper.PagingToQueryDsl.filterConverter;
import static org.squashtest.tm.service.internal.helper.PagingToQueryDsl.sortConverter;


public class AutomationRequestDaoImpl implements CustomAutomationRequestDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomationRequestDaoImpl.class);

	private static final String ILLEGAL_STATUS = "One or more AutomationRequest do not have the expected status";

	private static final String DEFAULT_TRANSMITTED_STATUS_FILTER = String.join(PagingToQueryDsl.LIST_SEPARATOR, AUTOMATION_IN_PROGRESS.toString(), TRANSMITTED.toString());
	private static final String DEFAULT_GLOBAL_STATUS_FILTER = 	String.join(PagingToQueryDsl.LIST_SEPARATOR, WORK_IN_PROGRESS.toString(), TRANSMITTED.toString(), AUTOMATED.toString() );
	private static final String DEFAULT_TO_VALIDATE_FILTER = 	String.join(PagingToQueryDsl.LIST_SEPARATOR, SUSPENDED.toString(), WORK_IN_PROGRESS.toString(), REJECTED.toString() );

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
												 .addFilter("assignedTo.login", username);

		return innerFindAll(pageable, filterWithAssignee, (converter) -> {
			// force equality comparison for the assigned user login
			converter.compare("assignedTo.login").withEquality();
		}, inProjectIds);
	}

	@Override
	public Page<AutomationRequest> findAllTransmitted(Pageable pageable, ColumnFiltering columnFiltering, Collection<Long> inProjectIds) {
		ColumnFiltering filterWithTraitment = overrideStatusAndAssignedToFilter(columnFiltering, DEFAULT_TRANSMITTED_STATUS_FILTER, null);

		return innerFindAll(pageable, filterWithTraitment, (converter) -> {
			converter.compare("requestStatus").withIn()
				.compare("assignedTo").isNull();
		}, inProjectIds);
	}

	@Override
	public Page<AutomationRequest> findAllForGlobal(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds) {

		LOGGER.debug("searching for automation requests, paged and filtered");

		return innerFindAll(pageable, filtering, (converter) -> {
			converter.compare("assignedTo.login").withEquality();
		}, inProjectIds);

	}

	@Override
	public Page<AutomationRequest> findAllValid(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds) {
		ColumnFiltering filterWithAssignee = overrideStatusFilter(filtering, READY_TO_TRANSMIT.toString());

		return innerFindAll(pageable, filterWithAssignee, null, inProjectIds);
	}

	@Override
	public Page<AutomationRequest> findAllToValidate(Pageable pageable, ColumnFiltering filtering, Collection<Long> inProjectIds) {

		ColumnFiltering effective = withStatusFilterOrDefault(filtering, DEFAULT_TO_VALIDATE_FILTER);

		return innerFindAll(pageable, effective, (converter) -> {
			converter.compare("requestStatus").withIn();
		}, inProjectIds);

	}


	@Override
	public Integer countAutomationRequestForCurrentUser(Long idUser) {

		return DSL.selectCount()
			.from(AUTOMATION_REQUEST)
			.innerJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(AUTOMATION_REQUEST.TEST_CASE_ID))
			.innerJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE.TCLN_ID.eq(TEST_CASE_LIBRARY_NODE.TCLN_ID))
			.innerJoin(PROJECT).on(PROJECT.PROJECT_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
			.where(AUTOMATION_REQUEST.ASSIGNED_TO.eq(idUser))
			.and(PROJECT.ALLOW_AUTOMATION_WORKFLOW.isTrue())
			.and(TEST_CASE.AUTOMATABLE.eq(TestCaseAutomatable.Y.toString()))
			.fetchOne().value1();
	}
	@Override
	public Map<Long, String> getTransmittedByForCurrentUser(Long idUser, List<String> requestStatus, List<Long> projectIds) {

		Condition condition = org.jooq.impl.DSL.trueCondition();
		if(idUser != null) {
			condition = AUTOMATION_REQUEST.ASSIGNED_TO.eq(idUser);
		}

		return DSL.selectDistinct(CORE_USER.PARTY_ID, CORE_USER.LOGIN).from(CORE_USER)
			.innerJoin(TEST_CASE_LIBRARY_NODE).on(CORE_USER.LOGIN.eq(TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_BY))
			.innerJoin(TEST_CASE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.innerJoin(PROJECT).on(TEST_CASE_LIBRARY_NODE.PROJECT_ID.eq(PROJECT.PROJECT_ID))
			.innerJoin(AUTOMATION_REQUEST).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(AUTOMATION_REQUEST.TEST_CASE_ID))
			.where(condition)
			.and(AUTOMATION_REQUEST.REQUEST_STATUS.in(requestStatus))
			.and(TEST_CASE.AUTOMATABLE.eq(TestCaseAutomatable.Y.name()))
			.and(PROJECT.ALLOW_AUTOMATION_WORKFLOW.isTrue())
			.and(PROJECT.PROJECT_ID.in(projectIds))
			.orderBy(CORE_USER.LOGIN)
			.fetch().intoMap(CORE_USER.PARTY_ID, CORE_USER.LOGIN);

	}

	@Override
	public Map<Long, String> getTcLastModifiedByToAutomationRequestNotAssigned(List<String> requestStatus, List<Long> projectIds) {

		return DSL.selectDistinct(CORE_USER.PARTY_ID, CORE_USER.LOGIN).from(CORE_USER)
			.innerJoin(TEST_CASE_LIBRARY_NODE).on(CORE_USER.LOGIN.eq(TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_BY))
			.innerJoin(TEST_CASE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.innerJoin(PROJECT).on(TEST_CASE_LIBRARY_NODE.PROJECT_ID.eq(PROJECT.PROJECT_ID))
			.innerJoin(AUTOMATION_REQUEST).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(AUTOMATION_REQUEST.TEST_CASE_ID))
			.where(AUTOMATION_REQUEST.ASSIGNED_TO.isNull())
			.and(AUTOMATION_REQUEST.REQUEST_STATUS.in(requestStatus))
			.and(TEST_CASE.AUTOMATABLE.eq(TestCaseAutomatable.Y.name()))
			.and(PROJECT.ALLOW_AUTOMATION_WORKFLOW.isTrue())
			.and(PROJECT.PROJECT_ID.in(projectIds))
			.orderBy(CORE_USER.LOGIN)
			.fetch().intoMap(CORE_USER.PARTY_ID, CORE_USER.LOGIN);

	}

	@Override
	public Map<Long, String> getAssignedToForAutomationRequests(List<Long> projectIds) {

		return DSL.selectDistinct(CORE_USER.PARTY_ID, CORE_USER.LOGIN).from(CORE_USER)
					.innerJoin(AUTOMATION_REQUEST).on(CORE_USER.PARTY_ID.eq(AUTOMATION_REQUEST.ASSIGNED_TO))
					.innerJoin(TEST_CASE).on(AUTOMATION_REQUEST.TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
					.innerJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
					.innerJoin(PROJECT).on(TEST_CASE_LIBRARY_NODE.PROJECT_ID.eq(PROJECT.PROJECT_ID))
					.where(PROJECT.ALLOW_AUTOMATION_WORKFLOW.isTrue())
					.and(PROJECT.PROJECT_ID.in(projectIds))
					.and(TEST_CASE.AUTOMATABLE.eq(TestCaseAutomatable.Y.name()))
					.orderBy(CORE_USER.LOGIN.asc())
					.fetch().intoMap(CORE_USER.PARTY_ID, CORE_USER.LOGIN);
	}

	@Override
	public void updateAutomationRequestStatus(List<Long> reqIds, AutomationRequestStatus requestStatus, List<AutomationRequestStatus> allowedStatuses) {

		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest req SET req.requestStatus = :requestStatus " +
			"where req.id in :reqIds and req.requestStatus in :allowedStatuses")
			.setParameter("requestStatus", requestStatus)
			.setParameter("reqIds", reqIds)
			.setParameter("allowedStatuses", allowedStatuses)
			.executeUpdate();

		if(reqIds.size() != automationRequestUpdates) {
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
		entityManager.createQuery("update AutomationRequest ar set ar.assignedTo = NULL, ar.assignmentDate = NULL" +
			" where ar.id in :requestIds")
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
			.setParameter("requestStatus", TRANSMITTED)
			.setParameter("transmittedBy", transmittedBy)
			.setParameter("requestIds", reqIds).executeUpdate();

			if(reqIds.size() != automationRequestUpdates) {
				throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
			}
	}

	@Override
	public void updateConflictAssociation(Long testCaseId, String newValue) {
		entityManager.createNamedQuery("AutomationRequest.updateConflictAssociation")
			.setParameter("conflictAssociation", newValue)
			.setParameter("testCaseId", testCaseId)
			.executeUpdate();
	}

	@Override
	public void updateIsManual(Long testCaseId, boolean newValue) {
		entityManager.createNamedQuery("AutomationRequest.updateIsManual")
		.setParameter("isManual", newValue)
		.setParameter("testCaseId", testCaseId)
		.executeUpdate();
	}

	@Override
	public void updateStatusToAutomated(List<Long> reqIds, AutomationRequestStatus requestStatus, List<AutomationRequestStatus> initialStatus) {
		int automationRequestUpdates = entityManager.createQuery("UPDATE AutomationRequest req SET req.requestStatus = :requestStatus " +
			"where req.id in :reqIds and req.requestStatus in :initialStatus and req.transmissionDate is not null")
			.setParameter("requestStatus", requestStatus)
			.setParameter("reqIds", reqIds)
			.setParameter("initialStatus", initialStatus)
			.executeUpdate();

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
	public Integer countAutomationRequestValid(List<Long> readableIds) {
		return DSL.selectCount()
			.from(AUTOMATION_REQUEST)
			.innerJoin(TEST_CASE).on(TEST_CASE.TCLN_ID.eq(AUTOMATION_REQUEST.TEST_CASE_ID))
			.innerJoin(TEST_CASE_LIBRARY_NODE).on(TEST_CASE.TCLN_ID.eq(TEST_CASE_LIBRARY_NODE.TCLN_ID))
			.innerJoin(PROJECT).on(PROJECT.PROJECT_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
			.where(AUTOMATION_REQUEST.REQUEST_STATUS.eq(READY_TO_TRANSMIT.toString()))
			.and(PROJECT.ALLOW_AUTOMATION_WORKFLOW.isTrue())
			.and(TEST_CASE.AUTOMATABLE.eq(TestCaseAutomatable.Y.toString()))
			.and(PROJECT.PROJECT_ID.in(readableIds))
			.fetchOne()
			.value1();
	}

	@Override
	public void assignedToRequestIds(List<Long> reqIds, User user) {
		int automationRequestUpdates = entityManager
			.createQuery("UPDATE AutomationRequest ar SET ar.assignedTo = :assignee, ar.assignmentDate = :assignedOn where ar.id in :reqIds")
			.setParameter("assignee", user)
			.setParameter("reqIds", reqIds)
			.setParameter("assignedOn", new Timestamp(new Date().getTime()))
			.executeUpdate();
		if(reqIds.size() != automationRequestUpdates) {
			throw new IllegalAutomationRequestStatusException(ILLEGAL_STATUS);
		}
	}

	// *************** boilerplate ****************

	private List<AutomationRequest> findRequests(HibernateQuery<AutomationRequest> baseQuery, Pageable pageable){

		// first, clone the baseQuery to make sure we won't alter the original
		HibernateQuery<AutomationRequest> fetchRequest = baseQuery.clone();

		// apply paging and sorting
		fetchRequest.offset(pageable.getOffset()).limit(pageable.getPageSize());

		if (pageable.getSort() != null) {
			OrderSpecifier<?>[] orderSpecifiers = toQueryDslSorting(pageable.getSort());
			fetchRequest.orderBy(orderSpecifiers);
		}

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
						.and(project.allowAutomationWorkflow.isTrue())
						.and(project.automationWorkflowType.eq(AutomationWorkflowType.NATIVE)));


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
					.compare("id", "automationPriority", "testCase.audit.lastModifiedBy").withEquality();

		// override if necessary
		if (override != null){
			override.accept(converter);
		}

		return converter.build();

	}

	private ColumnFiltering withStatusFilterOrDefault(ColumnFiltering filtering, String statusFilter){
		ColumnFiltering effective = filtering;
		if (filtering.getFilter("requestStatus").isEmpty()){
			effective = overrideStatusFilter(filtering, statusFilter);
		}
		return effective;
	}


	private ColumnFiltering overrideStatusFilter(ColumnFiltering filtering, String statusFilter){
		return new SimpleColumnFiltering(filtering)
				 .addFilter("requestStatus",statusFilter);
	}

	private ColumnFiltering overrideStatusAndAssignedToFilter(ColumnFiltering filtering, String statusFilter, String userID){
		ColumnFiltering effective = null;
		if(filtering.getFilter("requestStatus").isEmpty()) {
			effective = new SimpleColumnFiltering(filtering)
				.addFilter("requestStatus",statusFilter)
				.addFilter("assignedTo", userID);
		} else {
			effective = new SimpleColumnFiltering(filtering)
				.addFilter("requestStatus",filtering.getFilter("requestStatus"))
				.addFilter("assignedTo", userID);
		}
		return effective;
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
