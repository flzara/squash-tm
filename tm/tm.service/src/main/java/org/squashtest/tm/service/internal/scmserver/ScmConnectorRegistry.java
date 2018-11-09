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
package org.squashtest.tm.service.internal.scmserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.squashtest.tm.core.scm.spi.ScmConnectorProvider;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ScmConnectorRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmConnectorRegistry.class);

	/**
	 * The Collection of collected ScmConnectors from the classpath.
	 */
	@Autowired(required = false)
	private Collection<ScmConnectorProvider> collectedScmConnectors = Collections.EMPTY_LIST;

	/**
	 * The Map of registered ScmConnectors mapped by their kind.
	 */
	private Map<String, ScmConnectorProvider> registeredScmConnectorsMap = new ConcurrentHashMap<>(3);

	/**
	 * Register all the detected ScmConnectorProviders in the connector Map.
	 */
	@PostConstruct
	public void registerScmConnectorProviders() {
		for(ScmConnectorProvider provider : collectedScmConnectors) {
			String kind = provider.getScmKind();
			if(kind == null) {
				throw new IllegalArgumentException("Could not register ScmConnector, its kind is undefined.");
			}
			registeredScmConnectorsMap.put(kind, provider);
			LOGGER.info("Registered Connector of kind '{}' for Source Code Management.", kind);
		}
	}

	/**
	 * Get a all the ScmConnector kinds registered.
	 * @return A Set of all the ScmConnector kinds registered as Strings.
	 */
	public Set<String> getRegisteredScmKinds() {
		return registeredScmConnectorsMap.keySet();
	}

}
