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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.requirement.RequirementStatisticsService;
import org.squashtest.tm.service.statistics.requirement.*;

import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.max;
import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.service.statistics.requirement.RequirementVersionBundleStat.SimpleRequirementStats.REDACTION_RATE_KEY;
import static org.squashtest.tm.service.statistics.requirement.RequirementVersionBundleStat.SimpleRequirementStats.VALIDATION_RATE_KEY;
import static org.squashtest.tm.service.statistics.requirement.RequirementVersionBundleStat.SimpleRequirementStats.VERIFICATION_RATE_KEY;

@Service("RequirementStatisticsService")
@Transactional(readOnly = true)

// that SuppressWarning is intended for SONAR so it ignores the rule squid:S1192 (that is, define constants for long Strings)
@SuppressWarnings("squid:S1192")
public class RequirementStatisticsServiceImpl implements RequirementStatisticsService {

	private static final Logger LOGGER = LoggerFactory
		.getLogger(RequirementStatisticsService.class);

	/*
	 * This query cannot be expressed in hql because the CASE construct doesn't
	 * support multiple WHEN.
	 *
	 * See definition of sct.sizeclass in the CASE WHEN construct.
	 */
	private static final String SQL_BOUND_TCS_STATISTICS =
		"Select coverage.sizeclass, count(coverage.sizeclass) as count "
			+ "From "
			+ "(Select case "
			+ "When count(cov.verified_req_version_id) = 0 then 0 "
			+ "When count(cov.verified_req_version_id) = 1 then 1 "
			+ "Else 2 "
			+ "End as sizeclass "
			+ "From REQUIREMENT req "
			+ "Left Outer Join REQUIREMENT_VERSION_COVERAGE cov on req.current_version_id = cov.verified_req_version_id "
			+ "Where req.rln_id in (:requirementIds) "
			+ "Group By req.rln_id) as coverage "
			+ "Group By coverage.sizeclass";

	private static final String SQL_BOUND_DESC_STATISTICS =
		"Select (Case When res.description != '' AND res.description is not null Then 1 Else 0 End) as hasDescription, count(res.res_id) "
			+ "From REQUIREMENT req "
			+ "Inner Join REQUIREMENT_VERSION reqVer on req.current_version_id = reqVer.res_id "
			+ "Inner Join RESOURCE res on reqVer.res_id = res.res_id "
			+ "Where req.rln_id in (:requirementIds) "
			+ "Group By hasDescription";

	private static final String SQL_COVERAGE_STATISTICS =
		"Select totalSelection.criticality, Coalesce(coveredSelection.coverCount, 0), totalSelection.totalCount "
			+ "From "
			+ "(Select coverage.criticality as criticality, count(sizeclass) as coverCount "
			+ "From (Select reqVer.criticality as criticality, reqVer.res_id as id, Case When count(reqVerCov.verified_req_version_id) = 0 then 0 Else 1 End as sizeclass "
			+ "From REQUIREMENT_VERSION as reqVer "
			+ "Inner Join REQUIREMENT req on reqVer.res_id = req.current_version_id "
			+ "Left Outer Join REQUIREMENT_VERSION_COVERAGE reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id "
			+ "Where req.rln_id in (:requirementIds) Group By reqVer.res_id) as coverage "
			+ "Where sizeclass = 1 "
			+ "Group By coverage.criticality) "
			+ "as coveredSelection "
			+ "Right Outer Join "
			+ "(Select reqVer2.criticality as criticality, count(reqVer2.res_id) as totalCount "
			+ "From REQUIREMENT_VERSION as reqVer2 "
			+ "Inner Join REQUIREMENT req2 on reqVer2.res_id = req2.current_version_id "
			+ "Where req2.rln_id in (:requirementIds) Group By reqVer2.criticality) "
			+ "as totalSelection "
			+ "On coveredSelection.criticality = totalSelection.criticality";

