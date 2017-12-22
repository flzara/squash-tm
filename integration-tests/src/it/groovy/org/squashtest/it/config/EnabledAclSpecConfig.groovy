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
package org.squashtest.it.config

import javax.inject.Inject
import javax.sql.DataSource

import org.springframework.context.annotation.*
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.AclCache
import org.springframework.security.authentication.encoding.PasswordEncoder
import org.springframework.security.authentication.encoding.ShaPasswordEncoder
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.squashtest.tm.service.SecurityConfig
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager
import org.squashtest.tm.service.security.AdministratorAuthenticationService;
import org.squashtest.tm.service.security.StubLookupStrategy
import org.squashtest.tm.service.security.acls.model.NullAclCache

/**
 * Configuration for Service specification. Instanciates service and repo layer beans
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@ComponentScan(
basePackages = ["org.squashtest.tm.security.acls","org.squashtest.tm.service.security", "org.squashtest.tm.service.internal.security"]
)
@EnableSpringConfigured
class EnabledAclSpecConfig {
	
	@Inject
	DataSource dataSource
	
	@Inject
	PermissionFactory permFactory;
	
	SecurityConfig seconf = null // instanciated on demand later
	
	@Bean
	PasswordEncoder passwordEncoder() {
		new ShaPasswordEncoder()
	}
	
	@Bean
	LookupStrategy lookupStrategy(){
		getSeconf().lookupStrategy()
	}
	
	@Bean
	AclCache aclCache() {
		new NullAclCache();
	}
	
	// have to manually create an instance of SecurityConfig and selectively pick 
	// the items we want from it
	@Bean(name="squashtest.core.security.JdbcUserDetailsManager")
	SquashUserDetailsManager userDetailsManager(){
		getSeconf().caseInensitiveUserDetailsManager()
	}
	
	// for OAuth
	@Bean
	JdbcClientDetailsService jdbcClientDetailsService(){
		new JdbcClientDetailsService(dataSource);
	}

	
	SecurityConfig getSeconf(){
		if (seconf == null){
			seconf = new SecurityConfig(dataSource : dataSource, permissionFactory : permFactory)
		}
		seconf
	}
}
