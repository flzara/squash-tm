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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.squashtest.tm.api.security.authentication.SecurityExemptionEndPoint;
import org.squashtest.tm.web.internal.controller.authentication.HttpSessionRequestCacheWithExceptions;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;

/**
 * This configures Spring Security
 * <p/>
 * #configure(AuthenticationManagerBuilder) should not be overriden ! When it is overriden, it supersedes any "global"
 * AuthenticationManager. This means any third party authentication provider will be ignored.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class WebSecurityConfig {

	private static final String ALTERNATE_AUTH_PATH = "/auth/**";
	private static final String LOGIN = "/login";
	private static final String ROOT_PATH = "/";

	/* *********************************************************
	 *
	 *  Enpoint-specific security filter chains
	 *
	 * *********************************************************/

	@Configuration
	@Order(10)
	public static class SquashTAWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		/**
		 * part of the fix for [Issue #6900]
		 */
		@Value("${squash.security.basic.token-charset}")
		private String basicAuthCharset = "ISO-8859-1";

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				//NO CSRF for these URL. They must be used by Jenkins to post execution results in Squash TM.
				.csrf().disable()
				.antMatcher("/automated-executions/**")
					.authorizeRequests()
						.anyRequest().access("hasRole('ROLE_TA_API_CLIENT')")
				.and()
				.httpBasic()
					.withObjectPostProcessor(new BasicAuthCharsetConfigurer(basicAuthCharset));
			// @formatter:on
		}
	}

	@Configuration
	@Order(20)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		/**
		 * part of the fix for [Issue #6900]
		 */
		@Value("${squash.security.basic.token-charset}")
		private String basicAuthCharset = "ISO-8859-1";


		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.csrf().disable()
				.antMatcher("/api/**")
					.authorizeRequests()
						.anyRequest()
					.authenticated()
					.and()
						.httpBasic()
							.withObjectPostProcessor(new BasicAuthCharsetConfigurer(basicAuthCharset))
							.realmName("squash-api")
							.authenticationEntryPoint(new AuthenticationEntryPoint() {

								@Override
								public void commence(HttpServletRequest request,
										HttpServletResponse response, AuthenticationException authException)
										throws IOException, ServletException {
									// TODO Auto-generated method stub

									response.addHeader("WWW-Authenticate", "Basic realm=\"squah-api\"");
									response.addHeader("Content-Type", "application/json");
									response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
											authException.getMessage() +
											". You may authenticate using "+
											"1/ basic authentication or " +
											"2/ fetching a cookie JSESSIONID from /login");
								}
							})
					.and()
						.logout()
						.permitAll()
						.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
						.invalidateHttpSession(true)
						.logoutSuccessUrl("/");
			// @formatter:on
		}
	}



	@Configuration
	@Order(30)
	public static class StandardWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		private static final List<String> DEFAULT_IGNORE_AUTH_URLS =
			Arrays.asList(ROOT_PATH, LOGIN, ALTERNATE_AUTH_PATH, "/logout", "/logged-out");

		/**
		 * The Collection of collected SecurityExemptionEndPoint from the classpath.
		 */
		@Autowired(required = false)
		private Collection<SecurityExemptionEndPoint> securityExemptionEndPoints = Collections.EMPTY_LIST;

		@Value("${squash.security.filter.debug.enabled:false}")
		private boolean debugSecurityFilter;

		@Value("${squash.security.preferred-auth-url:/login}")
		private String entryPointUrl = LOGIN;

		@Value("${squash.security.ignored:/scripts/**}")
		private String[] secIngored;

		@Inject
		SquashAuthenticationSuccessHandler successHandler;

		@Override
		public void configure(WebSecurity web) throws Exception {
			web.debug(debugSecurityFilter)
				.ignoring()
				.antMatchers(secIngored);
		}


		@Override
		protected void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.csrf()
					.ignoringAntMatchers(gatherIgnoringCsrfUrlPatterns())
				.and()
				.headers()
				.defaultsDisabled()
				// w/o cache control, some browser's cache policy is too aggressive
				.cacheControl()
				.and().frameOptions().sameOrigin()

				//.and() .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))

				// cache configuration
				.and()
				.requestCache()
					.requestCache(new HttpSessionRequestCacheWithExceptions(http, "/error"))

				// main entry point for unauthenticated users
				.and()
					.exceptionHandling()
					.authenticationEntryPoint(mainEntryPoint())

				// URL security
				.and()
				.authorizeRequests()
					// allow access to main/alternate authentication portals
					// note : on this domain the requests will always succeed,
					// thus the user will not be redirected via the main entry
					// point
					.antMatchers(gatherIgnoringAuthUrlPatterns())
					.permitAll()
					// Administration namespace. Some of which can be accessed by PMs
					.antMatchers(
						"/administration",
						"/administration/milestones",
						"/administration/milestones/**",
						"/milestone/**",
						"/administration/info-lists",
						"/administration/info-lists/**",
						"/administration/projects",
						"/administration/projects/**",
						"/administration/scm-repositories"
					).access(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
					.antMatchers(
						"/admin",
						"/admin/**",
						"/administration/**",
						"/configuration",
						"/configuration/**",
						"/platform/**"
					).access(HAS_ROLE_ADMIN)

					.antMatchers("/accessDenied").permitAll()

					// Namespace reserved for other use
					.antMatchers("/management/**").denyAll()

					.anyRequest().authenticated()

				.and()
					.formLogin()
						.permitAll()
						.loginPage(LOGIN)
						.failureUrl("/login?error")
						.successHandler(successHandler)


				.and()
					.logout()
						.permitAll()
						.invalidateHttpSession(true)
						.logoutSuccessUrl("/logged-out")

				.and()
					.addFilterAfter(new HttpPutFormContentFilter(), SecurityContextPersistenceFilter.class);
			//@formatter:on
		}

		@Bean
		public AuthenticationEntryPoint mainEntryPoint(){
			MainEntryPoint entryPoint = new MainEntryPoint(entryPointUrl);
			return entryPoint;
		}

		private String[] gatherIgnoringCsrfUrlPatterns() {
			List<String> result = new ArrayList<>();
			result.add(ALTERNATE_AUTH_PATH);
			for(SecurityExemptionEndPoint endPoint : securityExemptionEndPoints) {
				result.addAll(endPoint.getIgnoreCsrfUrlPatterns());
			}
			return result.toArray(new String[result.size()]);
		}

		private String[] gatherIgnoringAuthUrlPatterns() {
			List<String> result = new ArrayList<>();
			result.addAll(DEFAULT_IGNORE_AUTH_URLS);
			for(SecurityExemptionEndPoint endPoint : securityExemptionEndPoints) {
				result.addAll(endPoint.getIgnoreAuthUrlPatterns());
			}
			return result.toArray(new String[result.size()]);
		}

	}




	/**
	 * [Issue #6900]
	 *
	 * The base64-encoded token for basic auth has a charset too. Spring Sec expects it to be UTF-8 but many people around expects it
	 * to be Latin-1 (iso-8859-1). This configurer allows to configure the desired encoding.
	 *
	 *
	 * @author bsiri
	 *
	 */
	private static final class BasicAuthCharsetConfigurer implements ObjectPostProcessor<BasicAuthenticationFilter>{

		private final String charset;

		public BasicAuthCharsetConfigurer(String charset) {
			super();
			this.charset = charset;
		}

		@Override
		public <O extends BasicAuthenticationFilter> O postProcess(O object) {
			object.setCredentialsCharset(charset);
			return object;
		}

	}

}

