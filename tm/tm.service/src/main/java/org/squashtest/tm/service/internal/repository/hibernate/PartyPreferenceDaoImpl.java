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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyPreference;
import org.squashtest.tm.service.internal.repository.CustomPartyPreferenceDao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartyPreferenceDaoImpl implements CustomPartyPreferenceDao {

	@PersistenceContext
	EntityManager em;

	@Override
	public Map<String, String> findAllPreferencesForParty(Party party) {
		return findAllPreferencesForParty(party.getId());
	}

	@Override
	public Map<String, String> findAllPreferencesForParty(long partyId) {
		Map<String, String> result = new HashMap<>();
		Query q = em.createNamedQuery("partyPreference.findAllForParty");
		q.setParameter("partyId", partyId);
		List<PartyPreference> prefs = q.getResultList();
		for (PartyPreference pref : prefs) {
			result.put(pref.getPreferenceKey(), pref.getPreferenceValue());
		}
		return result;
	}

	@Override
	public PartyPreference findByPartyAndPreferenceKey(Party party, String preferenceKey) {
		return findByPartyAndPreferenceKey(party.getId(), preferenceKey);
	}

	@Override
	public PartyPreference findByPartyAndPreferenceKey(long partyId, String preferenceKey) {
		Query q = em.createNamedQuery("partyPreference.findByPartyAndKey");
		q.setParameter("partyId", partyId);
		q.setParameter("preferenceKey", preferenceKey);
		PartyPreference preference;
		try {
			preference = (PartyPreference) q.getSingleResult();
		} catch (NoResultException e) { // NOSONAR : this exception is part of the nominal use case
			return null;
		}
		return preference;
	}
}
