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
package org.squashtest.tm.web.internal.security.authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/***
 * This class is used to commence a redirect to Login Form
 *
 * @author xpetitrenaud
 *
 */
@Component
public class RedirectEntryPoint extends LoginUrlAuthenticationEntryPoint {
	public RedirectEntryPoint() {
		super("/login");
	}

	/***
	 * This method detects if there's an ajax request and then send the appropriate response
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			// Send an 401 response for ajax request
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} else {
			// Send the login page
			super.commence(request, response, authException);
		}
	}

}
