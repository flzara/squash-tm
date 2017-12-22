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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.squashtest.tm.domain.planning.StandardWorkloadCalendar;
import org.squashtest.tm.domain.planning.WorkloadCalendar;


public final class ScheduledIteration{

	public static final String SCHED_ITER_NO_ITERATIONS_I18N = "dashboard.campaigns.progression.errors.nodata";
	public static final String SCHED_ITER_MISSING_DATES_I18N = "dashboard.campaigns.progression.errors.nulldates";
	public static final String SCHED_ITER_OVERLAP_DATES_I18N = "dashboard.campaigns.progression.errors.overlap";
	public static final String LONE_ITERATION_MISSING_DATES_I18N = "dashboard.iteration.progression.errors.nulldates";

	private long id;
	private String name;
	private long testplanCount;
	private Date scheduledStart;
	private Date scheduledEnd;

	// an entry = { Date, int }
	private Collection<Object[]> cumulativeTestsByDate = new LinkedList<>();

	public ScheduledIteration(){
		super();
	}


	public ScheduledIteration(long id, String name, long testplanCount,
			Date scheduledStart, Date scheduledEnd) {
		super();
		this.id = id;
		this.name = name;
		this.testplanCount = testplanCount;
		this.scheduledStart = scheduledStart;
		this.scheduledEnd = scheduledEnd;
	}



	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public long getTestplanCount() {
		return testplanCount;
	}

	public void setTestplanCount(long testplanCount) {
		this.testplanCount = testplanCount;
	}

	public Date getScheduledStart() {
		return scheduledStart;
	}

	public void setScheduledStart(Date scheduledStart) {
		this.scheduledStart = scheduledStart;
	}

	public Date getScheduledEnd() {
		return scheduledEnd;
	}

	public void setScheduledEnd(Date scheduledEnd) {
		this.scheduledEnd = scheduledEnd;
	}

	public Collection<Object[]> getCumulativeTestsByDate() {
		return cumulativeTestsByDate;
	}

	public void addCumulativeTestByDate(Object[] testByDate) {
		cumulativeTestsByDate.add(testByDate);
	}


	/**
	 * Will fill the informations of field cumulativeTestsByDate. Basically it means the cumulative average number of test that must be run
	 * within the scheduled time period per day, according to their workload.
	 */
	public void computeCumulativeTestByDate(float initialCumulativeTests){

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(scheduledStart);
		WorkloadCalendar workloadCalendar = new StandardWorkloadCalendar();

		float workload = workloadCalendar.getWorkload(scheduledStart, scheduledEnd);
		float incrementPerDay = testplanCount / workload;

		// ready to iterate
		Date curDate = scheduledStart;
		float cumulativeTests = initialCumulativeTests;
		do{
			cumulativeTests += workloadCalendar.getWorkload(curDate) * incrementPerDay;
			cumulativeTestsByDate.add(new Object[]{curDate, cumulativeTests});

			calendar.add(Calendar.DAY_OF_YEAR, 1);
			curDate = calendar.getTime();
		}
		while(! curDate.after(scheduledEnd));


	}


	// ********************** static part *************************

	public static void checkIterationDatesIntegrity(ScheduledIteration iteration){
		Date start = iteration.scheduledStart;
		Date end = iteration.scheduledEnd;

		if (start == null || end == null){
			throw new IllegalArgumentException(SCHED_ITER_MISSING_DATES_I18N);
		}
	}

	public static void checkIterationDatesAreSet(ScheduledIteration iteration){
		Date start = iteration.scheduledStart;
		Date end = iteration.scheduledEnd;

		if (start == null || end == null){
			throw new IllegalArgumentException(LONE_ITERATION_MISSING_DATES_I18N);
		}
	}

	public static void checkIterationsDatesIntegrity(Collection<ScheduledIteration> iterations){

		Date prevEnd = null;
		Date start;
		Date end;

		if (iterations.isEmpty()){
			throw new IllegalArgumentException(SCHED_ITER_NO_ITERATIONS_I18N);
		}

		for (ScheduledIteration iter : iterations){

			start = iter.scheduledStart;
			end = iter.scheduledEnd;

			if (start == null || end == null){
				throw new IllegalArgumentException(SCHED_ITER_MISSING_DATES_I18N);
			}

			if (end.before(start)){
				throw new IllegalArgumentException(SCHED_ITER_OVERLAP_DATES_I18N);
			}

			if (prevEnd != null && ! start.after(prevEnd)){
				throw new IllegalArgumentException(SCHED_ITER_OVERLAP_DATES_I18N);
			}

			prevEnd = end;
		}
	}

}
