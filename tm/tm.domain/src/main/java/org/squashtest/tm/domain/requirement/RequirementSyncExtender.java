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
package org.squashtest.tm.domain.requirement;

import java.net.URL;
import java.util.Date;

import javax.persistence.*;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.synchronisation.RemoteSynchronisation;


/**
 * This is an optional dependency to a Requirement. A Requirement having this extender
 * will be treated as synchronized. The extension holds additional information
 * regarding the source of this synchronized requirement.
 *
 *
 * @author bsiri
 *
 */
@Entity
public class RequirementSyncExtender {

	@Id
	@Column(name = "REQ_SYNC_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_sync_extender_req_sync_id_seq")
	@SequenceGenerator(
			name = "requirement_sync_extender_req_sync_id_seq",
			sequenceName = "requirement_sync_extender_req_sync_id_seq",
			allocationSize=1
			)
	private Long id;

	@OneToOne
	@JoinColumn(name = "REQUIREMENT_ID", referencedColumnName = "RLN_ID")
	private Requirement requirement;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "SERVER_ID", referencedColumnName = "BUGTRACKER_ID")
	private BugTracker server;

	@Column
	private String remoteReqId;

	@Column
	private String remoteProjectId;

	@Column
	private String remoteFilterName;

	@Column(name = "REMOTE_REQ_URL")
	private URL remoteUrl;

	@Column(name = "REMOTE_LAST_UPDATED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date remoteLastUpdated;

	@ManyToOne
	@JoinColumn(name = "REMOTE_SYNCHRONISATION_ID")
	private RemoteSynchronisation remoteSynchronisation;

	@Column
	private String remoteParentId;

	public RequirementSyncExtender(){
		super();
	}

	/**
	 * synchronize the core attributes of the synchronized Requirement with the
	 * attributes of the arguments. Custom Fields have to be treated separately.
	 *
	 * @param v
	 */
	public void synchronize(RequirementVersion v){
		requirement.setName(v.getName());
		requirement.setReference(v.getReference());
		requirement.setCategory(v.getCategory());
		requirement.setCriticality(v.getCriticality());
		requirement.setDescription(v.getDescription());
		requirement.setStatus(v.getStatus());
	}

	public Requirement getRequirement() {
		return requirement;
	}

	public void setRequirement(Requirement requirement) {
		this.requirement = requirement;
	}

	public String getRemoteReqId() {
		return remoteReqId;
	}

	public void setRemoteReqId(String remoteReqId) {
		this.remoteReqId = remoteReqId;
	}

	public String getRemoteProjectId() {
		return remoteProjectId;
	}

	public void setRemoteProjectId(String remoteProjectId) {
		this.remoteProjectId = remoteProjectId;
	}

	public String getRemoteFilterName() {
		return remoteFilterName;
	}

	public void setRemoteFilterName(String remoteFilterName) {
		this.remoteFilterName = remoteFilterName;
	}

	public BugTracker getServer() {
		return server;
	}

	public void setServer(BugTracker server) {
		this.server = server;
	}

	public URL getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(URL remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	public Date getRemoteLastUpdated() {
		return remoteLastUpdated;
	}

	public void setRemoteLastUpdated(Date remoteLastUpdated) {
		this.remoteLastUpdated = remoteLastUpdated;
	}

	public RemoteSynchronisation getRemoteSynchronisation() {
		return remoteSynchronisation;
	}

	public void setRemoteSynchronisation(RemoteSynchronisation remoteSynchronisation) {
		this.remoteSynchronisation = remoteSynchronisation;
	}

	public String getRemoteParentId() {
		return remoteParentId;
	}

	public void setRemoteParentId(String remoteParentId) {
		this.remoteParentId = remoteParentId;
	}
}

