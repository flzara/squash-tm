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
package org.squashtest.tm.web.internal.model.json;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.core.foundation.i18n.Internationalizable;

/**
 * @author Gregory Fouquet
 * 
 */
public class JsonInternationalizableEnum<E extends Enum<?> & Internationalizable> {
	private E value;
	private String label;

	public JsonInternationalizableEnum() {
		super();
	}

	public String getId() {
		return value.name();
	}

	public String getLabel() {
		return label;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(@NotNull E value) {
		this.value = value;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(@NotNull String label) {
		this.label = label;
	}
}
