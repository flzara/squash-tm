/**
 * This file is part of the Squashtest platform.
 * Copyright (C) Henix, henix.fr
 * <p>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.web.internal.filter;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.io.CharStreams;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("HtmlSanitizationFilter was initialized");
		}
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

		private ServletInputStream stream;

		HtmlSafeRequestWrapper(HttpServletRequest request) throws IOException {
			super(request);
			this.request = request;
			if (mustBeSecured(request)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Sanitizing json post request for request " + request.toString());
				}
				stream = new SafeServletInputStreamWrapper(request);
			} else {
				stream = request.getInputStream();
			}
		}

		/**
		 * To be eligible to json sanitation a request must :
		 * <ul>
		 * <li>Be a POST Request</li>
		 * <li>Have a content with length > 0</li>
		 * <li>Declare Json Media Type</li>
		 * </ul>
		 *
		 * @param request the request
		 * @return true if it's a json payload, false if not
		 */
		private boolean mustBeSecured(HttpServletRequest request) {
			String contentType = request.getContentType();
			boolean contentTypeSayCheck = false;
			if (contentType != null) {
				List<MediaType> mediaTypes = MediaType.parseMediaTypes(contentType);
				contentTypeSayCheck = mediaTypes.contains(MediaType.APPLICATION_JSON) || mediaTypes.contains(MediaType.APPLICATION_JSON_UTF8);
			}
			return HttpMethod.POST.matches(request.getMethod()) && request.getContentLength() > 0 && contentTypeSayCheck;
		}

		@Override
		public String getParameter(String name) {
			String value = request.getParameter(name);
			if (value == null) {
				return null;
			}
			String[] cleaned = escapeValue(new String[]{value});
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

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return stream;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return new BufferedReader(new InputStreamReader(stream));
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

	private static final class SafeServletInputStreamWrapper extends ServletInputStream {

		private final ServletInputStream wrapperServletInputStream;

		private InputStream securedStream;

		public SafeServletInputStreamWrapper(HttpServletRequest request) throws IOException {
			ServletInputStream inputStream = request.getInputStream();
			wrapperServletInputStream = inputStream;

			String unsecuredContent = CharStreams.toString(new BufferedReader(new InputStreamReader(inputStream, request.getCharacterEncoding())));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(unsecuredContent);
			sanitizeJsonObjectNode((ObjectNode) node); //NOSONAR first node of a json payload is always an object node...
			String securedContent = node.toString();
			securedStream = new ByteArrayInputStream(securedContent.getBytes(request.getCharacterEncoding()));
		}

		private void sanitizeJsonObjectNode(ObjectNode node) {
			Iterator<Map.Entry<String, JsonNode>> childIterators = node.fields();
			while (childIterators.hasNext()) {
				Map.Entry<String, JsonNode> child = childIterators.next();
				JsonNode childValue = child.getValue();
				JsonNodeType childNodeType = childValue.getNodeType();
				switch (childNodeType) {
					case STRING:
						String securedContent = Jsoup.clean(childValue.asText(""), Whitelist.basicWithImages());
						node.set(child.getKey(), new TextNode(securedContent));
						break;
					case ARRAY:
						if (childValue.size() > 0) {
							sanitizeJsonArrayNode((ArrayNode) child);//NOSONAR Jackson said it's an Array node ...
						}
						break;
					case OBJECT:
						if (childValue.size() > 0) {
							sanitizeJsonObjectNode((ObjectNode) child); //NOSONAR Jackson said it's an Object node ...
						}
						break;
					default:
						break;
				}
			}
		}

		private void sanitizeJsonArrayNode(ArrayNode arrayNode) {
			for (int i = 0; i < arrayNode.size(); i++) {
				JsonNode childNode = arrayNode.get(i);
				JsonNodeType childNodeType = childNode.getNodeType();
				switch (childNodeType) {
					case STRING:
						String securedContent = Jsoup.clean(childNode.asText(""), Whitelist.basicWithImages());
						arrayNode.set(i, new TextNode(securedContent));
						break;
					case ARRAY:
						if (childNode.size() > 0) {
							sanitizeJsonArrayNode((ArrayNode) childNode);//NOSONAR Jackson said it's an Array node ...
						}
						break;
					case OBJECT:
						if (childNode.size() > 0) {
							sanitizeJsonObjectNode((ObjectNode) childNode); //NOSONAR Jackson said it's an Object node ...
						}
						break;
					default:
						break;
				}
			}
		}


		@Override
		public boolean isFinished() {
			return wrapperServletInputStream.isFinished();
		}

		@Override
		public boolean isReady() {
			return wrapperServletInputStream.isReady();
		}

		@Override
		public void setReadListener(ReadListener readListener) {
			wrapperServletInputStream.setReadListener(readListener);
		}

		@Override
		public int read() throws IOException {
			return securedStream.read();
		}
	}

}
