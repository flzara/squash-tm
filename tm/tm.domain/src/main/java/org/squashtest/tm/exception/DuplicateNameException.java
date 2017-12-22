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


/**
 * /!\ This exception is used both for renaming clashes and new entity name clash. 
 * You should favor the use of NameAlreadyInUseException for new entities 
 *
 */
public class DuplicateNameException extends DomainException {
	
	private static final String NAME = "name";
	private static final long serialVersionUID = 2815263509542519285L;
	/**
	 * Reports an error on the name, hence the setField("name");
	 * @param oldName : oldName to display on the exception message.
	 * @param newName : new name to display on the exception message.
	 */
	public DuplicateNameException(String oldName, String newName) {
		super(makeMessage(oldName, newName), NAME);
	}
	
	
	/**
	 * Reports an error on the name, hence the setField("name");
	 */
	public DuplicateNameException() {
		super(NAME);
	}
	
	/**
	 * Reports an error on the name, hence the setField("name");
	 * @param message : the exception message.
	 */
	public DuplicateNameException(String message) {
		super(message, NAME);
	}
	
	private static String makeMessage(String oldName, String newName) {
		return "Cannot rename " + oldName + " : " + newName + " already exists within the same container";
	}
	
	@Override
	public String getI18nKey() {
		return "squashtm.domain.exception.duplicate.name";
	}


}
