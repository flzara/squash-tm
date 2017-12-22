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

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;

import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * That class simply extends RequirementAuditEvent. Indeed, the additional information here is the type itself.  
 * 
 * @author bsiri
 */
@Entity
@PrimaryKeyJoinColumn(name = "EVENT_ID")
public class RequirementCreation extends RequirementAuditEvent {

	
	public RequirementCreation(){
		super();
	}
	
	public RequirementCreation(RequirementVersion requirementVersion, String author) {
		super(requirementVersion, author);
	}

	@Override
	public void accept(RequirementAuditEventVisitor visitor) {
		visitor.visit(this);
	}	
}
