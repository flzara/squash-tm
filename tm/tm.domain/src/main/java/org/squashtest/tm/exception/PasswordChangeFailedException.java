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

public class PasswordChangeFailedException extends DomainException {

	/**
	 * 
	 */
	private static final String OLD_PASSWORD = "oldPassword";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6637408521482514143L;

	public PasswordChangeFailedException() {
		super(OLD_PASSWORD);
	}

	public PasswordChangeFailedException(String message) {
		super(message, OLD_PASSWORD);

	}
	public PasswordChangeFailedException(String message, Throwable cause){
		super(message, OLD_PASSWORD, cause);
	}
	@Override
	public String getI18nKey() {
		return "squashtm.domain.exception.password.change.fail";
	}

}
