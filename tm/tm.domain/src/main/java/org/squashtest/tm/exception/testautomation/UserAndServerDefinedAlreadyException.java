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
package org.squashtest.tm.exception.testautomation;

import java.net.URL;

import org.squashtest.tm.exception.DomainException;


/**
 * <p>
 * 	The name of this exception is the plain english translation of
 * the unique constraint on (URL, login) that applies to TestAutomationServers :
 * 	A TestAutomationServer can be registered twice only if uses a different account
 * than the others, and conversely one cannot register an account on a server that
 * is already in use for that server.
 * </p>
 * 
 * @author bsiri
 *
 */
public class UserAndServerDefinedAlreadyException extends DomainException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Object[] args;

	private static final String USER_SERVER_DEFINED_ALREADY = "testautomation.exceptions.userserveralreadyregistered";


	public UserAndServerDefinedAlreadyException(String login, URL url){
		this(login, url, "login");
	}

	public UserAndServerDefinedAlreadyException(String login, URL url, String fieldName){
		super("User '"+login+"' is already registered on '"+url+"'",fieldName);
		args = new Object[]{login, url};
	}

	@Override
	public String getI18nKey() {
		return USER_SERVER_DEFINED_ALREADY;
	}

	@Override
	public Object[] getI18nParams() {
		return args;
	}

}
