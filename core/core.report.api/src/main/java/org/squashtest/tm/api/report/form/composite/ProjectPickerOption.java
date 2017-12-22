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
package org.squashtest.tm.api.report.form.composite;

import org.squashtest.tm.api.report.form.ContainerOption;
import org.squashtest.tm.api.report.form.InputType;
import org.squashtest.tm.api.report.form.ProjectPicker;

/**
 * Composite input : project picker inside an option. Rem : it can also be configured by hand.
 * 
 * @author Gregory Fouquet
 * 
 */
public class ProjectPickerOption extends ContainerOption<ProjectPicker> {
	/**
	 * 
	 */
	public ProjectPickerOption() {
		super();
		super.setContent(new ProjectPicker());
		super.setValue(InputType.PROJECT_PICKER.name());
	}

	/**
	 * @see org.squashtest.tm.api.report.form.ContainerOption#setContent(org.squashtest.tm.api.report.form.Input)
	 */
	@Override
	public void setContent(ProjectPicker content) {
		throw new IllegalArgumentException(
				"Content cannot be set, it is automatically set to ProjetPicker. Remove the <property name=\"content\" /> tag");
	}

	/**
	 * @return the pickerName
	 */
	public String getPickerName() {
		return getContent().getName();
	}

	/**
	 * @param pickerName
	 *            the pickerName to set
	 */
	public void setPickerName(String pickerName) {
		getContent().setName(pickerName);
	}

	/**
	 * @return the pickerLabelKey
	 */
	public String getPickerLabelKey() {
		return getContent().getLabelKey();
	}

	/**
	 * This is the value of the container option. It is automatically set to a sensible "PROJECT_PICKER" value
	 * 
	 * @see org.squashtest.tm.api.report.form.OptionInput#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) { // NOSONAR I do want to override for doc purposes
		// overriden for doc only
		super.setValue(value);
	}

	/**
	 * @param pickerLabelKey
	 *            the pickerLabelKey to set
	 */
	public void setPickerLabelKey(String pickerLabelKey) {
		getContent().setLabelKey(pickerLabelKey);
	}

}
