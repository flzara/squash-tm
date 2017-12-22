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
 * should not be needed anymore when [Task 1682] is done
 * @author mpagnon
 *
 */
public class WrongStringSizeException extends DomainException {

	/**
	 * TODO my eclipse coudn't thanks - mpagnon
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String KEY = "squashtm.domain.exception.wong.string.size";
	private Object[] i18nParams = new Object[2];
	
	public WrongStringSizeException(String fieldName, int min, int max) {
		super("Property" + fieldName + " should be between "+min+" and "+max+" chars.",fieldName);
		this.i18nParams[0] = min;
		this.i18nParams[1] = max;
	}
	
	
	
	@Override
	public String getI18nKey() {
		return KEY ;
	}



	@Override
	public Object[] getI18nParams() {
		return this.i18nParams;
	}
	
	

}
