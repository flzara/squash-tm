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
package org.squashtest.tm.web.internal.model.json;

import java.util.Date;

import org.squashtest.tm.core.foundation.lang.DateUtils;
import org.squashtest.tm.domain.audit.AuditableMixin;

public class JsonGeneralInfo {

	private String createdOn;
	private String createdBy;
	private String modifiedOn;
	private String modifiedBy;
	
	public JsonGeneralInfo(String createdOn, String createdBy,
			String modifiedOn, String modifiedBy) {
		super();
		this.createdOn = createdOn;
		this.createdBy = createdBy;
		this.modifiedOn = modifiedOn;
		this.modifiedBy = modifiedBy;
	}
	
	public JsonGeneralInfo(Date createdOn, String createdBy,
			Date modifiedOn, String modifedBy){
		super();
		this.createdOn = DateUtils.formatIso8601DateTime(createdOn);
		this.createdBy = createdBy;
		this.modifiedOn = DateUtils.formatIso8601DateTime(modifiedOn);
		this.modifiedBy = modifedBy;
	}
	
	public JsonGeneralInfo(AuditableMixin mixin){
		super();
		this.createdOn = DateUtils.formatIso8601DateTime(mixin.getCreatedOn());
		this.createdBy = mixin.getCreatedBy();
		this.modifiedOn = DateUtils.formatIso8601DateTime(mixin.getLastModifiedOn());
		this.modifiedBy = mixin.getLastModifiedBy();		
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getModifiedOn() {
		return modifiedOn;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	
	
}
