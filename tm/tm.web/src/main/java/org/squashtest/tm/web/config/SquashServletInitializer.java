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
package org.squashtest.tm.web.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.squashtest.tm.SquashTm;

/**
 * This class replaces the web deployment descriptor (web.xml). It should not be tampered with !
 * We want the Servlet Context initialized as soon as possible. When it requires too many dependencies, its
 * initialization is delayed by Spring. When it's delayed too long, ServletContextAware beans don't get notified and the
 * app fails to start / bootstrap correctly
 *
 *
 *
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
public class SquashServletInitializer extends SpringBootServletInitializer {
	/**
	 * This is required for embedded tomcat to be able to handle JSPs.
	 * See https://github.com/spring-projects/spring-boot/tree/v1.2.4.RELEASE/spring-boot-samples/spring-boot-sample-web-jsp
	 *
	 * @param application
	 * @return
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SquashTm.class);
	}
}
