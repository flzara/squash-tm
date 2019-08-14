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

import org.hibernate.annotations.Type;
import org.squashtest.tm.domain.Identified;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.security.annotation.AclConstrainedObject;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "AUTOMATION_REQUEST")
public class AutomationRequest implements Identified {

	@Id
	@Column(name="AUTOMATION_REQUEST_ID")
	@GeneratedValue(generator = "automation_request_automation_request_id_seq", strategy = GenerationType.AUTO)
	@SequenceGenerator(name = "automation_request_automation_request_id_seq", sequenceName = "automation_request_automation_request_id_seq", allocationSize = 1)
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "REQUEST_STATUS")
	private AutomationRequestStatus requestStatus = AutomationRequestStatus.WORK_IN_PROGRESS;

	@Column(name = "TRANSMITTED_ON")
	@Temporal(TemporalType.TIMESTAMP)
	private Date transmissionDate;

	@ManyToOne
	@JoinColumn(name = "ASSIGNED_TO")
	private User assignedTo;

	@Column(name = "AUTOMATION_PRIORITY")
	private Integer automationPriority;

	@Column(name = "ASSIGNED_ON")
	@Temporal(TemporalType.TIMESTAMP)
	private Date assignmentDate;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROJECT_ID")
	private Project project;

	/*TM-13*/
	@Column(name="CONFLICT_ASSOCIATION")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String conflictAssociation = "";

	/*Tm-13: true if testCase's TA script is from a manual association*/
	@Column(name= "IS_MANUAL")
	private boolean isManual;

	public String getConflictAssociation() {
		if(conflictAssociation==null){
			return "";
		}
		return conflictAssociation;
	}

	public void setConflictAssociation(String conflictAssociation) {
		this.conflictAssociation = conflictAssociation;
	}

	public boolean isManual() {
		return isManual;
	}

	public void setManual(boolean manual) {
		isManual = manual;
	}



	@OneToOne(mappedBy = "automationRequest", optional = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private RemoteAutomationRequestExtender remoteAutomationRequestExtender;

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

	public Date getAssignmentDate() {
		return assignmentDate;
	}

	public void setAssignmentDate(Date assignmentDate) {
		this.assignmentDate = assignmentDate;
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

	public RemoteAutomationRequestExtender getRemoteAutomationRequestExtender() {
		return remoteAutomationRequestExtender;
	}

	public void setRemoteAutomationRequestExtender(RemoteAutomationRequestExtender remoteAutomationRequestExtender) {
		this.remoteAutomationRequestExtender = remoteAutomationRequestExtender;
	}

	public Project getProject() {
		return project;
	}


	/**
	 * A setter with a fancier name than setProject, that remains consistent with the similar method one can find in
	 * other library-based entities
	 *
	 * @param project
	 */
	public void notifyAssociatedWithProject(Project project){
		this.project = project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@AclConstrainedObject
	public AutomationRequestLibrary getLibrary(){
		return  this.project.getAutomationRequestLibrary();
	}
}

