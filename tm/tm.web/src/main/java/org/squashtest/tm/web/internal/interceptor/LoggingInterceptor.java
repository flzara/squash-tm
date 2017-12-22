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
package org.squashtest.tm.web.internal.interceptor;

import org.slf4j.MDC;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * Created by jthebault on 20/12/2016.
 */
public class LoggingInterceptor implements WebRequestInterceptor {

	@Override
	public void preHandle(WebRequest webRequest) throws Exception {
		MDC.put("requestInfo",webRequest.toString());
	}

	@Override
	public void postHandle(WebRequest webRequest, ModelMap modelMap) throws Exception {
		//Nothing to do here
	}

	@Override
	public void afterCompletion(WebRequest webRequest, Exception e) throws Exception {
		MDC.remove("requestInfo");
	}
}
