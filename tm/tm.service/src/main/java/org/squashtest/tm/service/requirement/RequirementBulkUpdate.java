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
package org.squashtest.tm.service.requirement;

import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;

/**
 * This class defines new values for some requirement version attributes, used in the context of bulk updates.
 */
public class RequirementBulkUpdate {

	private RequirementCriticality criticality = null;

	private String category = null;

	private RequirementStatus status = null;

	public RequirementCriticality getCriticality() {
		return criticality;
	}

	public void setCriticality(RequirementCriticality criticality) {
		this.criticality = criticality;
	}


	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public RequirementStatus getStatus() {
		return status;
	}

	public void setStatus(RequirementStatus status) {
		this.status = status;
	}

	public boolean hasCriticalityDefined(){
		return this.criticality != null;
	}

	public boolean hasStatusDefined(){
		return this.status != null;
	}

	public boolean hasCategoryDefined(){
		return this.category != null;
	}
}
