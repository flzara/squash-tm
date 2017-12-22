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
package org.squashtest.tm.domain.planning;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

public class StandardWorkloadCalendar implements WorkloadCalendar {

	private static final float BUSINESS_DAY_WORKLOAD = 1.0f;
	private static final float WEEKEND_DAY_WORKLOAD = 0.0f;

	@Override
	public float getWorkload(Date date) {
		return isWeekend(date) ? WEEKEND_DAY_WORKLOAD : BUSINESS_DAY_WORKLOAD;
	}

	@Override
	public float getWorkload(Date start, Date end){
		return getWorkload(new LocalDate(start), new LocalDate(end));
	}



	/*
	 * We use Joda time, that uses the ISO 8601 format. As such sunday is the last day of a week, not the first day.
	 */
	public float getWorkload(LocalDate start, LocalDate end){

		if (end.isBefore(start)){
			throw new IllegalArgumentException("dashboard.error.date");
		}

		LocalDate lstart = skipWeekendToMonday(start);
		LocalDate lend = truncateWeekendToLastFriday(end);

		// the following arises iif both days where in the weekend of the same week
		if (lend.isBefore(lstart)){
			return Days.daysBetween(start, end).getDays() * WEEKEND_DAY_WORKLOAD;
		}

		int daysbetween = Days.daysBetween(lstart, lend).getDays() +1;
		int adjustedDaysbetween = daysbetween + lstart.getDayOfWeek()-1;
		int nbWeekend = adjustedDaysbetween / 7;
		int nbweekdays = daysbetween - nbWeekend*2;

		return nbweekdays * BUSINESS_DAY_WORKLOAD;

	}



	private boolean isWeekend(Date date){
		Calendar c = Calendar.getInstance();	// XXX thread safety ?
		c.setTime(date);
		int day = c.get(Calendar.DAY_OF_WEEK) ;
		return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
	}

	private boolean isWeekend(LocalDate date){
		return date.getDayOfWeek() == DateTimeConstants.SATURDAY || date.getDayOfWeek() == DateTimeConstants.SUNDAY;
	}


	private LocalDate skipWeekendToMonday(LocalDate date){
		if (isWeekend(date)){
			return date.plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY);
		}
		else{
			return date;
		}
	}

	private LocalDate truncateWeekendToLastFriday(LocalDate date){
		if (isWeekend(date)){
			return date.withDayOfWeek(DateTimeConstants.FRIDAY);
		}
		else{
			return date;
		}
	}

}
