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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

@Entity
@DiscriminatorValue("RTF")
public class RichTextField extends CustomField {

	@Lob
	@Type(type="org.hibernate.type.TextType")
	private String largeDefaultValue;

	public RichTextField() {
		super(InputType.RICH_TEXT);
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		largeDefaultValue = defaultValue;
	}

	@Override
	public String getDefaultValue() {
		if (largeDefaultValue != null) {
			return largeDefaultValue;
		} else {
			return "";
		}
	}

}
