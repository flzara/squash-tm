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
package org.squashtest.tm.service.internal.bugtracker;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.IdCollector;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueList;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.servers.AuthenticationStatus;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.IssueAlreadyBoundException;
import org.squashtest.tm.service.bugtracker.BugTrackersLocalService;
import org.squashtest.tm.service.bugtracker.BugTrackersService;
import org.squashtest.tm.service.bugtracker.RequirementVersionIssueOwnership;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.ExecutionStepDao;
import org.squashtest.tm.service.internal.repository.IssueDao;
import org.squashtest.tm.service.internal.repository.IterationTestPlanDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.RequirementSyncExtenderDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.servers.StoredCredentialsManager;
import org.squashtest.tm.service.servers.UserCredentialsCache;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_CAMPAIGN_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_CAMPFOLDER_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_EXECSTEP_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_EXECUTION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_ITERATION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_REQVERSION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_TC_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_TS_OR_ROLE_ADMIN;

/**
 * See doc on the interface
 */
@Service("squashtest.tm.service.BugTrackersLocalService")
public class BugTrackersLocalServiceImpl implements BugTrackersLocalService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackersLocalServiceImpl.class);

	@Value("${squashtm.bugtracker.timeout:15}")
	private long timeout;

	@Inject
	private IssueDao issueDao;

	@Inject
	private BugTrackersService remoteBugTrackersService;

	@Inject
	private ExecutionDao executionDao;

	@Inject
	private ExecutionStepDao executionStepDao;


	@Inject
	private IterationTestPlanDao iterationTestPlanDao;


	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private BugTrackerDao bugTrackerDao;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private CredentialsProvider credentialsProvider;

	@Inject
	private StoredCredentialsManager storedCredentialsManager;

	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	@Inject
	private Map<String, IssueOwnershipFinder> issueOwnershipFinderByBeanName;
	@Inject
	private RequirementVersionIssueFinder requirementVersionIssueFinder;

	@Inject
	private RequirementSyncExtenderDao requirementSyncExtenderDao;

	@Override
	public BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugTracker) {
		return remoteBugTrackersService.getInterfaceDescriptor(bugTracker);
	}

	private LocaleContext getLocaleContext() {
		return LocaleContextHolder.getLocaleContext();
	}

	private SecurityContext getSecurityContext(){
		return SecurityContextHolder.getContext();
	}

	private UserCredentialsCache getCredentialsCache(){
		return credentialsProvider.getCache();
	}

	@Override
	@PreAuthorize("hasPermission(#entity, 'EXECUTE')" + OR_HAS_ROLE_ADMIN)
	public AuthenticationStatus checkBugTrackerStatus(Project project) {
		AuthenticationStatus status;

		if (!project.isBugtrackerConnected()) {
			status = AuthenticationStatus.UNDEFINED;
		} else if (remoteBugTrackersService.isCredentialsNeeded(project.findBugTracker())) {
			status = AuthenticationStatus.NON_AUTHENTICATED;
		} else {
			status = AuthenticationStatus.AUTHENTICATED;
		}
		return status;
	}

	@Override
	public AuthenticationStatus checkBugTrackerStatus(Long projectId) {
		Project project = projectDao.getOne(projectId);
		return checkBugTrackerStatus(project);
	}

	@Override
	public AuthenticationStatus checkAuthenticationStatus(Long bugtrackerId) {
		AuthenticationStatus status;
		Optional<BugTracker> optBugtracker = bugTrackerDao.findById(bugtrackerId);

		if (optBugtracker.isPresent()){
			boolean needs = remoteBugTrackersService.isCredentialsNeeded(optBugtracker.get());
			status = needs ? AuthenticationStatus.NON_AUTHENTICATED : AuthenticationStatus.AUTHENTICATED;
		}
		else{
			// sloppy semantics, but I'm not fixing what is not broken yet.
			status = AuthenticationStatus.UNDEFINED;
		}

		return status;
	}

	private RemoteIssue createRemoteIssue(IssueDetector entity, RemoteIssue btIssue) {

		BugTracker bugTracker = entity.getBugTracker();
		String btName = bugTracker.getName();
		btIssue.setBugtracker(btName);

		RemoteIssue createdIssue = remoteBugTrackersService.createIssue(btIssue, bugTracker);
		createdIssue.setBugtracker(btName);

		return createdIssue;
	}

	@Override
	public RemoteIssue createIssue(IssueDetector entity, RemoteIssue btIssue) {

		RemoteIssue createdIssue = createRemoteIssue(entity, btIssue);
		// if success we set the bug in Squash TM database
		// a success being : we reach this code with no exceptions
		BugTracker bugTracker = entity.getBugTracker();

		Issue sqIssue = new Issue();
		sqIssue.setRemoteIssueId(createdIssue.getId());
		sqIssue.setBugtracker(bugTracker);

		IssueList list = entity.getIssueList();

		list.addIssue(sqIssue);

		issueDao.save(sqIssue);
		return createdIssue;
	}

	@Override
	public RemoteIssue getIssue(String issueKey, BugTracker bugTracker) {
		return remoteBugTrackersService.getIssue(issueKey, bugTracker);
	}

	@Override
	public List<RemoteIssue> getIssues(List<String> issueKeyList, BugTracker bugTracker) {

		try {
			Future<List<RemoteIssue>> futureIssues = remoteBugTrackersService.getIssues(issueKeyList, bugTracker, getCredentialsCache(), getLocaleContext(), getSecurityContext());
			return futureIssues.get(timeout, TimeUnit.SECONDS);
		}
		catch (TimeoutException timex) {
			throw new BugTrackerRemoteException(timex);
		}
		catch (InterruptedException | ExecutionException e) {
			throw new BugTrackerRemoteException(e);
		}

	}

	@Override
	// when purgeOnFail is true, we need to alter the database content (eg deleting the faulty credentials) then commit,
	// while still propagating the exception. Here we define an exception rule to the default "rollback on exception" policy.
	@Transactional(noRollbackFor={BugTrackerNoCredentialsException.class, UnsupportedAuthenticationModeException.class})
	public void validateCredentials(BugTracker bugTracker, Credentials credentials, boolean purgeOnFail) throws BugTrackerRemoteException {

		LOGGER.debug("BugTrackerLocalServiceImpl : validating credentials for server '{}'", bugTracker.getName());

		try{
			remoteBugTrackersService.testCredentials(bugTracker, credentials);
			LOGGER.debug("BugTrackerLocalServiceImpl : credentials successfully validated");
			credentialsProvider.cacheCredentials(bugTracker, credentials);
		}
		catch(BugTrackerNoCredentialsException | UnsupportedAuthenticationModeException ex){
			LOGGER.debug("BugTrackerLocalServerImpl : credentials were rejected ({})", ex.getClass());
			if (purgeOnFail){
				LOGGER.debug("BugTrackerLocalServiceImpl : removing failed credentials as requested");
				credentialsProvider.uncacheCredentials(bugTracker);
				storedCredentialsManager.deleteUserCredentials(bugTracker.getId(), credentialsProvider.currentUser());
			}
			// propagate exception
			throw ex;
		}

	}

	@Override
	// see comment above
	@Transactional(noRollbackFor={BugTrackerNoCredentialsException.class, UnsupportedAuthenticationModeException.class})
	public void validateCredentials(Long bugtrackerId, Credentials credentials, boolean purgeOnFail) throws BugTrackerRemoteException {
		BugTracker server = bugTrackerDao.getOne(bugtrackerId);
		validateCredentials(server, credentials, purgeOnFail);
	}


	/* ************** delegate methods ************* */

	@Override
	public RemoteProject findRemoteProject(String name, BugTracker bugTracker) {
		return remoteBugTrackersService.findProject(name, bugTracker);

	}

	@Override
	public RemoteIssue createReportIssueTemplate(String projectName, BugTracker bugTracker) {
		return remoteBugTrackersService.createReportIssueTemplate(projectName, bugTracker);
	}



	@Override
	public URL getIssueUrl(String btIssueId, BugTracker bugTracker) {
		return remoteBugTrackersService.getViewIssueUrl(btIssueId, bugTracker);
	}

	@Override
	public void forwardAttachments(String remoteIssueKey, String bugtrackerName, List<Attachment> attachments) {
		BugTracker bugtracker = bugTrackerDao.findByName(bugtrackerName); // NOTE : this may crash is multiple
		// bugtracker have the same name. One could
		// cross check with the remoteissuekey if
		// one day shit happened.
		remoteBugTrackersService.forwardAttachments(remoteIssueKey, bugtracker, attachments);
	}

	@Override
	public Object forwardDelegateCommand(DelegateCommand command, String bugtrackerName) {
		BugTracker bugtracker = bugTrackerDao.findByName(bugtrackerName);
		return remoteBugTrackersService.forwardDelegateCommand(command, bugtracker);
	}

	@Override
	@PreAuthorize("hasPermission(#bugged, 'EXECUTE')" + OR_HAS_ROLE_ADMIN)
	public void attachIssue(IssueDetector bugged, String remoteIssueKey) {

		IssueList issueList = bugged.getIssueList();

		if (issueList.hasRemoteIssue(remoteIssueKey)) {
			throw new IssueAlreadyBoundException();
		} else {
			Issue issue = new Issue();
			issue.setBugtracker(bugged.getBugTracker());
			issue.setRemoteIssueId(remoteIssueKey);
			issueList.addIssue(issue);
			issueDao.save(issue);
		}

	}

	@Override
	public void detachIssue(long id) {
		IssueDetector bugged = issueDao.findIssueDetectorByIssue(id);
		PermissionsUtils.checkPermission(permissionEvaluationService, new SecurityCheckableObject(bugged, "EXECUTE"));

		Issue issue = issueDao.getOne(id);
		TestCase testCase = this.findTestCaseRelatedToIssue(issue.getId());
		issueDao.delete(issue);
	}

	/* ------------------------ExecutionStep--------------------------------------- */
	@Override
	@PreAuthorize(READ_EXECSTEP_OR_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnerShipsForExecutionStep(
		Long executionStepId, PagingAndSorting sorter) {
		return issueFinder("executionStepIssueFinder").findSorted(executionStepId, sorter);

	}


	/* ------------------------Execution--------------------------------------- */
	@Override
	@PreAuthorize(READ_EXECUTION_OR_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsforExecution(
		Long executionId, PagingAndSorting sorter) {
		// FIXME : SHOULD RETURN EXECS AND STEPS PAIRS !
		return issueFinder("executionIssueFinder").findSorted(executionId, sorter);
	}

	/* ------------------------TestSuite--------------------------------------- */
	@Override
	@PreAuthorize(READ_TS_OR_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsForTestSuite(
		Long testSuiteId, PagingAndSorting sorter) {
		return issueFinder("testSuiteIssueFinder").findSorted(testSuiteId, sorter);
	}

	/* ------------------------Iteration--------------------------------------- */

	@Override
	@PreAuthorize(READ_ITERATION_OR_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForIteration(
		Long iterationId, PagingAndSorting sorter) {
		return issueFinder("iterationIssueFinder").findSorted(iterationId, sorter);
	}

	@SuppressWarnings("unchecked")
	private IssueOwnershipFinder issueFinder(String finderBeanName) {
		IssueOwnershipFinder res = issueOwnershipFinderByBeanName.get(finderBeanName);
		if (res == null) {
			throw new IllegalArgumentException("Bean of type 'IssueOwnershipFinderSupport' and named '" + finderBeanName + "' could not be found. This either means the bean was not instanciated by Spring or it has another name");
		}
		return res;
	}

	@Override
	@PreAuthorize(READ_CAMPAIGN_OR_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsForCampaign(
		Long campaignId, PagingAndSorting sorter) {
		return issueFinder("campaignIssueFinder").findSorted(campaignId, sorter);
	}

	@Override
	@PreAuthorize(READ_CAMPFOLDER_OR_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForCampaignFolder(
		Long campFolderId, PagingAndSorting sorter) {
		return issueFinder("campaignFolderIssueFinder").findSorted(campFolderId, sorter);
	}

	@Override
	@PreAuthorize(READ_REQVERSION_OR_ROLE_ADMIN)
	public PagedCollectionHolder<List<RequirementVersionIssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForRequirmentVersion(
		 Long requirementVersionId, String panelSource, PagingAndSorting sorter) {
		return requirementVersionIssueFinder.findSorted(requirementVersionId, panelSource, sorter);
	}

	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	public PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForTestCase(
		Long testCaseId, PagingAndSorting sorter) {
		return issueFinder("testCaseIssueFinder").findSorted(testCaseId, sorter);
	}


	@Override
	@PreAuthorize(READ_TC_OR_ROLE_ADMIN)
	public List<IssueOwnership<RemoteIssueDecorator>> findIssueOwnershipForTestCase(long testCaseId) {

		// create filtredCollection of IssueOwnership<BTIssue>
		DefaultPagingAndSorting sorter = new DefaultPagingAndSorting("Issue.id", true);

		return findSortedIssueOwnershipForTestCase(testCaseId, sorter).getPagedItems();
	}


	/* ------------------------generic--------------------------------------- */

	@SuppressWarnings("unchecked")
	private List<ExecutionStep> collectExecutionStepsFromExecution(List<Execution> executions) {
		List<Long> execIds = (List<Long>) CollectionUtils.collect(executions, new IdCollector(), new ArrayList<Long>());
		return executionDao.findStepsForAllExecutions(execIds);
	}


	/**
	 * creates the map [detector.issueList.id : detector] from a list of detectors
	 *
	 */
	private Map<Long, IssueDetector> createIssueDetectorByIssueListId(List<? extends IssueDetector> issueDetectors) {
		Map<Long, IssueDetector> issueDetectorByListId = new HashMap<>();

		for (IssueDetector issueDetector : issueDetectors) {
			issueDetectorByListId.put(issueDetector.getIssueListId(), issueDetector);
		}
		return issueDetectorByListId;
	}


	@Override
	public Set<String> getProviderKinds() {
		return remoteBugTrackersService.getProviderKinds();
	}


	@Override
	public int findNumberOfIssueForTestCase(Long tcId) {

		// Find all concerned IssueDetector
		List<Execution> executions = testCaseDao.findAllExecutionByTestCase(tcId);
		List<ExecutionStep> executionSteps = collectExecutionStepsFromExecution(executions);

		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByIssueListId(executions);
		Map<Long, IssueDetector> executionStepByListId = createIssueDetectorByIssueListId(executionSteps);
		issueDetectorByListId.putAll(executionStepByListId);

		// Extract ids out of Executions and ExecutionSteps
		List<Long> executionIds = IdentifiedUtil.extractIds(executions);
		List<Long> executionStepIds = IdentifiedUtil.extractIds(executionSteps);

		return issueDao.countIssuesfromExecutionAndExecutionSteps(executionIds, executionStepIds);
	}

	@Override
	public int findNumberOfIssueForItemTestPlanLastExecution(Long itemTestPlanId) {

		IterationTestPlanItem itp = iterationTestPlanDao.getOne(itemTestPlanId);

		Execution execution = itp.getLatestExecution();
		if (execution == null) {
			return 0;
		} else {
			List<Execution> executions = new ArrayList<>();
			executions.add(execution);
			return findNumberOfIssueForExecutions(executions);
		}
	}

	@Override
	public int findNumberOfIssueForExecutionStep(Long testStepId) {
		List<Long> executionStepIds = new ArrayList<>();
		executionStepIds.add(testStepId);
		return issueDao.countIssuesfromExecutionSteps(executionStepIds);
	}

	private int findNumberOfIssueForExecutions(List<Execution> executions) {

		List<ExecutionStep> executionSteps = collectExecutionStepsFromExecution(executions);

		Map<Long, IssueDetector> issueDetectorByListId = createIssueDetectorByIssueListId(executions);
		Map<Long, IssueDetector> executionStepByListId = createIssueDetectorByIssueListId(executionSteps);
		issueDetectorByListId.putAll(executionStepByListId);

		// Extract ids out of Executions and ExecutionSteps
		List<Long> executionIds = IdentifiedUtil.extractIds(executions);
		List<Long> executionStepIds = IdentifiedUtil.extractIds(executionSteps);

		if(!executionStepIds.isEmpty()){
			return issueDao.countIssuesfromExecutionAndExecutionSteps(executionIds, executionStepIds);
		} else {
			return issueDao.countIssuesfromEmptyExecutions(executionIds);
		}

	}

	@Override
	public TestCase findTestCaseRelatedToIssue(Long issueId) {
		return issueDao.findTestCaseRelatedToIssue(issueId);
	}

	@Override
	public Issue findIssueById(Long id) {
		return issueDao.getOne(id);
	}

	@Override
	public List<Issue> getIssueList(String remoteid, String name) {
		BugTracker bugtracker = bugTrackerDao.findByName(name);
		if (bugtracker != null) {
			return issueDao.findIssueListByRemoteIssue(remoteid, bugtracker);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public Execution findExecutionByIssueId(Long id) {
		return issueDao.findExecutionRelatedToIssue(id);
	}

	@Override
	public List<Execution> findExecutionsByRemoteIssue(String remoteid, String name) {
		List<Issue> issues = getIssueList(remoteid, name);
		List<Execution> executions = new ArrayList<>();
		for (Issue issue : issues) {
			Execution execution = issueDao.findExecutionRelatedToIssue(issue.getId());
			if (execution != null) {
				executions.add(execution);
			}
		}
		return executions;
	}

	@Override
	public List<String> findAllRemoteReqIdByServerUrlVerifiedByATestCase(String serverUrl, Long testCaseId){
		return requirementSyncExtenderDao.findAllRemoteReqIdVerifiedByATestCaseByServerUrl(serverUrl, testCaseId);
	}

	@Override
	public void linkIssueToRemoteRequirements(String issueId, List<String> remoteReqIds, BugTracker bugtracker) {
		remoteBugTrackersService.linkIssueToRemoteRequirements(issueId, remoteReqIds, bugtracker);
	}

}
