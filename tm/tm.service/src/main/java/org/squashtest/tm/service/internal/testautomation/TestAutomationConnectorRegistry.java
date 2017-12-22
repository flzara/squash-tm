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
package org.squashtest.tm.service.internal.testautomation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.UnknownConnectorKind;

@Component("squashtest.testautomation.connector-registry")
public class TestAutomationConnectorRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnectorRegistry.class);

	@Autowired(required = false)
	private Collection<TestAutomationConnector> connectors = Collections.emptyList();
	
	/**
	 * Registered providers mapped by connector kind.
	 */
	private Map<String, TestAutomationConnector> availableConnectors = new ConcurrentHashMap<>(
			5);

	public Collection<String> listRegisteredConnectors() {
		return availableConnectors.keySet();
	}

	public TestAutomationConnector getConnectorForKind(String kind) {
		TestAutomationConnector connector = availableConnectors.get(kind);
		
		if (connector == null) {
			UnknownConnectorKind ex = new UnknownConnectorKind("TestAutomationConnector : unknown kind '" + kind + "'");
			ex.addArg(kind);
			throw ex;
		}
		return connector;
	}


	/**
	 * Registers a new kind of connector connector.
	 * 
	 * @param provider
	 */
	@PostConstruct
	public void registerConnector() {
		for (TestAutomationConnector connector : connectors) {
			String kind = connector.getConnectorKind();
			
			if (kind == null) {
				throw new IllegalArgumentException("TestAutomationConnector : kind is undefined");
			}
			
			LOGGER.info("Registering connector for test automation platforms of kind '{}'", kind);
			
			availableConnectors.put(kind, connector);
		}
	}

}