	private static final String SQL_VALIDATION_STATISTICS =
		"Select Selection1.criticality, Selection1.status, count(*) "
			+ "From "
			+ "(Select Distinct req.rln_id as requirement, reqVer.criticality as criticality, tc.tcln_id as testCase, dataset.dataset_id as dataset, itpi.execution_status as status, itpi.last_executed_on as execDate "
			+ "From REQUIREMENT as req "
			+ "Inner Join REQUIREMENT_VERSION as reqVer on req.current_version_id = reqVer.res_id "
			+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id "
			+ "Inner Join TEST_CASE as tc on tc.tcln_id = reqVerCov.verifying_test_case_id "
			+ "Left Outer Join ITERATION_TEST_PLAN_ITEM itpi on itpi.tcln_id = tc.tcln_id "
			+ "Left Outer Join DATASET dataset on dataset.dataset_id = itpi.dataset_id "
			+ "Where req.rln_id in (:requirementIds)) as Selection1 "
			+ "Inner Join "
			+ "(Select req.rln_id as requirement, reqVer.criticality, tc.tcln_id as testCase, dataset.dataset_id as dataset, max(itpi.last_executed_on) as lastDate "
			+ "From REQUIREMENT as req "
			+ "Inner Join REQUIREMENT_VERSION as reqVer on req.current_version_id = reqVer.res_id "
			+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id "
			+ "Inner Join TEST_CASE as tc on tc.tcln_id = reqVerCov.verifying_test_case_id "
			+ "Left Outer Join ITERATION_TEST_PLAN_ITEM itpi on itpi.tcln_id = tc.tcln_id "
			+ "Left Outer Join DATASET as dataset on dataset.dataset_id = itpi.dataset_id "
			+ "Inner Join "
			+ "(Select Max(req.rln_id) as requirement, reqVer.criticality as criticality, tc.tcln_id as testCase "
			+ "From REQUIREMENT req "
			+ "Inner Join REQUIREMENT_VERSION as reqVer On req.current_version_id = reqVer.res_id "
			+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov On reqVerCov.verified_req_version_id = reqVer.res_id "
			+ "Inner Join TEST_CASE as tc On tc.tcln_id = reqVerCov.verifying_test_case_id "
			+ "Where req.rln_id in (:requirementIds) "
			+ "Group By criticality, testCase) as NoDuplicateTCByCritSelection "
			+ "On NoDuplicateTCByCritSelection.requirement = req.rln_id "
			+ "And NoDuplicateTCByCritSelection.criticality = reqVer.criticality "
			+ "And NoDuplicateTCByCritSelection.testCase = tc.tcln_id "
			+ "Where req.rln_id in (:requirementIds) "
			+ "Group By req.rln_id, reqVer.criticality, tc.tcln_id, dataset.dataset_id) as LastExecutionSelection "
			+ "On Selection1.requirement = LastExecutionSelection.requirement And Selection1.testCase = LastExecutionSelection.testCase "
			+ "And (Selection1.execDate = LastExecutionSelection.lastDate Or (Selection1.execDate is Null And LastExecutionSelection.lastDate Is Null)) "
			+ "And (Selection1.dataset = LastExecutionSelection.dataset Or (Selection1.dataset is Null And LastExecutionSelection.dataset Is Null)) "
			+ "Group By Selection1.criticality, Selection1.status";

