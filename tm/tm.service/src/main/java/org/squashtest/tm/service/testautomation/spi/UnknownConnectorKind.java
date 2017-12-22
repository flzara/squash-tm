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

import java.util.LinkedList;
import java.util.List;



public class UnknownConnectorKind extends TestAutomationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5396461746323466331L;

	private static final String UNKNOWN_KIND_EXCEPTION = "testautomation.exceptions.unknownkind";
	
	
	private List<String> args = new LinkedList<>();

	public UnknownConnectorKind() {
		super();
	}

	public UnknownConnectorKind(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownConnectorKind(String message) {
		super(message);
	}

	public UnknownConnectorKind(Throwable cause) {
		super(cause);
	}
	
	@Override
	public Object[] messageArgs(){
		return args.toArray();
	}

	public void addArg(String arg){
		args.add(arg);
	}

	@Override
	public String getI18nKey() {
		return UNKNOWN_KIND_EXCEPTION;
	}
	
	
	
}
