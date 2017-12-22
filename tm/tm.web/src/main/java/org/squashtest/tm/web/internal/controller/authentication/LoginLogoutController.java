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

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.squashtest.tm.service.configuration.ConfigurationService;

@Controller
public class LoginLogoutController {

	@Inject private ConfigurationService configService;

	@Value("${info.app.version}")
    private String version;

	private static final String LOGIN_MESSAGE = "LOGIN_MESSAGE";

	@RequestMapping("/login")
	public String login(Model model) {
		String welcomeMessage = configService.findConfiguration(LOGIN_MESSAGE);
		model.addAttribute("welcomeMessage", welcomeMessage);
		model.addAttribute("version", version);
		return "page/authentication/login";
	}

}
