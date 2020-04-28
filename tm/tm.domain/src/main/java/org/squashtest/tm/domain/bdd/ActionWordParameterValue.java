package org.squashtest.tm.domain.bdd;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.domain.testcase.KeywordTestStep;

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
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "ACTION_WORD_FRAGMENT_ID")
	private ActionWordParameter actionWordParam;

	@NotNull
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "TEST_STEP_ID")
	private KeywordTestStep keywordTestStep;

	public ActionWordParameterValue() {
	}

	public ActionWordParameterValue(String value) {
		if (StringUtils.isNotEmpty(value)) {
			if (value.contains("\"")) {
				throw new IllegalArgumentException("Action word parameter value cannot contain double quote.");
			}
			String trimmedValue = value.trim();
			if (value.length() > ACTION_WORD_PARAM_VALUE_MAX_LENGTH) {
				throw new IllegalArgumentException("Action word parameter value length cannot exceed 255 characters.");
			}
			this.value = formatText(trimmedValue);
		} else {
			this.value = "";
		}
	}

	//this method is to replace all extra-spaces by a single space, for ex:' this is a    text    '-->' this is a text '
	private String formatText(String text) {
		return text.replaceAll("[\\s]+", " ");
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
