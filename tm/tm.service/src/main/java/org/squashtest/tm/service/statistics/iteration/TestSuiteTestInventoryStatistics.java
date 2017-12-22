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
package org.squashtest.tm.service.statistics.iteration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.planning.StandardWorkloadCalendar;
import org.squashtest.tm.domain.planning.WorkloadCalendar;

public class TestSuiteTestInventoryStatistics {

	private String testsuiteName = "";
	private Date scheduledStart;
	private Date scheduledEnd;

	private Map<ExecutionStatus, Integer> statusesNb;
	private int nbVeryHigh = 0;
	private int nbHigh = 0;
	private int nbMedium = 0;
	private int nbLow = 0;

	public TestSuiteTestInventoryStatistics() {
		initStatusesNb();
	}

	private void initStatusesNb() {
		statusesNb = new HashMap<>(ExecutionStatus.getCanonicalStatusSet().size());
		for (ExecutionStatus status : ExecutionStatus.getCanonicalStatusSet()) {
			statusesNb.put(status, 0);
		}
		statusesNb.put(ExecutionStatus.SETTLED, 0);
		statusesNb.put(ExecutionStatus.UNTESTABLE, 0);
	}

	public String getTestsuiteName() {
		return testsuiteName;
	}

	public void setTestsuiteName(String testsuiteName) {
		this.testsuiteName = testsuiteName;
	}

	public int getNbTotal() {
		int tot = 0;
		for (Entry<ExecutionStatus, Integer> statusNb : statusesNb.entrySet()) {
			tot += statusNb.getValue();
		}
		return tot;
	}

	public int getNbToExecute() {
		return getNbReady() + getNbRunning();
	}

	public int getNbExecuted() {
		return getNbSuccess() + getNbFailure() + getNbBlocked() + getNbUntestable() + getNbSettled();
	}

	public int getNbReady() {
		return statusesNb.get(ExecutionStatus.READY);
	}

	public int getNbRunning() {
		return statusesNb.get(ExecutionStatus.RUNNING);
	}

	public int getNbSuccess() {
		return statusesNb.get(ExecutionStatus.SUCCESS);
	}

	public int getNbSettled() {
		return statusesNb.get(ExecutionStatus.SETTLED);
	}

	public int getNbFailure() {
		return statusesNb.get(ExecutionStatus.FAILURE);
	}

	public int getNbBlocked() {
		return statusesNb.get(ExecutionStatus.BLOCKED);
	}

	public int getNbUntestable() {
		return statusesNb.get(ExecutionStatus.UNTESTABLE);
	}

	public float getPcProgress() {
		return Math.round((float) getNbExecuted() / (float) getNbTotal() * 10000) / (float) 100;
	}

	public float getPcSuccess() {
		return Math.round((float) (getNbSuccess() + getNbSettled()) / (float) getNbExecuted() * 10000) / (float) 100;
	}

	public float getPcFailure() {
		return Math.round((float) getNbFailure() / (float) getNbExecuted() * 10000) / (float) 100;
	}

	public float getPcPrevProgress() {
            float nbToExecuteToDate = nbOfTestsToExecuteToDate(scheduledStart, scheduledEnd, new Date(), getNbTotal());
            // next line : SONAR says that's how you compare floats with 0.0f
            if ( Float.floatToRawIntBits(nbToExecuteToDate) == 0) {
                return getPcProgress();
            } else {
                return Math.round(getNbExecuted() / nbOfTestsToExecuteToDate(scheduledStart, scheduledEnd,
                        new Date(), getNbTotal()) * 10000) / (float) 100;
            }
            
        }

	public int getNbPrevToExecute() {
		return (int) nbOfTestsToExecuteToDate(scheduledStart, scheduledEnd, new Date(), getNbTotal()) - getNbExecuted();
	}

	public int getNbVeryHigh() {
		return nbVeryHigh;
	}

	public void addNbVeryHigh(int nbVeryHigh) {
		this.nbVeryHigh += nbVeryHigh;
	}

	public int getNbHigh() {
		return nbHigh;
	}

	public void addNbHigh(int nbHigh) {
		this.nbHigh += nbHigh;
	}

	public int getNbMedium() {
		return nbMedium;
	}

	public void addNbMedium(int nbMedium) {
		this.nbMedium += nbMedium;
	}

	public int getNbLow() {
		return nbLow;
	}

	public void addNbLow(int nbLow) {
		this.nbLow += nbLow;
	}
        
	private float nbOfTestsToExecuteToDate(Date scheduledStart, Date scheduledEnd, Date currentDate, int nbTests) {

		float result;

		// if current date is before the start of the previsional schedule
		if (scheduledStart == null || scheduledEnd == null || currentDate.before(scheduledStart)) {
			result = 0.00f;
			// if current date is after the end of the execution schedule
		} else if (currentDate.after(scheduledEnd)) {
			result = nbTests;
		} else {

			// Get total number of business days
			WorkloadCalendar workloadCalendar = new StandardWorkloadCalendar();
			float totalNumberOfBusinessDays = workloadCalendar.getWorkload(scheduledStart, scheduledEnd);

			// Get number of open days before current date
			float numberOfSpentBusinessDays = workloadCalendar.getWorkload(scheduledStart, currentDate);

			// Compute percentage of already spent time
			float spentTime = numberOfSpentBusinessDays / totalNumberOfBusinessDays;

			result = nbTests * spentTime;
		}
		return result;
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

	public void addNumber(int nb, ExecutionStatus status) {
		Integer thisNb = statusesNb.get(status);
		if (thisNb != null) {
			thisNb += nb;
			statusesNb.put(status, thisNb);
		} else {
			statusesNb.put(status, nb);
		}
	}
}
