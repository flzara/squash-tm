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
package org.squashtest.tm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.squashtest.tm.api.config.SquashPathProperties;
import org.squashtest.tm.web.config.ResourceResolverProperties;

/**
 * Application bootstrapper. Uses spring boot to start a Spring MVC webapp.
 * This class both bootstraps the app and replaces web.xml
 * <p/>
 * Important : @EnableWebSecurity should be here and not on spring-sec config class, otherwise the servlet context is
 * initialized *after* ServletContextAware components, leading to the app to crash.
 *
 * TODO : cant we now use jpa auto config ?
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@SpringBootApplication(exclude = { HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class,
		BatchAutoConfiguration.class })
@EnableConfigurationProperties({ResourceResolverProperties.class, SquashPathProperties.class})
@EnableWebSecurity
@ImportResource({"classpath*:META-INF/spring/dynamicdao-context.xml", "classpath*:META-INF/spring/dynamicmanager-context.xml"})
public class SquashTm {
	private static final Logger LOGGER = LoggerFactory.getLogger(SquashTm.class);

	public static void main(String[] args) {
		new SpringApplication(SquashTm.class).run(args);
	}

}
