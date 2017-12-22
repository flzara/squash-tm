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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.http.SessionCreationPolicy.NEVER;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class OAuth2Config {
	
	private static final String OAUTH_SUBPATH = "/whatever"; 
	
	
	public static class AuthenticationServerConfig extends AuthorizationServerConfigurerAdapter {
		@Inject
		private
		DataSource dataSource;

		@Inject
		private AuthenticationManager authenticationManager;

		@Override
		public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//			security.checkTokenAccess()
//		security;
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			// @formatter:off
			endpoints.authorizationCodeServices(authorizationCodeServices())
			.authenticationManager(authenticationManager)
			.tokenStore(tokenStore());
//			.tokenServices(tokenServices());
			// @formatter:on
		}


		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			clients.jdbc(dataSource);

		}

		@Bean
		public TokenStore tokenStore() {
			return new JdbcTokenStore(dataSource);
		}

		@Bean
		JdbcClientDetailsService clientDetailsService() {
			JdbcClientDetailsService service = new JdbcClientDetailsService(dataSource);
			// TODO right now we ues a noop password encoder !

			return service;
		}

		@Bean
		JdbcAuthorizationCodeServices authorizationCodeServices() {
			return new JdbcAuthorizationCodeServices(dataSource);
		}
	}

	@Configuration
	@EnableResourceServer
	public static class ResourceServerConfig extends ResourceServerConfigurerAdapter {
		@Inject
		TokenStore tokenStore;

		@Override
		public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
			resources.tokenStore(tokenStore);
		}

		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http.sessionManagement()
				.sessionCreationPolicy(NEVER)

			.and().requestMatchers()
				.antMatchers(OAUTH_SUBPATH+"/**")

			.and().authorizeRequests()
				.antMatchers(GET, OAUTH_SUBPATH+"/**").fullyAuthenticated();
			// @formatter:on
		}
	}

}
