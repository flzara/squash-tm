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
package org.squashtest.tm.service.internal.feature;

import static org.squashtest.tm.service.configuration.ConfigurationService.Properties.CASE_INSENSITIVE_LOGIN_FEATURE_ENABLED;
import static org.squashtest.tm.service.configuration.ConfigurationService.Properties.MILESTONE_FEATURE_ENABLED;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.milestone.MilestoneManagerService;

/**
 * @author Gregory Fouquet
 *
 */
@Service(value = "featureManager")
@Transactional
public class FeatureManagerImpl implements FeatureManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManagerImpl.class);

	@Inject
	@Lazy
	private ConfigurationService configuration;

	@Inject
	@Lazy
	private MilestoneManagerService milestoneManager;

	/**
	 * @see org.squashtest.tm.service.feature.FeatureManager#isEnabled(org.squashtest.tm.service.feature.FeatureManager.Feature)
	 */
	@Override
	public boolean isEnabled(Feature feature) {
		LOGGER.trace("Polling feature {}", feature);

		boolean enabled;

		switch (feature) {
			case MILESTONE:
				enabled = configuration.getBoolean(MILESTONE_FEATURE_ENABLED);
				break;

			case CASE_INSENSITIVE_LOGIN:
				enabled = configuration.getBoolean(CASE_INSENSITIVE_LOGIN_FEATURE_ENABLED);
				break;
			default:
				throw new IllegalArgumentException("I don't know feature '" + feature
					+ "'. I am unable to tell if it's enabled or not");
		}

		return enabled;
	}

	/**
	 *
	 * @see org.squashtest.tm.service.feature.FeatureManager#setEnabled(org.squashtest.tm.service.feature.FeatureManager.Feature,
	 *      boolean)
	 */
	@Override
	public void setEnabled(Feature feature, boolean enabled) {
		LOGGER.trace("Setting feature {} to {}", feature, enabled);

		switch (feature) {
			case MILESTONE:
				setMilestoneFeatureEnabled(enabled);
				break;

			case CASE_INSENSITIVE_LOGIN:
				setCaseInsensitiveLoginFeatureEnabled(enabled);
				break;

			default:
				throw new IllegalArgumentException("I don't know feature '" + feature
					+ "'. I am unable to switch its enabled status to " + enabled);
		}
	}

	/**
	 * @param enabled
	 */
	private void setCaseInsensitiveLoginFeatureEnabled(boolean enabled) {
		// TODO check if possible
		configuration.set(CASE_INSENSITIVE_LOGIN_FEATURE_ENABLED, enabled);
	}

	private void setMilestoneFeatureEnabled(boolean enabled) {
		configuration.set(MILESTONE_FEATURE_ENABLED, enabled);
		if (enabled) {
			milestoneManager.enableFeature();
		} else {
			milestoneManager.disableFeature();
		}

	}
}
