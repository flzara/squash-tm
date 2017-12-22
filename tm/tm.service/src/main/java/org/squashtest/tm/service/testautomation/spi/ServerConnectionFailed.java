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


public class ServerConnectionFailed extends TestAutomationException {

	private static final String CONNECTION_FAILED_KEY = "testautomation.exceptions.connectionfailed";

	/**
	 *
	 */
	private static final long serialVersionUID = -8208900275653805118L;

	public ServerConnectionFailed() {
		super("server is unreachable");
	}

	public ServerConnectionFailed(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ServerConnectionFailed(String arg0) {
		super(arg0);
	}

	public ServerConnectionFailed(Throwable arg0) {
		super("server is unreachable", arg0);
	}

	@Override
	public String getI18nKey() {
		return CONNECTION_FAILED_KEY;
	}

}
