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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.user.AdministrationService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

@Component
@Configuration
public class SquashAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private static final String ROLE_TF_AUTOMATION_PROGRAMMER= "ROLE_TF_AUTOMATION_PROGRAMMER";
	private static final String ROLE_TF_FUNCTIONAL_TESTER ="ROLE_TF_FUNCTIONAL_TESTER";
	private RequestCache requestCache = new HttpSessionRequestCache();

	@Inject
	private AdministrationService administrationService;

	public SquashAuthenticationSuccessHandler() {
		super();
		setUseReferer(true);
		setAlwaysUseDefaultTargetUrl(false);
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

		HttpSession session = request.getSession();
		UserDetails authUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		session.setAttribute("username", authUser.getUsername());
		response.setStatus(HttpServletResponse.SC_OK);
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		SavedRequest savedRequest = requestCache.getRequest(request, response);

		String targetUrl = "/home-workspace";
		if (isUserProgrammerButNotTester(authorities)) {
			targetUrl = "/automation-workspace";
		} else if (savedRequest != null) {
			targetUrl = savedRequest.getRedirectUrl();
		}

		if (administrationService.findInformation().size() != 0)  {
			getRedirectStrategy().sendRedirect(request, response, "/information?targetUrl=" + targetUrl);
		} else {
			getRedirectStrategy().sendRedirect(request, response, targetUrl);
		}
	}

	private boolean isUserProgrammerButNotTester(Collection<? extends GrantedAuthority> authorities) {
		return authorities.stream().anyMatch(auth -> ROLE_TF_AUTOMATION_PROGRAMMER.equals(((GrantedAuthority) auth).getAuthority())) &&
				(authorities.stream().noneMatch(auth -> ROLE_TF_FUNCTIONAL_TESTER.equals(((GrantedAuthority) auth).getAuthority())));
	}

	@Override
	public void setRequestCache(RequestCache requestCache) {
		this.requestCache = requestCache;
	}
}
