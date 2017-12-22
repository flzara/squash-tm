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
package org.squashtest.tm.web.internal.fileupload;

public class UploadSummary {
	
	public static final int INT_UPLOAD_STATUS_OK = 0;
	public static final int INT_UPLOAD_STATUS_WRONGFILETYPE = 1;  
	
	private String name;
	private String status;
	private int iStatus;
	

	
	public int getiStatus() {
		return iStatus;
	}
	public void setiStatus(int iStatus) {
		this.iStatus = iStatus;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public UploadSummary(){
		
	}
	
	public UploadSummary(String name, String status, int iStatus){
		this.name=name;
		this.status=status;
		this.iStatus = iStatus;
	}
	
}
