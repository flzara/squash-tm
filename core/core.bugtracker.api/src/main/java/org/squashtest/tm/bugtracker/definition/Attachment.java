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
package org.squashtest.tm.bugtracker.definition;

import java.io.InputStream;

public class Attachment {

	private String name;
	private long size;
	private InputStream streamContent;
	
	public Attachment(){
		super();
	}
	
	
	
	public Attachment(String name, long size, InputStream streamContent) {
		super();
		this.name = name;
		this.size = size;
		this.streamContent = streamContent;
	}



	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}

	public InputStream getStreamContent() {
		return streamContent;
	}

	public void setStreamContent(InputStream streamContent) {
		this.streamContent = streamContent;
	}
	


}
