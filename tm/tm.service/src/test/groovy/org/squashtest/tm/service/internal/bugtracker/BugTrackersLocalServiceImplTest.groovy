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

import org.squashtest.csp.core.bugtracker.domain.BTIssue
import org.squashtest.csp.core.bugtracker.domain.BTProject
import org.squashtest.csp.core.bugtracker.domain.BugTracker
import org.squashtest.csp.core.bugtracker.service.BugTrackerContextHolder
import org.squashtest.csp.core.bugtracker.service.BugTrackersService
import org.squashtest.tm.domain.bugtracker.Issue
import org.squashtest.tm.domain.bugtracker.IssueList
import org.squashtest.tm.domain.bugtracker.IssueOwnership
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.servers.AuthenticationStatus
import org.squashtest.tm.service.advancedsearch.IndexationService
import org.squashtest.tm.service.internal.repository.IssueDao
import spock.lang.Specification

/**
 * @author Gregory Fouquet
 *
 */
class BugTrackersLocalServiceImplTest extends Specification {
	BugTrackersLocalServiceImpl service = new BugTrackersLocalServiceImpl()

	IssueDao issueDao = Mock()
	BugTrackersService bugTrackersService = Mock()
	IndexationService indexationService = Mock();

	// alias
	BugTrackersService remoteService = bugTrackersService;

	def setup() {
		service.issueDao = issueDao
		service.remoteBugTrackersService = bugTrackersService
		service.indexationService = indexationService;
		service.contextHolder = Mock(BugTrackerContextHolder)
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

	def "should set the credentials"() {

		given:
		def name = "bob"
		def password = "bobpassword"
		BugTracker bugTracker = Mock()


		when:
		service.setCredentials(name, password, bugTracker)


		then:
		1 * remoteService.setCredentials(name, password, bugTracker);
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
