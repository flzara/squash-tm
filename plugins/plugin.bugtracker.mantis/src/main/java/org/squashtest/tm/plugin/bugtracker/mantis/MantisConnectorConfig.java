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
package org.squashtest.tm.plugin.bugtracker.mantis;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.squashtest.csp.core.bugtracker.core.NamespacedBugtrackerMessageSource;
import org.squashtest.tm.api.config.SquashPathProperties;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Configuration
@ComponentScan(basePackages = {"org.squashtest.csp.core.bugtracker.mantis", "org.squashtest.csp.core.bugtracker.internal.mantis"} )
public class MantisConnectorConfig {
	@Inject
	private SquashPathProperties squashPathProperties;

	@Bean
	public MessageSource mantisConnectorMessageSource() {
		ReloadableResourceBundleMessageSource bean = new ReloadableResourceBundleMessageSource();
		bean.setCacheSeconds(60);
		bean.setBasenames(
			"classpath:/org/squashtest/tm/plugin/bugtracker/mantis/messages",
			squashPathProperties.getLanguagesPath() + "/plugin.bugtracker.mantis/mantis-bugmessages",
			squashPathProperties.getLanguagesPath() + "/plugin.bugtracker.mantis/messages"
		);
		return new NamespacedBugtrackerMessageSource(bean, "bugtracker.mantis.");
	}
}
