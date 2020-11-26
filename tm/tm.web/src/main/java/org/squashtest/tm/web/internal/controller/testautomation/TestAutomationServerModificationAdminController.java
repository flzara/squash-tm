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
package org.squashtest.tm.web.internal.controller.testautomation;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.internal.testautomation.TestAutomationConnectorRegistry;
import org.squashtest.tm.service.testautomation.TestAutomationServerCredentialsService;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;
import org.squashtest.tm.web.internal.controller.thirdpartyserver.ThirdPartyServerCredentialsManagementBean;
import org.squashtest.tm.web.internal.controller.thirdpartyserver.ThirdPartyServerCredentialsManagementHelper;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.squashtest.tm.web.internal.controller.bugtracker.BugTrackerControllerHelper.retrieveAsteriskedPassword;

// XSS OK
@Controller
@RequestMapping("/administration/test-automation-servers/{serverId}")
public class TestAutomationServerModificationAdminController {


	@Inject
	private TestAutomationServerManagerService service;

	@Inject
	private TestAutomationServerCredentialsService testAutomationServerCredentialsService;

	@Inject
	private ThirdPartyServerCredentialsManagementHelper credentialsBeanHelper;

	@Inject
	private TestAutomationConnectorRegistry testAutomationConnectorRegistry;


	@RequestMapping(method = RequestMethod.GET)
	public String showTAServer(@PathVariable("serverId") long serverId, Model model, Locale locale) {

		TestAutomationServer server = service.findById(serverId);

		ThirdPartyServerCredentialsManagementBean authConf = makeAuthBean(server, locale);
		// SQUASH-1305
		String asteriskedPassword = retrieveAsteriskedPassword(server.getAuthenticationProtocol(), authConf.getCredentials());

		model.addAttribute("server", server);
		model.addAttribute("asteriskedPassword", asteriskedPassword);
		model.addAttribute("authConf", authConf);
		model.addAttribute("serverDescription", HTMLCleanupUtils.cleanHtml(server.getDescription()));

		return "test-automation/server-modification.html";

	}

	// ********************** internal *****************************************


	private ThirdPartyServerCredentialsManagementBean makeAuthBean(TestAutomationServer server, Locale locale){

		ThirdPartyServerCredentialsManagementBean bean = credentialsBeanHelper.initializeFor(server, locale);

		AuthenticationProtocol[] availableProtos = testAutomationServerCredentialsService.getSupportedProtocols(server);
		bean.setAvailableProtos(Arrays.asList(availableProtos));

		// force auth policy to app level, no other is supported
		bean.setAuthPolicy(AuthenticationPolicy.APP_LEVEL);
		bean.setFeatureAuthPolicySelection(false);

		//also the credentials are not optional, and are not testable
		bean.setFeatureTestCredentialsButton(false);
		bean.setAppLevelCredsAreOptional(false);

		return bean;

	}

}
