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

import org.squashtest.tm.domain.bdd.util.ActionWordUtil;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.exception.actionword.InvalidActionWordParameterValueException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author qtran - created on 27/04/2020
 */
@Entity
@Table(name = "ACTION_WORD_PARAMETER_VALUE")
public class ActionWordParameterValue {
	private static final int ACTION_WORD_PARAM_VALUE_MAX_LENGTH = 255;

	@Id
	@Column(name = "ACTION_WORD_PARAMETER_VALUE_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "action_word_parameter_value_action_word_parameter_value_id_seq")
	@SequenceGenerator(name = "action_word_parameter_value_action_word_parameter_value_id_seq", sequenceName = "action_word_parameter_value_action_word_parameter_value_id_seq", allocationSize = 1)
	private Long id;

	@Column(name = "VALUE")
	@Size(max = 255)
	private String value;

	@NotNull
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "ACTION_WORD_FRAGMENT_ID")
	private ActionWordParameter actionWordParam;

	@ManyToOne
	@JoinColumn(name = "KEYWORD_TEST_STEP_ID")
	private KeywordTestStep keywordTestStep;

	public ActionWordParameterValue() {
	}

	public ActionWordParameterValue(String value) {
		if (value == null) {
			throw new InvalidActionWordParameterValueException("Action word parameter value cannot be null.");
		}
		if (value.contains("\"")) {
			throw new InvalidActionWordParameterValueException("Action word parameter value cannot contain \", < or >.");
		}
		String trimmedValue = value.trim();
		if (value.length() > ACTION_WORD_PARAM_VALUE_MAX_LENGTH) {
			throw new InvalidActionWordParameterValueException("Action word parameter value length cannot exceed 255 characters.");
		}
		this.value = ActionWordUtil.replaceExtraSpacesInText(trimmedValue);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ActionWordParameter getActionWordParam() {
		return actionWordParam;
	}

	public void setActionWordParam(ActionWordParameter actionWordParam) {
		this.actionWordParam = actionWordParam;
	}

	public KeywordTestStep getKeywordTestStep() {
		return keywordTestStep;
	}

	public void setKeywordTestStep(KeywordTestStep keywordTestStep) {
		this.keywordTestStep = keywordTestStep;
	}
}
