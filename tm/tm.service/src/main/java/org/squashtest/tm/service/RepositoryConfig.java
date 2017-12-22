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


import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.TransactionProvider;
import org.jooq.conf.RenderNameStyle;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.SpringConfiguredConfiguration;
import org.springframework.core.Ordered;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.validation.ValidatorFactory;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Configuration for repository layer.
 *
 * "implements TransactionManagementConfigurer" seems necessary because we dont't use standard JPA. As a consequence,
 * @EnableTransactionManagement magic don't seem to properly kick in.
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE + 100, mode = AdviceMode.PROXY, proxyTargetClass = false)
@Import(AspectJTransactionManagementConfiguration.class)
@EnableJpaRepositories("org.squashtest.tm.service.internal.repository")
public class RepositoryConfig implements TransactionManagementConfigurer {
	private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryConfig.class);

	@Inject
	private DataSource dataSource;

	@Inject
	private AbstractEnvironment env;

	public RepositoryConfig() {
		super();
	}

	@Bean
	public DefaultLobHandler lobHandler() {
		return new DefaultLobHandler();
	}


	@Bean(name = "entityManagerFactory")
	@DependsOn(SpringConfiguredConfiguration.BEAN_CONFIGURER_ASPECT_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public EntityManagerFactory entityManagerFactory() {

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		factory.setDataSource(dataSource);
		factory.setPackagesToScan(
			// annotated packages (scanned in this method since Spring 4.1)
			"org.squashtest.tm.service.internal.repository.hibernate",
			"org.squashtest.tm.service.internal.hibernate",

			// annotated classes
			"org.squashtest.tm.domain",
			"org.squashtest.csp.core.bugtracker.domain"
		);

		// setting the properties
		Properties hibProperties = hibernateProperties();
		factory.setJpaProperties(hibProperties);

		factory.afterPropertiesSet();

		return factory.getObject();
	}


	@Bean
	//@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	@DependsOn("entityManagerFactory")
	public JpaTransactionManager transactionManager() {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		jpaTransactionManager.setEntityManagerFactory(entityManagerFactory());
		//jpaTransactionManager.setEntityManagerFactory(emf);
		// Below is useful to be able to perform direct JDBC operations using this same tx mgr.
		jpaTransactionManager.setDataSource(dataSource);
		return jpaTransactionManager;
	}


	@Bean
	public Properties hibernateProperties() {
		Set<String> names = new HashSet<>();

		for (PropertySource ps : env.getPropertySources()) {
			if (ps instanceof EnumerablePropertySource) {
				for (String name : ((EnumerablePropertySource) ps).getPropertyNames()) {
					if (name.toLowerCase().startsWith("hibernate")) {
						names.add(name);
						// Don't directly get the property because in case of duplicate props, it would short-circuit
						// property priority, which is managed by Environment object
					}
				}
			}
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Filtering hibernate properties from environment : {}", names);
		}

		Properties props = new Properties();

		for (String name : names) {
			props.put(name, env.getProperty(name));
		}


		return props;
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public static ValidatorFactory validatorFactory() {
		return new LocalValidatorFactoryBean();
	}


	@Override
	//[Issue 6432]
	//trying to remove a nasty double bean PlatformTransactionManager bug by commenting the @Bean below
	//see http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/transaction/annotation/TransactionManagementConfigurer.html#annotationDrivenTransactionManager--
	//@Bean
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return transactionManager();
	}

	@Bean
	public org.jooq.Configuration jooqConfiguration(TransactionProvider transactionProvider, ConnectionProvider connectionProvider, DefaultExecuteListenerProvider defaultExecuteListenerProvider) {
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
