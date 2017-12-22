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
package org.squashtest.tm.service.user;

import java.util.Map;

import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyPreference;

/**
 * Created by jthebault on 29/03/2016.
 */
public interface PartyPreferenceService {
	/**
	 * Find a {@link PartyPreference} for a given party and a key
	 * @param party
	 * @param preferenceKey
     * @return
     */
	PartyPreference findPreference(Party party, String preferenceKey);

	/**
	 * Find all {@link PartyPreference} for a given party
	 * @param party
	 * @return
	 */
	Map<String,String> findPreferences(Party party);

	/**
	 * Find a {@link PartyPreference} for a given key and for the current user
	 * @param preferenceKey
	 * @return
	 */
	PartyPreference findPreferenceForCurrentUser(String preferenceKey);

	/**
	 * Find all {@link PartyPreference} for the current user
	 * @return
	 */
	Map<String,String> findPreferencesForCurrentUser();

	/**
	 * Add a {@link PartyPreference} for the given party and the given key, with the given value. If the pref doesn't
	 * exist in db, it will create and persit it. Otherwise it update it
	 * @param party
	 * @param preferenceKey
	 * @param preferenceValue
     */
	void addOrUpdatePreference(Party party, String preferenceKey, String preferenceValue);

	/**
	 * Add a {@link PartyPreference} for the current user and the given key, with the given value. If the pref doesn't
	 * exist in db, it will create and persit it. Otherwise it update it
	 * @param preferenceKey
	 * @param preferenceValue
	 */
	void addOrUpdatePreferenceForCurrentUser( String preferenceKey, String preferenceValue);

	void chooseWelcomeMessageAsHomeContentForCurrentUser();

	void chooseFavoriteDashboardAsHomeContentForCurrentUser();
}
