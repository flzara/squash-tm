package org.squashtest.tm.domain.bdd;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author qtran - created on 27/04/2020
 */
@Entity
@Table(name = "ACTION_WORD_TEXT")
@PrimaryKeyJoinColumn(name = "ACTION_WORD_FRAGMENT_ID")
public class ActionWordText extends ActionWordFragment{
	private static final int ACTION_WORD_TEXT_MAX_LENGTH = 255;

	@NotBlank
	@Column(name = "TEXT")
	@Size(max = 255)
	private String text;

	public ActionWordText() {
	}

	public ActionWordText(String text) {
		if(StringUtils.isBlank(text) || text.contains("\"")) {
			throw new IllegalArgumentException("Action word text cannot be empty.");
		}
		if(text.contains("\"")) {
			throw new IllegalArgumentException("Action word text cannot contain double quote.");
		}
		if (text.length() > ACTION_WORD_TEXT_MAX_LENGTH) {
			throw new IllegalArgumentException("Action word text length cannot exceed 255 characters.");
		}
		this.text = formatText(text);
	}

	//this method is to replace all extra-spaces by a single space, for ex:' this is a    text    '-->' this is a text '
	private String formatText(String text) {
		return text.replaceAll("[\\s]+"," ");
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
