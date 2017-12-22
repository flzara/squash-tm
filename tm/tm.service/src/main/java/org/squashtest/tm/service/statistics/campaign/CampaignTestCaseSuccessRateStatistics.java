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

public class CampaignTestCaseSuccessRateStatistics {

	private Map<ExecutionStatus, Integer> testsOfLowImportance = new EnumMap<>(ExecutionStatus.class);
	private Map<ExecutionStatus, Integer> testsOfMediumImportance = new EnumMap<>(ExecutionStatus.class);
	private Map<ExecutionStatus, Integer> testsOfHighImportance = new EnumMap<>(ExecutionStatus.class);
	private Map<ExecutionStatus, Integer> testsOfVeryHighImportance = new EnumMap<>(ExecutionStatus.class);

	public void addNbLow(ExecutionStatus status, int number){

		if(!this.testsOfLowImportance.containsKey(status)){
			this.testsOfLowImportance.put(status, 0);
		}

		Integer nb = this.testsOfLowImportance.get(status);
		nb += number;
		this.testsOfLowImportance.put(status, nb);
	}

	public void addNbMedium(ExecutionStatus status, int number){

		if(!this.testsOfMediumImportance.containsKey(status)){
			this.testsOfMediumImportance.put(status, 0);
		}

		Integer nb = this.testsOfMediumImportance.get(status);
		nb += number;
		this.testsOfMediumImportance.put(status, nb);
	}

	public void addNbHigh(ExecutionStatus status, int number){

		if(!this.testsOfHighImportance.containsKey(status)){
			this.testsOfHighImportance.put(status, 0);
		}

		Integer nb = this.testsOfHighImportance.get(status);
		nb += number;
		this.testsOfHighImportance.put(status, nb);

	}

	public void addNbVeryHigh(ExecutionStatus status, int number){

		if(!this.testsOfVeryHighImportance.containsKey(status)){
			this.testsOfVeryHighImportance.put(status, 0);
		}

		Integer nb = this.testsOfVeryHighImportance.get(status);
		nb += number;
		this.testsOfVeryHighImportance.put(status, nb);

	}

