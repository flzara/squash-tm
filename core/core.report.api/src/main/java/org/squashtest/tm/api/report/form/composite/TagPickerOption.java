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
import org.squashtest.tm.api.report.form.TagPicker;

public class TagPickerOption  extends ContainerOption<TagPicker>  {


	public TagPickerOption(){
		super(new TagPicker(), InputType.TAG_PICKER.name());
	}

	/**
	 * @see org.squashtest.tm.api.report.form.ContainerOption#setContent(org.squashtest.tm.api.report.form.Input)
	 */
	@Override
	@SuppressWarnings("Duplicates")
	public void setContent(TagPicker content) {
		throw new IllegalArgumentException(
				"Content cannot be set, it is automatically set toTagPicker. Remove the <property name=\"content\" /> tag");
	}

	/**
	 * @return the pickerName
	 */
	@SuppressWarnings("Duplicates")
	public String getPickerName() {
		return getContent().getName();
	}

	/**
	 * @param pickerName
	 *            the pickerName to set
	 */
	@SuppressWarnings("Duplicates")
	public void setPickerName(String pickerName) {
		getContent().setName(pickerName);
	}

	/**
	 * @return the pickerLabelKey
	 */
	@SuppressWarnings("Duplicates")
	public String getPickerLabelKey() {
		return getContent().getLabelKey();
	}

	/**
	 * This is the value of the container option. It is automatically set to a sensible "MILESTONE_PICKER" value
	 *
	 * @see org.squashtest.tm.api.report.form.OptionInput#setValue(java.lang.String)
	 */
	@Override
	@SuppressWarnings("Duplicates")
	public void setValue(String value) { // NOSONAR I do want to override for doc purposes
		// overriden for doc only
		super.setValue(value);
	}

	/**
	 * @param pickerLabelKey
	 *            the pickerLabelKey to set
	 */
	@SuppressWarnings("Duplicates")
	public void setPickerLabelKey(String pickerLabelKey) {
		getContent().setLabelKey(pickerLabelKey);
	}

	@SuppressWarnings("Duplicates")
	public void setPickerBoundEntity(String boundEntity){
		getContent().setBoundEntity(boundEntity);
	}

	public String getPickerBoundEntity(){
		return getContent().getBoundEntity();
	}

}
