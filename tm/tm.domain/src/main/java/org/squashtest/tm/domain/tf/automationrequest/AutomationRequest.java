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
package org.squashtest.tm.domain.tf.automationrequest;

import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "AUTOMATION_REQUEST")
public class AutomationRequest {

	@Id
	@Column(name="AUTOMATION_REQUEST_ID")
	@GeneratedValue(generator = "automation_request_automation_request_id_seq", strategy = GenerationType.AUTO)
	@SequenceGenerator(name = "automation_request_automation_request_id_seq", sequenceName = "automation_request_automation_request_id_seq",allocationSize = 1)
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "REQUEST_STATUS")
	private AutomationRequestStatus requestStatus = AutomationRequestStatus.TO_VALIDATE;

	@Column(name = "TRANSMITTED_ON")
	@Temporal(TemporalType.TIMESTAMP)
	private Date transmissionDate;

	@ManyToOne
	@JoinColumn(name = "ASSIGNED_TO")
	private User assignedTo;

	@NotNull
	@Column(name = "AUTOMATION_PRIORITY")
	private Integer automationPriority;

	@Column(name = "ASSIGNED_ON")
	@Temporal(TemporalType.TIMESTAMP)
	private Date assigmentDate;

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(name="TEST_CASE_ID")
	private TestCase testCase;

	@ManyToOne
	@JoinColumn(name = "CREATED_BY")
	private User createdBy;

	@ManyToOne
	@JoinColumn(name = "TRANSMITTED_BY")
	private User transmittedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AutomationRequestStatus getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(AutomationRequestStatus requestStatus) {
		this.requestStatus = requestStatus;
	}

	public Date getTransmissionDate() {
		return transmissionDate;
	}

	public void setTransmissionDate(Date transmissionDate) {
		this.transmissionDate = transmissionDate;
	}

	public User getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(User assignedTo) {
		this.assignedTo = assignedTo;
	}

	public Integer getAutomationPriority() {
		return automationPriority;
	}

	public void setAutomationPriority(Integer automationPriority) {
		this.automationPriority = automationPriority;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}



	public Date getAssigmentDate() {
		return assigmentDate;
	}

	public void setAssigmentDate(Date assigmentDate) {
		this.assigmentDate = assigmentDate;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public User getTransmittedBy() {
		return transmittedBy;
	}

	public void setTransmittedBy(User transmittedBy) {
		this.transmittedBy = transmittedBy;
	}
}
