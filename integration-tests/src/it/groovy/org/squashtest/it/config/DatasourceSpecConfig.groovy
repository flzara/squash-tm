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

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties

import javax.sql.DataSource
import javax.validation.ValidatorFactory

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.aspectj.AspectJTransactionManagementConfiguration
import org.squashtest.it.stub.milestone.StubActiveMilestoneHolder;
import org.squashtest.it.stub.validation.StubValidatorFactory
import org.squashtest.tm.service.RepositoryConfig
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.unitils.database.UnitilsDataSourceFactoryBean

@Configuration
@EnableSpringConfigured
@ComponentScan(basePackages="org.squashtest.tm.service.internal.api.repository")
class DatasourceSpecConfig {

	@Bean
	ValidatorFactory validatorFactory() {
		return new StubValidatorFactory()
	}

	@Bean
	static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean(name = "squashtest.core.persistence.jdbc.DataSource")
	DataSource dataSource() {
		return new UnitilsDataSourceFactoryBean().getObject()
	}


	@Primary
	@Bean ActiveMilestoneHolder activeMilestoneHolder(){
		new StubActiveMilestoneHolder()
	}

	@Bean
	DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties()
	}



}
