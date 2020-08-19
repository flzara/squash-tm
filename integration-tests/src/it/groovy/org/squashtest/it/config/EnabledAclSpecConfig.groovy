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


import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.springframework.core.env.AbstractEnvironment
import org.springframework.security.acls.domain.PermissionFactory
import org.springframework.security.acls.jdbc.LookupStrategy
import org.springframework.security.acls.model.AclCache
import org.springframework.security.acls.model.ObjectIdentityGenerator
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService
import org.squashtest.tm.service.SecurityConfig
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager
import org.squashtest.tm.tools.unittest.reflection.ReflectionCategory

import javax.inject.Inject
import javax.sql.DataSource

/**
 * Instantiates a minimal Acl context. Most services are configured with DisabledAclSpecConfig, which shortcuts the ACL context
 * as it is not relevant to the test at hand. However some require a valid ACL Context, this is where this config class will be used.
 *
 * Test classes that needs to benefit from it must still inherit from the adequate parent, and redeclare a @ContextConfiguration
 * that will use EnableAclSpecConfig using the name "aclcontext", so that Spring knows it must override the preview aclcontext
 * with this one. See for instance LookupStrategyConfigIT for an example of usage.
 *
 *
 * Due to the intertwined nature of Spring security configuration, the boilerplate here is just as obnoxious. We cannot
 * import directly the native SecurityContext but still need to reuse some of its @Bean methods, yet on the other hand
 * still use mocks for some of its methods.
 *
 * We work around this by creating a subclass of SecurityConfig, override the methods that needs to return stubs,
 * and invoke the relevant methods on a case by case basis.
 *
 * @since 1.13.0
 */
@Configuration
@ComponentScan(basePackages = [
	"org.squashtest.tm.security.acls",
	"org.squashtest.tm.service.security",
	"org.squashtest.tm.service.internal.security"
])
@EnableSpringConfigured
class EnabledAclSpecConfig {

	@Inject
	DataSource dataSource

	@Inject
	private AbstractEnvironment springEnv;

	SecurityConfig seconf = null;

	public getSpringEnv(){
		return springEnv
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		new MessageDigestPasswordEncoder("SHA-1")
	}

	@Bean
	AclCache aclCache() {
		new NullAclCache();
	}

	@Bean
	PermissionFactory permissionFactory(){
		return getSeconf().permissionFactory()
	}

	@Bean
	LookupStrategy lookupStrategy(){
		// for now we can get away with a null cacheManager
		getSeconf().lookupStrategy(dataSource, null)
	}

	// have to manually create an instance of SecurityConfig and selectively pick
	// the items we want from it
	@Bean(name="squashtest.core.security.JdbcUserDetailsManager")
	SquashUserDetailsManager userDetailsManager(){
		getSeconf().caseInensitiveUserDetailsManager(dataSource)
	}

	// for OAuth
	@Bean
	JdbcClientDetailsService jdbcClientDetailsService(){
		new JdbcClientDetailsService(dataSource);
	}


	@Bean("squashtest.core.security.ObjectIdentityRetrievalStrategy")
	public ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy(){
		getSeconf().objectIdentityRetrievalStrategy()
	}

	@Bean("squashtest.core.security.ObjectIdentityGeneratorStrategy")
	public ObjectIdentityGenerator objectIdentityGenerator(){
		getSeconf().objectIdentityGenerator()
	}



	SecurityConfig getSeconf(){
		if (seconf == null){
			seconf = new SecurityConfig(){
				@Override
				AclCache aclCache(CacheManager cacheManager) {
					return new NullAclCache()
				}
			}

			// also set the database type, used for creating the BasicLookupStrategy
			String dbtype = getSpringEnv().getRequiredProperty("jooq.sql.dialect")
			use(ReflectionCategory){
				SecurityConfig.set "field": "dbType", "of": seconf, "to": dbtype
			}
		}
		seconf
	}
}
