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
package org.squashtest.tm.service.internal.dto;

import java.util.*;

public class ITPIDto {
	private Long id;
	private String status="";
	private String userName="";
	private Date lastExecutedOn;
	private Set<Long> executionSet = new HashSet<>();
	private Set<Long> issueSet = new HashSet<>();
	private String dataset="";
	private List<String> testSuiteList = new ArrayList<>();
	private TestCaseDto testCase;

	public ITPIDto(Long id, String status, String userName, Date lastExecutedOn) {
		super();
		this.id = id;
		this.status = status;
		if(userName != null){
			this.userName = userName;
		}
		this.lastExecutedOn = lastExecutedOn;
	}

	public ITPIDto(){
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getLastExecutedOn() {
		return lastExecutedOn;
	}

	public void setLastExecutedOn(Date lastExecutedOn) {
		this.lastExecutedOn = lastExecutedOn;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public List<String> getTestSuiteList() {
		return testSuiteList;
	}

	public void setTestSuiteList(List<String> testSuiteList) {
		this.testSuiteList = testSuiteList;
	}

	public TestCaseDto getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCaseDto testCase) {
		this.testCase = testCase;
	}

	public boolean isTestCaseDeleted(){
		if(testCase == null){
			return true;
		}
		return false;
	}

	public String getTestSuiteNames() {

		StringBuilder builder = new StringBuilder();

		for (String suite : testSuiteList) {
			builder.append(suite).append(", ");
		}
		return builder.toString().replaceFirst(", $", "");	//this eliminates the last comma
	}

	public Set<Long> getExecutionSet() {
		return executionSet;
	}

	public void setExecutionSet(Set<Long> executionSet) {
		this.executionSet = executionSet;
	}

	public Set<Long> getIssueSet() {
		return issueSet;
	}

	public void setIssueSet(Set<Long> issueSet) {
		this.issueSet = issueSet;
	}

	public void addExecution(Long executionId){
		executionSet.add(executionId);
	}

	public void addIssue(Long issueId){
		issueSet.add(issueId);
	}
}
