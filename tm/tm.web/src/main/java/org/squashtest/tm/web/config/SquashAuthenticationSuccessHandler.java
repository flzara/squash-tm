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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

@Component
@Configuration
public class SquashAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private static final String ROLE_TF_AUTOMATION_PROGRAMMER= "ROLE_TF_AUTOMATION_PROGRAMMER";
	private static final String ROLE_TF_FUNCTIONAL_TESTER ="ROLE_TF_FUNCTIONAL_TESTER";
	private RequestCache requestCache = new HttpSessionRequestCache();

	public SquashAuthenticationSuccessHandler() {
		super();
		setUseReferer(true);
		setAlwaysUseDefaultTargetUrl(false);
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		HttpSession session = request.getSession();
		User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		session.setAttribute("username", authUser.getUsername());
		response.setStatus(HttpServletResponse.SC_OK);
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		SavedRequest savedRequest = requestCache.getRequest(request, response);

		if(authorities.stream().filter(auth -> ((GrantedAuthority) auth).getAuthority().equals(ROLE_TF_AUTOMATION_PROGRAMMER)).findAny().isPresent() &&
			!authorities.stream().filter(auth -> ((GrantedAuthority) auth).getAuthority().equals(ROLE_TF_FUNCTIONAL_TESTER)).findAny().isPresent()) {
			getRedirectStrategy().sendRedirect(request, response,"/automation-workspace");
		} else {
			if(savedRequest != null) {
				String targetUrl = savedRequest.getRedirectUrl();
				getRedirectStrategy().sendRedirect(request, response, targetUrl);
			} else {
				getRedirectStrategy().sendRedirect(request, response,"/home-workspace");
			}

		}
	}

	@Override
	public void setRequestCache(RequestCache requestCache) {
		this.requestCache = requestCache;
	}
}
