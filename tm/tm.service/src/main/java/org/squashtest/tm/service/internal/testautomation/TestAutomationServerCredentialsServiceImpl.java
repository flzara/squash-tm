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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.TestAutomationServerCredentialsService;

import javax.inject.Inject;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

@Service("TestAutomationServerCredentialsService")
@Transactional
public class TestAutomationServerCredentialsServiceImpl implements TestAutomationServerCredentialsService {

	@Inject
	private TestAutomationConnectorRegistry taConnectorRegistry;

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public AuthenticationProtocol[] getSupportedProtocols(TestAutomationServer server) {
		return taConnectorRegistry.createConnector(server).getSupportedProtocols();
	}
}
