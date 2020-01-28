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
package org.squashtest.tm.domain.bdd;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class BehaviorPhrase {

	private static final int BEHAVIOR_PHRASE_MAX_LENGTH = 255;

	@Id
	@Column(name = "BEHAVIOR_PHRASE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "behavior_phrase_behavior_phrase_id_seq")
	@SequenceGenerator(name = "behavior_phrase_behavior_phrase_id_seq", sequenceName = "behavior_phrase_behavior_phrase_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "PHRASE")
	private String phrase;

	BehaviorPhrase() {
	}

	public BehaviorPhrase(String phrase) {
		if(StringUtils.isBlank(phrase)) {
			throw new IllegalArgumentException("Behavior phrase cannot be blank.");
		}
		String trimmedWord = phrase.trim();
		if(trimmedWord.length() > BEHAVIOR_PHRASE_MAX_LENGTH) {
			throw new IllegalArgumentException("Behavior phrase length cannot exceed 255 characters.");
		}
		this.phrase = trimmedWord;
	}

	public String getPhrase() {
		return phrase;
	}
}
