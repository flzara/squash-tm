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
package org.squashtest.tm.service.infolist;

import java.util.Date;

import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.audit.AuditableSupport;
import org.squashtest.tm.domain.infolist.InfoList;

/**
 * @author Gregory
 *
 */
public abstract class InfoListAdapter implements AuditableMixin {
	private static final AuditableSupport DUMMY_AUDITABLE_SUPPORT = new AuditableSupport();

	protected final InfoList delegate;
	private final AuditableMixin auditable;
	protected InfoListAdapter(InfoList delegate) {
		super();
		this.delegate = delegate;
		auditable = (AuditableMixin) delegate;
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#getCreatedOn()
	 */

	public Date getCreatedOn() {
		return auditable.getCreatedOn();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#getCreatedBy()
	 */

	public String getCreatedBy() {
		return auditable.getCreatedBy();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#getLastModifiedOn()
	 */

	public Date getLastModifiedOn() {
		return auditable.getLastModifiedOn();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#getLastModifiedBy()
	 */

	public String getLastModifiedBy() {
		return auditable.getLastModifiedBy();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.infolist.InfoList#getLabel()
	 */
	public String getLabel() {
		return delegate.getLabel();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.infolist.InfoList#getDescription()
	 */
	public String getDescription() {
		return delegate.getDescription();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.infolist.InfoList#getCode()
	 */
	public String getCode() {
		return delegate.getCode();
	}

	/**
	 * @return
	 * @see org.squashtest.tm.domain.infolist.InfoList#getId()
	 */
	public Long getId() {
		return delegate.getId();
	}

	/**
	 * @param createdBy
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setCreatedBy(java.lang.String)
	 */

	public void setCreatedBy(String createdBy) {
		auditable.setCreatedBy(createdBy);
	}

	/**
	 * @param createdOn
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setCreatedOn(java.util.Date)
	 */

	public void setCreatedOn(Date createdOn) {
		auditable.setCreatedOn(createdOn);
	}

	/**
	 * @param lastModifiedBy
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setLastModifiedBy(java.lang.String)
	 */

	public void setLastModifiedBy(String lastModifiedBy) {
		auditable.setLastModifiedBy(lastModifiedBy);
	}

	/**
	 * @param lastModifiedOn
	 * @see org.squashtest.tm.domain.audit.AuditableMixin#setLastModifiedOn(java.util.Date)
	 */

	public void setLastModifiedOn(Date lastModifiedOn) {
		auditable.setLastModifiedOn(lastModifiedOn);
	}

	public void setAudit(AuditableSupport audit) {
		// NOOP requested by compiler but we dont want to mess with delegate's member
	}
	public AuditableSupport getAudit() {
		// Requested by compiler, hopefully wont be read and won't break.
		return DUMMY_AUDITABLE_SUPPORT;
	}

	// Feature 6763 - This class implements AuditableMixin, so it must also have the new attribute (getter + setter)
	public boolean isSkipModifyAudit() {
		return getAudit().isSkipModifyAudit();
	}

	public void setSkipModifyAudit(boolean skipModifyAudit) {
		getAudit().setSkipModifyAudit(skipModifyAudit);
	}

}
