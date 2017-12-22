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
package org.squashtest.tm.domain.search;

import java.lang.reflect.Field;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.joda.time.LocalDateTime;
import org.springframework.util.ReflectionUtils;

/**
 * Sets the time component of a date to 00h00 accounting for the timezone of the server and the daylight saving
 * time for this date, then stores it as milliseconds since epoch in the index. Yuk.
 */
public class NotGMTDateBridge implements FieldBridge {

	/*
	 	XXX replaced the systematic set accessible true/false on each method call by a static init block .
	 	Still awful but wont change the behavior.

	 	 I've considered other solutions but truncating the time by other means would either truncate to midnight UTC -
	 	 which is not what we want because anyone not living in UK would then have day effectively set back to the day before,
	 	 or would take too long if we account for timezone offset and the daylight saving time (the later requires to be checked
	 	 again for each date treated).

	 	 The current solution is disgusting yet accurate (the meaning of "accurate" here is also twisted in a disgusting way).

	 	 Another option, instead of storing the milliseconds since epoch we could format then store the date as string.
	  */
	private static final Field miliField;

	static {
		miliField = ReflectionUtils.findField(LocalDateTime.class, "iLocalMillis");
		miliField.setAccessible(true);
	}

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (value == null) {
			return;
		}

		Date date = (Date) value;

		long numericDate = (long) ReflectionUtils.getField(miliField,
				new LocalDateTime(date.getTime()).withTime(0, 0, 0, 0));
		luceneOptions.addNumericFieldToDocument(name, numericDate, document);

	}


}
