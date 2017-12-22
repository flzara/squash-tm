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
package org.squashtest.tm.service.bugtracker;

import java.net.URL;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor;
import org.squashtest.tm.bugtracker.advanceddomain.DelegateCommand;
import org.squashtest.tm.bugtracker.definition.Attachment;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.bugtracker.definition.RemoteProject;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.bugtracker.Issue;
import org.squashtest.tm.domain.bugtracker.IssueDetector;
import org.squashtest.tm.domain.bugtracker.IssueOwnership;
import org.squashtest.tm.domain.bugtracker.RemoteIssueDecorator;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.servers.AuthenticationStatus;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.testcase.TestCase;

@Transactional
public interface BugTrackersLocalService {

	/* ******************* Squash TM - side methods ****************** */

	/**
	 * adds a new Issue to the entity. The entity must implement IssueDetector.
	 *
	 * @param entityId
	 *            : the id of that entity.
	 * @param entityClass
	 *            : the actual class of that entity, that implements IssueDetector.
	 * @param issue
	 *            : the issue to add
	 * @return the BTIssue corresponding to the bug remotely created
	 */
	RemoteIssue createIssue(IssueDetector entity, RemoteIssue issue);

	/**
	 *
	 * Gets the url of a remote Issue given its Id
	 *
	 * @param btIssueId
	 *            the id of that issue
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return the URL where you may find that issue.
	 */
	URL getIssueUrl(String btIssueId, BugTracker bugTracker);

	/**
	 * An InterfaceDescriptor contains informations relevant to the generation of a view/GUI. See the class for more
	 * details.
	 *
	 * @param bugTracker
	 *            the concerned BugTracker
	 * @return an InterfaceDescriptor.
	 */
	BugTrackerInterfaceDescriptor getInterfaceDescriptor(BugTracker bugTracker);

	/**
	 * Given an ExecutionStep, returns a list of linked BTIssue (not Issue). <br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param stepId
	 *            of which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 * @return a PagedCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the PagingAndSorting.
	 */
	PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnerShipsForExecutionStep(Long stepId,
			PagingAndSorting sorter);

	/**
	 * Given an Execution, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param execId
	 *            of which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 *
	 * @return a PagedCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the PagingAndSorting.
	 */
	PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsforExecution(Long execId,
			PagingAndSorting sorter);

	/**
	 * Given an Iteration, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param iterId
	 *            of which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 *
	 * @return a PagedCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the PagingAndSorting.
	 */
	PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForIteration(Long iterId,
			PagingAndSorting sorter);

	/**
	 * Given an Campaign, returns a list of linked BTIssue (not Issue)<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param campId
	 *            of which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 *
	 * @return a PagedCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the PagingAndSorting.
	 */
	PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsForCampaign(Long campId,
			PagingAndSorting sorter);

	/**
	 * Given a TestSuite, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param testSuiteId
	 *            for which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 *
	 * @return a PagedCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the PagingAndSorting.
	 */
	PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipsForTestSuite(Long testSuiteId,
			PagingAndSorting sorter);

	/**
	 * Given a TestCase, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param testCase
	 *            id for which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 *
	 * @return a PagedCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the PagingAndSorting.
	 */
	PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForTestCase(Long tcId,
			PagingAndSorting sorter);

	/**
	 * Given a CampaignFolder, returns a list of linked BTIssue (not Issue) for all campaigns, iterations etc this folder contains.<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param cfId
	 *            for which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 *
	 * @return a PagedCollectionHolder containing a non-null but possibly empty list of IssueOwnership<Issue>, sorted
	 *         and filtered according to the PagingAndSorting.
	 */
	PagedCollectionHolder<List<IssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForCampaignFolder(Long cfId,
			PagingAndSorting sorter);


	/**
	 * Given a RequirmentVersion, can return a list of linked BTIssue (not Issue) for all leaf requirements this requirement contains.<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a RequirementVersionIssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param rvId
	 *            for which we need to get the issues,
	 * @param sorter
	 *            that tells us how we should sort and filter the data
	 *
	 * @return a PagedCollectionHolder containing a non-null but possibly empty list of RequirementVersionIssueOwnership<Issue>, sorted
	 *         and filtered according to the PagingAndSorting.
	 */
	PagedCollectionHolder<List<RequirementVersionIssueOwnership<RemoteIssueDecorator>>> findSortedIssueOwnershipForRequirmentVersion( Long rvId, String panelSource,
																																	 PagingAndSorting sorter);

	/**
	 * Given a TestCase, returns a list of linked BTIssue (not Issue).<br>
	 * <br>
	 * To keep track of which IssueDetector owns which issue, the data are wrapped in a IssueOwnership (that just pair
	 * the informations together).
	 *
	 * @param testCase
	 *            id for which we need to get the issues,
	 *
	 * @return a  non-null but possibly empty list of IssueOwnership,
	 **/
	List<IssueOwnership<RemoteIssueDecorator>> findIssueOwnershipForTestCase(long testCaseId);

	/* ****************** BugTracker - side methods ******************** */

	/**
	 * tests if the bugtracker is ready for use
	 *
	 * @param project : the concerned Project
	 * @return the status of the bugtracker
	 *
	 */
	AuthenticationStatus checkBugTrackerStatus(Project project);


	/**
	 * same as {@link #checkBugTrackerStatus(Project)}, using the id of the project
	 * instead.
	 *
	 * @param projectId
	 * @return
	 */
	AuthenticationStatus checkBugTrackerStatus(Long projectId);

	/**
	 * says whether the user is authenticated against that bugtracker regardless
	 * of the bindings with projects.
	 *
	 * @param bugtracker
	 * @return
	 */
	AuthenticationStatus checkAuthenticationStatus(Long bugtrackerId);


