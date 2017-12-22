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
package org.squashtest.tm.exception.customfield;

import org.squashtest.tm.exception.DomainException;

public class CodeAlreadyExistsException extends DomainException {

	/**
	 * TODO my eclipse coudn't thanks - mpagnon
	 */
	private static final long serialVersionUID = 1L;
	private static final String CODE = "code";
	private static final String KEY = "squashtm.domain.exception.duplicate.code";
	/**
	 * Reports an error on the code, hence the setField("code");
	 * @param oldCode : oldCode to display on the exception message.
	 * @param newCode : new code to display on the exception message.
	 * @param holder : class of the holder
	 */
	public CodeAlreadyExistsException(String oldCode, String newCode, Class<?> holder) {
		super(makeMessage(oldCode, newCode, holder.getCanonicalName()), CODE);
	}
	
	
	/**
	 * Reports an error on the code, hence the setField("code");
	 */
	public CodeAlreadyExistsException() {
		super(CODE);
	}
	
	/**
	 * Reports an error on the code, hence the setField("code");
	 * @param message : the exception message.
	 */
	public CodeAlreadyExistsException(String message) {
		super(message, CODE);
	}
	
	private static String makeMessage(String oldCode, String newCode, String holder) {
		if(oldCode == null || oldCode.equals(newCode)){
			return "Cannot create the "+holder+" of code :'"+newCode+"' because this code is already used by another "+holder+".";
		}else{
			return "Cannot change code '" + oldCode + "' for '" + newCode + "' this code is already used by another "+holder+".";
				
		}
		
	}
	
	@Override
	public String getI18nKey() {
		return KEY ;
	}


}
