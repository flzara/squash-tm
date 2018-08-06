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


import javax.inject.Inject;
import javax.validation.ValidatorFactory;

import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.TransactionProvider;
import org.jooq.conf.RenderNameStyle;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.PlatformTransactionManagerCustomizer;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.aspectj.SpringConfiguredConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuration for repository layer.
 *
 * @author Gregory Fouquet
 * @author bsiri
 * @since 1.13.0
 */
@Configuration

// Transaction management
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE + 100, mode = AdviceMode.PROXY, proxyTargetClass = false)
@Import({SpringConfiguredConfiguration.class,AspectJTransactionManagementConfiguration.class})
// Hibernate autoconf
@EntityScan({
	// annotated packages
	"org.squashtest.tm.service.internal.repository.hibernate",
	"org.squashtest.tm.service.internal.hibernate",

	// annotated classes
	"org.squashtest.tm.domain",
	"org.squashtest.csp.core.bugtracker.domain"})

// Spring Data JPA
@EnableJpaRepositories("org.squashtest.tm.service.internal.repository")
public class RepositoryConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryConfig.class);

	@Inject
	private AbstractEnvironment env;

	public RepositoryConfig() {
		super();
	}

	@Bean
	public DefaultLobHandler lobHandler() {
		LOGGER.info("init lobHandler");
		return new DefaultLobHandler();
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static ValidatorFactory validatorFactory() {
		LOGGER.info("init LocalValudatorFactory");
		return new LocalValidatorFactoryBean();
	}


	/*
	 * Because the SessionFieldBridge are @Configurable we need to ensure that SpringConfiguredConfiguration is ready
	 * when the EntityManagerFactory is created (by the autoconfiguration class HibernateJpaConfiguration). Since there
	 * is no way to ensure the order of autoconfiguration application we resort to hacks such as here.
	 *
	 * Technically this bean will register a configurer for the transaction manager that does nothing (which will then be
	 * created early as part of the EMF configuration), and incidentally allows us to declare @DependsOn. I couldn't find
	 * of any other cleaner, functional way to make it work.
	 *
	 * @return some useless bytecode that will sit in the RAM forever
	 */
	@Bean
	@DependsOn(SpringConfiguredConfiguration.BEAN_CONFIGURER_ASPECT_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public PlatformTransactionManagerCustomizer<PlatformTransactionManager> platformTransactionManagerCustomize(){
		return (manager) -> {};
	}


	@Bean
	public org.jooq.Configuration jooqConfiguration(TransactionProvider transactionProvider, ConnectionProvider connectionProvider, DefaultExecuteListenerProvider defaultExecuteListenerProvider) {
		LOGGER.info("init JooqConfiguration");
		DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
		String sqlDialect = env.getRequiredProperty("jooq.sql.dialect");
		SQLDialect dialect = SQLDialect.valueOf(sqlDialect);
		defaultConfiguration.set(dialect);
		defaultConfiguration.set(connectionProvider);
		defaultConfiguration.set(transactionProvider);
		defaultConfiguration.set(defaultExecuteListenerProvider);
		//no need to render schema, squash tm should always be on default schema
		defaultConfiguration.settings().withRenderCatalog(false);
		defaultConfiguration.settings().withRenderSchema(false);
		switch (dialect) {
			case MYSQL://mysql and h2 should receive request with names in upper case
			case H2:
				break;
			case POSTGRES:
				defaultConfiguration.settings().setRenderNameStyle(RenderNameStyle.LOWER); //postgres names are all lower case or we get unknown table errors
				break;
			default:
				throw new IllegalArgumentException("Invalid jOOQ dialect. Use H2, MYSQL or POSTGRES ");
		}
		return defaultConfiguration;
	}


}
