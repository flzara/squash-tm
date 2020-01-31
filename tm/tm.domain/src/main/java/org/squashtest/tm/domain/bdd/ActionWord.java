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
public class ActionWord {

	private static final int ACTION_WORD_MAX_LENGTH = 255;

	@Id
	@Column(name = "BEHAVIOR_PHRASE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "behavior_phrase_behavior_phrase_id_seq")
	@SequenceGenerator(name = "behavior_phrase_behavior_phrase_id_seq", sequenceName = "behavior_phrase_behavior_phrase_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "PHRASE")
	private String word;

	ActionWord() {
	}

	public ActionWord(String word) {
		if(StringUtils.isBlank(word)) {
			throw new IllegalArgumentException("Action word cannot be blank.");
		}
		String trimmedWord = word.trim();
		if(trimmedWord.length() > ACTION_WORD_MAX_LENGTH) {
			throw new IllegalArgumentException("Action word length cannot exceed 255 characters.");
		}
		this.word = trimmedWord;
	}

	public String getWord() {
		return word;
	}
}