	private static final String SQL_REQUIREMENTS_IDS_FROM_VALIDATION =
		"Select Distinct Selection1.requirement "
			+ "From "
			+ "(Select Distinct req.rln_id as requirement, reqVer.criticality as criticality, tc.tcln_id as testCase, dataset.dataset_id as dataset, Coalesce(itpi.execution_status, 'NOT_FOUND' ) as status, itpi.last_executed_on as execDate "
			+ "From REQUIREMENT as req "
			+ "Inner Join REQUIREMENT_VERSION as reqVer on req.current_version_id = reqVer.res_id "
			+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id "
			+ "Inner Join TEST_CASE as tc on tc.tcln_id = reqVerCov.verifying_test_case_id "
			+ "Left Outer Join ITERATION_TEST_PLAN_ITEM itpi on itpi.tcln_id = tc.tcln_id "
			+ "Left Outer Join DATASET dataset on dataset.dataset_id = itpi.dataset_id "
			+ "Where req.rln_id In (:requirementIds)) as Selection1 "
			+ "Inner Join "
			+ "(Select req.rln_id as requirement, reqVer.criticality, tc.tcln_id as testCase, dataset.dataset_id as dataset, max(itpi.last_executed_on) as lastDate "
			+ "From REQUIREMENT as req "
			+ "Inner Join REQUIREMENT_VERSION as reqVer on req.current_version_id = reqVer.res_id "
			+ "Inner Join REQUIREMENT_VERSION_COVERAGE as reqVerCov on reqVerCov.verified_req_version_id = reqVer.res_id "
			+ "Inner Join TEST_CASE as tc on tc.tcln_id = reqVerCov.verifying_test_case_id "
			+ "Left Outer Join ITERATION_TEST_PLAN_ITEM itpi on itpi.tcln_id = tc.tcln_id "
			+ "Left Outer Join DATASET as dataset on dataset.dataset_id = itpi.dataset_id "
			+ "Where req.rln_id In (:requirementIds) "
			+ "Group By req.rln_id, reqVer.criticality, tc.tcln_id, dataset.dataset_id) as LastExecutionSelection "
			+ "On Selection1.requirement = LastExecutionSelection.requirement And Selection1.testCase = LastExecutionSelection.testCase "
			+ "And (Selection1.execDate = LastExecutionSelection.lastDate Or (Selection1.execDate is Null And LastExecutionSelection.lastDate Is Null)) "
			+ "And (Selection1.dataset = LastExecutionSelection.dataset Or (Selection1.dataset is Null And LastExecutionSelection.dataset Is Null)) "
			+ "Where Selection1.criticality = (:criticality) "
			+ "And Selection1.status In (:validationStatus)";

	private static String reqParamName = "requirementIds";
	private static String critPramName = "criticality";
	private static String validationStatusParamName = "validationStatus";

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private DSLContext DSL;

	@Override
	public RequirementBoundTestCasesStatistics gatherBoundTestCaseStatistics(
		Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementBoundTestCasesStatistics();
		}

		Query query = entityManager.createNativeQuery(SQL_BOUND_TCS_STATISTICS);
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		RequirementBoundTestCasesStatistics stats = new RequirementBoundTestCasesStatistics();

