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

import org.spockframework.mock.MockNature
import spock.mock.DetachedMockFactory

import javax.validation.ValidatorFactory

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.squashtest.csp.core.bugtracker.service.BugTrackersService
import org.squashtest.it.stub.validation.StubValidatorFactory

/**
 * Configuration for Repository (DAO) specifications. Instanciates repository layer beans.
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@ComponentScan(
		basePackages = ["org.squashtest.tm.service.internal.repository", "org.squashtest.tm.service.internal.api"],
		excludeFilters = @ComponentScan.Filter(Configuration))
@EnableSpringConfigured
class RepositorySpecConfig {

	@Bean
	@Primary
	BugTrackersService bugTrackerService() {
		new DetachedMockFactory().createMock("bugTrackerService", BugTrackersService, MockNature.MOCK, [:])
	}

	@Bean
	ValidatorFactory validatorFactory() {
		return new StubValidatorFactory()
	}

	@Bean
	static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
