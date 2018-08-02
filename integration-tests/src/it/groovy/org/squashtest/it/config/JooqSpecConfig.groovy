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

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.aspectj.EnableSpringConfigured
import org.squashtest.it.stub.messages.MessageSourceStub

/**
 * Class responsible for creating Jooq Related Beans. As in IT Spring boot Autoconfigure are deactivated, we need to add beans manually
 */
@Configuration
@EnableSpringConfigured
@PropertySource(["classpath:jooq.properties"])
@ImportAutoConfiguration(JooqAutoConfiguration.class)
class JooqSpecConfig {

	@Primary
	@Bean
	MessageSource messageSource() {
		return new MessageSourceStub()
	}
}
