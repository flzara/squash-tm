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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ITPIDto {
	private Long id = -1L;
	private String status="";
	private String userName="";
	private Date lastExecutedOn;
	private Map<Long, ExecutionDto> executionMap = new HashMap<>();
	private Set<Long> issueSet = new HashSet<>();
	private String dataset="";
	private Set<String> testSuiteSet = new HashSet<>();
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

	public Set<String> getTestSuiteSet() {
		return testSuiteSet;
	}

	public void setTestSuiteSet(Set<String> testSuiteSet) {
		this.testSuiteSet = testSuiteSet;
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

		for (String suite : testSuiteSet) {
			builder.append(suite).append(", ");
		}
		return builder.toString().replaceFirst(", $", "");	//this eliminates the last comma
	}

	public Set<Long> getIssueSet() {
		return issueSet;
	}

	public void setIssueSet(Set<Long> issueSet) {
		this.issueSet = issueSet;
	}

	public void addExecution(ExecutionDto execution){
		executionMap.put(execution.getId(), execution);
	}

	public void addIssue(Long issueId){
		issueSet.add(issueId);
	}

	public ExecutionDto getLatestExecution() {
		if(!executionMap.isEmpty()){
			Long max = Collections.max(executionMap.keySet());
			return executionMap.get(max);
		}
		return null;
	}

	public Map<Long, ExecutionDto> getExecutionMap() {
		return executionMap;
	}

	public void setExecutionMap(Map<Long, ExecutionDto> executionMap) {
		this.executionMap = executionMap;
	}

	public ExecutionDto getExecution(Long executionId){
		return this.executionMap.get(executionId);
	}
}
