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

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebSecurityExpressionRoot;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Exposes a security expression evaluator into the model and view under the "sec" name. Inspired by
 * http://forum.thymeleaf.org/Thymeleaf-and-Spring-Security-td3205099.html
 *
 * @author Gregory Fouquet
 * @author http://forum.thymeleaf.org/Thymeleaf-and-Spring-Security-td3205099.html
 *
 */
@Component
public class SecurityExpressionResolverExposerInterceptor extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityExpressionResolverExposerInterceptor.class);

	@Inject
	private PermissionEvaluator permissionEvaluator;

	private static final FilterChain DUMMY_CHAIN = new FilterChain() {
		@Override
		public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
			throw new UnsupportedOperationException();
		}
	};

	private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

	/**
	 * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#postHandle(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) {
		if (modelAndView != null && modelAndView.hasView()
				&& !StringUtils.startsWith(modelAndView.getViewName(), "redirect:")) {
			FilterInvocation filterInvocation = new FilterInvocation(request, response, DUMMY_CHAIN);

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			if (authentication == null) {
				LOGGER.debug("No authentication available for '{}{}'. Thymeleaf won't have access to '#sec' in view '{}'",
					request.getServletPath(), request.getPathInfo(), modelAndView.getViewName());
				return;
			}

			WebSecurityExpressionRoot expressionRoot = new WebSecurityExpressionRoot(authentication, filterInvocation);

			expressionRoot.setTrustResolver(trustResolver);
			expressionRoot.setPermissionEvaluator(permissionEvaluator);
			modelAndView.addObject("sec", expressionRoot);
		}
	}
}
