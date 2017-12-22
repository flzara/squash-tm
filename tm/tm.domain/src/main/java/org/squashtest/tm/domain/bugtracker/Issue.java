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
package org.squashtest.tm.domain.bugtracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.execution.Execution;

@Entity
@NamedQueries(value = {
		@NamedQuery(name = "Issue.findAllForIteration", query = "select i from Issue i where i.id in "
				+ "(select ei.id from Iteration it join it.testPlans itp join itp.executions e join e.issueList eil join eil.issues ei where it.id = :id)"
				+ " or i.id in "
				+ "(select esi.id from Iteration it join it.testPlans itp join itp.executions e join e.steps es join es.issueList esil join esil.issues esi where it.id = :id) "),
				@NamedQuery(name = "Issue.findAllForTestSuite", query = "select i from Issue i where i.id in "
						+"(select ei.id from IterationTestPlanItem itp join itp.testSuites ts join itp.executions e join e.issueList eil join eil.issues ei where :id in (select suites.id from itp.testSuites suites))"
						+" or id.id in "
						+"(select esi.id from IterationTestPlanItem itp join itp.testSuites ts join itp.executions e join e.steps es join es.issueList esil join esil.issues esi where :id in (select suites.id from itp.testSuites suites))"),
						@NamedQuery(name="Issue.findExecution", query = "select exec " +
								"from Execution exec join exec.issueList eil join eil.issues issue " +
								"where issue.id = :id "
								),
								@NamedQuery(name="Issue.findExecutionStep", query = "select execStep " +
										"from ExecutionStep execStep join execStep.issueList esil join esil.issues issue " +
										"where  issue.id = :id "
										)
})
public class Issue implements Identified{
	@Id
	@Column(name = "ISSUE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "issue_issue_id_seq")
	@SequenceGenerator(name = "issue_issue_id_seq", sequenceName = "issue_issue_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ISSUE_LIST_ID")
	private IssueList issueList;

	@OneToOne(optional = false)
	@ForeignKey(name = "FK_Issue_Bugtracker")
	@JoinColumn(name = "BUGTRACKER_ID")
	private BugTracker bugtracker;



	/*
	 * TRANSITIONAL - job half done here. The full job would involve something among the lines of RequirementVersionCoverage
	 * 
	 * The following mapping gives all issues reported in the scope of this execution : its own issues, and
	 * the issues reported in its steps.
	 * 
	 * The underlying table is actually a view. So this one is read only and might be quite slow to use.
	 */
	@Transient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinTable(name="EXECUTION_ISSUES_CLOSURE",
	joinColumns=@JoinColumn(name="ISSUE_ID", insertable=false, updatable=false ),
	inverseJoinColumns = @JoinColumn(name="EXECUTION_ID"))
	private Execution execution;


	private String remoteIssueId;

	@Override
	public Long getId() {
		return id;
	}

	public String getRemoteIssueId() {
		return remoteIssueId;
	}

	public void setRemoteIssueId(String btId) {
		this.remoteIssueId = btId;
	}

	public IssueList getIssueList() {
		return issueList;
	}

	void setIssueList(IssueList issueList) {
		this.issueList = issueList;
	}

	public BugTracker getBugtracker() {
		return bugtracker;
	}

	public void setBugtracker(BugTracker bugtracker) {
		this.bugtracker = bugtracker;
	}


}
