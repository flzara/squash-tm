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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.squashtest.tm.domain.execution.ExecutionStatus;


public class CampaignTestCaseStatusStatistics {


	private Map<ExecutionStatus, Integer> statistics ;
	public CampaignTestCaseStatusStatistics() {
		initStatistics();
	}

	private void initStatistics() {
		statistics = new EnumMap<>(ExecutionStatus.class);
		for(ExecutionStatus status: ExecutionStatus.getCanonicalStatusSet()){
			statistics.put(status, 0);
		}
		statistics.put(ExecutionStatus.UNTESTABLE, 0);
		statistics.put(ExecutionStatus.SETTLED, 0);

	}

	public void addNumber(int nb, ExecutionStatus status){
		Integer thisNb = statistics.get(status);
		if(thisNb != null){
			thisNb += nb;
			statistics.put(status, thisNb);
		}else{
			statistics.put(status, nb);
		}
	}

	public int getNbReady() {
		return statistics.get(ExecutionStatus.READY);
	}

	public int getNbRunning() {
		return statistics.get(ExecutionStatus.RUNNING);
	}

	public int getNbSuccess() {
		return statistics.get(ExecutionStatus.SUCCESS);
	}

	public int getNbFailure() {
		return statistics.get(ExecutionStatus.FAILURE);
	}

	public int getNbBlocked() {
		return statistics.get(ExecutionStatus.BLOCKED);
	}

	public int getNbUntestable() {
		return statistics.get(ExecutionStatus.UNTESTABLE);
	}

	public int getNbSettled() {
		return statistics.get(ExecutionStatus.SETTLED);
	}


}
