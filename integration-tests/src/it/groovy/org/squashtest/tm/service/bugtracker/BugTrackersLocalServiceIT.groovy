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
package org.squashtest.tm.service.bugtracker

import org.spockframework.util.NotThreadSafe
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.squashtest.csp.core.bugtracker.core.BugTrackerNotFoundException
import org.squashtest.csp.core.bugtracker.domain.*
import org.squashtest.csp.core.bugtracker.spi.BugTrackerInterfaceDescriptor
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.domain.bugtracker.Issue
import org.squashtest.tm.domain.bugtracker.IssueOwnership
import org.squashtest.tm.domain.execution.Execution
import org.squashtest.tm.domain.execution.ExecutionStep
import org.squashtest.tm.domain.servers.AuthenticationStatus
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Ignore
import spock.unitils.UnitilsSupport

import javax.inject.Inject

/*
 * @author bsiri
 *
 *  BugTracker Test Server Configuration :
 *
 *  This test class is meant for a Mantis bugtracker
 *  (see bugtracker.properties and bugtracker-core-context-IT.xml for configuration)
 *
 *  this test requires on the Mantis instance :
 *  	a default administrator account : login "administrator", password "root"
 *  	a project named 'squashbt',
 *  		- with version 1.0, 1.01, 1.02 (declared in that order)
 *  		- belonging to categories General, Non Squash, Squash, Squash tm
 *  	a user with an access level of 'reporter' for all projects
 *  	that no project named "non-existant project" exists
 *
 */


@NotThreadSafe
@UnitilsSupport
@ContextConfiguration(["classpath:bugtracker-core-context-IT.xml"])
@Transactional
@Ignore
class BugTrackersLocalServiceIT extends DbunitServiceSpecification  {


	/*
	 *  should test the following methods :
	 *
	 void addIssue( Long entityId, Class<? extends Bugged> entityClass, Issue issue);
	 String findProjectName(Bugged entity);
	 BugTrackerStatus checkBugTrackerStatus();
	 void setCredentials(String username, String password);
	 BTProject findRemoteProject(String name);
	 List<Priority> getRemotePriorities();
	 */

	@Inject
	private BugTrackersLocalService btService




	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should get an issue from a given execution step"(){
		given :
			ExecutionStep estep = findEntity(ExecutionStep.class, 1l)

		when :
			def issue = estep.getIssueList().findIssue(2l)

