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
package org.squashtest.tm.domain.users.preferences;

/**
 * Convenient enumeration to declare all user preferences used in SquashTM core
 * Created by jthebault on 30/03/2016.
 */
public enum CorePartyPreference {
	//as the values here are used to persist user preferences in database, don't change these keys, or if you have to, do a proper data migration...

	//value should be "dashboard" or "default"
	HOME_WORKSPACE_CONTENT("squash.core.dashboard.content.home"),
	REQUIREMENT_WORKSPACE_CONTENT("squash.core.dashboard.content.requirement"),
	TEST_CASE_WORKSPACE_CONTENT("squash.core.dashboard.content.tc"),
	CAMPAIGN_WORKSPACE_CONTENT("squash.core.dashboard.content.campaign"),

	//for favorites dashboards, values should be CustomReportLibraryNode id
	FAVORITE_DASHBOARD_HOME("squash.core.favorite.dashboard.home"),
	FAVORITE_DASHBOARD_REQUIREMENT("squash.core.favorite.dashboard.requirement"),
	FAVORITE_DASHBOARD_TEST_CASE("squash.core.favorite.dashboard.tc"),
	FAVORITE_DASHBOARD_CAMPAIGN("squash.core.favorite.dashboard.campaign"),

	//for choosing automatic or manual logging to bugtrackers
	BUGTRACKER_MODE("squash.bug.tracker.mode");

	private String preferenceKey;

	CorePartyPreference(String preferenceKey) {
		this.preferenceKey = preferenceKey;
	}

	public String getPreferenceKey() {
		return preferenceKey;
	}

}
