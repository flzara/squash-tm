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
package org.squashtest.tm.web.internal.controller.administration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.AdministrationStatistics;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.user.AdministrationService;

import javax.inject.Inject;

//XSS ok 2018-01-19 JTH
@Controller
public class AdministrationController {
	@Inject
	private AdministrationService administrationService;

	@Inject
	private ConfigurationService configurationService;

	@Value("${info.app.version}")
    private String sqTMversion;

	@RequestMapping("/administration")
	public ModelAndView administration() {
		AdministrationStatistics adminStat = administrationService.findAdministrationStatistics();
		ModelAndView mav = new ModelAndView("page/administration/administration");
		mav.addObject("adminStats", adminStat);
		mav.addObject("sqTMversion", sqTMversion);

		// License information
		String userLicenseInformation = configurationService.findConfiguration(ConfigurationService.Properties.ACTIVATED_USER_EXCESS);
		String dateLicenseInformation = configurationService.findConfiguration(ConfigurationService.Properties.PLUGIN_LICENSE_EXPIRATION);

		mav.addObject("userLicenseInformation", userLicenseInformation);
		mav.addObject("dateLicenseInformation", dateLicenseInformation);
		return mav;
	}

}
