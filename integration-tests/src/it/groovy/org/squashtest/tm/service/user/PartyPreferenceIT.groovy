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
package org.squashtest.tm.service.user

import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.users.Party
import org.squashtest.tm.domain.users.PartyPreference
import org.squashtest.tm.domain.users.User
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

/**
 * @author mpagnon
 *
 */
@UnitilsSupport
@Transactional
class PartyPreferenceIT extends DbunitServiceSpecification {

	@Inject
	PartyPreferenceService partyPreferenceService;

	@DataSet("PartyPreferenceIT.sandbox.xml")
	def "should find all prefs for a user"(){
		given:
		def partyId = -10L
		when:
		def party = findEntity(Party.class, -10L)
		Map<String,String> preferences = partyPreferenceService.findPreferences(party)
		then:
		preferences.size() == 2
		preferences.get("squash.core.favorite.dashboard") == "dashboard15"
		preferences.get("squash.core.favorite.color") == "blue"
	}

	@DataSet("PartyPreferenceIT.sandbox.xml")
	def "should find one pref for a user"(){
		given:
		def partyId = -10L
		when:
		def party = findEntity(Party.class, -10L)
		PartyPreference preference = partyPreferenceService.findPreference(party,"squash.core.favorite.dashboard")
		then:
		preference.getPreferenceValue() == "dashboard15"
	}

	@DataSet("PartyPreferenceIT.sandbox.xml")
	def "should update one pref for a user"(){
		given:
		def partyId = -10L
		when:
		def party = findEntity(Party.class, -10L)
		partyPreferenceService.addOrUpdatePreference(party,"squash.core.favorite.dashboard","Zoli dashboard")
		then:
		PartyPreference pref = findEntity(PartyPreference.class, -1L)
		pref.getPreferenceValue() == "Zoli dashboard"
	}

	@DataSet("PartyPreferenceIT.sandbox.xml")
	def "should create one pref for a user"(){
		given:
		def partyId = -10L
		when:
		def party = findEntity(Party.class, -10L)
		partyPreferenceService.addOrUpdatePreference(party,"squash.core.favorite.typo","Zoli typo")
		Map<String,String> preferences = partyPreferenceService.findPreferences(party)
		then:
		preferences.size() == 3
		preferences.get("squash.core.favorite.typo")== "Zoli typo"
	}

}
