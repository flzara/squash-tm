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
package org.squashtest.tm.domain.audit;

import java.util.Date;

import javax.persistence.*;

/**
 * Embeddable delegate for Auditable entities.
 *
 * Feature 6763 - Add a transient boolean for checking the necessity of modifying the 'last modified on' date.
 * This date was automatically modified and it wasn't relevant for the update of the 'last connected on' date.
 *
 * @author Gregory Fouquet
 *
 */
@Embeddable
public class AuditableSupport {
	@Column(updatable = false)
	private String createdBy;

	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdOn;

	@Column(insertable = false)
	private String lastModifiedBy;

	@Column(insertable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModifiedOn;

	// Feature 6763 - Add a transient boolean, initialized at false, its purpose is to not modify the 'last modified on'
	// when the 'last connected on' is modified, at each connection.
	@Transient
	private boolean skipModifyAudit = false;

	public String getCreatedBy() {
		return createdBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}

	@Transient
	public void setSkipModifyAudit(boolean isSkipModifyAudit) {
		this.skipModifyAudit = isSkipModifyAudit;
	}

	@Transient
	public boolean isSkipModifyAudit() {
		return skipModifyAudit;
	}

}
