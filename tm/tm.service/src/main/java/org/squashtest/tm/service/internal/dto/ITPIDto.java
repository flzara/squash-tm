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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ITPIDto {
	private Long id;
	private String status;
	private String userName;
	private Date lastExecutedOn;
	private Integer executionCount;
	private Integer issueCount;
	private String dataset;
	private List<String> testSuiteList = new ArrayList<>();
	private TestCaseDto testCase;

	public ITPIDto(Long id, String status, String userName, Date lastExecutedOn, Integer executionCount, Integer issueCount, String dataset) {
		super();
		this.id = id;
		this.status = status;
		this.userName = userName;
		this.lastExecutedOn = lastExecutedOn;
		this.executionCount = executionCount;
		this.issueCount = issueCount;
		this.dataset = dataset;
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

	public Integer getExecutionCount() {
		return executionCount;
	}

	public void setExecutionCount(Integer executionCount) {
		this.executionCount = executionCount;
	}

	public Integer getIssueCount() {
		return issueCount;
	}

	public void setIssueCount(Integer issueCount) {
		this.issueCount = issueCount;
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
}
