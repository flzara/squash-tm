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
package org.squashtest.tm.core.dynamicmanager.exception;

import java.lang.reflect.Method;

/**
 * Indicates a Dynamic Manager is not able to handle a specific method call.
 * 
 * @author Gregory Fouquet
 * 
 */
public class UnsupportedMethodException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -971438788310348625L;

	public UnsupportedMethodException(Method method, Object[] args) {
		super(createMessage(method, args));
	}

	private static String createMessage(Method method, Object[] args) {
		StringBuilder sb = new StringBuilder("The method '");
		sb.append(method.getReturnType().getCanonicalName()).append(' ').append(method.getName()).append('(');
		
		for (Class<?> paramType : method.getParameterTypes()) {
			sb.append(paramType.getCanonicalName()).append(',');
		}
		
		sb.append(") ' cannot be dynamically handled or delegated to custom manager" );
		
		return sb.toString();
	}


}
