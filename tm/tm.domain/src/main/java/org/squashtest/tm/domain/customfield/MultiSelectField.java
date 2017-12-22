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

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A CustomField which stores a multi option selected from a list.
 *
 * @author Karim Drifi
 */

@Entity
@DiscriminatorValue("MSF")
public class MultiSelectField extends CustomField {

	public static final String SEPARATOR = "|";
	public static final String SEPARATOR_EXPR = "\\|";

	@ElementCollection
	@CollectionTable(name = "CUSTOM_FIELD_OPTION", joinColumns = @JoinColumn(name = "CF_ID"))
	private Set<CustomFieldOption> options = new HashSet<>();

	/**
	 * Created a SingleSelectField with a
	 */
	public MultiSelectField() {
		super(InputType.TAG);
	}

	public Set<CustomFieldOption> getOptions() {
		return Collections.unmodifiableSet(options);
	}

	@Override
	public void accept(CustomFieldVisitor visitor) {
		visitor.visit(this);
	}


	public void addOption(String label) {
		CustomFieldOption newOption = new CustomFieldOption(label);
		options.add(newOption);
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		String[] values = defaultValue.split(SEPARATOR_EXPR);
		for (String v : values) {
			addOption(v);
		}
		this.defaultValue = defaultValue;
	}
}
