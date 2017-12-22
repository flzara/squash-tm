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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.lang.Assert;

/**
 * This interceptor wrapper filters out requests based on their pattern. When we use spring 3.2, this class may become
 * obsolete.
 *
 * For the moment, only filters "extensions".
 *
 * @author Gregory Fouquet
 *
 */
public class ExcludeRequestInterceptorWrapper implements HandlerInterceptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcludeRequestInterceptorWrapper.class);

	private final HandlerInterceptor delegate;
	private Collection<String> excludedExtensions = Collections.emptyList();

	/**
	 * Creates an interceptor wrapper which delegates to the given {@link HandlerInterceptor} any request which is not
	 * filtered out.
	 *
	 * @param delegate the delegate interceptor. Should not be <code>null</code>.
	 */
	public ExcludeRequestInterceptorWrapper(HandlerInterceptor delegate) {
		super();
		Assert.parameterNotNull(delegate, "delegate");
		this.delegate = delegate;
	}

	/**
	 * Delegates non filtered-out requests to {@link #delegate}
	 *
	 * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object)
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception { // NOSONAR throws is required for propagation
		if (proceedWith(request)) {
			return delegate.preHandle(request, response, handler);
		} else {
			return true; // false breaks the chain as per interface spec.
		}
	}

	/**
	 * @param request
	 * @return <code>true</code> if the interceptor should proceed, false if the request maps an excluded pattern.
	 */
	private boolean proceedWith(HttpServletRequest request) {
		return !matchesExcludedExtensions(request);
	}

	private boolean matchesExcludedExtensions(HttpServletRequest request) {
		if (excludedExtensions.isEmpty()) {
			return false;
		}

		String pathInfo = request.getServletPath() + StringUtils.defaultString(request.getPathInfo());
		int dotPos = pathInfo.lastIndexOf('.');

		if (dotPos > -1) {
			String ext = pathInfo.substring(dotPos + 1).toLowerCase().trim();

			boolean excluded = excludedExtensions.contains(ext);

			if (excluded) {
				LOGGER.trace("Request {} matches excluded extensions", pathInfo);
			}

			return excluded;
		}

		return false;
	}

	/**
	 * Delegates any request to {@link #delegate}
	 *
	 * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception { // NOSONAR throws is required for propagation
		delegate.postHandle(request, response, handler, modelAndView);

	}

	/**
	 * Delegates any request to  {@link #delegate}
	 *
	 * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception { // NOSONAR throws is required for propagation
		delegate.afterCompletion(request, response, handler, ex);

	}

	/**
	 * @param excludedExtensions
	 *            the excludedExtensions to set
	 */
	@SuppressWarnings("unchecked")
	public void setExcludedExtensions(String[] excludedExtensions) {
		Assert.parameterNotNull(excludedExtensions, "excludedExtensions");

		LOGGER.debug("Excluded extensions : {} ({} excluded extensions)", excludedExtensions,
				excludedExtensions.length); // had to cast to call the right method

		Collection<String> nonNullExtensions = CollectionUtils.select(Arrays.asList(excludedExtensions),
				NotNullPredicate.getInstance());

		this.excludedExtensions = CollectionUtils.collect(nonNullExtensions, new Transformer() {
			@Override
			public Object transform(Object input) {
				return ((String) input).trim().toLowerCase();
			}
		});
	}

}
