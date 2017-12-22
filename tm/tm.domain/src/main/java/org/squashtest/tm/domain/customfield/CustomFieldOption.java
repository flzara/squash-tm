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

import javax.persistence.Embeddable;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.DigestUtils;
import org.squashtest.tm.domain.Sizes;

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
	@Pattern(regexp = CustomField.CODE_REGEXP)
	private String code = "";

	public CustomFieldOption(String label, String code) {
		this.label = label;
		this.code = code;
	}

	public CustomFieldOption(String label){

		// when no code is supplied we need to create it.
		// To do so we md5-hash it then truncate to 30 characters because we
		// don't care anyway.
		String code = DigestUtils.md5DigestAsHex(label.getBytes());

		this.label = label;
		this.code = code.substring(0,30);
	}

	/**
	 * For Hibernate.
	 */
	protected CustomFieldOption() {
		super();
	}

	public void setLabel(String label) {
		this.label = label.trim();
	}

	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	@Override
	public int hashCode() {
		final int prime = 57; // NOSONAR : look somewhere else
		int result = 53; // NOSONAR : look somewhere else
		result = prime * result + (label == null ? 0 : label.hashCode());
		return result;
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
