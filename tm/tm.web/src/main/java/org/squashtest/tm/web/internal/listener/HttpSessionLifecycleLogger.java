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
package org.squashtest.tm.web.internal.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSessionLifecycleLogger implements HttpSessionListener, ServletRequestListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpSessionLifecycleLogger.class);

	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		// NOOP

	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();

		Cookie sessionCookie = findSessionIdCookie(request);

		if (sessionCookie != null) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Received request for HTTP session id {} at {}", sessionCookie.getValue(),
						System.currentTimeMillis());
			}
		} else {
			LOGGER.info("Received request with no session cookie : {}", request.getRequestURI());
		}

	}

	private Cookie findSessionIdCookie(HttpServletRequest request) {
		// /!\ No cookies means null array !
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if ("JSESSIONID".equals(cookie.getName())) {
					return cookie;
				}
			}
		}

		return null;
	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		LOGGER.info("Created a HTTP session id {} at {}", se.getSession().getId(), System.currentTimeMillis());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		LOGGER.info("Destroyed HTTP session id {} at {}", se.getSession().getId(), System.currentTimeMillis());
	}

}
