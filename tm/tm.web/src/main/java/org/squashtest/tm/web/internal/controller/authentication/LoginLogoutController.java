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
package org.squashtest.tm.web.internal.controller.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

// XSS OK
@Controller
public class LoginLogoutController {

	@Inject private ConfigurationService configService;

	@Value("${info.app.version}")
    private String version;

	// Issue 7509, Spring is a myth, please don't replace @Autowired by @Inject,
	// otherwise, the ClassLoader can not find the class used in Trac plugin
	@Autowired
	private Environment environment;

	private static final String LOGIN_MESSAGE = "LOGIN_MESSAGE";

	@RequestMapping("/login")
	public String login(Model model) {
		String welcomeMessage = configService.findConfiguration(LOGIN_MESSAGE);
		model.addAttribute("welcomeMessage", HTMLCleanupUtils.cleanHtml(welcomeMessage));
		model.addAttribute("version", version);
		List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
		model.addAttribute("isH2",activeProfiles.contains("h2"));
		return "page/authentication/login";
	}


	@RequestMapping("/logged-out")
	public String logout(){
		return "logged-out.html";
	}

}
