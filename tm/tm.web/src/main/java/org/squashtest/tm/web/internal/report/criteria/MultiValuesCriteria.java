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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.InputType;

/**
 * @author Gregory
 * 
 */
public class MultiValuesCriteria extends CriteriaBase implements Criteria {
	private final MultiValueMap values = new MultiValueMap();

	/**
	 * @param name
	 * @param inputType
	 */
	public MultiValuesCriteria(String name, InputType inputType) {
		super(name, inputType);
	}

	/**
	 * 
	 * @see org.squashtest.tm.api.report.criteria.Criteria#getValue()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Collection<?>> getValue() {
		return Collections.unmodifiableMap(values);
	}

	/**
	 * @see org.squashtest.tm.api.report.criteria.Criteria#hasValue()
	 */
	@Override
	public boolean hasValue() {
		return true;
	}

	/**
	 * @param key
	 * @param value
	 */
	public void addValue(String key, Object value) {
		values.put(key, value);
	}

	public Collection<?> getValues(String key) {
		Collection<?> res = values.getCollection(key);

		return res == null ? Collections.emptyList() : res;
	}
}
