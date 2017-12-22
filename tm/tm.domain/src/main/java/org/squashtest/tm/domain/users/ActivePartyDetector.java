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
package org.squashtest.tm.domain.users;

/** NOT THREAD SAFE ! */
public class ActivePartyDetector implements PartyVisitor{

	private Boolean active; 
	
	public Boolean isActive(Party party){
		active = Boolean.FALSE;
		party.accept(this);
		return active;
	}
	
	@Override
	public void visit(User user) {
		active = user.getActive();
	}

	@Override
	public void visit(Team team) {
		active = Boolean.TRUE;	// so far a team is never deactivated
	}

	
	
}
