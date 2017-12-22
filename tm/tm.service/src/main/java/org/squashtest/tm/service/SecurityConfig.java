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
package org.squashtest.tm.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.ehcache.EhCacheFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.domain.EhCacheBasedAclCache;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.squashtest.tm.security.acls.Slf4jAuditLogger;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.internal.security.AffirmativeBasedCompositePermissionEvaluator;
import org.squashtest.tm.service.internal.security.SquashUserDetailsManager;
import org.squashtest.tm.service.internal.security.SquashUserDetailsManagerImpl;
import org.squashtest.tm.service.internal.security.SquashUserDetailsManagerProxyFactory;
import org.squashtest.tm.service.internal.spring.ArgumentPositionParameterNameDiscoverer;
import org.squashtest.tm.service.internal.spring.CompositeDelegatingParameterNameDiscoverer;
import org.squashtest.tm.service.security.acls.ExtraPermissionEvaluator;

/**
 * Partial Spring Sec config. Should be with the rest of spring sec's config now that we dont have osgi bundles segregation
 *
 *
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class SecurityConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

	/**
	 * Configures method security. It has to be annotated @EnableGlobalMethodSecurity according to
	 * GlobalMethodSecurityConfiguration javadoc.
	 * <p/>
	 * We would put this in SecurityConfig if we could, but GlobalMethodSecurityConfiguration requires a PermissionEvaluator
	 * which would induce a depencency cycle on SecurityConfig itself.
	 */
	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, order = Ordered.HIGHEST_PRECEDENCE + 100, mode = AdviceMode.PROXY, proxyTargetClass = false)
	public static class SquashMethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {
		@Override
		protected MethodSecurityExpressionHandler createExpressionHandler() {
			MethodSecurityExpressionHandler meh = super.createExpressionHandler();

			// We want to configure the MSEH, yet we don't want to copy-pasta Spring-Sec source code, hence this instanceof test.
			if (meh instanceof DefaultMethodSecurityExpressionHandler) {
				((DefaultMethodSecurityExpressionHandler) meh).setParameterNameDiscoverer(new CompositeDelegatingParameterNameDiscoverer(
					Arrays.asList(
						new LocalVariableTableParameterNameDiscoverer(),
						new ArgumentPositionParameterNameDiscoverer()
					)
				));
			} else {
				LOGGER.error("Programmatic error : MethodSecurityExpressionHandler is not an instance of DefaultMethodSecurityExpressionHandler ! Check Spring Security source and fix " + this.getClass().getSimpleName() + " accordingly");
			}
			return meh;
		}

		@Override
		protected AccessDecisionManager accessDecisionManager() {
			AccessDecisionManager accessDecisionManager = super.accessDecisionManager();

			// We want to configure the ADM, yet we don't want to copy-pasta Spring-Sec source code, hence this instanceof test.
			if (accessDecisionManager instanceof AffirmativeBased) {
				((AffirmativeBased) accessDecisionManager).setAllowIfAllAbstainDecisions(true);
			} else {
				LOGGER.error("Programmatic error : AccesDecisionManager is not an instance of AffirmativeBased ! Check Spring Security source and fix " + this.getClass().getSimpleName() + " accordingly");
			}

			return accessDecisionManager;
		}
	}

	@Inject
	private DataSource dataSource;
	@Inject
	private PermissionFactory permissionFactory;

	@Inject
	@Named("squashtest.core.security.ObjectIdentityRetrievalStrategy")
	private ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy;
	@Inject
	private ObjectIdentityGenerator objectIdentityGenerator;

	@Inject
	@Lazy
	private FeatureManager featureManager;

	@Inject
	private AuthenticationManager authenticationManager;

	@Inject
	@Named("squashtest.core.security.AclService")
	private AclService aclService;

	@Autowired(required = false)
	private Collection<ExtraPermissionEvaluator> extraPermissionEvaluators = Collections.emptyList();

	@Bean
	public GrantedAuthority aclAdminAuthority() {
		return new SimpleGrantedAuthority("ROLE_ADMIN");
	}

	@Bean
	public BasicLookupStrategy lookupStrategy() {
		BasicLookupStrategy strategy = new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), grantingStrategy());

		// formatter:off
		strategy.setSelectClause(
			"select oid.IDENTITY as object_id_identity,\n" +
				"  gp.PERMISSION_ORDER,\n" +
				"  oid.ID as acl_id,\n" +
				"  null as parent_object, /* oid.parent */\n" +
				"  true as entries_inheriting, /* oid.entries_inheriting*/\n" +
				"  rse.ID as ace_id,\n" +
				"  gp.PERMISSION_MASK as mask,\n" +
				"  gp.GRANTING as granting,\n" +
				"  true as audit_success, /* audit success */\n" +
				"  false as audit_failure, /* audit failure */\n" +
				"  true as ace_principal, /* sid is principal */\n" +
				"  u.LOGIN as ace_sid,\n" +
				"  true as acl_principal, /* owner is prinipal */\n" +
				"  u.LOGIN as acl_sid, /* owner sid */\n" +
				"  ocl.CLASSNAME as class\n" +
				"from ACL_OBJECT_IDENTITY oid\n" +
				"  left join ACL_CLASS ocl on ocl.ID = oid.CLASS_ID\n" +
				"  left join ACL_GROUP_PERMISSION gp on gp.CLASS_ID = ocl.ID\n" +
				"  left join ACL_GROUP g on g.ID = gp.ACL_GROUP_ID\n" +
				"  left join ACL_RESPONSIBILITY_SCOPE_ENTRY rse on rse.ACL_GROUP_ID = g.ID and rse.OBJECT_IDENTITY_ID = oid.ID\n" +
				"  inner join CORE_PARTY party on party.PARTY_ID = rse.PARTY_ID\n" +
				"  left join CORE_TEAM team on team.PARTY_ID = party.PARTY_ID\n" +
				"  left join CORE_TEAM_MEMBER tmemb on tmemb.TEAM_ID = team.PARTY_ID,\n" +
				"  CORE_USER u \n" +
				"where((u.PARTY_ID = tmemb.USER_ID) or (u.PARTY_ID = party.PARTY_ID)) and u.ACTIVE = true and (\n");
		// formatter:on

		strategy.setLookupObjectIdentitiesWhereClause("(oid.IDENTITY = ? and ocl.CLASSNAME = ?)");
		strategy.setLookupPrimaryKeysWhereClause("(oid.ID = ?)");
		strategy.setOrderByClause(") order by oid.IDENTITY asc, gp.PERMISSION_ORDER asc");
		strategy.setPermissionFactory(permissionFactory);

		return strategy;
	}

	@Bean
	public DefaultPermissionGrantingStrategy grantingStrategy() {
		return new DefaultPermissionGrantingStrategy(new Slf4jAuditLogger());
	}

	@Bean
	public AclAuthorizationStrategy aclAuthorizationStrategy() {
		return new AclAuthorizationStrategyImpl(aclAdminAuthority(), aclAdminAuthority(), aclAdminAuthority());
	}

	@Bean(name = "squashtest.core.security.JdbcUserDetailsManager")
	@Primary
	public SquashUserDetailsManagerProxyFactory userDetailsManager() {
		SquashUserDetailsManagerProxyFactory factoryBean = new SquashUserDetailsManagerProxyFactory();
		factoryBean.setCaseInsensitiveManager(caseInensitiveUserDetailsManager());
		factoryBean.setCaseSensitiveManager(caseSensitiveUserDetailsManager());
		factoryBean.setFeatures(featureManager);
		return factoryBean;
	}

	@Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public PasswordEncoder shaPasswordEncoder() {
		return new ShaPasswordEncoder();
	}

	@Bean(name = "userDetailsManager.caseSensitive")
	public SquashUserDetailsManager caseSensitiveUserDetailsManager() {
		SquashUserDetailsManagerImpl manager = configure(new SquashUserDetailsManagerImpl());
		manager.setUsersByUsernameQuery("select LOGIN, PASSWORD, ACTIVE from AUTH_USER where LOGIN = ?");
		manager.setUserExistsSql("select LOGIN from AUTH_USER where LOGIN = ?");
		manager.setGroupAuthoritiesByUsernameQuery(
			"select g.ID, g.QUALIFIED_NAME, ga.AUTHORITY \n" +
				"from CORE_GROUP g \n" +
				"  inner join CORE_GROUP_AUTHORITY ga on ga.GROUP_ID = g.ID\n" +
				"  inner join CORE_GROUP_MEMBER gm on gm.GROUP_ID = g.ID\n" +
				"  inner join CORE_PARTY party on party.PARTY_ID = gm.PARTY_ID\n" +
				"  left join CORE_TEAM team on team.PARTY_ID = party.PARTY_ID\n" +
				"  left join CORE_TEAM_MEMBER tmemb on tmemb.TEAM_ID = team.PARTY_ID,\n" +
				"  CORE_USER u \n" +
				"where( (u.PARTY_ID = tmemb.USER_ID AND u.ACTIVE = true) or (u.PARTY_ID = party.PARTY_ID) ) and ( u.LOGIN = ?)"
		);

		manager.setAuthoritiesByUsernameQuery("" +
				"select cpa.PARTY_ID,  cpa.AUTHORITY from CORE_PARTY_AUTHORITY cpa\n" +
				"  inner join CORE_PARTY party on party.PARTY_ID = cpa.PARTY_ID\n" +
				"  left join CORE_TEAM team on team.PARTY_ID = party.PARTY_ID\n" +
				"  left join CORE_TEAM_MEMBER tmemb on tmemb.TEAM_ID = team.PARTY_ID,\n" +
				"  CORE_USER u \n" +
				"where( (u.PARTY_ID = tmemb.USER_ID AND u.ACTIVE = true) or (u.PARTY_ID = party.PARTY_ID) ) and ( u.LOGIN = ?)"
		);

		return manager;
	}

	@Bean(name = "userDetailsManager.caseInsensitive")
	public SquashUserDetailsManager caseInensitiveUserDetailsManager() {
		SquashUserDetailsManagerImpl manager = configure(new SquashUserDetailsManagerImpl());
		manager.setUsersByUsernameQuery("select LOGIN, PASSWORD, ACTIVE from AUTH_USER where lower(LOGIN) = lower(?)");
		manager.setUserExistsSql("select LOGIN from AUTH_USER where lower(LOGIN) = lower(?)");
		manager.setGroupAuthoritiesByUsernameQuery(
			"select g.ID, g.QUALIFIED_NAME, ga.AUTHORITY \n" +
				"from CORE_GROUP g \n" +
				"  inner join CORE_GROUP_AUTHORITY ga on ga.GROUP_ID = g.ID\n" +
				"  inner join CORE_GROUP_MEMBER gm on gm.GROUP_ID = g.ID\n" +
				"  inner join CORE_PARTY party on party.PARTY_ID = gm.PARTY_ID\n" +
				"  left join CORE_TEAM team on team.PARTY_ID = party.PARTY_ID\n" +
				"  left join CORE_TEAM_MEMBER tmemb on tmemb.TEAM_ID = team.PARTY_ID,\n" +
				"  CORE_USER u \n" +
				"where ((u.PARTY_ID = tmemb.USER_ID AND u.ACTIVE = true) or (u.PARTY_ID = party.PARTY_ID)) \n" +
				"  and (lower(u.LOGIN) = lower(?))"
		);

		manager.setAuthoritiesByUsernameQuery("" +
				"select cpa.PARTY_ID,  cpa.AUTHORITY \n" +
				"from CORE_PARTY_AUTHORITY cpa\n" +
				"  inner join CORE_PARTY party on party.PARTY_ID = cpa.PARTY_ID\n" +
				"  left join CORE_TEAM team on team.PARTY_ID = party.PARTY_ID\n" +
				"  left join CORE_TEAM_MEMBER tmemb on tmemb.TEAM_ID = team.PARTY_ID,\n" +
				"  CORE_USER u \n" +
				"where ((u.PARTY_ID = tmemb.USER_ID AND u.ACTIVE = true) or (u.PARTY_ID = party.PARTY_ID) ) \n" +
				"  and (lower(u.LOGIN) = lower(?))"
		);

		return manager;
	}

	private SquashUserDetailsManagerImpl configure(SquashUserDetailsManagerImpl manager) {
		// TODO AuthenticationManagerDelegator provides low coupling necessary because of chicken / egg problem induced by OSGi
		// hopefully we can remove this turdy trick when OSGi's gone.
		// TODO nosgi
		manager.setAuthenticationManager(authenticationManager);
		manager.setDataSource(dataSource);
		manager.setChangePasswordSql("update AUTH_USER set PASSWORD = ? where LOGIN = ?");
		manager.setUpdateUserSql("update AUTH_USER set PASSWORD = ?, ACTIVE = ? where LOGIN = ?");
		manager.setDeleteUserSql("delete from AUTH_USER where LOGIN = ?");
		manager.setCreateUserSql("insert into AUTH_USER (LOGIN, PASSWORD, ACTIVE) values (?,?,?)");
		manager.setCreateAuthoritySql("insert into CORE_PARTY_AUTHORITY (PARTY_ID, AUTHORITY) values ((select cu.PARTY_ID from CORE_USER cu where cu.LOGIN = ?), ?)");
		manager.setDeleteUserAuthoritiesSql(
			"delete from CORE_PARTY_AUTHORITY\n" +
				"where PARTY_ID in (\n" +
				"  select cu.PARTY_ID from CORE_USER cu\n" +
				"  where cu.LOGIN = ?\n" +
				")"
		);
		/**
		 A successful login attempt requires the user to have at least one authority set (and also a valid login/pass).

		 Our policy regarding authorities is group-based : there are no per-user authorities and they
		 must be attached to a group.

		 However due to the restriction mentioned above (about successful login),  we want the user to be granted
		 a default authority if he doesn't belong to a group yet. That authority can merely allow him to
		 connect to the application, but without proper entry in acls he couldn't do much anyway.

		 So we fetch both personal authorities for the default one, and the group authorities for the real ones.
		 */
		/*

		 */
		manager.setEnableAuthorities(true);
		manager.setEnableGroups(true);

		return manager;
	}

	@Bean
	public EhCacheBasedAclCache aclCache() {
		return new EhCacheBasedAclCache(ehCache().getObject(), grantingStrategy(), aclAuthorizationStrategy());
	}

	@Bean
	public EhCacheFactoryBean ehCache() {
		EhCacheFactoryBean ehCache = new EhCacheFactoryBean();
		ehCache.setOverflowToDisk(false);
		ehCache.setTimeToIdle(600);
		ehCache.setTimeToLive(1800);
		ehCache.setCacheManager(ehCacheManagerFactoryBean().getObject());
		ehCache.setCacheName("AclCache");
		ehCache.afterPropertiesSet();
		return ehCache;
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
		return new EhCacheManagerFactoryBean();
	}

	@Bean
	public AffirmativeBasedCompositePermissionEvaluator permissionEvaluator() {
		AffirmativeBasedCompositePermissionEvaluator evaluator = new AffirmativeBasedCompositePermissionEvaluator(aclService, extraPermissionEvaluators);
		evaluator.setObjectIdentityRetrievalStrategy(objectIdentityRetrievalStrategy);
		evaluator.setObjectIdentityGenerator(objectIdentityGenerator);
		evaluator.setPermissionFactory(permissionFactory);

		return evaluator;
	}
}
