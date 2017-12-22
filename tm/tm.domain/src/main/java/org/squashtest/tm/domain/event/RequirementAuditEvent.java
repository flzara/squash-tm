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
package org.squashtest.tm.domain.event;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.squashtest.tm.domain.requirement.RequirementVersion;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class RequirementAuditEvent {

	@Id
	@Column(name = "EVENT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "requirement_audit_event_event_id_seq")
	@SequenceGenerator(name = "requirement_audit_event_event_id_seq", sequenceName = "requirement_audit_event_event_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "REQ_VERSION_ID")
	@NotNull
	private RequirementVersion requirementVersion;

	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	@Column(name = "EVENT_DATE")
	private Date date;

	@NotNull
	@Size(max = 255)
	private String author;


	public Long getId() {
		return id;
	}


	public RequirementVersion getRequirementVersion() {
		return requirementVersion;
	}


	public Date getDate() {
		return date;
	}


	public String getAuthor() {
		return author;
	}

	public RequirementAuditEvent(){
		super();
	}

	public RequirementAuditEvent(RequirementVersion requirementVersion, String author) {
		super();
		this.requirementVersion = requirementVersion;
		this.author = author;
		this.date = new Date();
	}


	public abstract void accept(RequirementAuditEventVisitor visitor);


}
