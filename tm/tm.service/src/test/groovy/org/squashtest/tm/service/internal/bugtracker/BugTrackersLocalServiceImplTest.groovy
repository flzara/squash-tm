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
package org.squashtest.tm.service.internal.bugtracker

import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException
import org.squashtest.csp.core.bugtracker.domain.BTIssue
import org.squashtest.csp.core.bugtracker.domain.BTProject
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.tm.domain.bugtracker.Issue
import org.squashtest.tm.domain.bugtracker.IssueList
import org.squashtest.tm.domain.bugtracker.IssueOwnership
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.servers.AuthenticationStatus

import org.squashtest.tm.service.bugtracker.BugTrackersService
import org.squashtest.tm.service.internal.repository.IssueDao
import org.squashtest.tm.service.servers.CredentialsProvider
import org.squashtest.tm.service.servers.StoredCredentialsManager
import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class BugTrackersLocalServiceImplTest extends Specification {
	BugTrackersLocalServiceImpl service = new BugTrackersLocalServiceImpl()

	IssueDao issueDao = Mock()
	BugTrackersService bugTrackersService = Mock()
	CredentialsProvider credentialsProvider = Mock()
	StoredCredentialsManager storedCredentialsManager = Mock()

	// alias
	BugTrackersService remoteService = bugTrackersService;

	def setup() {
		service.issueDao = issueDao
		service.remoteBugTrackersService = bugTrackersService
		service.credentialsProvider = credentialsProvider
		service.storedCredentialsManager = storedCredentialsManager

		credentialsProvider.currentUser() >> "bob"
	}


	def "should say bugtracker needs credentials"() {

		given:
		Project project = Mock()
		project.isBugtrackerConnected() >> true
		BugTracker bugTracker = Mock()
		project.findBugTracker() >> bugTracker

		remoteService.isCredentialsNeeded(bugTracker) >> true

		when:
		def status = service.checkBugTrackerStatus(project)

		then:
		status == AuthenticationStatus.NON_AUTHENTICATED
	}


	def "should say bugtracker is ready for use"() {

		given:
		Project project = Mock()
		project.isBugtrackerConnected() >> true
		BugTracker bugTracker = Mock()
		project.findBugTracker() >> bugTracker
		remoteService.isCredentialsNeeded(bugTracker) >> false

		when:
		def status = service.checkBugTrackerStatus(project)

		then:
		status == AuthenticationStatus.AUTHENTICATED
	}


	def "should create an issue"() {

		given:
		BugTracker bugTracker = Mock()
		bugTracker.getName() >> "default"
		BTIssue btIssue = Mock()
		btIssue.getId() >> "1"

		remoteService.createIssue(_, _) >> btIssue


		and:
		Execution execution = Mock()
		execution.getBugTracker() >> bugTracker
		IssueList issueList = Mock()
		execution.getIssueList() >> issueList
		BTIssue issue = new BTIssue()

		when:

		BTIssue reissue = service.createRemoteIssue(execution, issue)

		then:
		reissue == btIssue
	}


	def "should retrieve the URL of a given issue"() {

		given:
		BugTracker bugTracker = Mock()
		URL url = new URL("http://www.mybugtracker.com/issues/1");
		remoteService.getViewIssueUrl(_, _) >> url;

		when:
		URL geturl = service.getIssueUrl("myissue", bugTracker)


		then:

		geturl == url;
	}


	def "should find a remote project"() {

		given:
		BugTracker bugTracker = Mock()
		BTProject project = Mock()
		remoteService.findProject(_, _) >> project

		when:
		def reproject = service.findRemoteProject("squashbt", bugTracker)

		then:
		reproject == project
	}

	def "should validate the credentials"() {

		given:
		def creds = new BasicAuthenticationCredentials("bob", "bobpassword" as char[])
		BugTracker bugTracker = Mock()


		when:
		service.validateCredentials(bugTracker, creds, true)


		then:
		1 * remoteService.testCredentials(bugTracker, creds);
		1 * credentialsProvider.cacheCredentials(bugTracker, creds)
		notThrown Exception
	}


	def "should remove the credentials on authentication failure"(){

		given:
		def creds = new BasicAuthenticationCredentials("bob", "bobpassword" as char[])
		BugTracker bugTracker = Mock()
		bugTracker.getId() >> 1L
		bugTracker.getName() >> "some bugtracker"


		when:
		service.validateCredentials(bugTracker, creds, true)


		then:
		1 * remoteService.testCredentials(bugTracker, creds)  >> { throw new BugTrackerNoCredentialsException(null)}
		1 * credentialsProvider.uncacheCredentials(bugTracker)
		1 * storedCredentialsManager.deleteUserCredentials(1L, "bob")
		thrown BugTrackerNoCredentialsException

	}

	def "should not remove the credentials on authentication failure"(){

		given:
		def creds = new BasicAuthenticationCredentials("bob", "bobpassword" as char[])
		BugTracker bugTracker = Mock()
		bugTracker.getId() >> 1L
		bugTracker.getName() >> "some bugtracker"


		when:
		service.validateCredentials(bugTracker, creds, false)


		then:
		1 * remoteService.testCredentials(bugTracker, creds)  >> { throw new BugTrackerNoCredentialsException(null)}
		0 * credentialsProvider.uncacheCredentials(bugTracker, creds)
		0 * storedCredentialsManager.deleteUserCredentials(1L)
		thrown BugTrackerNoCredentialsException

	}


	def remoteIssue(id) {
		BTIssue rIssue = Mock()
		rIssue.getId() >> id
		return rIssue;
	}

	def localOwnership(id) {
		IssueOwnership<Issue> ownership = Mock()
		Issue issue = Mock()

		ownership.getIssue() >> issue
		issue.getRemoteIssueId() >> id

		return ownership
	}


	def issue(listId, remoteId, localId) {
		IssueList mIL = Mock(IssueList)
		Issue mi = Mock(Issue)

		mIL.getId() >> listId
		mi.getIssueList() >> mIL

		mi.getRemoteIssueId() >> remoteId
		mi.getId() >> localId

		return mi

	}
}