		then :
			issue.id==2l
	}

	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should get all the issues for a given execution step"(){

		given :

			ExecutionStep estep = findEntity(ExecutionStep.class, 1l)
		when :
			List<Issue> issues = estep.getIssueList().getAllIssues()

		then :
			issues.size() == 3
			issues.collect { it -> it.id } == [2l, 4l, 6l]

	}

	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should get a list of paired issues for a step"(){

		given :
			ExecutionStep estep = findEntity(ExecutionStep.class, 1l)

			DefaultPagingAndSorting sorter = new DefaultPagingAndSorting("Issue.id", 10);
			sorter.setSortOrder(SortOrder.DESCENDING)

		when :
			PagedCollectionHolder<List<IssueOwnership<Issue>>> ownedIssues =
					btService.findSortedIssueOwnerShipsForExecutionStep(estep.id, sorter)

		then :
			ownedIssues.totalNumberOfItems == 3
			List<IssueOwnership<Issue>> list = ownedIssues.pagedItems

			list.collect { it -> it.issue.id} == [6l, 4l, 2l]



	}

	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should get a list of paired issues for an execution"(){

		given :
			Execution exec = findEntity(Execution.class, 1l)
			ExecutionStep step1 = findEntity(ExecutionStep.class, 1l)
			ExecutionStep step2 = findEntity(ExecutionStep.class, 2l)


			DefaultPagingAndSorting sorter = new DefaultPagingAndSorting("Issue.id", 10);
			sorter.setSortOrder(SortOrder.DESCENDING)

		when :
			PagedCollectionHolder<List<IssueOwnership<Issue>>> ownedIssues =
					btService.findSortedIssueOwnershipsforExecution(exec.id, sorter)



		then :
			ownedIssues.totalNumberOfItems == 8
			List<IssueOwnership<Issue>> list = ownedIssues.pagedItems

			list.collect { it -> it.issue.id} == [8l, 7l, 6l, 5l, 4l, 3l,  2l, 1l]

			def ownership8 = list.get(0)
			def ownership7 = list.get(1)
			def ownership6 = list.get(2)
			def ownership5 = list.get(3)
			def ownership4 = list.get(4)
			def ownership3 = list.get(5)
			def ownership2 = list.get(6)
			def ownership1 = list.get(7)

			ownership8.owner == ownership7.owner
			ownership7.owner == exec

			ownership2.owner == ownership4.owner
			ownership4.owner == ownership6.owner
			ownership6.owner == step1

			ownership1.owner == ownership3.owner
			ownership3.owner == ownership5.owner
			ownership5.owner == step2

	}



	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should not find an issue from a given execution step"(){
		given :
			ExecutionStep estep = findEntity(ExecutionStep.class, 1l)

		when :
			def issue = estep.getIssueList().findIssue(8l)

		then :
			issue==null
	}


	def "should 1) warn that the bugtracker needs credential, 2) set credentials then say it's all green"(){
		given :

		when :
		AuthenticationStatus status1 = btService.checkAuthenticationStatus()
		btService.setCredentials("administrator", "root")
		AuthenticationStatus status2 = btService.checkAuthenticationStatus()
		then :
		status1 == AuthenticationStatus.NON_AUTHENTICATED
		status2 == AuthenticationStatus.AUTHENTICATED

	}

	def "should get the list of Mantis priorities"(){
		given :
		btService.setCredentials("administrator", "root")
		when :
		def priorities = btService.getRemotePriorities()
		then :
		priorities.collect{it.name} == [
			"feature",
			"trivial",
			"text",
			"tweak",
			"minor",
			"major",
			"crash",
			"block"
		]


	}

	def "should find a remote Project based on its name"(){
		given :
		def projectname="squashbt"
		btService.setCredentials("administrator", "root")

		when :
		BTProject project = btService.findRemoteProject(projectname)

		then :
		project != null
		project.name == "squashbt"
		project.users.collect {it.name}.contains ("administrator")
		project.users.collect {it.name}.contains ("user")

		project.versions.collect { it.name } == ["1.02", "1.01", "1.0"]
		project.categories.collect { it.name }== ["General", "Non Squash", "Squash", "Squash tm"]

		def admPerms = [
			"viewer",
			"reporter",
			"updater",
			"developer",
			"manager",
			"administrator"
		]
		def userPerms =  ["viewer", "reporter"]

		User user1 = project.users.get(0)
		User user2 = project.users.get(1)

		for (User user : [user1, user2]){
			if (user.name=="administrator"){
				user.permissions.collect{it.name} == admPerms
			}else{
				user.permissions.collect{it.name} == userPerms
			}
		}

	}

	def "should find an issue list"(){
		given:
			btService.setCredentials("administrator", "root")

		and :
			//need a bug tracker like mantis or JIRA and you have to know the issue id
			String issueId1 = "1"
			String issueId2 = "3"
			List<String> issueIdList = new ArrayList<String>()
			issueIdList.add(issueId1)
			issueIdList.add(issueId2)

		when:
			List<BTIssue> btIssueList = btService.getIssues(issueIdList)


		then:
			btIssueList.size() > 0
			btIssueList.get(0).id.equals(issueId1)
			btIssueList.get(1).id.equals(issueId2)
	}

	def "should throw an exception when fetching a remote project that doesn't exists"(){
		given :
		def projectname="non-existant project"
		btService.setCredentials("administrator", "root")

		when :
		BTProject project = btService.findRemoteProject(projectname)

		then :
		thrown BugTrackerNotFoundException

	}



	def "should fetch the labels for the interface"(){

		given :

		btService.setCredentials("administrator", "root")


		when :
		BugTrackerInterfaceDescriptor descriptor = btService.getInterfaceDescriptor()

		then :
		descriptor!=null
		descriptor.getReportCategoryLabel().contains("Cat")
		descriptor.getReportCategoryLabel().contains("gor")
	}



	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should create an issue on the remote bugtracker and persist its Id locally"(){


		given :

			btService.setCredentials("administrator", "root")

		and :
			ExecutionStep estep = findEntity(ExecutionStep.class, 1l)

			BTProject project = btService.findRemoteProject(estep.getProject().getName())
			Version version = project.getVersions().get(0)
			Category category = project.getCategories().get(0)
			User assignee = project.getUsers().get(0)
			Priority priority = btService.getRemotePriorities().get(0)

		and :

			BTIssue issue = new BTIssue()
			issue.setProject(project)
			issue.setAssignee(assignee)
			issue.setVersion(version)
			issue.setCategory(category)
			issue.setPriority(priority)
			issue.setSummary("test bug # 1")
			issue.setDescription("issue description")
			issue.setComment("this is a comment for test bug # 1")


		when :
			BTIssue reIssue = btService.createIssue(estep, issue)

			//we update the content of the step by refetching it
			estep = findEntity(ExecutionStep.class, 1l)

		then :

			reIssue.getId()!=null
			reIssue.getAssignee().getName()== assignee.getName()
			reIssue.getVersion().getName() == version.getName()
			reIssue.getCategory() == category
			reIssue.getPriority() == priority
			reIssue.getSummary() == "test bug # 1"
			reIssue.getDescription() == "issue description"
			reIssue.getComment() == "this is a comment for test bug # 1"


	}


	@DataSet("BugTrackerLocalServiceIT.execution-step-setup.xml")
	def "should find the URL of a given issue"(){


		given :
			String issueId = "00001"

		when :
			URL url = btService.getIssueUrl(issueId)

		then :
			url.toExternalForm().contains("http://localhost/mantisbt")
			url.toExternalForm().contains("view.php?id="+issueId)


	}

	def "should detach an issue from an execution"(){

		given:
			Execution ex = findEntity(Execution.class,1l)

		when:
			ex.getIssueList().findIssue(7l) != null
			btService.detachIssue(7l)
			def linkedIssue = ex.getIssueList().findIssue(7l)
			def issue = findEntity(Issue.class, 7l)

		then:
			linkedIssue==null
			issue!=null
	}

	def "should detach an issue from an execution step"(){

		given:
			ExecutionStep estep = findEntity(Execution.class,1l)

		when:
			estep.getIssueList().findIssue(5l) != null
			btService.detachIssue(5l)
			def linkedIssue = estep.getIssueList().findIssue(5l)
			def issue = findEntity(Issue.class, 5l)

		then:
			linkedIssue==null
			issue!=null
	}


}
