package org.squashtest.tm.domain.bdd;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
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
	private String name;

	@NotNull
	@Column(name = "DEFAULT_VALUE")
	@Size(max = 255)
	private String defaultValue;

	@NotNull
	@OneToMany(mappedBy = "actionWordParam", cascade = CascadeType.ALL)
	private List<ActionWordParameterValue> values = new ArrayList<>();

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

		if(StringUtils.isNotEmpty(defaultValue)){
			if(defaultValue.contains("\"")) {
				throw new IllegalArgumentException("Action word parameter default value cannot contain double quote.");
			}
			String trimmedDefaultValue =  defaultValue.trim();
			if (trimmedDefaultValue.length() > ACTION_WORD_FRAGMENT_INPUT_MAX_LENGTH) {
				throw new IllegalArgumentException("Action word parameter default value length cannot exceed 255 characters.");
			}
			this.defaultValue = formatText(trimmedDefaultValue);
		} else {
			this.defaultValue =  "";
		}
	}

	private void checkIfParamNameIsValid(String trimmedName) {
		Pattern pattern = Pattern.compile("[^\\w-_]");
		Matcher matcher = pattern.matcher(trimmedName);
		if (matcher.find()){
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

	public List<ActionWordParameterValue> getValues() {
		return values;
	}

	public void setValues(List<ActionWordParameterValue> values) {
		this.values = values;
	}

	public void addValue(@NotNull ActionWordParameterValue value) {
		getValues().add(value);
	}
}
