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
package org.squashtest.tm.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

@Component
@Configuration
public class SquashAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private static final String ROLE_TF_AUTOMATION_PROGRAMMER= "ROLE_TF_AUTOMATION_PROGRAMMER";
	private static final String ROLE_TF_FUNCTIONAL_TESTER ="ROLE_TF_FUNCTIONAL_TESTER";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		HttpSession session = request.getSession();
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		session.setAttribute("username", authUser.getUsername());
		response.setStatus(HttpServletResponse.SC_OK);

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		if(authorities.stream().filter(auth -> ((GrantedAuthority) auth).getAuthority().equals(ROLE_TF_AUTOMATION_PROGRAMMER)).findAny().isPresent() &&
			!authorities.stream().filter(auth -> ((GrantedAuthority) auth).getAuthority().equals(ROLE_TF_FUNCTIONAL_TESTER)).findAny().isPresent()) {
			response.sendRedirect(request.getContextPath()+"/automation-workspace");
		} else {
			response.sendRedirect(request.getContextPath()+"/home-workspace");
		}

	}
}