	public int getNbVeryHighSuccess() {
		return getValue(testsOfVeryHighImportance, ExecutionStatus.SUCCESS)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.WARNING)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.SETTLED);
	}
	public int getNbHighSuccess() {
		return getValue(testsOfHighImportance, ExecutionStatus.SUCCESS)+
				getValue(testsOfHighImportance, ExecutionStatus.WARNING)+
				getValue(testsOfHighImportance, ExecutionStatus.SETTLED);
	}
	public int getNbMediumSuccess() {
		return getValue(testsOfMediumImportance, ExecutionStatus.SUCCESS)+
				getValue(testsOfMediumImportance, ExecutionStatus.WARNING)+
				getValue(testsOfMediumImportance, ExecutionStatus.SETTLED);
	}
	public int getNbLowSuccess() {
		return getValue(testsOfLowImportance, ExecutionStatus.SUCCESS)+
				getValue(testsOfLowImportance, ExecutionStatus.WARNING)+
				getValue(testsOfLowImportance, ExecutionStatus.SETTLED);
	}
	public int getNbVeryHighFailure() {
		return getValue(testsOfVeryHighImportance, ExecutionStatus.FAILURE)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.ERROR);
	}
	public int getNbHighFailure() {
		return getValue(testsOfHighImportance, ExecutionStatus.FAILURE)+
				getValue(testsOfHighImportance, ExecutionStatus.ERROR);
	}
	public int getNbMediumFailure() {
		return getValue(testsOfMediumImportance, ExecutionStatus.FAILURE)+
				getValue(testsOfMediumImportance, ExecutionStatus.ERROR);
	}
	public int getNbLowFailure() {
		return getValue(testsOfLowImportance, ExecutionStatus.FAILURE)+
				getValue(testsOfLowImportance, ExecutionStatus.ERROR)
				;
	}
	public int getNbVeryHighOther() {
		return getValue(testsOfVeryHighImportance, ExecutionStatus.BLOCKED)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.UNTESTABLE)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.NOT_RUN)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.NOT_FOUND);
	}
	public int getNbHighOther() {
		return getValue(testsOfHighImportance, ExecutionStatus.BLOCKED)+
				getValue(testsOfHighImportance, ExecutionStatus.UNTESTABLE)+
				getValue(testsOfHighImportance, ExecutionStatus.NOT_RUN)+
				getValue(testsOfHighImportance, ExecutionStatus.NOT_FOUND);
	}
	public int getNbMediumOther() {
		return getValue(testsOfMediumImportance, ExecutionStatus.BLOCKED)+
				getValue(testsOfMediumImportance, ExecutionStatus.UNTESTABLE)+
				getValue(testsOfMediumImportance, ExecutionStatus.NOT_RUN)+
				getValue(testsOfMediumImportance, ExecutionStatus.NOT_FOUND);
	}
	public int getNbLowOther() {
		return getValue(testsOfLowImportance, ExecutionStatus.BLOCKED)+
				getValue(testsOfLowImportance, ExecutionStatus.UNTESTABLE)+
				getValue(testsOfLowImportance, ExecutionStatus.NOT_RUN)+
				getValue(testsOfLowImportance, ExecutionStatus.NOT_FOUND);
	}
	public int getNbVeryHighExecuted() {
		return getValue(testsOfVeryHighImportance, ExecutionStatus.BLOCKED)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.UNTESTABLE)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.SUCCESS)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.WARNING)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.SETTLED)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.FAILURE)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.ERROR)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.NOT_RUN)+
				getValue(testsOfVeryHighImportance, ExecutionStatus.NOT_FOUND);
	}
	public int getNbHighExecuted() {
		return getValue(testsOfHighImportance, ExecutionStatus.BLOCKED)+
				getValue(testsOfHighImportance, ExecutionStatus.UNTESTABLE)+
				getValue(testsOfHighImportance, ExecutionStatus.SUCCESS)+
				getValue(testsOfHighImportance, ExecutionStatus.WARNING)+
				getValue(testsOfHighImportance, ExecutionStatus.SETTLED)+
				getValue(testsOfHighImportance, ExecutionStatus.FAILURE)+
				getValue(testsOfHighImportance, ExecutionStatus.ERROR)+
				getValue(testsOfHighImportance, ExecutionStatus.NOT_RUN)+
				getValue(testsOfHighImportance, ExecutionStatus.NOT_FOUND);
	}
	public int getNbMediumExecuted() {
		return getValue(testsOfMediumImportance, ExecutionStatus.BLOCKED)+
				getValue(testsOfMediumImportance, ExecutionStatus.UNTESTABLE)+
				getValue(testsOfMediumImportance, ExecutionStatus.SUCCESS)+
				getValue(testsOfMediumImportance, ExecutionStatus.WARNING)+
				getValue(testsOfMediumImportance, ExecutionStatus.SETTLED)+
				getValue(testsOfMediumImportance, ExecutionStatus.FAILURE)+
				getValue(testsOfMediumImportance, ExecutionStatus.ERROR)+
				getValue(testsOfMediumImportance, ExecutionStatus.NOT_RUN)+
				getValue(testsOfMediumImportance, ExecutionStatus.NOT_FOUND);
	}
	public int getNbLowExecuted() {
		return getValue(testsOfLowImportance, ExecutionStatus.BLOCKED)+
				getValue(testsOfLowImportance, ExecutionStatus.UNTESTABLE)+
				getValue(testsOfLowImportance, ExecutionStatus.SUCCESS)+
				getValue(testsOfLowImportance, ExecutionStatus.WARNING)+
				getValue(testsOfLowImportance, ExecutionStatus.SETTLED)+
				getValue(testsOfLowImportance, ExecutionStatus.FAILURE)+
				getValue(testsOfLowImportance, ExecutionStatus.ERROR)+
				getValue(testsOfLowImportance, ExecutionStatus.NOT_RUN)+
				getValue(testsOfLowImportance, ExecutionStatus.NOT_FOUND);
	}
	public int getValue(Map<ExecutionStatus, Integer> map, ExecutionStatus status){
		if(map.containsKey(status)){
			return map.get(status);
		} else {
			return 0;
		}
	}
}
