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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author qtran - created on 23/04/2020
 */
@Entity
@Table(name = "ACTION_WORD_FRAGMENT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ActionWordFragment {
	protected static final int ACTION_WORD_FRAGMENT_INPUT_MAX_LENGTH = 255;

	@Id
	@Column(name = "ACTION_WORD_FRAGMENT_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "action_word_fragment_action_word_fragment_id_seq")
	@SequenceGenerator(name = "action_word_fragment_action_word_fragment_id_seq", sequenceName = "action_word_fragment_action_word_fragment_id_seq", allocationSize = 1)
	private Long id;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "ACTION_WORD_ID")
	private ActionWord actionWord;

	public ActionWordFragment() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ActionWord getActionWord() {
		return actionWord;
	}

	public void setActionWord(ActionWord actionWord) {
		this.actionWord = actionWord;
	}
}
