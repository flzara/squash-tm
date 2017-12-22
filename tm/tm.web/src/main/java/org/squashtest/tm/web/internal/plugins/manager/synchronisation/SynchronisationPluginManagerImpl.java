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
package org.squashtest.tm.web.internal.plugins.manager.synchronisation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.squashtest.tm.api.wizard.SynchronisationPlugin;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;

@Component
public class SynchronisationPluginManagerImpl implements SynchronisationPluginManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronisationPluginManagerImpl.class);
	public static final int DEFAULT_DELAY = 300;
	public static final int MIN_DELAY = 5;

	@Autowired(required = false)
	private Collection<SynchronisationPlugin> plugins = Collections.emptyList();

	@Inject
	@Named("squashtest.tm.service.ThreadPoolTaskScheduler")
	private TaskScheduler taskScheduler;

	@Inject
	private Environment environment;

	@PostConstruct
	public void registerSynchronisationPlugin() {
		int delay = getDelay();
		for (SynchronisationPlugin plugin : plugins) {
			LOGGER.info("Registering synchronisation plugin {} as {}", plugin, plugin.getName());
			taskScheduler.scheduleWithFixedDelay(plugin.performSynchronisation(), delay);
		}
	}

	private int getDelay() {
		//this delay is expressed in seconds
		String property = environment.getProperty("squash.external.synchronisation.delay");
		int delay = DEFAULT_DELAY;
		if (StringUtils.isNotBlank(property)) {
            try {
                delay = Integer.parseInt(property);
				delay = Math.max(delay, MIN_DELAY);
				LOGGER.info("Found the property 'squash.external.synchronisation.delay'. Delay between sync will be  : " + delay + " seconds.");
            } catch (NumberFormatException e) {
                //we keep default value and emit a warning
                LOGGER.error("Impossible to parse the property 'squash.external.synchronisation.delay' as a number. Please provide a valid synchronisation delay.");
                throw e;
            }
        }
		delay = delay * 1000;//convert to milli second as specified by spring
		return delay;
	}

	@Override
	public Collection<SynchronisationPlugin> findAll() {
		return plugins;
	}
}
