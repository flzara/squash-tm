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
import javax.validation.constraints.Size;

/**
 * Defines an option which can be selected among a list and set as a custom field's value.
 *
 * @author Gregory Fouquet
 */
@Embeddable
public class CustomFieldOption extends DenormalizedCustomFieldOption {

	@Size(max = 7)
	private String colour;

	public CustomFieldOption(String label, String code) {
		super(label, code);
	}

	public CustomFieldOption(String label, String code, String colour) {
		super(label, code);
		this.colour = colour;
	}

	public CustomFieldOption(String label) {
		super(label);
	}

	/**
	 * For Hibernate.
	 */
	protected CustomFieldOption() {
		super();
	}


	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

}
