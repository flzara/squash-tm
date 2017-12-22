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
package org.squashtest.tm.exception;

import org.squashtest.tm.core.foundation.exception.ActionException;

public class CyclicStepCallException extends ActionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7668234787125033427L;
	private static final String CYCLIC_MESSAGE_KEY = "squashtm.action.exception.callstepcycle.label";
	
	
	public CyclicStepCallException(Exception ex){
		super(ex);
	}
	
	public CyclicStepCallException(String message){
		super(message);
	}
	
	public CyclicStepCallException(){
		
	}
	
	@Override
	public String getI18nKey() {
		return CYCLIC_MESSAGE_KEY;
	}
	

	
}
