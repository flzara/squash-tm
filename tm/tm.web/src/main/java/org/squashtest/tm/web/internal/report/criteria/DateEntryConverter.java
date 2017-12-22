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

import static org.squashtest.tm.web.internal.report.criteria.FormEntryConstants.EMPTY_DATE_ENTRY;
import static org.squashtest.tm.web.internal.report.criteria.FormEntryConstants.INPUT_VALUE;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.springframework.util.Assert;
import org.squashtest.tm.api.report.criteria.Criteria;
import org.squashtest.tm.api.report.form.InputType;
import org.squashtest.tm.core.foundation.lang.DateUtils;

/**
 * @author Gregory
 *
 */
class DateEntryConverter implements SimpleEntryConverter {
	/**
	 *
	 * @see org.squashtest.tm.web.internal.report.criteria.SimpleEntryConverter#convertEntry(java.lang.String,
	 *      java.util.Map, org.squashtest.tm.api.report.form.InputType)
	 */
	@Override
	public Criteria convertEntry(String name, Map<String, Object> entry, InputType type) {
		Assert.isTrue(InputType.DATE == type, "Type should be " + InputType.DATE);

		String raw = (String) entry.get(INPUT_VALUE);

		if (EMPTY_DATE_ENTRY.equals(raw)) {
			return EmptyCriteria.createEmptyCriteria(name, type);
		}

		Date value = parseDate(raw);
		return new SimpleCriteria<>(name, value, type);
	}

	private Date parseDate(String raw) {
		try {
			return DateUtils.parseIso8601Date(raw);
		} catch (ParseException e) {
			throw new InconsistentDateFormatException(raw, e);
		}
	}

}
