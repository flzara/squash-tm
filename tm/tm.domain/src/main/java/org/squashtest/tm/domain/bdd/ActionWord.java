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
import org.squashtest.tm.domain.bdd.util.ActionWordUtil;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ActionWord {

	private static final int ACTION_WORD_MAX_LENGTH = 255;

	public static final String ACTION_WORD_TEXT_TOKEN = "T";

	public static final String ACTION_WORD_PARAM_TOKEN = "P";

	@Id
	@Column(name = "ACTION_WORD_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "action_word_action_word_id_seq")
	@SequenceGenerator(name = "action_word_action_word_id_seq", sequenceName = "action_word_action_word_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "WORD")
	@NotBlank
	@Size(max = 255)
	private String word;

	@NotBlank
	@Column(name = "TOKEN")
	private String token;

	@NotNull
	@OneToMany(mappedBy = "actionWord", cascade = CascadeType.ALL)
	private List<ActionWordFragment> fragments = new ArrayList<>();

	public List<ActionWordFragment> getFragments() {
		return fragments;
	}

	public void setFragments(List<ActionWordFragment> fragments) {
		this.fragments = fragments;
	}

	public void addFragment(@NotNull ActionWordFragment fragment) {
		getFragments().add(fragment);
	}

	public ActionWord() {
	}

	public ActionWord(String word) {
		if (StringUtils.isBlank(word)) {
			throw new IllegalArgumentException("Action word cannot be blank.");
		}
		String trimmedWord = word.trim();
		if (trimmedWord.length() > ACTION_WORD_MAX_LENGTH) {
			throw new IllegalArgumentException("Action word length cannot exceed 255 characters.");
		}
		this.word = trimmedWord;
		this.token = ACTION_WORD_TEXT_TOKEN+"-"+ActionWordUtil.formatText(trimmedWord)+"-";
	}

	public ActionWord(String word, String token) {
		this(word);
		setToken(token);
	}

	public Long getId() {
		return id;
	}

	public String getWord() {
		return word;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return "ActionWord{" +
			"id=" + id +
			", word='" + word + '\'' +
			'}';
	}
}
