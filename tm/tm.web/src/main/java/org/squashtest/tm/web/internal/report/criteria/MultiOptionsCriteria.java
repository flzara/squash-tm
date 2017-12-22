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
package org.squashtest.tm.web.internal.report.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.InputType;

/**
 * @author Gregory Fouquet
 * 
 */
public final class MultiOptionsCriteria extends CriteriaBase implements Criteria {
	private final Map<Object, Boolean> isSelectedByOption = new HashMap<>();
	private final List<Object> selectedOptions = new ArrayList<>();

	/**
	 * @param name
	 * @param sourceInput
	 */
	MultiOptionsCriteria(String name, InputType sourceInput) {
		super(name, sourceInput);
	}

	/**
	 * @param optionValue
	 * @param selected
	 */
	void addOption(Object optionValue, boolean selected) {
		isSelectedByOption.put(optionValue, selected);

		if (selected) {
			selectedOptions.add(optionValue);
		}
	}

	/**
	 * @see org.squashtest.tm.api.report.criteria.Criteria#getValue()
	 * 
	 */
	@Override
	public Collection<Object> getValue() {
		return Collections.unmodifiableList(selectedOptions);
	}

	public Collection<Object> getSelectedOptions() {
		return getValue();
	}

	/**
	 * @see org.squashtest.tm.api.report.criteria.Criteria#hasValue()
	 */
	@Override
	public boolean hasValue() {
		return true;
	}

	/**
	 * Tells if the given option value is selected or not.
	 * 
	 * @param optionValue
	 * @return
	 * @throws IllegalArgumentException
	 *             if the given value is not a known option value.
	 */
	public boolean isSelected(String optionValue) throws IllegalArgumentException {
		Boolean res = isSelectedByOption.get(optionValue);
		if (res == null) {
			throw new IllegalArgumentException('\'' + optionValue
					+ "' does not belong to the known option values. Known values are : " + isSelectedByOption.values());
		}

		return res;
	}
}
