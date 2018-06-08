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
package org.squashtest.tm.web.internal.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UriUtils {
	private UriUtils() {
		super();
	}

	private static final Map<String, Integer> DEFAULT_PORTS;

	static {
		DEFAULT_PORTS = new HashMap<>(3);
		DEFAULT_PORTS.put("ftp", 23);
		DEFAULT_PORTS.put("http", 80);
		DEFAULT_PORTS.put("https", 443);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(UriUtils.class);

	/**
	 * Returns the part of the request's URL between the URI prefix and the URI suffix.
	 * @param request
	 * @param uriPrefix
	 * @param uriSuffix
	 * @return
	 */
	public static String extractPath(HttpServletRequest request, String uriPrefix, String uriSuffix) {
		String requestUri = request.getRequestURI();
		String pattern = "/" + uriPrefix + "/(.*)" + uriSuffix;
		LOGGER.trace("Trying to extract path from URI " + requestUri + " using pattern " + pattern);
		Matcher matcher = Pattern.compile(pattern).matcher(requestUri);
		matcher.find();
		String path = matcher.group(1);
		LOGGER.trace("Path extracted = " + path);
		return path;
	}


	/**
	 * Extract the base url of Squash-TM based on the incoming request.
	 * Implemenation is ripped from spring-security-saml : MetadataGeneratorFilter#getDefaultBaseURL
	 *
	 * @param request
	 * @return
	 */
	public static String extractBaseUrl(HttpServletRequest request){
		StringBuilder sb = new StringBuilder();
		sb.append(request.getScheme()).append("://").append(request.getServerName()).append(":").append(request.getServerPort());
		sb.append(request.getContextPath());
		String url = sb.toString();
		String finalUrl = canonicalize(url);
		return finalUrl;
	}

	/*
	 * Canonicalize an URL.
	 * Ripped from opensaml : SimpleURLCanonicalizer
	 */
	public static final String canonicalize(String strUrl){
		try{
			URIBuilder builder = new URIBuilder(strUrl);

			String scheme = builder.getScheme();
			if (scheme != null){
				scheme = scheme.toLowerCase();
				builder.setScheme(scheme);

				Integer port = DEFAULT_PORTS.get(scheme);
				if (port != null && port == builder.getPort()){
					builder.setPort(-1);
				}

			}

			if (builder.getHost() != null){
				builder.setHost(builder.getHost().toLowerCase());
			}

			return builder.toString();

		}
		catch(URISyntaxException ex){
			throw new RuntimeException(ex);
		}
	}



}
