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

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.squashtest.tm.service.internal.bugtracker.BugTrackerConnectorFactory;
import org.squashtest.tm.service.servers.BugTrackerContextHolder;
import org.squashtest.tm.service.bugtracker.BugTrackersService;
import org.squashtest.tm.service.internal.bugtracker.BugTrackersServiceImpl;
import org.squashtest.tm.service.internal.servers.ThreadLocalBugTrackerContextHolder;
import org.squashtest.csp.core.bugtracker.spi.AdvancedBugTrackerConnectorProvider;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider;
import org.squashtest.csp.core.bugtracker.spi.OslcBugTrackerConnectorProvider;
import org.squashtest.tm.service.servers.StoredCredentialsManager;

/**
 * Spring configuration for bugtracker connectors subsystem
 *
 * @author gfouquet
 */
@Configuration
public class BugTrackerConfig {
	@Autowired(required = false)
	private Collection<BugTrackerConnectorProvider> providers = Collections.emptyList();

	@Autowired(required = false)
	private Collection<AdvancedBugTrackerConnectorProvider> advancedProviders = Collections.emptyList();

	@Autowired(required = false)
	private Collection<OslcBugTrackerConnectorProvider> oslcProviders = Collections.emptyList();


	@Bean(name = "squashtest.core.bugtracker.BugTrackerContextHolder")
	public BugTrackerContextHolder bugTrackerContextHolder() {
		return new ThreadLocalBugTrackerContextHolder();
	}

	@Bean(name = "squashtest.core.bugtracker.BugTrackerConnectorFactory")
	public BugTrackerConnectorFactory bugTrackerConnectorFactory() {
		BugTrackerConnectorFactory bean = new BugTrackerConnectorFactory();
		bean.setAdvancedProviders(advancedProviders);
		bean.setProviders(providers);
		bean.setOslcProviders(oslcProviders);
		return bean;
	}

	@Bean
	public BugTrackersService bugTrackersService(StoredCredentialsManager credentialsManager) {
		BugTrackersServiceImpl service = new BugTrackersServiceImpl();
		service.setBugTrackerConnectorFactory(bugTrackerConnectorFactory());
		service.setContextHolder(bugTrackerContextHolder());
		service.setCredentialsManager(credentialsManager);

		return service;
	}
}
