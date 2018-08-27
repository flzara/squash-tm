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
package org.squashtest.tm.domain.customfield;

import javax.validation.constraints.NotBlank;
import org.springframework.util.DigestUtils;
import org.squashtest.tm.domain.Sizes;

import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Defines an option which can be selected among a list and set as a custom field's value.
 *
 * @author Gregory Fouquet
 */
@Embeddable
public class CustomFieldOption {
	@NotBlank
	@Size(max = Sizes.LABEL_MAX)
	private String label;

	@NotBlank
	@Size(max = 30)
	//The message here is in resource bundle ValidationMessages in tm.service module
	@Pattern(regexp = CustomField.CODE_REGEXP, message = "{org.squashtest.tm.validation.constraint.onlyStdChars}")
	private String code = "";

	@Size(max = 7)
	private String colour;

	public CustomFieldOption(String label, String code) {
		this.label = label;
		this.code = code;
	}

	public CustomFieldOption(String label, String code, String colour) {
		this.label = label;
		this.code = code;
		this.colour = colour;
	}

	public CustomFieldOption(String label) {

		// when no code is supplied we need to create it.
		// To do so we md5-hash it then truncate to 30 characters because we
		// don't care anyway.
		String generatedCode = DigestUtils.md5DigestAsHex(label.getBytes());

		this.label = label;
		this.code = generatedCode.substring(0, 30);
	}

	/**
	 * For Hibernate.
	 */
	protected CustomFieldOption() {
		super();
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label.trim();
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public int hashCode() {
		final int prime = 57; // NOSONAR : look somewhere else
		int result = 53; // NOSONAR : look somewhere else
		result = prime * result + (label == null ? 0 : label.hashCode());
		return result;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	@Override//NOSONAR code generation, assumed to be safe
	public boolean equals(Object obj) { // GENERATED:START
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CustomFieldOption)) {
			return false;
		}
		CustomFieldOption other = (CustomFieldOption) obj;
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		return true;
	}// GENERATED:END

}
