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
package org.squashtest.csp.core.bugtracker.domain;

import org.squashtest.tm.bugtracker.definition.RemoteStatus;

public class Status implements Identifiable<Status>, RemoteStatus {


	public static final Status NO_STATUS = new Status(Identifiable.DUMMY_ID,Identifiable.DUMMY_NAME);

	private String id;
	private String name;


	public Status(){
		//Default constructor
	}

	public Status(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isDummy() {
		return this.id.equals(NO_STATUS.id);
	}


	public void setId(String id) {
		this.id = id;
	}


	public void setName(String name) {
		this.name = name;
	}

	/** exists for the purpose of being javabean compliant */
	public void setDummy(Boolean dummy){

	}

}
