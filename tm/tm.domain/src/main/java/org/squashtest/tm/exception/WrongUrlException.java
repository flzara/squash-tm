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
 * When url cannot be created from a string
 * @author mpagnon
 *
 */
public class WrongUrlException extends DomainException {

	public WrongUrlException(String field, Throwable cause) {
		super("The property "+field+" should be an url", field, cause);

	}



	/**
	 * TODO my eclipse coudn't thanks - mpagnon
	 */
	private static final long serialVersionUID = 1L;

	private static final String KEY = "squashtm.domain.exception.wong.url";
	private Object[] i18nParams = new Object[2];





	@Override
	public String getI18nKey() {
		return KEY ;
	}



	@Override
	public Object[] getI18nParams() {
		return this.i18nParams;
	}



}
