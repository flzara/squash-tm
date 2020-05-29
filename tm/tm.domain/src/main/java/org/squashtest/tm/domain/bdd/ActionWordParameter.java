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
import org.squashtest.tm.domain.testcase.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qtran - created on 27/04/2020
 */
@Entity
@Table(name = "ACTION_WORD_PARAMETER")
@PrimaryKeyJoinColumn(name = "ACTION_WORD_FRAGMENT_ID")
public class ActionWordParameter extends ActionWordFragment {


	@NotBlank
	@Column(name = "NAME")
	@Size(max = 255)
	@javax.validation.constraints.Pattern(regexp = Parameter.NAME_REGEXP)
	private String name;

	@NotNull
	@Column(name = "DEFAULT_VALUE")
	@Size(max = 255)
	private String defaultValue;

	public ActionWordParameter() {
	}

	public ActionWordParameter(String name, String defaultValue) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("Action word parameter name cannot be blank.");
		}
		String trimmedName = name.trim();
		if (trimmedName.length() > ACTION_WORD_FRAGMENT_INPUT_MAX_LENGTH) {
			throw new IllegalArgumentException("Action word parameter name length cannot exceed 255 characters.");
		}
		checkIfParamNameIsValid(trimmedName);
		this.name = trimmedName;

		if (StringUtils.isNotEmpty(defaultValue)) {
			if (defaultValue.contains("\"")) {
				throw new IllegalArgumentException("Action word parameter default value cannot contain double quote.");
			}
			String trimmedDefaultValue = defaultValue.trim();
			if (trimmedDefaultValue.length() > ACTION_WORD_FRAGMENT_INPUT_MAX_LENGTH) {
				throw new IllegalArgumentException("Action word parameter default value length cannot exceed 255 characters.");
			}
			this.defaultValue = ActionWordUtil.formatText(trimmedDefaultValue);
		} else {
			this.defaultValue = "";
		}
	}

	private void checkIfParamNameIsValid(String trimmedName) {
		Pattern pattern = Pattern.compile("[^\\w-_]");
		Matcher matcher = pattern.matcher(trimmedName);
		if (matcher.find()) {
			throw new IllegalArgumentException("Action word parameter name must contain only alphanumeric, dash or underscore characters.");
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
