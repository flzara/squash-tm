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
package org.squashtest.tm.domain.requirement;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class used for Feat 5433 and 5434, Show cover, verification and validation rate
 * @author jthebault
 *
 */
public class RequirementCoverageStat {

	/**
	 * Used to mark if the perimeter(persisted in localstorage of each user) is referencing a suppressed Campaign or Iteration. 
	 */
	private boolean corruptedPerimeter = false;
	
	private boolean ancestor = false;
	
	private Map<String,Rate> rates = new HashMap<>();

	public RequirementCoverageStat() {
	}

	public boolean isAncestor() {
		return ancestor;
	}

	public Map<String, Rate> getRates() {
		return rates;
	}

	public void setRates(Map<String, Rate> rates) {
		this.rates = rates;
	}

	public void setAncestor(boolean ancestor) {
		this.ancestor = ancestor;
	}

	public static class Rate {
		//copy the boolean for convenience with Handlebar templating when iterating on rates
		private boolean ancestor = false;
		private double requirementVersionRate;
		private double requirementVersionChildrenRate;
		private double requirementVersionGlobalRate;
		
		public Rate() {
			// TODO Auto-generated constructor stub
		}

		public boolean isAncestor() {
			return ancestor;
		}

		public void setAncestor(boolean ancestor) {
			this.ancestor = ancestor;
		}

		public double getRequirementVersionRate() {
			return requirementVersionRate;
		}

		public void setRequirementVersionRate(double requirementVersionRate) {
			this.requirementVersionRate = requirementVersionRate;
		}

		public double getRequirementVersionChildrenRate() {
			return requirementVersionChildrenRate;
		}

		public void setRequirementVersionChildrenRate(
				double requirementVersionChildrenRate) {
			this.requirementVersionChildrenRate = requirementVersionChildrenRate;
		}

		public double getRequirementVersionGlobalRate() {
			return requirementVersionGlobalRate;
		}

		public void setRequirementVersionGlobalRate(double requirementVersionGlobalRate) {
			this.requirementVersionGlobalRate = requirementVersionGlobalRate;
		}
		
		public void convertToPercent(){
			this.requirementVersionRate = convertOneRateToPercent(requirementVersionRate);
			this.requirementVersionChildrenRate = convertOneRateToPercent(requirementVersionChildrenRate);
			this.requirementVersionGlobalRate = convertOneRateToPercent(requirementVersionGlobalRate);
		}
		
		private double convertOneRateToPercent(double rate){
			return Math.round(rate * 100);
		};
	}

	public void addRate(String key, Rate coverageRate) {
		rates.put(key, coverageRate);
	}
	
	public void convertRatesToPercent(){
		for (Rate rate : rates.values()) {
			rate.convertToPercent();
		}
	}

	/**
	 * @return the corruptedPerimeter
	 */
	public boolean isCorruptedPerimeter() {
		return corruptedPerimeter;
	}

	/**
	 * @param corruptedPerimeter the corruptedPerimeter to set
	 */
	public void setCorruptedPerimeter(boolean corruptedPerimeter) {
		this.corruptedPerimeter = corruptedPerimeter;
	};
}
