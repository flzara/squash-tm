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
package org.squashtest.tm.web.internal.controller.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.web.internal.listener.SquashConfigContextExposer;

import javax.inject.Inject;
import javax.servlet.ServletContext;

/**
 * Controller for Squash TM app wide features
 *
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping("/features")
public class SquashFeatureController {
	/**
	 * This "monitor" should be synchronized when performing changes on the app
	 * scope to emulate transactions. Consider using something higher level from
	 * java.concurrent when this class gets more complex.
	 *
	 * Note that we do **not** want to synchronize ServletContext for a
	 * potentially long time.
	 */
	private final Object monitor = new Object();

	@Inject
	private FeatureManager featureManager;

	@Inject
	private ServletContext applicationScope;

	@RequestMapping(value = "/milestones", method = RequestMethod.POST, params = "enabled")
	@ResponseBody
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Secured("ROLE_ADMIN")
	public void setMilestonesFeature(@RequestParam("enabled") boolean enabled) {
		synchronized (monitor) {
			Object prevState = applicationScope.getAttribute(SquashConfigContextExposer.MILESTONE_FEATURE_ENABLED_CONTEXT_ATTR);
			// nobody should be able to use the feature while it is being turned
			// on/off
			applicationScope.setAttribute(SquashConfigContextExposer.MILESTONE_FEATURE_ENABLED_CONTEXT_ATTR, false);

			try {
				featureManager.setEnabled(Feature.MILESTONE, enabled);
				applicationScope.setAttribute(SquashConfigContextExposer.MILESTONE_FEATURE_ENABLED_CONTEXT_ATTR, enabled);

			} catch (RuntimeException ex) {
				// exception occurred : we rollback the app state
				applicationScope.setAttribute(SquashConfigContextExposer.MILESTONE_FEATURE_ENABLED_CONTEXT_ATTR, prevState);
				throw ex;
			}
		}

	}

	@RequestMapping(value = "/case-insensitive-login", method = RequestMethod.POST, params = "enabled")
	@ResponseBody
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Secured("ROLE_ADMIN")
	public void setCaseInsensitiveLoginFeature(@RequestParam("enabled") boolean enabled) {
		featureManager.setEnabled(Feature.CASE_INSENSITIVE_LOGIN, enabled);
	}
}
