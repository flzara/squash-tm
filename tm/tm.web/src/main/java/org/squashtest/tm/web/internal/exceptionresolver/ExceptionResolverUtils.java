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
package org.squashtest.tm.web.internal.exceptionresolver;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.web.internal.http.RequestHeaders;

public final class ExceptionResolverUtils {
	private ExceptionResolverUtils (){
		
	}
	
	public static boolean clientAcceptsMIME(HttpServletRequest request, MimeType type) {
		Enumeration<String> e = request.getHeaders(RequestHeaders.ACCEPT);
		while (e.hasMoreElements()) {
			String header = e.nextElement();
			if (StringUtils.containsIgnoreCase(StringUtils.trimToEmpty(header), type.requestHeaderValue())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean clientAcceptsMIMEOrAnything(HttpServletRequest request, MimeType type) {
		Enumeration<String> e = request.getHeaders(RequestHeaders.ACCEPT);
		while (e.hasMoreElements()) {
			String header = e.nextElement();
			if (StringUtils.containsIgnoreCase(StringUtils.trimToEmpty(header), type.requestHeaderValue()) 
					|| StringUtils.containsIgnoreCase(StringUtils.trimToEmpty(header), MimeType.ANYTHING.requestHeaderValue())) {
				return true;
			}
		}
		return false;
	}
	
}
