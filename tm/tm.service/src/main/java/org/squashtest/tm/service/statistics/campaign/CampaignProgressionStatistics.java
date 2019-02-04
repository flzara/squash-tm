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

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public final class CampaignProgressionStatistics {

	private Collection<String> i18nErrors;

	private Collection<ScheduledIteration> scheduledIterations;

	private List<Object[]> cumulativeExecutionsPerDate;

	private StatisticUtils statisticUtils = new StatisticUtils();

	public Collection<ScheduledIteration> getScheduledIterations() {
		return scheduledIterations;
	}

	public void addi18nErrorMessage(String i18nErrorMessage){
		if (i18nErrors==null){
			i18nErrors = new LinkedList<>();
		}
		i18nErrors.add(i18nErrorMessage);
	}

	public Collection<String> getErrors(){
		return i18nErrors;
	}

	public void setScheduledIterations(
			Collection<ScheduledIteration> scheduledIterations) {
		this.scheduledIterations = scheduledIterations;
	}

	public List<Object[]> getCumulativeExecutionsPerDate() {
		return cumulativeExecutionsPerDate;
	}

	public void setCumulativeExecutionsPerDate(
			List<Object[]> cumulativeExecutionsPerDate) {
		this.cumulativeExecutionsPerDate = cumulativeExecutionsPerDate;
	}

	public void computeSchedule(){
		float cumulative = 0.0f;
		for (ScheduledIteration iteration : scheduledIterations){

			iteration.computeCumulativeTestByDate(cumulative);
			cumulative += iteration.getTestplanCount();

		}
	}


	// TODO : have the db do the job for me (hint : try with 'cast as Date')
	public void computeCumulativeTestPerDate(List<Date> dates){

		// that where I'd love to have collection.fold(), instead we do the following
		setCumulativeExecutionsPerDate(statisticUtils.gatherCumulativeTestsPerDate(dates));


	}


}
