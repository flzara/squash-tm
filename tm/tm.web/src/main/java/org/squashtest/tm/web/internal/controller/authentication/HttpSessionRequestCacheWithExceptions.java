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
package org.squashtest.tm.web.internal.controller.authentication;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

public class HttpSessionRequestCacheWithExceptions extends
HttpSessionRequestCache {

	private Collection<String> exceptions = new ArrayList<>(2);

	public Collection<String> getExceptions() {
		return exceptions;
	}

	public void setExceptions(Collection<String> exceptions) {
		this.exceptions = exceptions;
	}

	@Override
	public void saveRequest(HttpServletRequest request,
			HttpServletResponse response) {

		String path = request.getServletPath();

		if (exceptions.contains(path)){
			return;
		}

		super.saveRequest(request, response);
	}
}
