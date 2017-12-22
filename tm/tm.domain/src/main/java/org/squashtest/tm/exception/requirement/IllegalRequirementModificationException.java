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
package org.squashtest.tm.exception.requirement;

import org.squashtest.tm.core.foundation.exception.ActionException;

public class IllegalRequirementModificationException extends ActionException {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 4901610054565947807L;
	private static final String illegalReqModificationException = "squashtm.action.exception.illegalrequirementmodification.label";
	
	
	public IllegalRequirementModificationException(Exception ex){
		super(ex);
	}
	
	public IllegalRequirementModificationException(String message){
		super(message);
	}
	
	public IllegalRequirementModificationException(){
		
	}
	
	@Override
	public String getI18nKey() {
		return illegalReqModificationException;
	}
}
