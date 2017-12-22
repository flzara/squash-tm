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
package org.squashtest.tm.api.report.form;

import org.squashtest.tm.core.foundation.i18n.Labelled;

/**
 * @author Gregory Fouquet
 *
 */
public class OptionInput extends Labelled {
	private String name;
	private String value = "";
	private boolean defaultSelected = false;
	private String givesAccessTo = "none";
	/**
	 * please read {@link BasicInput#disabledBy}
	 */
	private String disabledBy;

	/**
	 *
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	public void setGivesAccessTo(String givesAccessTo) {
		if (givesAccessTo != null && !givesAccessTo.isEmpty()) {
			this.givesAccessTo = givesAccessTo;
		}
	}

	/**
	 * @return the name of the object the OptionInput gives access to
	 */
	public String getGivesAccessTo() {
		return givesAccessTo;
	}

	/**
	 * Callback - should be called by a {@link OptionsGroup} then this object is added to the group.
	 *
	 * @param group
	 */
	/* package */void addedTo(OptionsGroup group) {
		this.name = group.getName();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param defaultChecked
	 *            the defaultChecked to set
	 */
	public void setDefaultSelected(boolean defaultChecked) {
		this.defaultSelected = defaultChecked;
	}

	/**
	 * @return the defaultChecked
	 */
	public boolean isDefaultSelected() {
		return defaultSelected;
	}

	public String getDisabledBy() {
		return disabledBy;
	}

	public void setDisabledBy(String disabledBy) {
		this.disabledBy = disabledBy;
	}

}
