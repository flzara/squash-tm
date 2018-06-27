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
package org.squashtest.tm.service.statistics.campaign;

import org.apache.commons.lang3.time.DateUtils;

import java.util.*;

public final class StatisticUtils {

	public StatisticUtils() {
		//NOOP
	}

	public List<Object[]> gatherCumulativeTestsPerDate(List<Date> dates) {
		List<Object[]> cumulativeTestsPerDate = new LinkedList<>();
		if (!dates.isEmpty()) {
			// we use here a modified list with a dummy element at the end that will
			// help us to work around a corner case : handling the last element of the loop
			List<Date> trickedDates = new LinkedList<>(dates);
			trickedDates.add(null);

			Iterator<Date> dateIter = trickedDates.iterator();
			Date precDate = dateIter.next();
			Date curDate;
			Date truncated;
			int accumulator = 1;

			// iterate over the rest. Remember that the last element is a dummy null value
			while (dateIter.hasNext()) {
				curDate = dateIter.next();
				if (! isSameDay(precDate, curDate)) {
					truncated = DateUtils.truncate(precDate, Calendar.DATE);
					cumulativeTestsPerDate.add(new Object[]{truncated, accumulator});
				}
				accumulator++;
				precDate = curDate;
			}
		}
		return cumulativeTestsPerDate;


	}

	private boolean isSameDay(Date d1, Date d2){
		if (d1==null || d2 == null){
			return false;
		}
		else{
			return DateUtils.isSameDay(d1,  d2);
		}
	}

}
