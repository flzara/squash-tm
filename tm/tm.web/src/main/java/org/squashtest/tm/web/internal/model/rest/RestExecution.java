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
package org.squashtest.tm.web.internal.model.rest;

import java.util.Date;

import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.execution.Execution;

public class RestExecution {

	private Long id;

	private String reference;

	private String name;

	private RestProject project;

	private String testcasePath;

	private String description;

	private RestCampaignStub restCampaignStub;

	private RestIterationStub restIterationStub;

	private RestTestCaseStub restTestCaseStub;

	private String status;

	private String lastExecutedOn;

	private String lastExecutedBy;

	public RestExecution(){
		super();
	}

	public RestExecution(Execution execution, String testcasePath) {
		this.id = execution.getId();
		this.reference = execution.getReference();
		this.name = execution.getName();
		this.project = new RestProject(execution.getProject());
		this.testcasePath = testcasePath;
		this.description = execution.getTcdescription();
		this.restCampaignStub = new RestCampaignStub(execution.getCampaign());
		this.restIterationStub = new RestIterationStub(execution.getIteration());
		this.restTestCaseStub = new RestTestCaseStub(execution.getReferencedTestCase());
		this.status = execution.getExecutionStatus().name();
		this.lastExecutedOn = DateUtils.formatIso8601DateTime(execution.getLastExecutedOn());
		this.lastExecutedBy = execution.getLastExecutedBy();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RestCampaignStub getRestCampaignStub() {
		return restCampaignStub;
	}

	public void setRestCampaignStub(RestCampaignStub restCampaignStub) {
		this.restCampaignStub = restCampaignStub;
	}

	public RestIterationStub getRestIterationStub() {
		return restIterationStub;
	}

	public void setRestIterationStub(RestIterationStub restIterationStub) {
		this.restIterationStub = restIterationStub;
	}

	public RestTestCaseStub getRestTestCaseStub() {
		return restTestCaseStub;
	}

	public void setRestTestCaseStub(RestTestCaseStub restTestCaseStub) {
		this.restTestCaseStub = restTestCaseStub;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RestProject getProject() {
		return project;
	}

	public void setProject(RestProject project) {
		this.project = project;
	}

	public String getTestcasePath() {
		return testcasePath;
	}

	public void setTestcasePath(String testcasePath) {
		this.testcasePath = testcasePath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLastExecutedOn() {
		return lastExecutedOn;
	}

	public void setLastExecutedOn(Date lastExecutedOn) {
		this.lastExecutedOn = DateUtils.formatIso8601DateTime(lastExecutedOn);
	}

	public String getLastExecutedBy() {
		return lastExecutedBy;
	}

	public void setLastExecutedBy(String lastExecutedBy) {
		this.lastExecutedBy = lastExecutedBy;
	}


}
