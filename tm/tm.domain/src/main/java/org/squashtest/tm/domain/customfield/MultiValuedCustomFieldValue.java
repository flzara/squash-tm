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

import java.util.List;

/**
 * {@link CustomFieldValue} implementing that interface have a composite value.
 * 
 * @author bsiri
 *
 */
public interface MultiValuedCustomFieldValue {

	/**
	 * Returns the list of selected options as one concatenated string with ' ' as separator.
	 */
	public String getValue();

	/**
	 * Sets the value as a list of String. The CustomFieldValueOption should be
	 * created on the fly.
	 * @param values
	 */
	public void setValues(List<String> values);

	/**
	 * Sets the value as a list of CustomFieldValueOption
	 * 
	 * @param options
	 */
	public void setSelectedOptions(List<CustomFieldValueOption> options);

	/**
	 * Returns the selected options as String
	 */
	public List<String> getValues();

	/**
	 * Returns the selected options as a collection.
	 * 
	 * @return
	 */
	public List<CustomFieldValueOption> getSelectedOptions();

	/**
	 * Returns the value as a RawValue.
	 * @return
	 */
	public RawValue asRawValue();

}
