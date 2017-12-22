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
package org.squashtest.tm.service.testautomation.spi;


public class AccessDenied extends TestAutomationException {

	private static final String ACCESS_DENIED_KEY = "testautomation.exceptions.accessdenied";

	private static final long serialVersionUID = -5345068364658644042L;

	public AccessDenied() {
		super("Access is denied");
	}

	public AccessDenied(String message, Throwable cause) {
		super(message, cause);
	}

	public AccessDenied(String message) {
		super(message);
	}

	public AccessDenied(Throwable cause) {
		super("Access is denied", cause);
	}

	@Override
	public String getI18nKey() {
		return ACCESS_DENIED_KEY;
	}


}
