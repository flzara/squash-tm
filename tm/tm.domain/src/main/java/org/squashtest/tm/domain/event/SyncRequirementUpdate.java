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
 * A SyncRequirementUpdate says that a requirement has been updated through synchronization. As a developper it is important to note 
 * that such events will never be handled by the Hibernate interceptor (unlike other events) : it's up to you (developper) to create one.    
 * 
 * @author bsiri
 */
@Entity
@PrimaryKeyJoinColumn(name = "EVENT_ID")
public class SyncRequirementUpdate extends RequirementAuditEvent {

	private String source;	// the url from where the requirement was pulled in
	
	public SyncRequirementUpdate(){
		super();
	}
	
	public SyncRequirementUpdate(RequirementVersion requirementVersion, String author) {
		super(requirementVersion, author);
	}

	@Override
	public void accept(RequirementAuditEventVisitor visitor) {
		visitor.visit(this);
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}	
	
	
}
