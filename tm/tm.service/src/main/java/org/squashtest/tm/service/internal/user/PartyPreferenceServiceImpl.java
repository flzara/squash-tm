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
package org.squashtest.tm.service.internal.user;

import java.util.Map;

import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyPreference;
import org.squashtest.tm.domain.users.preferences.CorePartyPreference;
import org.squashtest.tm.domain.users.preferences.WorkspaceDashboardContentValues;
import org.squashtest.tm.service.internal.repository.PartyPreferenceDao;
import org.squashtest.tm.service.user.PartyPreferenceService;
import org.squashtest.tm.service.user.UserAccountService;

/**
 * Created by jthebault on 29/03/2016.
 */
@Service("squashtest.tm.service.PartyPreferenceService")
@Transactional
public class PartyPreferenceServiceImpl implements PartyPreferenceService{

	@Inject
	UserAccountService userAccountService;

	@Inject
	private PartyPreferenceDao partyPreferenceDao;

	@Override
	public PartyPreference findPreference(Party party, String preferenceKey) {
		return partyPreferenceDao.findByPartyAndPreferenceKey(party,preferenceKey);
	}

	@Override
	public Map<String, String> findPreferences(Party party) {
		return partyPreferenceDao.findAllPreferencesForParty(party);
	}

	@Override
	public PartyPreference findPreferenceForCurrentUser(String preferenceKey) {
		Party party = userAccountService.findCurrentUser();
		return partyPreferenceDao.findByPartyAndPreferenceKey(party,preferenceKey);
	}

	@Override
	public Map<String, String> findPreferencesForCurrentUser() {
		Party party = userAccountService.findCurrentUser();
		return partyPreferenceDao.findAllPreferencesForParty(party);
	}

	@Override
	public void addOrUpdatePreference(Party party, String preferenceKey, String preferenceValue) {
		PartyPreference pref = findPreference(party,preferenceKey);
		if (pref == null){
			pref = new PartyPreference();
			pref.setParty(party);
			pref.setPreferenceKey(preferenceKey);
			pref.setPreferenceValue(preferenceValue);
			partyPreferenceDao.save(pref);
		}
		else{
			pref.setPreferenceValue(preferenceValue);
		}

	}

	@Override
	public void addOrUpdatePreferenceForCurrentUser(String preferenceKey, String preferenceValue) {
		Party party = userAccountService.findCurrentUser();
		addOrUpdatePreference(party,preferenceKey,preferenceValue);
	}

	@Override
	public void chooseWelcomeMessageAsHomeContentForCurrentUser() {
		String key = CorePartyPreference.HOME_WORKSPACE_CONTENT.getPreferenceKey();
		String value = WorkspaceDashboardContentValues.DEFAULT.getPreferenceValue();
		addOrUpdatePreferenceForCurrentUser(key, value);
	}

	@Override
	public void chooseFavoriteDashboardAsHomeContentForCurrentUser() {
		String key = CorePartyPreference.HOME_WORKSPACE_CONTENT.getPreferenceKey();
		String value = WorkspaceDashboardContentValues.DASHBOARD.getPreferenceValue();
		addOrUpdatePreferenceForCurrentUser(key, value);
	}
}
