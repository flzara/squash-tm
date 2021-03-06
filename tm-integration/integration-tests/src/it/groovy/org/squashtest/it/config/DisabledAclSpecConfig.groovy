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


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.springframework.security.acls.model.AclService
import org.springframework.security.acls.model.ObjectIdentityGenerator
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.squashtest.it.stub.security.StubAclService
import org.squashtest.it.stub.security.StubObjectIdentityRetrievalGenerator
import org.squashtest.it.stub.security.StubUserDetailsManager
import org.squashtest.tm.service.internal.security.AdministratorAuthenticationServiceImpl
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager
import org.squashtest.tm.service.security.AdministratorAuthenticationService

/**
 * Configuration for Service specification. Instanciates service and repo layer beans
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@ComponentScan(
basePackages = ["org.squashtest.tm.service.security"],
excludeFilters = [
	@ComponentScan.Filter(Configuration),
	@ComponentScan.Filter(classes= AclService, type=FilterType.ASSIGNABLE_TYPE)
]
)
@EnableSpringConfigured
class DisabledAclSpecConfig {

	/*
	@Bean
	AclCache aclCache() {
		new NullAclCache();
	}
	*/

	@Bean
	PasswordEncoder passwordEncoder() {
		new MessageDigestPasswordEncoder("SHA-1")
	}


	/*
	 * The following implements both OjectAclService and ManageableAclService.
	 */

	@Primary
	@Bean(name = "squashtest.core.security.AclService")
	StubAclService aclService(){
		new StubAclService();
	}

	// defined in tm.service : SecurityConfig, and tricky as shit to create a real instance
	@Bean(name= "squashtest.core.security.JdbcUserDetailsManager")
	SquashUserDetailsManager squashUserDetailsManager(){
		new StubUserDetailsManager()
	}

	@Bean
	AdministratorAuthenticationService administratorAuthenticationService() {
		new AdministratorAuthenticationServiceImpl();
	}


	@Bean("squashtest.core.security.ObjectIdentityRetrievalStrategy")
	public ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy(){
		return new StubObjectIdentityRetrievalGenerator()
	}

	@Bean("squashtest.core.security.ObjectIdentityGeneratorStrategy")
	public ObjectIdentityGenerator objectIdentityGeneratorStrategy(){
		return new StubObjectIdentityRetrievalGenerator()
	}


}
