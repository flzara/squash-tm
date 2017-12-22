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
package org.squashtest.tm.domain.users;

import org.squashtest.tm.domain.Identified;
import javax.persistence.*;

@Entity
@Table(name = "PARTY_PREFERENCE")
public class PartyPreference implements Identified{

	private static final String TYPE = "PARTY_PREFERENCE";

	@Id
	@Column(name = "PREFERENCE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "party_preference_preference_id_seq")
	@SequenceGenerator(name = "party_preference_preference_id_seq", sequenceName = "party_preference_preference_id_seq", allocationSize = 1)
	protected Long id;

	@ManyToOne
	@JoinColumn(name = "PARTY_ID")
	private Party party;

	private String preferenceKey;

	private String preferenceValue;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPreferenceKey() {
		return preferenceKey;
	}

	public void setPreferenceKey(String preferenceKey) {
		this.preferenceKey = preferenceKey;
	}

	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	public String getPreferenceValue() {
		return preferenceValue;
	}

	public void setPreferenceValue(String preferenceValue) {
		this.preferenceValue = preferenceValue;
	}
}
