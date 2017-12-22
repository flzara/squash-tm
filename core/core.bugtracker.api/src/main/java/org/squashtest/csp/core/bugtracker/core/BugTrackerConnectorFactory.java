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
package org.squashtest.csp.core.bugtracker.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.csp.core.bugtracker.service.AdvancedBugtrackerConnectorAdapter;
import org.squashtest.csp.core.bugtracker.service.InternalBugtrackerConnector;
import org.squashtest.csp.core.bugtracker.service.OslcBugtrackerConnectorAdapter;
import org.squashtest.csp.core.bugtracker.service.SimpleBugtrackerConnectorAdapter;
import org.squashtest.csp.core.bugtracker.spi.AdvancedBugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.AdvancedBugTrackerConnectorProvider;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.BugTrackerConnectorProvider;
import org.squashtest.csp.core.bugtracker.spi.OslcBugTrackerConnector;
import org.squashtest.csp.core.bugtracker.spi.OslcBugTrackerConnectorProvider;
import org.squashtest.tm.core.foundation.exception.NullArgumentException;

/**
 * Factory of BugTrackerConnector objects. It delegates to {@link BugTrackerConnectorProvider} which should register to
 * this factory.
 *
 * @author Gregory Fouquet
 */
public class BugTrackerConnectorFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerConnectorFactory.class);

	/**
	 * Registered providers mapped by connector kind.
	 */
	private final Map<String, BugTrackerConnectorProvider> providerByKind = new ConcurrentHashMap<>(2);
	private final Map<String, AdvancedBugTrackerConnectorProvider> advancedProviderByKind = new ConcurrentHashMap<>(2);
	private final Map<String, OslcBugTrackerConnectorProvider> oslcProviderByKind = new ConcurrentHashMap<>(2);

	private Collection<BugTrackerConnectorProvider> providers = Collections.emptyList();
	private Collection<AdvancedBugTrackerConnectorProvider> advancedProviders = Collections.emptyList();
	private Collection<OslcBugTrackerConnectorProvider> oslcProviders = Collections.emptyList();

	public BugTrackerConnectorFactory() {
	}

	public Set<String> getProviderKinds() {
		Set<String> result = new HashSet<>();
		result.addAll(providerByKind.keySet());
		result.addAll(advancedProviderByKind.keySet());
		result.addAll(oslcProviderByKind.keySet());
		return result;
	}

	public InternalBugtrackerConnector createConnector(BugTracker bugTracker) {

		String kind = bugTracker.getKind();
		InternalBugtrackerConnector connector;

		LOGGER.debug("Creating Connector for bug tracker of kind {}", kind);

		if (isSimpleConnector(kind)) {
			connector = createAndWrapSimpleConnector(bugTracker);

		} else if (isAdvancedConnector(kind)) {
			connector = createAndWrapAdvancedConnector(bugTracker);

		} else if (isOslcConnector(kind)) {
			connector = createAndWrapOslcConnector(bugTracker);
		} else {

			throw new UnknownConnectorKindException(kind);
		}

		return connector;
	}

	@PostConstruct
	public void registerBugTrackers() {
		Assert.notNull(providers, "'providers' property should not be null");
		for (BugTrackerConnectorProvider provider : providers) {
			registerProvider(provider);
		}

		Assert.notNull(advancedProviders, "'advancedProviders' property should not be null");
		for (AdvancedBugTrackerConnectorProvider advancedProvider : advancedProviders) {
			registerAdvancedProvider(advancedProvider);
		}

		Assert.notNull(oslcProviders, "'oslcProviders' property should not be null");
		for (OslcBugTrackerConnectorProvider oslcProvider : oslcProviders) {
			registerOslcProvider(oslcProvider);
		}

	}

	private boolean isSimpleConnector(String kind) {
		return providerByKind.containsKey(kind);
	}

	private boolean isAdvancedConnector(String kind) {
		return advancedProviderByKind.containsKey(kind);
	}

	public boolean isOslcConnector(String kind) {
		return oslcProviderByKind.containsKey(kind);
	}

	private InternalBugtrackerConnector createAndWrapSimpleConnector(BugTracker bugTracker) {
		BugTrackerConnectorProvider provider = providerByKind.get(bugTracker.getKind());
		BugTrackerConnector connector = provider.createConnector(bugTracker);

		return new SimpleBugtrackerConnectorAdapter(connector);
	}

	private InternalBugtrackerConnector createAndWrapAdvancedConnector(BugTracker bugTracker) {
		AdvancedBugTrackerConnectorProvider provider = advancedProviderByKind.get(bugTracker.getKind());
		AdvancedBugTrackerConnector connector = provider.createConnector(bugTracker);

		return new AdvancedBugtrackerConnectorAdapter(connector);
	}

	private InternalBugtrackerConnector createAndWrapOslcConnector(BugTracker bugTracker) {
		OslcBugTrackerConnectorProvider provider = oslcProviderByKind.get(bugTracker.getKind());
		OslcBugTrackerConnector connector = provider.createConnector(bugTracker);

		return new OslcBugtrackerConnectorAdapter(connector);
	}

	/**
	 * Registers a new kind of connector provider, making it instantiable by this factory.
	 *
	 */
	private void registerProvider(BugTrackerConnectorProvider provider) {
		String kind = provider.getBugTrackerKind();

		if (kind == null) {
			throw new NullArgumentException("provider.bugTrackerKind");
		}

		LOGGER.info("Registering Connector provider for bug trackers of kind '{}'", kind);

		providerByKind.put(kind, provider);
	}

	/**
	 * Registers a new kind of connector provider, making it instantiable by this factory.
	 *
	 */
	private void registerAdvancedProvider(AdvancedBugTrackerConnectorProvider provider) {
		String kind = provider.getBugTrackerKind();

		if (kind == null) {
			throw new NullArgumentException("provider.bugTrackerKind");
		}

		LOGGER.info("Registering Connector provider for bug trackers of kind '{}'", kind);

		advancedProviderByKind.put(kind, provider);
	}

	private void registerOslcProvider(OslcBugTrackerConnectorProvider provider) {
		String kind = provider.getBugTrackerKind();

		if (kind == null) {
			throw new NullArgumentException("provider.bugTrackerKind");
		}

		LOGGER.info("Registering Connector provider for bug trackers of kind '{}'", kind);

		oslcProviderByKind.put(kind, provider);

	}

	public void setProviders(@NotNull Collection<BugTrackerConnectorProvider> providers) {
		this.providers = providers;
	}

	public void setAdvancedProviders(@NotNull Collection<AdvancedBugTrackerConnectorProvider> advancedProviders) {
		this.advancedProviders = advancedProviders;
	}

	public void setOslcProviders(@NotNull Collection<OslcBugTrackerConnectorProvider> oslcProviders) {
		this.oslcProviders = oslcProviders;

	}
}
