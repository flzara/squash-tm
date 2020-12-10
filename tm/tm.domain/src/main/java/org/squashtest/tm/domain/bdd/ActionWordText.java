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
import org.squashtest.tm.domain.actionword.ActionWordFragmentVisitor;
import org.squashtest.tm.domain.bdd.util.ActionWordUtil;
import org.squashtest.tm.exception.actionword.InvalidActionWordTextException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_CLOSE_GUILLEMET;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_DOUBLE_QUOTE;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_OPEN_GUILLEMET;

/**
 * @author qtran - created on 27/04/2020
 */
@Entity
@Table(name = "ACTION_WORD_TEXT")
@PrimaryKeyJoinColumn(name = "ACTION_WORD_FRAGMENT_ID")
public class ActionWordText extends ActionWordFragment {
	@NotNull
	@Column(name = "TEXT")
	@Size(max = 255)
	private String text;

	public ActionWordText() {
	}

	public ActionWordText(String text) {
		if (StringUtils.isEmpty(text)) {
			throw new InvalidActionWordTextException("Action word text cannot be empty.");
		}
		if (text.contains(ACTION_WORD_DOUBLE_QUOTE)) {
			throw new InvalidActionWordTextException("Action word text cannot contain double quote.");
		}
		if (text.contains(ACTION_WORD_OPEN_GUILLEMET)) {
			throw new InvalidActionWordTextException("Action word text cannot contain '<' symbol.");
		}
		if (text.contains(ACTION_WORD_CLOSE_GUILLEMET)) {
			throw new InvalidActionWordTextException("Action word text cannot contain '>' symbol.");
		}
		if (text.length() > ACTION_WORD_FRAGMENT_INPUT_MAX_LENGTH) {
			throw new InvalidActionWordTextException("Action word text length cannot exceed 255 characters.");
		}
		//Action word text can have space at the beginning or at the end; so DO NOT trim it!
		this.text = ActionWordUtil.replaceExtraSpacesInText(text);
	}

	@Override
	public void accept(ActionWordFragmentVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public ActionWordFragment createCopy() {
		ActionWordText copy = new ActionWordText();
		copy.setText(this.text);
		return copy;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