	/**
	 * sets the credentials of an user for authentication bugtracker-side. This operation is illegal if the bugtracker is set to use
	 * {@link org.squashtest.tm.domain.servers.AuthenticationPolicy#APP_LEVEL}.
	 *
	 * @param credentials
	 * @param bugTracker : the concerned BugTracker
	 * @return nothing
	 * @throws BugTrackerRemoteException if the credentials are wrong
	 * @throws org.squashtest.csp.core.bugtracker.service.WrongAuthenticationPolicyException
	 * @throws {@link UnsupportedAuthenticationModeException} if the connector does not support such authentication
	 */
	void setCredentials(Credentials credentials, BugTracker bugTracker) throws BugTrackerRemoteException;

	/**
	 * Same as {@link #setCredentials(String, String, BugTracker)}, but the bugtracker is identified by its id.
	 *
	 * @param credentials
	 * @param bugtrackerId
	 * @throws BugTrackerRemoteException
	 * @throws org.squashtest.csp.core.bugtracker.service.WrongAuthenticationPolicyException
	 * @throws {@link UnsupportedAuthenticationModeException}
	 */
	void setCredentials(Credentials credentials, Long bugtrackerId) throws BugTrackerRemoteException;

	/**
	 * Same as {@link #setCredentials(String, String, BugTracker)}, using behind the scene
	 * {@link org.squashtest.tm.domain.servers.BasicAuthenticationCredentials} with the
	 * given username and password
	 *
	 * @deprecated use {@link #setCredentials(Credentials, BugTracker)} instead
	 * @param username
	 * @param password
	 * @param bugTracker : the concerned BugTracker
	 * @return nothing
	 * @throws BugTrackerRemoteException if the credentials are wrong
	 * @throws org.squashtest.csp.core.bugtracker.service.WrongAuthenticationPolicyException
	 * @throws {@link UnsupportedAuthenticationModeException}
	 */
	@Deprecated
	void setCredentials(String username, String password, BugTracker bugTracker) throws BugTrackerRemoteException;

	/**
	 * Same as {@link #setCredentials(String, String, BugTracker)}, but the bugtracker is identified by its id.
	 *
	 * @deprecated
	 * @param username
	 * @param password
	 * @param bugtrackerId
	 * @throws BugTrackerRemoteException
	 * @throws org.squashtest.csp.core.bugtracker.service.WrongAuthenticationPolicyException
	 * @throws {@link UnsupportedAuthenticationModeException}
	 */
	@Deprecated
	void setCredentials(String username, String password, Long bugtrackerId) throws BugTrackerRemoteException;

	/**
	 * returns an instance of the remote project.
	 *
	 * @param name
	 *            : the name of the project.
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return the project filled with users and versions if found.
	 * @throw BugTrackerManagerException and subtypes.
	 *
	 */
	RemoteProject findRemoteProject(String name, BugTracker bugTracker);

	/**
	 * Must return ready-to-fill issue, ie with empty fields and its project configured with as many metadata as possible related to issue creation.
	 *
	 * @param projectName
	 * @param BugTracker bugTracker
	 * @return
	 */
	RemoteIssue createReportIssueTemplate(String projectName, BugTracker bugTracker);

	/**
	 * returns a remote issue using its key
	 *
	 * @param issueKey
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return a remote issue
	 */
	RemoteIssue getIssue(String issueKey, BugTracker bugTracker);

	/***
	 * returns a list of BTIssu corresponding to the given string keys
	 *
	 * @param issueKeyList
	 *            the remote issue key list
	 * @param bugTracker
	 *            : the concerned BugTracker
	 * @return a BTIssue list
	 */
	List<? extends RemoteIssue> getIssues(List<String> issueKeyList, BugTracker bugTracker);

	/**
	 * Will attach an existing issue to the issue detector
	 *
	 * @param bugged
	 *            : the future issue holder
	 * @param remoteIssueKey
	 *            : the identificator of the issue in the BT
	 *
	 */
	void attachIssue(IssueDetector bugged, String remoteIssueKey);

	/**
	 *
	 * @return the list of all bugtracker kinds available
	 */
	Set<String> getProviderKinds();


	/**
	 * Will detach an existing issue from an issue detector
	 *
	 *
	 * @param issueId
	 * 			: the id of the issue in Squash TM
	 */
	void detachIssue(long id);


	/**
	 * Given a remote issue key, will ask the bugtracker to attach the attachments to that issue.
	 * In order to prevent possible conflicts (multiple issue may have the same id if defined on different bugtrackers)
	 * the bugtracker name is required too.
	 *
	 * @param remoteIssueKey
	 * @Param bugtrackerName
	 * @param attachments
	 */
	void forwardAttachments(String remoteIssueKey, String bugtrackerName, List<Attachment> attachments);


	/**
	 * forwards a {@link DelegateCommand} to a connector
	 *
	 * @param command
	 * @return
	 */
	Object forwardDelegateCommand(DelegateCommand command, String bugtrackerName);

	int findNumberOfIssueForTestCase(Long id);

	/**
	 * self-explanatory
	 */
	int findNumberOfIssueForItemTestPlanLastExecution(Long itemTestPlanId);

	/**
	 * self-explanatory
	 */
	int findNumberOfIssueForExecutionStep(Long testStepId);

	TestCase findTestCaseRelatedToIssue(Long issueId);


	Issue findIssueById(Long id);

	List<Execution> findExecutionsByRemoteIssue(String remoteid, String name);

	List<Issue> getIssueList(String remoteid, String name);

	Execution findExecutionByIssueId(Long id);

}
