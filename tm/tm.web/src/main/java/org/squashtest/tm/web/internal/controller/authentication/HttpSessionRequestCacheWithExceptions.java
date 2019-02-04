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
package org.squashtest.tm.web.internal.controller.authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

/**
 * Configurable {@link RequestCache}, that allow to provide with additional excluded path. Paths are 
 * your regulart ant paths, starting with a '/'.
 * 
 * @author bsiri
 *
 */
/*
 * Code is largely ripped from RequestCacheConfigurer
 */
// XSS OK - bflessel
public final class HttpSessionRequestCacheWithExceptions extends HttpSessionRequestCache {


	/**
	 * Creates a cache with the given excluded paths, using {@link HeaderContentNegotiationStrategy} 
	 * for content negociation and that account for csrf-protected requests. 
	 * 
	 * @param excludedPaths
	 */
	public HttpSessionRequestCacheWithExceptions(String...excludedPaths){
		init(new HeaderContentNegotiationStrategy(), true, excludedPaths);
	}
	
	/**
	 * Creates a cache with the given excluded paths, with the given negociation strategy and csrf policy.
	 * 
	 * @param negociationStrategy
	 * @param isCsrfEnabled
	 * @param excludedPaths
	 */
	public HttpSessionRequestCacheWithExceptions(ContentNegotiationStrategy negociationStrategy, boolean isCsrfEnabled, String...excludedPaths){
		init(negociationStrategy, isCsrfEnabled, excludedPaths);
	}
	
	/**
	 * Creates a cache with the given excluded paths, that will look for the negociation and csrf 
	 * policies in the given security builder.
	 * 
	 * @param excludedPaths
	 * @param secBuilder
	 */
	public HttpSessionRequestCacheWithExceptions(HttpSecurityBuilder<?> secBuilder, String... excludedPaths){
		ContentNegotiationStrategy contentStrat = secBuilder.getSharedObject(ContentNegotiationStrategy.class);
		if (contentStrat == null){
			contentStrat = new HeaderContentNegotiationStrategy();
		}
		
		// I guess the compiler is fretting about whether using a raw type or the generic 
		// would affect comparison with null
		@SuppressWarnings("unchecked")    
		boolean isCsrfEnabled = (secBuilder.getConfigurer(CsrfConfigurer.class) != null);
		
		init(contentStrat, isCsrfEnabled, excludedPaths);
	}
	
	private void init(ContentNegotiationStrategy negociationStrategy, boolean isCsrfEnabled, String... exclusion){

		List<RequestMatcher> matchers = createBaseMatchers(negociationStrategy, isCsrfEnabled);
		
		if (exclusion != null){
			for (String path : exclusion){
				matchers.add(new NegatedRequestMatcher(new AntPathRequestMatcher(path)));
			}
		}

		setRequestMatcher(new AndRequestMatcher(matchers));
	}
	
	private List<RequestMatcher> createBaseMatchers(ContentNegotiationStrategy negociationStrategy, 
			boolean isCsrfEnabled){
		
		RequestMatcher notFavIcon = new NegatedRequestMatcher(new AntPathRequestMatcher("/**/favicon.ico"));

		MediaTypeRequestMatcher jsonRequest = new MediaTypeRequestMatcher(negociationStrategy, MediaType.APPLICATION_JSON);
		jsonRequest.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
		RequestMatcher notJson = new NegatedRequestMatcher(jsonRequest);

		RequestMatcher notXRequestedWith = new NegatedRequestMatcher(new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest"));

		List<RequestMatcher> matchers = new ArrayList<>();
		if (isCsrfEnabled) {
			RequestMatcher getRequests = new AntPathRequestMatcher("/**", "GET");
			matchers.add(0, getRequests);
		}
		matchers.add(notFavIcon);
		matchers.add(notJson);
		matchers.add(notXRequestedWith);
		
		return matchers;
	}
	
}