		Integer sizeClass;
		Integer count;
		for (Object[] tuple : tuples) {

			sizeClass = (Integer) tuple[0];
			count = ((BigInteger) tuple[1]).intValue();

			switch (sizeClass) {
				case 0:
					stats.setZeroTestCases(count);
					break;
				case 1:
					stats.setOneTestCase(count);
					break;
				case 2:
					stats.setManyTestCases(count);
					break;
				default:
					throw new IllegalArgumentException(
						"RequirementStatisticsServiceImpl#gatherBoundTestCaseStatistics : "
							+ "there should not be a sizeclass <0 or >2. It's a bug.");
			}
		}
		return stats;
	}

	@Override
	public RequirementCriticalityStatistics gatherRequirementCriticalityStatistics(
		Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementCriticalityStatistics();
		}

		Query query = entityManager.createNamedQuery(
			"RequirementStatistics.criticalityStatistics");
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		// format the result
		RequirementCriticalityStatistics stats = new RequirementCriticalityStatistics();

		RequirementCriticality criticality;
		Integer cardinality;
		for (Object[] tuple : tuples) {
			criticality = (RequirementCriticality) tuple[0];
			cardinality = ((Long) tuple[1]).intValue();
			switch (criticality) {
				case UNDEFINED:
					stats.setUndefined(cardinality);
					break;
				case MINOR:
					stats.setMinor(cardinality);
					break;
				case MAJOR:
					stats.setMajor(cardinality);
					break;
				case CRITICAL:
					stats.setCritical(cardinality);
					break;
				default:
					throw new IllegalArgumentException(
						"RequirementStatisticsService cannot handle the following RequirementCriticality value : '"
							+ tuple[0] + "'");
			}
		}

		return stats;
	}

	@Override
	public RequirementStatusesStatistics gatherRequirementStatusesStatistics(
		Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementStatusesStatistics();
		}
		Query query = entityManager.createNamedQuery(
			"RequirementStatistics.statusesStatistics");
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		// format the result
		RequirementStatusesStatistics stats = new RequirementStatusesStatistics();

		RequirementStatus status;
		Integer cardinality;
		for (Object[] tuple : tuples) {
			status = (RequirementStatus) tuple[0];
			cardinality = ((Long) tuple[1]).intValue();
			switch (status) {
				case WORK_IN_PROGRESS:
					stats.setWorkInProgress(cardinality);
					break;
				case UNDER_REVIEW:
					stats.setUnderReview(cardinality);
					break;
				case APPROVED:
					stats.setApproved(cardinality);
					break;
				case OBSOLETE:
					stats.setObsolete(cardinality);
					break;
				default:
					throw new IllegalArgumentException(
						"RequirmentStatisticsService cannot handle the following RequirementStatus value : '"
							+ tuple[0] + "'");
			}
		}
		return stats;
	}

	@Override
	public RequirementBoundDescriptionStatistics gatherRequirementBoundDescriptionStatistics(
		Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementBoundDescriptionStatistics();
		}


		Query query = entityManager.createNativeQuery(SQL_BOUND_DESC_STATISTICS);
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		RequirementBoundDescriptionStatistics stats = new RequirementBoundDescriptionStatistics();

		Boolean hasDescription;
		Integer count;
		for (Object[] tuple : tuples) {

			/* If only one requirement is present,
			* request return tuple[0] as a BigInteger, it returns an Integer in other cases. */
			try {
				hasDescription = (Integer) tuple[0] != 0;
			} catch (ClassCastException exception) {
				hasDescription = ((BigInteger) tuple[0]).intValue() != 0;
				LOGGER.info("BigInteger handled.", exception);
			}
			count = ((BigInteger) tuple[1]).intValue();

			if (hasDescription) {
				stats.setHasDescription(count);
			} else {
				stats.setHasNoDescription(count);
			}
		}


		return stats;
	}

	@Override
	public RequirementCoverageStatistics gatherRequirementCoverageStatistics(Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementCoverageStatistics();
		}

		Query query = entityManager.createNativeQuery(SQL_COVERAGE_STATISTICS);
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		RequirementCoverageStatistics stats = new RequirementCoverageStatistics();

		String criticality;
		Integer count;
		Integer total;

		for (Object[] tuple : tuples) {

			criticality = (String) tuple[0];
			count = ((BigInteger) tuple[1]).intValue();
			total = ((BigInteger) tuple[2]).intValue();

			switch (criticality) {
				case "UNDEFINED":
					stats.setUndefined(count);
					stats.setTotalUndefined(total);
					break;
				case "MINOR":
					stats.setMinor(count);
					stats.setTotalMinor(total);
					break;
				case "MAJOR":
					stats.setMajor(count);
					stats.setTotalMajor(total);
					break;
				case "CRITICAL":
					stats.setCritical(count);
					stats.setTotalCritical(total);
					break;
				default:
					throw new IllegalArgumentException(
						"RequirmentStatisticsService cannot handle the following RequirementCriticality value : '"
							+ tuple[0] + "'");
			}
		}

		return stats;
	}

	@Override
	public RequirementValidationStatistics gatherRequirementValidationStatistics(Collection<Long> requirementIds) {

		if (requirementIds.isEmpty()) {
			return new RequirementValidationStatistics();
		}

		Query query = entityManager.createNativeQuery(SQL_VALIDATION_STATISTICS);
		query.setParameter(reqParamName, requirementIds);

		List<Object[]> tuples = query.getResultList();

		RequirementValidationStatistics stats = new RequirementValidationStatistics();

		String requirementCriticality;
		String executionStatus;
		Integer count;

		for (Object[] tuple : tuples) {

			requirementCriticality = (String) tuple[0];
			executionStatus = (String) tuple[1];
			count = ((BigInteger) tuple[2]).intValue();

			// If the TestCase has no executions, it counts as an Undefined Test
			if (executionStatus == null)
				executionStatus = "NOT_RUN";

			determineValidationStatisticsCount(stats, requirementCriticality, executionStatus, count);
		}

		return stats;
	}

	private void determineValidationStatisticsCount(RequirementValidationStatistics stats, String requirementCriticality, String executionStatus, Integer count) {
		switch (executionStatus) {
			case "SUCCESS":
				determineConclusiveValidationCount(stats, requirementCriticality, count);
				break;
			case "FAILURE":
				determineInconclusiveValidationCount(stats, requirementCriticality, count);
				break;
			case "BLOCKED":
			case "ERROR":
			case "NOT_FOUND":
			case "NOT_RUN":
			case "READY":
			case "RUNNING":
			case "SETTLED":
			case "UNTESTABLE":
			case "WARNING":
				determineUndefinedValidationCount(stats, requirementCriticality, count);
				break;
			default:
				throw new IllegalArgumentException(
					"RequirementStatisticsService cannot handle the following ExecutionStatus value : '"
						+ executionStatus + "'");
		}
	}

	private void determineUndefinedValidationCount(RequirementValidationStatistics stats, String requirementCriticality, Integer count) {
		switch (requirementCriticality) {
			case "UNDEFINED":
				stats.setUndefinedUndefined(stats.getUndefinedUndefined() + count);
				break;
			case "MINOR":
				stats.setUndefinedMinor(stats.getUndefinedMinor() + count);
				break;
			case "MAJOR":
				stats.setUndefinedMajor(stats.getUndefinedMajor() + count);
				break;
			case "CRITICAL":
				stats.setUndefinedCritical(stats.getUndefinedCritical() + count);
				break;
			default:
				throw new IllegalArgumentException(
					"RequirementStatisticsService cannot handle the following RequirementCriticality value : '"
						+ requirementCriticality + "'");
		}
	}

	private void determineInconclusiveValidationCount(RequirementValidationStatistics stats, String requirementCriticality, Integer count) {
		switch (requirementCriticality) {
			case "UNDEFINED":
				stats.setInconclusiveUndefined(count);
				break;
			case "MINOR":
				stats.setInconclusiveMinor(count);
				break;
			case "MAJOR":
				stats.setInconclusiveMajor(count);
				break;
			case "CRITICAL":
				stats.setInconclusiveCritical(count);
				break;
			default:
				throw new IllegalArgumentException(
					"RequirementStatisticsService cannot handle the following RequirementCriticality value : '"
						+ requirementCriticality + "'");
		}
	}

	private void determineConclusiveValidationCount(RequirementValidationStatistics stats, String requirementCriticality, Integer count) {
		switch (requirementCriticality) {
			case "UNDEFINED":
				stats.setConclusiveUndefined(count);
				break;
			case "MINOR":
				stats.setConclusiveMinor(count);
				break;
			case "MAJOR":
				stats.setConclusiveMajor(count);
				break;
			case "CRITICAL":
				stats.setConclusiveCritical(count);
				break;
			default:
				throw new IllegalArgumentException(
					"RequirementStatisticsService cannot handle the following RequirementCriticality value : '"
						+ requirementCriticality + "'");
		}
		return;
	}

	@Override
	public RequirementStatisticsBundle gatherRequirementStatisticsBundle(
		Collection<Long> requirementIds) {

		RequirementBoundTestCasesStatistics tcs = gatherBoundTestCaseStatistics(requirementIds);
		RequirementStatusesStatistics status = gatherRequirementStatusesStatistics(requirementIds);
		RequirementCriticalityStatistics criticality = gatherRequirementCriticalityStatistics(requirementIds);
		RequirementBoundDescriptionStatistics description = gatherRequirementBoundDescriptionStatistics(requirementIds);
		RequirementCoverageStatistics coverage = gatherRequirementCoverageStatistics(requirementIds);
		RequirementValidationStatistics validation = gatherRequirementValidationStatistics(requirementIds);

		return new RequirementStatisticsBundle(tcs, status, criticality, description, coverage, validation, requirementIds);
	}

	@Override
	public Collection<Long> gatherRequirementIdsFromValidation(Collection<Long> requirementIds, RequirementCriticality criticality, Collection<String> validationStatus) {

		if (requirementIds.isEmpty()) {
			return new ArrayList<>();
		}
		Query query = entityManager.createNativeQuery(SQL_REQUIREMENTS_IDS_FROM_VALIDATION);
		query.setParameter(reqParamName, requirementIds);
		query.setParameter(critPramName, criticality.toString());
		query.setParameter(validationStatusParamName, validationStatus);

		List<BigInteger> bigIntIdsList = query.getResultList();
		List<Long> reqIdsList = new ArrayList<>(bigIntIdsList.size());
		for (BigInteger id : bigIntIdsList) {
			reqIdsList.add(id.longValue());
		}
		return reqIdsList;
	}

	@Override
	public RequirementVersionBundleStat findSimplifiedCoverageStats(Collection<Long> requirementIds) {
		RequirementVersionBundleStat bundle = new RequirementVersionBundleStat();
		computeRedactionRate(requirementIds, bundle);
		//compute verification rates
		computeItpiByStatusRate(requirementIds, bundle, ExecutionStatus.getTerminatedStatusSet(), EnumSet.allOf(ExecutionStatus.class), VERIFICATION_RATE_KEY);
		//compute validation rates
		computeItpiByStatusRate(requirementIds, bundle, ExecutionStatus.getSuccessStatusSet(), ExecutionStatus.getTerminatedStatusSet(), VALIDATION_RATE_KEY);
		return bundle;
	}

	/*
	 * Compute a ratio of ITPI status from a list of requirements.
	 * The ratio is like :
	 * nb of ITPI in matchingStatusSet / nb of ITPI in allStatusSet.
	 * The chosen ITPI are the most recently executed if at least one ITPI has execution or all of them is no execution date...
	 */
	private void computeItpiByStatusRate(Collection<Long> requirementIds, RequirementVersionBundleStat bundle, Set<ExecutionStatus> matchingStatusSet,Set<ExecutionStatus> allStatusSet, String key) {

		//preparing our join from RLN_RELATIONSHIP_CLOSURE to ITPI
		TableOnConditionStep<Record> joinFromAncestorToITPI = RLN_RELATIONSHIP_CLOSURE
			.innerJoin(REQUIREMENT).on(REQUIREMENT.RLN_ID.eq(RLN_RELATIONSHIP_CLOSURE.DESCENDANT_ID))
			.innerJoin(REQUIREMENT_VERSION).on(REQUIREMENT_VERSION.RES_ID.eq(REQUIREMENT.CURRENT_VERSION_ID))
			.innerJoin(REQUIREMENT_VERSION_COVERAGE).on(REQUIREMENT_VERSION_COVERAGE.VERIFIED_REQ_VERSION_ID.eq(REQUIREMENT_VERSION.RES_ID))
			.innerJoin(TEST_CASE).on(REQUIREMENT_VERSION_COVERAGE.VERIFYING_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
			.innerJoin(ITERATION_TEST_PLAN_ITEM).on(TEST_CASE.TCLN_ID.eq(ITERATION_TEST_PLAN_ITEM.TCLN_ID));

		Field<Long> lastExecutionTC = ITERATION_TEST_PLAN_ITEM.TCLN_ID.as("lastExecutionTC");
		Field<Long> lastExecutionDS = coalesce(ITERATION_TEST_PLAN_ITEM.DATASET_ID, 0L).as("lastExecutionDS");//coalesce null dataset to 0, so we can make a join
		Field<Timestamp> lastExecutionDate = coalesce(max(ITERATION_TEST_PLAN_ITEM.LAST_EXECUTED_ON), new Timestamp(0L)).as("lastExecutionDate");//coalesce null execution date to January 1, 1970, 00:00:00 GMT, so we can make a join

		// Here we perform a subrequest that will create a result like TCLN_ID, DATASET_ID, MAX(LAST_EXECUTED_ON)
		// So we can use this to perform a join with ours other sub queries :
		// A join between TCLN_ID, DATASET_ID, LAST_EXECUTED_ON  -> TCLN_ID, DATASET_ID, MAX(LAST_EXECUTED_ON)
		// So we have the last executed ITPI for every combination of TCLN_ID - DATASET_ID
		Table<Record3<Long, Long, Timestamp>> selectLastExecution = DSL.select(lastExecutionTC, lastExecutionDS, lastExecutionDate)
			.from(ITERATION_TEST_PLAN_ITEM)
			.groupBy(ITERATION_TEST_PLAN_ITEM.TCLN_ID, ITERATION_TEST_PLAN_ITEM.DATASET_ID)
			.asTable("selectLastExecution");


		Field<Long> reqIdAll = RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.as("reqIdAll");
		Field<Integer> countAll = count().as("countAll");

		Table<Record2<Long, Integer>> allITPI = DSL.select(reqIdAll, countAll)
			.from(joinFromAncestorToITPI)
			.innerJoin(selectLastExecution)//inner join to avoid correlated sub query like LAST_EXECUTED_ON = SELECT(MAX(LAST_EXECUTED_ON))... witch can be performance killer
			.on(ITERATION_TEST_PLAN_ITEM.TCLN_ID.eq(selectLastExecution.field(lastExecutionTC)))
			.and(coalesce(ITERATION_TEST_PLAN_ITEM.DATASET_ID, 0L).eq(selectLastExecution.field(lastExecutionDS)))//coalesce to allow join if no dataset
			.and(coalesce(ITERATION_TEST_PLAN_ITEM.LAST_EXECUTED_ON, new Timestamp(0L)).eq(selectLastExecution.field(lastExecutionDate)))//coalesce to allow join if no execution date
			.where(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.in(requirementIds))
			.and(ITERATION_TEST_PLAN_ITEM.EXECUTION_STATUS.in(allStatusSet))
			.groupBy(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID)
			.asTable("allITPI");


		Field<Long> reqIdMatch = RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.as("reqIdMatch");
		Field<Integer> countMatch = count().as("countMatch");

		Table<Record2<Long, Integer>> matchITPI = DSL.select(reqIdMatch, countMatch)
			.from(joinFromAncestorToITPI)
			.innerJoin(selectLastExecution)
			.on(ITERATION_TEST_PLAN_ITEM.TCLN_ID.eq(selectLastExecution.field(lastExecutionTC)))
			.and(coalesce(ITERATION_TEST_PLAN_ITEM.DATASET_ID, 0L).eq(selectLastExecution.field(lastExecutionDS)))
			.and(coalesce(ITERATION_TEST_PLAN_ITEM.LAST_EXECUTED_ON, new Timestamp(0L)).eq(selectLastExecution.field(lastExecutionDate)))
			.where(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.in(requirementIds))
			.and(ITERATION_TEST_PLAN_ITEM.EXECUTION_STATUS.in(matchingStatusSet))
			.groupBy(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID)
			.asTable("matchITPI");

		//making our joins versus our "virtual tables" and fetch into stat bundle
		DSL.select(REQUIREMENT.RLN_ID, allITPI.field(countAll), matchITPI.field(countMatch))
			.from(REQUIREMENT)
			.leftJoin(allITPI).on(REQUIREMENT.RLN_ID.eq(allITPI.field(reqIdAll)))
			.leftJoin(matchITPI).on(REQUIREMENT.RLN_ID.eq(matchITPI.field(reqIdMatch)))
			.where(REQUIREMENT.RLN_ID.in(requirementIds))
			.fetch()
			.forEach(r -> {
				Long reqId = r.get(REQUIREMENT.RLN_ID);
				Integer countAllITPI = r.get(allITPI.field(countAll));
				Integer countValidatedITPI = r.get(matchITPI.field(countMatch));
				bundle.computeRate(reqId,key, countAllITPI, countValidatedITPI);
			});
	}

	/*
	 * Compute the redaction rate for a list of requirements.
	 * The redaction rate is defined by the ratio Covering Test Case / Covering Test Case with status UNDER_REVIEW or APPROVED, for all the hierarchy of the requirement.
	 */
	private void computeRedactionRate(Collection<Long> requirementIds, RequirementVersionBundleStat bundle) {

		//preparing our join from RLN_CLOSURE to TEST_CASE
		TableOnConditionStep<Record> joinFromAncestorToTestCase = RLN_RELATIONSHIP_CLOSURE
			.innerJoin(REQUIREMENT).on(REQUIREMENT.RLN_ID.eq(RLN_RELATIONSHIP_CLOSURE.DESCENDANT_ID))
			.innerJoin(REQUIREMENT_VERSION).on(REQUIREMENT_VERSION.RES_ID.eq(REQUIREMENT.CURRENT_VERSION_ID))
			.innerJoin(REQUIREMENT_VERSION_COVERAGE).on(REQUIREMENT_VERSION_COVERAGE.VERIFIED_REQ_VERSION_ID.eq(REQUIREMENT_VERSION.RES_ID))
			.innerJoin(TEST_CASE).on(REQUIREMENT_VERSION_COVERAGE.VERIFYING_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID));

		//creating a "virtual table" like ReqVersionId | CountCoverageTC to avoid correlated sub queries
		Field<Long> reqIdAllTC = RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.as("reqIdAllTC");
		Field<Integer> countAllTC = count().as("countAllTC");
		Table<Record2<Long, Integer>> allTestCase = DSL
			.select(reqIdAllTC, countAllTC)
			.from(joinFromAncestorToTestCase)
			.where(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.in(requirementIds))
			.groupBy(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID)
			.asTable("allTestCase");

		//creating a "virtual table" like ReqVersionId | CountCoverageWithGoodStatus TC to avoid correlated sub queries
		Field<Long> reqIdValidatedTC = RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.as("reqIdValidatedTC");
		Field<Integer> countValidatedTC = count().as("countValidatedTC");
		Table<Record2<Long, Integer>> validatedTestCase = DSL
			.select(reqIdValidatedTC, countValidatedTC)
			.from(joinFromAncestorToTestCase)
			.where(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID.in(requirementIds))
			.and(TEST_CASE.TC_STATUS.in(TestCaseStatus.UNDER_REVIEW.name(), TestCaseStatus.APPROVED.name()))
			.groupBy(RLN_RELATIONSHIP_CLOSURE.ANCESTOR_ID)
			.asTable("verifiedTestCase");


		//making our main request with joins versus our "virtual tables" and fetch into stat bundle
		DSL.select(REQUIREMENT.RLN_ID, allTestCase.field(countAllTC), validatedTestCase.field(countValidatedTC))
			.from(REQUIREMENT)
			.leftJoin(allTestCase).on(REQUIREMENT.RLN_ID.eq(allTestCase.field(reqIdAllTC))) //performing left join to have tuple even if no coverages for requirement
			.leftJoin(validatedTestCase).on(REQUIREMENT.RLN_ID.eq(validatedTestCase.field(reqIdValidatedTC)))
			.where(REQUIREMENT.RLN_ID.in(requirementIds))
			.fetch()
			.forEach(r -> {
				Long reqId = r.get(REQUIREMENT.RLN_ID);
				Integer countAllTestCase = r.get(allTestCase.field(countAllTC));
				Integer countValidatedTestCase = r.get(validatedTestCase.field(countValidatedTC));
				bundle.computeRate(reqId, REDACTION_RATE_KEY, countAllTestCase, countValidatedTestCase);
			});

	}


}
