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
package org.squashtest.tm.web.internal.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

/*
 * that class needs to embbed a real HtmlValidator.
 *
 * TODO : check the OWASP library and the following class :
 *
 * http://owasp-esapi-java.googlecode.com/svn/trunk_doc/latest/org/owasp/esapi/filters/SecurityWrapperRequest.html
 *
 *
 */
public class HtmlSanitizationFilter implements Filter {
	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlSanitizationFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing special
		LOGGER.debug("HtmlSanitizationFilter was initialized");
	}

	@Override
	public void destroy() {
		// nothing special

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	ServletException {

		if (HttpServletRequest.class.isAssignableFrom(request.getClass())) {
			chain.doFilter(new HtmlSafeRequestWrapper((HttpServletRequest) request), response);
		} else {
			chain.doFilter(request, response);
		}

	}

	protected static String[] escapeValue(String[] orig) {
		if (orig == null) {
			return null;
		}

		String[] aString = new String[orig.length];

		int i = 0;
		for (String string : orig) {
			aString[i++] = HTMLCleanupUtils.stripJavascript(string);
		}

		return aString;
	}

	@SuppressWarnings("unchecked")
	private static final class HtmlSafeRequestWrapper extends HttpServletRequestWrapper {

		private HttpServletRequest request;

		HtmlSafeRequestWrapper(HttpServletRequest request) {
			super(request);
			this.request = request;
		}

		@Override
		public String getParameter(String name) {
			String value = request.getParameter(name);
			if (value == null) {
				return null;
			}
			String[] cleaned = escapeValue(new String[] { value });
			return cleaned != null ? cleaned[0] : null;
		}

		@Override
		public String[] getParameterValues(String name) {
			String[] values = request.getParameterValues(name);
			return escapeValue(values);
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return new HtmlSafeParameterMapWrapper(request.getParameterMap());
		}

	}

	private static final class HtmlSafeParameterMapWrapper implements Map<String, String[]> {

		private final Map<String, String[]> wrappedMap;

		public HtmlSafeParameterMapWrapper(Map<String, String[]> wrappedMap) {
			this.wrappedMap = wrappedMap;
		}

		@Override
		public void clear() {
			wrappedMap.clear();
		}

		@Override
		public boolean containsKey(Object key) {
			return wrappedMap.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return wrappedMap.containsValue(value);
		}

		@Override
		public Set<java.util.Map.Entry<String, String[]>> entrySet() {
			return wrappedMap.entrySet();
		}

		@Override
		public String[] get(Object key) {
			return escapeValue(wrappedMap.get(key));
		}

		@Override
		public boolean isEmpty() {
			return wrappedMap.isEmpty();
		}

		@Override
		public Set<String> keySet() {
			return wrappedMap.keySet();
		}

		@Override
		public String[] put(String key, String[] value) {
			return wrappedMap.put(key, value);

		}

		@Override
		public void putAll(Map<? extends String, ? extends String[]> m) {
			wrappedMap.putAll(m);
		}

		@Override
		public String[] remove(Object key) {
			return escapeValue(wrappedMap.remove(key));
		}

		@Override
		public int size() {
			return wrappedMap.size();
		}

		@Override
		public Collection<String[]> values() {
			Collection<String[]> values = wrappedMap.values();
			Collection<String[]> clean = new ArrayList<>();
			for (String[] origString : values) {
				clean.add(escapeValue(origString));
			}

			return clean;

		}

	}

}
