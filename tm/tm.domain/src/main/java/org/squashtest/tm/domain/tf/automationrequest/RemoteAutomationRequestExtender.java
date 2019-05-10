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

import org.squashtest.csp.core.bugtracker.domain.BugTracker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "REMOTE_AUTOMATION_REQUEST_EXTENDER")
public class RemoteAutomationRequestExtender {

	@Id
	@Column(name = "REMOTE_AUTOMATION_REQUEST_EXTENDER_ID")
	@SequenceGenerator(
		name = "remote_automation_request_extender_remote_automation_request_extender_id_seq",
		sequenceName = "remote_automation_request_extender_remote_automation_request_extender_id_seq",
		allocationSize = 1)
	@GeneratedValue(
		strategy = GenerationType.AUTO,
		generator = "remote_automation_request_extender_remote_automation_request_extender_id_seq")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "SERVER_ID", referencedColumnName = "BUGTRACKER_ID")
	private BugTracker server;

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(name = "AUTOMATION_REQUEST_ID", referencedColumnName = "AUTOMATION_REQUEST_ID")
	private AutomationRequest automationRequest;

	@NotNull
	@Column(name = "REMOTE_STATUS")
	private String automationRequestStatus;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public BugTracker getServer() {
		return server;
	}
	public void setServer(BugTracker server) {
		this.server = server;
	}

	public AutomationRequest getAutomationRequest() {
		return automationRequest;
	}
	public void setAutomationRequest(AutomationRequest automationRequest) {
		this.automationRequest = automationRequest;
	}

	public String getAutomationRequestStatus() {
		return automationRequestStatus;
	}
	public void setAutomationRequestStatus(String automationRequestStatus) {
		this.automationRequestStatus = automationRequestStatus;
	}
}
