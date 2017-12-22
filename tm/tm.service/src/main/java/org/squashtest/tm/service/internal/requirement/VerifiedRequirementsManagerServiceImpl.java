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

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.*;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.collections.MultiMap;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCoverageStat;
import org.squashtest.tm.domain.requirement.RequirementCoverageStat.Rate;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionStatus;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

import java.util.Optional;

@Service("squashtest.tm.service.VerifiedRequirementsManagerService")
@Transactional
public class VerifiedRequirementsManagerServiceImpl implements
	VerifiedRequirementsManagerService {

	private static final Logger LOGGER = LoggerFactory
		.getLogger(VerifiedRequirementsManagerServiceImpl.class);
	private static final String LINK_TC_OR_ROLE_ADMIN = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'LINK')"
		+ OR_HAS_ROLE_ADMIN;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private TestStepDao testStepDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private TestCaseCallTreeFinder callTreeFinder;

	@Inject
	private RequirementVersionCoverageDao requirementVersionCoverageDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private IndexationService indexationService;


	@Inject
	private RequirementDao requirementDao;

	@Inject
	private IterationDao iterationDao;

	@Inject
	private ExecutionStepDao executionStepDao;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;
	@Inject
	private PermissionEvaluationService permissionService;

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public Collection<VerifiedRequirementException> addVerifiedRequirementsToTestCase(List<Long> requirementsIds,
			long testCaseId) {

		List<RequirementVersion> requirementVersions = findRequirementVersions(requirementsIds);

		TestCase testCase = testCaseDao.findById(testCaseId);
		if (!requirementVersions.isEmpty()) {
			return doAddVerifyingRequirementVersionsToTestCase(
				requirementVersions, testCase);
		}
		return Collections.emptyList();
	}

	private List<RequirementVersion> extractVersions(List<Requirement> requirements) {

		List<RequirementVersion> rvs = new ArrayList<>(requirements.size());

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		for (Requirement requirement : requirements) {

			// normal mode
			if (!activeMilestone.isPresent()) {
				rvs.add(requirement.getResource());
			}
			// milestone mode
			else {
				rvs.add(requirement.findByMilestone(activeMilestone.get()));
			}

		}
		return rvs;
	}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public void removeVerifiedRequirementVersionsFromTestCase(
		List<Long> requirementVersionsIds, long testCaseId) {

		if (!requirementVersionsIds.isEmpty()) {

			List<RequirementVersionCoverage> requirementVersionCoverages = requirementVersionCoverageDao
				.byTestCaseAndRequirementVersions(requirementVersionsIds,
					testCaseId);

			for (RequirementVersionCoverage coverage : requirementVersionCoverages) {
				requirementVersionCoverageDao.delete(coverage);
			}

			indexationService.reindexTestCase(testCaseId);
			indexationService
				.reindexRequirementVersionsByIds(requirementVersionsIds);

			testCaseImportanceManagerService
				.changeImportanceIfRelationsRemovedFromTestCase(
					requirementVersionsIds, testCaseId);
		}
	}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public void removeVerifiedRequirementVersionFromTestCase(
		long requirementVersionId, long testCaseId) {
		RequirementVersionCoverage coverage = requirementVersionCoverageDao
			.byRequirementVersionAndTestCase(requirementVersionId,
				testCaseId);

		requirementVersionCoverageDao.delete(coverage);

		indexationService.reindexTestCase(testCaseId);
		indexationService.reindexRequirementVersion(requirementVersionId);
		testCaseImportanceManagerService
			.changeImportanceIfRelationsRemovedFromTestCase(
				Arrays.asList(requirementVersionId), testCaseId);
	}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public int changeVerifiedRequirementVersionOnTestCase(
		long oldVerifiedRequirementVersionId,
		long newVerifiedRequirementVersionId, long testCaseId) {
		RequirementVersion newReq = requirementVersionDao
			.findOne(newVerifiedRequirementVersionId);
		RequirementVersionCoverage coverage = requirementVersionCoverageDao
			.byRequirementVersionAndTestCase(
				oldVerifiedRequirementVersionId, testCaseId);
		coverage.setVerifiedRequirementVersion(newReq);
		indexationService.reindexTestCase(testCaseId);
		indexationService
			.reindexRequirementVersion(oldVerifiedRequirementVersionId);
		indexationService
			.reindexRequirementVersion(oldVerifiedRequirementVersionId);
		testCaseImportanceManagerService
			.changeImportanceIfRelationsRemovedFromTestCase(
				Arrays.asList(newVerifiedRequirementVersionId),
				testCaseId);

		return newReq.getVersionNumber();
	}

	/*
	 * regarding the @PreAuthorize for the verified requirements :
	 *
	 * I prefer to show all the requirements that the test case refers to even
	 * if some of those requirements belongs to a project the current user
	 * cannot "read", rather post filtering it.
	 *
	 * The reason for that is that such policy is impractical for the same
	 * problem in the context of Iteration-TestCase associations : filtering the
	 * test cases wouldn't make much sense and would lead to partial executions
	 * of a campaign.
	 *
	 * Henceforth the same policy applies to other cases of possible
	 * inter-project associations (like TestCase-Requirement associations in the
	 * present case), for the sake of coherence.
	 *
	 * @author bsiri
	 *
	 * (non-Javadoc)
	 */
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')"
		+ OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllDirectlyVerifiedRequirementsByTestCaseId(
		long testCaseId, PagingAndSorting pagingAndSorting) {
		List<RequirementVersionCoverage> reqVersionCoverages = requirementVersionCoverageDao
			.findAllByTestCaseId(testCaseId, pagingAndSorting);
		long verifiedCount = requirementVersionCoverageDao
			.numberByTestCase(testCaseId);
		return new PagingBackedPagedCollectionHolder<>(
			pagingAndSorting, verifiedCount,
			convertInDirectlyVerified(reqVersionCoverages));
	}

	private List<VerifiedRequirement> convertInDirectlyVerified(
		List<RequirementVersionCoverage> reqVersionCoverages) {
		List<VerifiedRequirement> result = new ArrayList<>(
			reqVersionCoverages.size());
		for (RequirementVersionCoverage rvc : reqVersionCoverages) {
			VerifiedRequirement convertionResult = new VerifiedRequirement(rvc,
				true).withVerifyingStepsFrom(rvc.getVerifyingTestCase());
			result.add(convertionResult);
		}
		return result;
	}

	@Override
	public Collection<VerifiedRequirementException> addVerifyingRequirementVersionsToTestCase(
		Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCase) {
		Collection<VerifiedRequirementException> rejections = new ArrayList<>();
		for (Entry<TestCase, List<RequirementVersion>> reqVsByTc : requirementVersionsByTestCase
			.entrySet()) {
			TestCase testCase = reqVsByTc.getKey();
			List<RequirementVersion> requirementVersions = reqVsByTc.getValue();
			Collection<VerifiedRequirementException> entrtyRejections = doAddVerifyingRequirementVersionsToTestCase(
				requirementVersions, testCase);
			rejections.addAll(entrtyRejections);
		}
		return rejections;

	}

	private Collection<VerifiedRequirementException> doAddVerifyingRequirementVersionsToTestCase(
		List<RequirementVersion> requirementVersions, TestCase testCase) {
		Collection<VerifiedRequirementException> rejections = new ArrayList<>();
		Iterator<RequirementVersion> iterator = requirementVersions.iterator();
		while (iterator.hasNext()) {
			RequirementVersion requirementVersion = iterator.next();
			try {
				RequirementVersionCoverage coverage = new RequirementVersionCoverage(
					requirementVersion, testCase);
				requirementVersionCoverageDao.persist(coverage);
				indexationService.reindexTestCase(testCase.getId());
				indexationService.reindexRequirementVersion(requirementVersion
					.getId());
			} catch (RequirementAlreadyVerifiedException | RequirementVersionNotLinkableException ex) {
				LOGGER.warn(ex.getMessage());
				rejections.add(ex);
				iterator.remove();
			}
		}
		testCaseImportanceManagerService
			.changeImportanceIfRelationsAddedToTestCase(
				requirementVersions, testCase);
		return rejections;

	}

	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'LINK')"
		+ OR_HAS_ROLE_ADMIN)
	public Collection<VerifiedRequirementException> addVerifiedRequirementsToTestStep(
List<Long> requirementsIds,
			long testStepId) {
		List<RequirementVersion> requirementVersions = findRequirementVersions(
requirementsIds);
		// init rejections
		Collection<VerifiedRequirementException> rejections = new ArrayList<>();
		// check if list not empty
		if (!requirementVersions.isEmpty()) {
			// collect concerned entities
			ActionTestStep step = testStepDao
				.findActionTestStepById(testStepId);
			TestCase testCase = step.getTestCase();
			// iterate on requirement versions
			Iterator<RequirementVersion> iterator = requirementVersions
				.iterator();
			while (iterator.hasNext()) {
				try {
					RequirementVersion requirementVersion = iterator.next();
					PermissionsUtils.checkPermission(permissionService,
						new SecurityCheckableObject(requirementVersion,
							"LINK"));
					boolean newReqCoverage = addVerifiedRequirementVersionToTestStep(
						requirementVersion, step, testCase);
					if (!newReqCoverage) {
						iterator.remove();
					}
				} catch (RequirementAlreadyVerifiedException | RequirementVersionNotLinkableException ex) {
					LOGGER.warn(ex.getMessage());
					iterator.remove();
					rejections.add(ex);
				}
			}
			testCaseImportanceManagerService
				.changeImportanceIfRelationsAddedToTestCase(
					requirementVersions, testCase);

		}
		return rejections;
	}

	/**
	 * Will find the RequirementVersionCoverage for the given requirement
	 * version and test case to add the step to it. If not found, will create a
	 * new RequirementVersionCoverage for the test case and add the step to it.<br>
	 *
	 * @param step
	 * @param testCase
	 * @return true if a new RequirementVersionCoverage has been created.
	 */
	private boolean addVerifiedRequirementVersionToTestStep(
		RequirementVersion requirementVersion, ActionTestStep step,
		TestCase testCase) {

		RequirementVersionCoverage coverage = requirementVersionCoverageDao
			.byRequirementVersionAndTestCase(requirementVersion.getId(),
				testCase.getId());
		if (coverage == null) {
			RequirementVersionCoverage newCoverage = new RequirementVersionCoverage(
				requirementVersion, testCase);
			newCoverage.addAllVerifyingSteps(Arrays.asList(step));
			requirementVersionCoverageDao.persist(newCoverage);
			indexationService.reindexTestCase(testCase.getId());
			indexationService.reindexRequirementVersion(requirementVersion
				.getId());
			return true;
		} else {
			coverage.addAllVerifyingSteps(Arrays.asList(step));
			return false;
		}

	}

	/**
	 * @see VerifiedRequirementsManagerService#addVerifiedRequirementVersionToTestStep(long,
	 *      long);
	 */
	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'LINK') and hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion' , 'LINK')"
		+ OR_HAS_ROLE_ADMIN)
	public Collection<VerifiedRequirementException> addVerifiedRequirementVersionToTestStep(
		long requirementVersionId, long testStepId) {
		ActionTestStep step = testStepDao.findActionTestStepById(testStepId);
		TestCase testCase = step.getTestCase();
		RequirementVersion version = requirementVersionDao
			.findOne(requirementVersionId);
		Collection<VerifiedRequirementException> rejections = new ArrayList<>(
			1);
		if (version == null) {
			throw new UnknownEntityException(requirementVersionId,
				RequirementVersion.class);
		}
		try {
			boolean newRequirementCoverageCreated = addVerifiedRequirementVersionToTestStep(
				version, step, testCase);
			if (newRequirementCoverageCreated) {
				testCaseImportanceManagerService
					.changeImportanceIfRelationsAddedToTestCase(
						Arrays.asList(version), testCase);
			}
		} catch (RequirementAlreadyVerifiedException | RequirementVersionNotLinkableException ex) {
			LOGGER.warn(ex.getMessage());
			rejections.add(ex);
		}
		return rejections;
	}

	private List<RequirementVersion> findRequirementVersions(
List<Long> requirementsIds) {

		List<RequirementLibraryNode> nodes = requirementLibraryNodeDao
			.findAllByIds(requirementsIds);

		if (!nodes.isEmpty()) {
			List<Requirement> requirements = new RequirementNodeWalker()
				.walk(nodes);
			if (!requirements.isEmpty()) {
				return extractVersions(requirements);
			}
		}
		return Collections.emptyList();
	}

	@Override
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllVerifiedRequirementsByTestCaseId(
		long testCaseId, PagingAndSorting pas) {

		LOGGER.debug("Looking for verified requirements of TestCase[id:{}]",
			testCaseId);

		Set<Long> calleesIds = callTreeFinder.getTestCaseCallTree(testCaseId);

		calleesIds.add(testCaseId);

		LOGGER.debug("Fetching Requirements verified by TestCases {}",
			calleesIds.toString());

		List<RequirementVersion> pagedVersionVerifiedByCalles = requirementVersionCoverageDao
			.findDistinctRequirementVersionsByTestCases(calleesIds, pas);

		TestCase mainTestCase = testCaseDao.findById(testCaseId);

		List<VerifiedRequirement> pagedVerifiedReqs = buildVerifiedRequirementList(
			mainTestCase, pagedVersionVerifiedByCalles);

		long totalVerified = requirementVersionCoverageDao
			.numberDistinctVerifiedByTestCases(calleesIds);

		LOGGER.debug("Total count of verified requirements : {}", totalVerified);

		return new PagingBackedPagedCollectionHolder<>(
			pas, totalVerified, pagedVerifiedReqs);
	}

	@Override
	public List<VerifiedRequirement> findAllVerifiedRequirementsByTestCaseId(
		long testCaseId) {
		LOGGER.debug("Looking for verified requirements of TestCase[id:{}]",
			testCaseId);

		Set<Long> calleesIds = callTreeFinder.getTestCaseCallTree(testCaseId);

		calleesIds.add(testCaseId);

		LOGGER.debug("Fetching Requirements verified by TestCases {}",
			calleesIds.toString());

		List<RequirementVersion> pagedVersionVerifiedByCalles = requirementVersionCoverageDao
			.findDistinctRequirementVersionsByTestCases(calleesIds);

		TestCase mainTestCase = testCaseDao.findById(testCaseId);

		return buildVerifiedRequirementList(mainTestCase,
			pagedVersionVerifiedByCalles);
	}


	@Override
	public Map<Long, Boolean> findisReqCoveredOfCallingTCWhenisReqCoveredChanged(
		long updatedTestCaseId, Collection<Long> toUpdateIds) {
		Map<Long, Boolean> result;
		result = new HashMap<>(toUpdateIds.size());
		if (testCaseHasDirectCoverage(updatedTestCaseId)
			|| testCaseHasUndirectRequirementCoverage(updatedTestCaseId)) {
			// set isReqCovered = true for all calling test cases
			for (Long id : toUpdateIds) {
				result.put(id, Boolean.TRUE);
			}
		} else {
			// check each calling testCase to see if their status changed
			for (Long id : toUpdateIds) {
				Boolean value = testCaseHasDirectCoverage(id)
					|| testCaseHasUndirectRequirementCoverage(id);
				result.put(id, value);
			}
		}

		return result;
	}


	@Override
	public boolean testCaseHasUndirectRequirementCoverage(long updatedTestCaseId) {
		List<Long> calledTestCaseIds = testCaseDao
			.findAllDistinctTestCasesIdsCalledByTestCase(updatedTestCaseId);
		if (!calledTestCaseIds.isEmpty()) {
			for (Long id : calledTestCaseIds) {
				if (testCaseHasDirectCoverage(id)
					|| testCaseHasUndirectRequirementCoverage(id)) {
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public boolean testCaseHasDirectCoverage(long updatedTestCaseId) {
		return requirementVersionDao.countVerifiedByTestCase(updatedTestCaseId) > 0;
	}

	private List<VerifiedRequirement> buildVerifiedRequirementList(
		final TestCase main,
		List<RequirementVersion> pagedVersionVerifiedByCalles) {

		List<VerifiedRequirement> toReturn = new ArrayList<>(
			pagedVersionVerifiedByCalles.size());

		for (RequirementVersion rVersion : pagedVersionVerifiedByCalles) {
			boolean isDirect = main.verifies(rVersion);
			toReturn.add(new VerifiedRequirement(rVersion, isDirect)
				.withVerifyingStepsFrom(main));
		}

		return toReturn;
	}

	@Override
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllDirectlyVerifiedRequirementsByTestStepId(
		long testStepId, PagingAndSorting paging) {
		TestStep step = testStepDao.findById(testStepId);
		return findAllDirectlyVerifiedRequirementsByTestCaseId(step
			.getTestCase().getId(), paging);
	}

	@Override
	public void removeVerifiedRequirementVersionsFromTestStep(
		List<Long> requirementVersionsIds, long testStepId) {

		List<RequirementVersionCoverage> coverages = requirementVersionCoverageDao
			.byRequirementVersionsAndTestStep(requirementVersionsIds,
				testStepId);

		// if cast exception well, the input were wrong and the thread was bound
		// to grind to halt.
		ActionTestStep ts = (ActionTestStep) testStepDao.findById(testStepId);
		for (RequirementVersionCoverage cov : coverages) {
			ts.removeRequirementVersionCoverage(cov);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion' , 'READ')"
		+ OR_HAS_ROLE_ADMIN)
	public void findCoverageStat(Long requirementVersionId, List<Long> iterationsIds, RequirementCoverageStat stats) {

		RequirementVersion mainVersion = requirementVersionDao.findOne(requirementVersionId);
		Requirement mainRequirement = mainVersion.getRequirement();
		List<RequirementVersion> descendants = findValidDescendants(mainRequirement);
		findCoverageRate(mainRequirement, mainVersion, descendants, stats);
		//if we have a valid perimeter (ie iteration(s)), we'll have to calculate verification and validation rates
		if (!iterationsIds.isEmpty()) {
			checkPerimeter(iterationsIds, stats);
			if (!stats.isCorruptedPerimeter()) {
				findExecutionRate(mainVersion, descendants, stats, iterationsIds);
			}
		}
		stats.convertRatesToPercent();
	}

	private void checkPerimeter(List<Long> iterationsIds,
								RequirementCoverageStat stats) {
		List<Iteration> iterations = iterationDao.findAllByIds(iterationsIds);
		if (iterations.size() != iterationsIds.size()) {
			stats.setCorruptedPerimeter(true);
		}
	}

	/**
	 * Extract a {@link Map}, key : {@link ExecutionStatus} value : {@link Long}.
	 * The goal is to perform arithmetic operation with this map to calculate several rates on {@link RequirementVersion}
	 * Constraints from specification Feat 4434 :
	 * <code>
	 * <ul>
	 * <li>Requirement without linked {@link TestStep} must be treated at {@link Execution} level, for last execution.
	 * We must also include fast pass so we take the {@link IterationTestPlanItem} status</li>
	 * <li>Requirement with linked {@link TestStep} must be treated at {@link ExecutionStep} level</li>
	 * <li>Only last execution must be considered for a given {@link IterationTestPlanItem}</li>
	 * <li>FastPass must be considered for all cases (ie even if the {@link RequirementVersion} is linked to {@link TestStep})</li>
	 * <li>Rate must be calculate on the designed {@link Requirement} and it's descendants</li>
	 * <li>The descendant list must be filtered by {@link Milestone} and exclude {@link RequirementVersion} with {@link RequirementStatus#OBSOLETE}</li>
	 * </ul>
	 * </code>
	 * @param mainVersion
	 * @param descendants
	 * @param stats pojo containing the computed stats
	 * @param iterationsIds
	 */
	private void findExecutionRate(RequirementVersion mainVersion,
								   List<RequirementVersion> descendants,
								   RequirementCoverageStat stats, List<Long> iterationsIds) {
		boolean hasDescendant = !descendants.isEmpty();
		Rate verificationRate = new Rate();
		Rate validationRate = new Rate();

		//see http://javadude.com/articles/passbyvalue.htm to understand why an array (or any object) is needed here
		Long[] mainUntestedElementsCount = new Long[]{0L};
		Map<ExecutionStatus, Long> mainStatusMap = new EnumMap<>(ExecutionStatus.class);
		makeStatusMap(mainVersion.getRequirementVersionCoverages(), mainUntestedElementsCount, mainStatusMap, iterationsIds);
		verificationRate.setRequirementVersionRate(doRateVerifiedCalculation(mainStatusMap, mainUntestedElementsCount[0]));
		validationRate.setRequirementVersionRate(doRateValidatedCalculation(mainStatusMap));

		if (hasDescendant) {
			verificationRate.setAncestor(true);
			validationRate.setAncestor(true);
			Set<RequirementVersionCoverage> descendantCoverages = getDescendantCoverages(descendants);
			Long[] descendantUntestedElementsCount = new Long[]{0L};
			Map<ExecutionStatus, Long> descendantStatusMap = new EnumMap<>(ExecutionStatus.class);
			makeStatusMap(descendantCoverages, descendantUntestedElementsCount, descendantStatusMap, iterationsIds);
			verificationRate.setRequirementVersionChildrenRate(doRateVerifiedCalculation(descendantStatusMap, descendantUntestedElementsCount[0]));
			validationRate.setRequirementVersionChildrenRate(doRateValidatedCalculation(descendantStatusMap));

			Long[] allUntestedElementsCount = new Long[]{0L};
			allUntestedElementsCount[0] = mainUntestedElementsCount[0] + descendantUntestedElementsCount[0];
			Map<ExecutionStatus, Long> allStatusMap = mergeMapResult(mainStatusMap, descendantStatusMap);
			verificationRate.setRequirementVersionGlobalRate(doRateVerifiedCalculation(allStatusMap, allUntestedElementsCount[0]));
			validationRate.setRequirementVersionGlobalRate(doRateValidatedCalculation(allStatusMap));
		}

		stats.addRate("verification", verificationRate);
		stats.addRate("validation", validationRate);
	}


	/**
	 * Return a merged map. For each {@link ExecutionStatus}, the returned value is the value in map1 + value in map 2.
	 * The state of the two arguments maps is preserved
	 * @param mainStatusMap
	 * @param descendantStatusMap
	 * @return
	 */
	private Map<ExecutionStatus, Long> mergeMapResult(
		Map<ExecutionStatus, Long> mainStatusMap,
		Map<ExecutionStatus, Long> descendantStatusMap) {
		Map<ExecutionStatus, Long> mergedStatusMap = new EnumMap<>(ExecutionStatus.class);
		EnumSet<ExecutionStatus> allStatus = EnumSet.allOf(ExecutionStatus.class);
		for (ExecutionStatus executionStatus : allStatus) {
			Long mainCount = mainStatusMap.get(executionStatus) == null ? 0l : mainStatusMap.get(executionStatus);
			Long descendantCount = descendantStatusMap.get(executionStatus) == null ? 0l : descendantStatusMap.get(executionStatus);
			Long totalCount = mainCount + descendantCount;
			mergedStatusMap.put(executionStatus, totalCount);
		}
		return mergedStatusMap;
	}

	/**
	 * As above but with no return. The second map is merged into the first map, witch orginal state is lost
	 * @param statusMap
	 * @param statusMapToMerge
	 */
	private void fusionMapResult(Map<ExecutionStatus, Long> statusMap,
								 Map<ExecutionStatus, Long> statusMapToMerge) {
		for (Entry<ExecutionStatus, Long> mergeEntry : statusMapToMerge.entrySet()) {
			ExecutionStatus executionStatus = mergeEntry.getKey();
			Long originalValue = statusMap.get(executionStatus);
			Long mergedValue = mergeEntry.getValue();
			if (mergedValue != null && originalValue == null) {
				statusMap.put(executionStatus, mergedValue);
			}
			if (mergedValue != null && originalValue != null) {
				statusMap.put(executionStatus, mergedValue + originalValue);
			}
		}
	}


	private Set<RequirementVersionCoverage> getDescendantCoverages(
		List<RequirementVersion> descendants) {
		Set<RequirementVersionCoverage> covs = new HashSet<>();
		for (RequirementVersion requirementVersion : descendants) {
			Set<RequirementVersionCoverage> coverages = requirementVersion.getRequirementVersionCoverages();
			if (!coverages.isEmpty()) {
				covs.addAll(coverages);
			}
		}
		return covs;
	}

	private List<Long> filterTCIds(List<Long> TCIds,
								   List<Long> tCWithItpiIds) {
		List<Long> filtered = new ArrayList<>();
		filtered.addAll(TCIds);
		filtered.removeAll(tCWithItpiIds);
		return filtered;
	}

	private List<Long> convertSetToList(Map<Long, Long> nbSimpleCoverageByTestCase) {
		List<Long> testCaseIds = new ArrayList<>();
		testCaseIds.addAll(nbSimpleCoverageByTestCase.keySet());
		return testCaseIds;
	}

	private List<Long> findTCWithItpi(
		List<Long> tcIds,
		List<Long> iterationsIds) {
		return iterationDao.findVerifiedTcIdsInIterations(tcIds, iterationsIds);
	}

	private void makeStatusMap(Set<RequirementVersionCoverage> covs,
							   Long[] untestedElementsCount, Map<ExecutionStatus, Long> statusMap, List<Long> iterationsIds) {
		List<RequirementVersionCoverage> simpleCoverage = new ArrayList<>();
		List<RequirementVersionCoverage> stepedCoverage = new ArrayList<>();
		Map<Long, Long> nbSimpleCoverageByTestCase = new HashMap<>();
		Map<Long, Long> nbSteppedCoverageByTestCase = new HashMap<>();
		partRequirementVersionCoverage(covs, simpleCoverage, stepedCoverage, nbSimpleCoverageByTestCase, nbSteppedCoverageByTestCase);
		//Find the test case with at least one itpi
		List<Long> simpleCoverageTCIds = convertSetToList(nbSimpleCoverageByTestCase);
		List<Long> simpleTCWithItpiIds = findTCWithItpi(simpleCoverageTCIds, iterationsIds);
		//Filter to have the test case without itpi
		List<Long> mainVersionTCWithoutItpiIds = filterTCIds(simpleCoverageTCIds, simpleTCWithItpiIds);
		Map<ExecutionStatus, Long> statusMapForSimple = findResultsForSimpleCoverage(simpleTCWithItpiIds, iterationsIds, nbSimpleCoverageByTestCase);

		//STEPPED
		//we need : TC without ITPI -> untested, TC With ITPI but no execution -> treated like fastpass (ie the status of ITPI is applied to each stepped coverage),
		//TC with execution -> treated at test step level
		List<Long> steppedCoverageTCIds = convertSetToList(nbSteppedCoverageByTestCase);
		List<Long> steppedCoverageTCIdsWithITPI = iterationDao.findVerifiedTcIdsInIterations(steppedCoverageTCIds, iterationsIds);
		List<Long> steppedCoverageTCIdsWithExecution = iterationDao.findVerifiedTcIdsInIterationsWithExecution(steppedCoverageTCIds, iterationsIds);
		List<Long> steppedCoverageTCIdsWithoutITPI = filterTCIds(steppedCoverageTCIds, steppedCoverageTCIdsWithITPI);
		List<Long> steppedCoverageTCIdsWithoutExecution = filterTCIds(steppedCoverageTCIdsWithITPI, steppedCoverageTCIdsWithExecution);

		//TC With ITPI but no execution are treated like simple testcase
		Map<ExecutionStatus, Long> statusMapForSteppedNoExecution = findResultsForSteppedCoverageWithoutExecution(stepedCoverage, steppedCoverageTCIdsWithoutExecution, iterationsIds);
		untestedElementsCount[0] = calculateUntestedElementCount(mainVersionTCWithoutItpiIds, nbSimpleCoverageByTestCase, stepedCoverage, steppedCoverageTCIdsWithoutITPI);
		Map<ExecutionStatus, Long> statusMapForSteppedWithExecution = findResultsForSteppedCoverageWithExecution(stepedCoverage, steppedCoverageTCIdsWithExecution);

		//merging the three map of results
		fusionMapResult(statusMap, statusMapForSimple);
		fusionMapResult(statusMap, statusMapForSteppedNoExecution);
		fusionMapResult(statusMap, statusMapForSteppedWithExecution);
	}


	@SuppressWarnings("unchecked")
	private Map<ExecutionStatus, Long> findResultsForSteppedCoverageWithoutExecution(
		List<RequirementVersionCoverage> stepedCoverage, List<Long> testCaseIds,
		List<Long> iterationsIds) {
		MultiMap testCaseExecutionStatus = iterationDao.findVerifiedITPI(testCaseIds, iterationsIds);
		Map<ExecutionStatus, Long> result = new EnumMap<>(ExecutionStatus.class);
		for (RequirementVersionCoverage cov : stepedCoverage) {
			Long tcId = cov.getVerifyingTestCase().getId();
			List<TestCaseExecutionStatus> tcsStatus = (List<TestCaseExecutionStatus>) testCaseExecutionStatus.get(tcId);
			if (tcsStatus != null) {
				for (TestCaseExecutionStatus tcStatus : tcsStatus) {
					//For each cov we must count one status per steps. So fast pass status is forwarded to steps...
					result.put(tcStatus.getStatus(), (long) cov.getVerifyingSteps().size());
				}
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private Map<ExecutionStatus, Long> findResultsForSteppedCoverageWithExecution(
		List<RequirementVersionCoverage> stepedCoverage, List<Long> mainVersionTCWithItpiIds) {
		List<Long> testStepsIds = new ArrayList<>();
		Map<ExecutionStatus, Long> result = new EnumMap<>(ExecutionStatus.class);
		//First we compute all testStep id in a list, to allow multiple occurrence of the same step.
		//Witch is not a good practice but is allowed by the app so we must take this possibility in account for calculations.
		for (RequirementVersionCoverage cov : stepedCoverage) {
			Long tcId = cov.getVerifyingTestCase().getId();
			if (mainVersionTCWithItpiIds.contains(tcId)) {
				for (ActionTestStep step : cov.getVerifyingSteps()) {
					testStepsIds.add(step.getId());
				}
			}
		}
		//now retrieve a list of exec steps
		MultiMap executionsStatus = executionStepDao.findStepExecutionsStatus(mainVersionTCWithItpiIds, testStepsIds);
		for (Long testStepsId : testStepsIds) {
			// [Issue 6943] If the testStep has never been executed, then it will not be take into account for the calculation.
			if (executionsStatus.containsKey(testStepsId)){
				List<ExecutionStep> executionSteps = (List<ExecutionStep>) executionsStatus.get(testStepsId);
				for (ExecutionStep executionStep : executionSteps) {
					//Here come horrible code to detect if ITPI was fast passed AFTER execution.
					//We have no attribute in model to help us, and no time to develop a proper solution.
					//So we'll use execution date on itpi and exec. If the delta between two date is superior to 2 seconds,
					//we consider it's a fast pass
					Execution execution = executionStep.getExecution();
					IterationTestPlanItem itpi = execution.getTestPlan();
					Date itpiDateLastExecutedOn = itpi.getLastExecutedOn();
					Date execDateLastExecutedOn = execution.getLastExecutedOn();
					ExecutionStatus status = ExecutionStatus.READY;
					//if execution dates are null, the execution was only READY, so we don't compare dates to avoid npe
					if (itpiDateLastExecutedOn != null && execDateLastExecutedOn != null) {
						DateTime itpiLastExecutedOn = new DateTime(itpi.getLastExecutedOn().getTime());
						DateTime execLastExecutedOn = new DateTime(execution.getLastExecutedOn().getTime());
						Interval interval = new Interval(execLastExecutedOn, itpiLastExecutedOn);
						boolean fastPass = interval.toDuration().isLongerThan(new Duration(2000L));
						//If we have a fast path use it for step status
						status = fastPass ? itpi.getExecutionStatus() : executionStep.getExecutionStatus();
					}
					Long memo = result.get(status);
					if (memo == null) {
						result.put(status, 1L);
					} else {
						result.put(status, memo + 1);
					}
				}
			}
		}
		return result;
	}

	private Long calculateUntestedElementCount(List<Long> mainVersionTCWithoutItpiIds, Map<Long, Long> nbSimpleCoverageByTestCase,
											   List<RequirementVersionCoverage> stepedCoverage, List<Long> steppedCoverageTCIdsWithoutITPI) {
		Long total = 0L;
		for (Long tcId : mainVersionTCWithoutItpiIds) {
			Long nbCovegrage = nbSimpleCoverageByTestCase.get(tcId);
			if (nbCovegrage != null && nbCovegrage != 0L) {
				total += nbCovegrage;
			}
		}
		for (Long tcId : steppedCoverageTCIdsWithoutITPI) {
			for (RequirementVersionCoverage cov : stepedCoverage) {
				if (cov.getVerifyingTestCase().getId().equals(tcId)) {
					total += cov.getVerifyingSteps().size();
				}
			}
		}
		return total;
	}

	private double doRateVerifiedCalculation(Map<ExecutionStatus, Long> fullCoverageResult, Long untestedElementsCount) {
		Set<ExecutionStatus> statusSet = getVerifiedStatus();
		return doRateCalculation(statusSet, fullCoverageResult, untestedElementsCount);
	}

	private double doRateValidatedCalculation(Map<ExecutionStatus, Long> fullCoverageResult) {
		Set<ExecutionStatus> validStatusSet = getValidatedStatus();
		Set<ExecutionStatus> verifiedStatusSet = getVerifiedStatus();
		return doRateCalculation(validStatusSet, verifiedStatusSet, fullCoverageResult);
	}


	/**
	 * Rate calculation for two status set.
	 * The count on the first one will be the numerator, the count one second set will be the denominator
	 * @param numeratorStatus
	 * @param fullCoverageResult
	 * @return
	 */
	private double doRateCalculation(Set<ExecutionStatus> numeratorStatus, Set<ExecutionStatus> denominatorStatus, Map<ExecutionStatus, Long> fullCoverageResult) {
		double numerator = countforStatus(fullCoverageResult, numeratorStatus);
		double denominator = countforStatus(fullCoverageResult, denominatorStatus);
		return numerator / denominator;
	}

	/**
	 * Rate calculation with some untested elements
	 * @param statusSet
	 * @param fullCoverageResult
	 * @param untestedElementsCount
	 * @return
	 */
	private double doRateCalculation(Set<ExecutionStatus> statusSet, Map<ExecutionStatus, Long> fullCoverageResult, Long untestedElementsCount) {
		//Implicit conversion of all Long and Integer in floating point number to allow proper rate operation
		double execWithRequiredStatus = countforStatus(fullCoverageResult, statusSet);
		double allExecutionCount = getCandidateExecCount(fullCoverageResult);
		double nbTCWithoutItpi = untestedElementsCount;
		return execWithRequiredStatus / (allExecutionCount + nbTCWithoutItpi);
	}

	private Long getCandidateExecCount(
		Map<ExecutionStatus, Long> fullCoverageResult) {
		Long nbStatus = 0L;
		for (Long countForOneStatus : fullCoverageResult.values()) {
			nbStatus += countForOneStatus;
		}
		return nbStatus;
	}


	private Long countforStatus(Map<ExecutionStatus, Long> fullCoverageResult,
								Set<ExecutionStatus> statusSet) {
		Long count = 0L;
		for (Entry<ExecutionStatus, Long> executionStatus : fullCoverageResult.entrySet()) {
			if (statusSet.contains(executionStatus.getKey())) {

				count += executionStatus.getValue();
			}
		}
		return count;
	}

	private Set<ExecutionStatus> getVerifiedStatus() {
		Set<ExecutionStatus> verifiedStatus = new HashSet<>();
		verifiedStatus.add(ExecutionStatus.SUCCESS);
		verifiedStatus.add(ExecutionStatus.SETTLED);
		verifiedStatus.add(ExecutionStatus.FAILURE);
		verifiedStatus.add(ExecutionStatus.BLOCKED);
		verifiedStatus.add(ExecutionStatus.UNTESTABLE);
		return verifiedStatus;
	}

	private Set<ExecutionStatus> getValidatedStatus() {
		Set<ExecutionStatus> verifiedStatus = new HashSet<>();
		verifiedStatus.add(ExecutionStatus.SUCCESS);
		verifiedStatus.add(ExecutionStatus.SETTLED);
		return verifiedStatus;
	}

	private Map<ExecutionStatus, Long> findResultsForSimpleCoverage(
		List<Long> testCaseIds, List<Long> iterationIds, Map<Long, Long> nbSimpleCoverageByTestCase) {
		List<TestCaseExecutionStatus> testCaseExecutionStatus = iterationDao.findExecStatusForIterationsAndTestCases(testCaseIds, iterationIds);
		Map<ExecutionStatus, Long> computedResults = new EnumMap<>(ExecutionStatus.class);
		for (TestCaseExecutionStatus oneTCES : testCaseExecutionStatus) {
			ExecutionStatus status = oneTCES.getStatus();
			Long nbCoverage = nbSimpleCoverageByTestCase.get(oneTCES.getTestCaseId());
			if (computedResults.containsKey(status)) {
				computedResults.put(status, computedResults.get(status) + nbCoverage);
			} else {
				computedResults.put(status, nbCoverage);
			}
		}
		return computedResults;
	}

	/**
	 * Part the {@link RequirementVersionCoverage} list in two list :
	 * One with {@link RequirementVersionCoverage} with linked test steps.
	 * One with {@link RequirementVersionCoverage} without linked test steps.
	 * This is necessary as {@link RequirementVersionCoverage} with {@link TestStep} linked must be treated at step level
	 * @param requirementVersionCoverages
	 * @param simpleCoverage
	 * @param stepedCoverage
	 * @param nbSimpleCoverageByTestCase
	 */
	private void partRequirementVersionCoverage(
		Set<RequirementVersionCoverage> requirementVersionCoverages, List<RequirementVersionCoverage> simpleCoverage,
		List<RequirementVersionCoverage> stepedCoverage, Map<Long, Long> nbSimpleCoverageByTestCase,
		Map<Long, Long> nbSteppedCoverageByTestCase) {
		for (RequirementVersionCoverage requirementVersionCoverage : requirementVersionCoverages) {
			Long tcId = requirementVersionCoverage.getVerifyingTestCase().getId();
			if (requirementVersionCoverage.hasSteps()) {
				stepedCoverage.add(requirementVersionCoverage);
				if (nbSteppedCoverageByTestCase.containsKey(tcId)) {
					nbSteppedCoverageByTestCase.put(tcId, nbSteppedCoverageByTestCase.get(tcId) + 1);
				} else {
					nbSteppedCoverageByTestCase.put(tcId, 1L);
				}
			} else {
				simpleCoverage.add(requirementVersionCoverage);
				if (nbSimpleCoverageByTestCase.containsKey(tcId)) {
					nbSimpleCoverageByTestCase.put(tcId, nbSimpleCoverageByTestCase.get(tcId) + 1);
				} else {
					nbSimpleCoverageByTestCase.put(tcId, 1L);
				}
			}
		}

	}

	private void findCoverageRate(Requirement mainRequirement, RequirementVersion mainVersion,
								  List<RequirementVersion> descendants, RequirementCoverageStat stats) {

		Rate coverageRate = new Rate();
		boolean hasValidDescendant = !descendants.isEmpty();
		coverageRate.setRequirementVersionRate(calculateCoverageRate(mainVersion));

		if (hasValidDescendant) {
			coverageRate.setRequirementVersionChildrenRate(calculateCoverageRate(descendants));
			List<RequirementVersion> all = getAllRequirementVersion(mainVersion, descendants);
			coverageRate.setRequirementVersionGlobalRate(calculateCoverageRate(all));
			coverageRate.setAncestor(true);
		}
		stats.addRate("coverage", coverageRate);
		stats.setAncestor(hasValidDescendant);
	}

	private List<RequirementVersion> getAllRequirementVersion(
		RequirementVersion mainVersion, List<RequirementVersion> descendants) {
		List<RequirementVersion> all = new ArrayList<>();
		all.add(mainVersion);
		all.addAll(descendants);
		return all;
	}

	private double calculateCoverageRate(List<RequirementVersion> rvs) {
		double total = 0;
		double size = rvs.size();
		for (RequirementVersion rv : rvs) {
			total += calculateCoverageRate(rv);
		}
		return total / size;
	}

	/**
	 * Coverage Rate is 100% for 1+ {@link TestCase} linked to this {@link RequirementVersion}. 0% if no link
	 * @param mainVersion
	 * @return
	 */
	private Long calculateCoverageRate(RequirementVersion mainVersion) {
		if (!mainVersion.getRequirementVersionCoverages().isEmpty()) {
			return 1L;
		}
		return 0L;
	}

	private List<RequirementVersion> findValidDescendants(Requirement requirement) {
		List<Long> candidatesIds = requirementDao.findDescendantRequirementIds(Arrays.asList(requirement.getId()));
		List<Requirement> candidates = requirementDao.findAllByIds(candidatesIds);
		return extractCurrentVersions(candidates);
	}

	private List<RequirementVersion> extractCurrentVersions(List<Requirement> requirements) {

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		List<RequirementVersion> rvs = new ArrayList<>(requirements.size());
		for (Requirement requirement : requirements) {
			RequirementVersion rv = requirement.getResource();

			if (rv.isNotObsolete()) {

				if (!activeMilestone.isPresent() || rv.getMilestones().contains(activeMilestone.get())) {
					rvs.add(rv);
				}

			}
		}
		return rvs;
	}

}
