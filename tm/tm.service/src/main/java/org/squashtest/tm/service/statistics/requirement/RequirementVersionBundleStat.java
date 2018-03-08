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
package org.squashtest.tm.service.statistics.requirement;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class RequirementVersionBundleStat {

	Map<Long, SimpleRequirementStats> requirementStats = new HashMap<>();

	public Map<Long, SimpleRequirementStats> getRequirementStats() {
		return requirementStats;
	}

	private SimpleRequirementStats getSimpleStats(Long reqId) {
		SimpleRequirementStats rates;
		if(requirementStats.containsKey(reqId)){
			rates = requirementStats.get(reqId);
		} else {
			rates = new SimpleRequirementStats(reqId);
			requirementStats.put(reqId, rates);
		}
		return rates;
	}

	public void computeRate(Long reqId , String key, Integer countAll, Integer countMatching) {
		SimpleRequirementStats stats = getSimpleStats(reqId);
		double rate = calculateRate(countAll, countMatching);
		stats.setRate(key, rate, countAll, countMatching);
	}

	private double calculateRate(Integer countAll, Integer countMatching) {
		double rate = 0d;
		if(countAll != null && countAll != 0 && countMatching != null){
			rate = countMatching.doubleValue() / countAll.doubleValue();
		}
		rate = makeProperRoundedRate(rate);
		return rate;
	}

	private double makeProperRoundedRate(double rate) {
		rate = rate * 100;
		BigDecimal bigDecimal = BigDecimal.valueOf(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
		rate = bigDecimal.doubleValue();
		return rate;
	}

	public static class SimpleRequirementStats {
		private Long reqId;

		public static final String REDACTION_RATE_KEY = "REDACTION_RATE";
		public static final String VERIFICATION_RATE_KEY = "VERIFICATION_RATE";
		public static final String VALIDATION_RATE_KEY = "VALIDATION_RATE";

		private Map<String, StatisticData> rates = new HashMap<>();

		SimpleRequirementStats(Long reqId) {
			this.reqId = reqId;
		}

		public void setRate(String key, Double rate, Integer countAll, Integer countMatching){
			switch (key){
				case REDACTION_RATE_KEY :
				case VERIFICATION_RATE_KEY :
				case VALIDATION_RATE_KEY :
					rates.put(key, new StatisticData(countMatching,countAll,rate));
					break;
				default:
					throw new IllegalArgumentException("Programmatic error : Unknown key : " + key);
			}
		}

		public Long getReqId() {
			return reqId;
		}

		public Double getRedactionRate() {
			return getRate(REDACTION_RATE_KEY);
		}

		public Integer getAllTestCaseCount (){
			return getCount(REDACTION_RATE_KEY);
		}

		public Integer getRedactedTestCase (){
			return getMatching(REDACTION_RATE_KEY);
		}

		public Integer getVerifiedTestCase (){
			return getMatching(VERIFICATION_RATE_KEY);
		}

		public Integer getPlannedTestCase (){
			return getCount(VERIFICATION_RATE_KEY);
		}

		public Integer getValidatedTestCase (){
			return getMatching(VALIDATION_RATE_KEY);
		}

		public Integer getExecutedTestCase (){
			return getCount(VALIDATION_RATE_KEY);
		}

		private Double getRate(String rateKey) {
			if(rates.containsKey(rateKey)){
				return rates.get(rateKey).getRate();
			}
			throw throwNoDataExeception(rateKey);
		}


		private Integer getCount(String rateKey) {
			if(rates.containsKey(rateKey)){
				return rates.get(rateKey).getCountAll();
			}
			throw throwNoDataExeception(rateKey);
		}

		private Integer getMatching(String rateKey) {
			if(rates.containsKey(rateKey)){
				return rates.get(rateKey).getMatchingCount();
			}
			throw throwNoDataExeception(rateKey);
		}

		public Double getVerificationRate() {
			return getRate(VERIFICATION_RATE_KEY);
		}

		public Double getValidationRate() {
			return getRate(VALIDATION_RATE_KEY);
		}

		private IllegalArgumentException throwNoDataExeception(String rateKey) {
			return new IllegalArgumentException("You must initialize statistic bundle before get rates... no value for key " + rateKey + " in statistic map.");
		}



	}

	public static class StatisticData {
		private Integer matchingCount;
		private Integer countAll;
		private Double rate;

		StatisticData(Integer matchingCount, Integer countAll, Double rate) {
			this.matchingCount = matchingCount;
			this.countAll = countAll;
			this.rate = rate;
		}

		public Integer getMatchingCount() {
			if (matchingCount == null) {
				return 0;
			}
			return matchingCount;
		}

		public Integer getCountAll() {
			if (countAll == null) {
				return 0;
			}
			return countAll;
		}

		public Double getRate() {
			return rate;
		}


	}
}
