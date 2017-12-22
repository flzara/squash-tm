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
package org.squashtest.tm.internal.domain.report.common.dto;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.squashtest.tm.domain.requirement.RequirementStatus;

public class ReqCoverageByTestProjectDto implements HasMilestoneLabel{

	private String milestone;

	/***
	 * Name of the project
	 */
	private String projectName;

	/***
	 * List of all requirement
	 */
	private List<ReqCoverageByTestRequirementSingleDto> singleRequirementList = new ArrayList<>();

	/****************************/
	/** STATISTICS **/
	/****************************/

	private EnumMap<ReqCoverageByTestStatType, Long> requirementNumbers = new EnumMap<>(ReqCoverageByTestStatType.class);
	private Map<String, Long> requirementStatusNumbers = new HashMap<>();


	/* RATES */
	private Byte globalRequirementCoverage;

	// by criticality
	private Byte criticalRequirementCoverage;

	private Byte majorRequirementCoverage;

	private Byte minorRequirementCoverage;

	private Byte undefinedRequirementCoverage;

	/* Work in progress RATES */
	private Byte workInProgressGlobalRequirementCoverage;

	// by criticality
	private Byte workInProgressCriticalRequirementCoverage;

	private Byte workInProgressMajorRequirementCoverage;

	private Byte workInProgressMinorRequirementCoverage;

	private Byte workInProgressUndefinedRequirementCoverage;

	/* Under review RATES */
	private Byte underReviewGlobalRequirementCoverage;

	// by criticality
	private Byte underReviewCriticalRequirementCoverage;

	private Byte underReviewMajorRequirementCoverage;

	private Byte underReviewMinorRequirementCoverage;

	private Byte underReviewUndefinedRequirementCoverage;

	/* approved RATES */
	private Byte approvedGlobalRequirementCoverage;

	// by criticality
	private Byte approvedCriticalRequirementCoverage;

	private Byte approvedMajorRequirementCoverage;

	private Byte approvedMinorRequirementCoverage;

	private Byte approvedUndefinedRequirementCoverage;

	/* obsolete RATES */
	private Byte obsoleteGlobalRequirementCoverage;

	// by criticality
	private Byte obsoleteCriticalRequirementCoverage;

	private Byte obsoleteMajorRequirementCoverage;

	private Byte obsoleteMinorRequirementCoverage;

	private Byte obsoleteUndefinedRequirementCoverage;


	public ReqCoverageByTestProjectDto() {
		for (ReqCoverageByTestStatType reqStatType : ReqCoverageByTestStatType.values()) {
			requirementNumbers.put(reqStatType, 0L);
		}

		for (RequirementStatus status : RequirementStatus.values()) {
			for (ReqCoverageByTestStatType reqStatType : ReqCoverageByTestStatType.values()) {
				String key = status.toString() + reqStatType.toString();
				requirementStatusNumbers.put(key, 0L);
			}
		}
	}
	
	
	/**
	 * Increments the number identified by the ReqCoverageByTestStatType
	 *
	 * @param type
	 *            identify the reqNumber type to increment
	 */
	public void incrementReqNumber(ReqCoverageByTestStatType type) {
		Long number = requirementNumbers.get(type);
		number++;
		requirementNumbers.put(type, number);
	}

	/**
	 * Increments the number identified by the String status+reqCoverageByTestStartType
	 *
	 * @param key
	 *            the String status+reqCoverageByTestStartType
	 */
	public void incrementReqStatusNumber(String key) {
		Long number = requirementStatusNumbers.get(key);
		number++;
		requirementStatusNumbers.put(key, number);
	}

	/**
	 *
	 * Method which add the given values to the totals
	 *
	 * @param requirementNumbers
	 *            of the project that will increase the projectTotals numbers
	 */
	public void increaseTotals(Map<ReqCoverageByTestStatType, Long> requirementNumbers2,
			Map<String, Long> requirementStatusNumbers2) {
		for (Entry<ReqCoverageByTestStatType, Long> parameterEntry : requirementNumbers2.entrySet()) {
			ReqCoverageByTestStatType concernedType = parameterEntry.getKey();
			Long reqNumber = this.requirementNumbers.get(concernedType);
			reqNumber += parameterEntry.getValue();
			this.requirementNumbers.put(concernedType, reqNumber);
		}
		for (Entry<String, Long> parameterEntry : requirementStatusNumbers2.entrySet()) {
			String concernedType = parameterEntry.getKey();
			Long reqNumber = this.requirementStatusNumbers.get(concernedType);
			reqNumber += parameterEntry.getValue();
			this.requirementStatusNumbers.put(concernedType, reqNumber);
		}

	}

	/* ACCESSORS */

	public List<ReqCoverageByTestRequirementSingleDto> getSingleRequirementList() {
		return singleRequirementList;
	}

	public void addRequirement(ReqCoverageByTestRequirementSingleDto requirementSingleDto) {
		this.singleRequirementList.add(requirementSingleDto);
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public Long getTotalRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.TOTAL);
	}

	public Long getTotalVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.TOTAL_VERIFIED);
	}

	public Long getCriticalRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.CRITICAL);
	}

	public Long getCriticalVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.CRITICAL_VERIFIED);
	}

	public Long getMajorRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.MAJOR);
	}

	public Long getMajorVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.MAJOR_VERIFIED);
	}

	public Long getMinorRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.MINOR);
	}

	public Long getMinorVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.MINOR_VERIFIED);
	}

	public Long getUndefinedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.UNDEFINED);
	}

	public Long getUndefinedVerifiedRequirementNumber() {
		return requirementNumbers.get(ReqCoverageByTestStatType.UNDEFINED_VERIFIED);
	}

	public Byte getGlobalRequirementCoverage() {
		return globalRequirementCoverage;
	}

	public Byte getCriticalRequirementCoverage() {
		return criticalRequirementCoverage;
	}

	public Byte getMajorRequirementCoverage() {
		return majorRequirementCoverage;
	}

	public Byte getMinorRequirementCoverage() {
		return minorRequirementCoverage;
	}

	public Byte getUndefinedRequirementCoverage() {
		return undefinedRequirementCoverage;
	}

	public void setGlobalRequirementCoverage(Byte globalRequirementCoverage) {
		this.globalRequirementCoverage = globalRequirementCoverage;
	}

	public void setCriticalRequirementCoverage(Byte criticalRequirementCoverage) {
		this.criticalRequirementCoverage = criticalRequirementCoverage;
	}

	public void setMajorRequirementCoverage(Byte majorRequirementCoverage) {
		this.majorRequirementCoverage = majorRequirementCoverage;
	}

	public void setMinorRequirementCoverage(Byte minorRequirementCoverage) {
		this.minorRequirementCoverage = minorRequirementCoverage;
	}

	public void setUndefinedRequirementCoverage(Byte undefinedRequirementCoverage) {
		this.undefinedRequirementCoverage = undefinedRequirementCoverage;
	}

	public Map<ReqCoverageByTestStatType, Long> getRequirementNumbers() {
		return requirementNumbers;
	}

	public void setRequirementNumbers(EnumMap<ReqCoverageByTestStatType, Long> requirementNumbers) {
		this.requirementNumbers = requirementNumbers;
	}

	public Long getWorkInProgressTotalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
	}

	public Long getWorkInProgressTotalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
	}

	public Long getWorkInProgressCriticalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.CRITICAL.toString());
	}

	public Long getWorkInProgressCriticalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.CRITICAL_VERIFIED.toString());
	}

	public Long getWorkInProgressMajorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.MAJOR.toString());
	}

	public Long getWorkInProgressMajorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.MAJOR_VERIFIED.toString());
	}

	public Long getWorkInProgressMinorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.MINOR.toString());
	}

	public Long getWorkInProgressMinorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.MINOR_VERIFIED.toString());
	}

	public Long getWorkInProgressUndefinedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.UNDEFINED.toString());
	}

	public Long getWorkInProgressUndefinedVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.WORK_IN_PROGRESS.toString()
				+ ReqCoverageByTestStatType.UNDEFINED_VERIFIED.toString());
	}

	public Long getUnderReviewTotalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
	}

	public Long getUnderReviewTotalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
	}

	public Long getUnderReviewCriticalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.CRITICAL.toString());
	}

	public Long getUnderReviewCriticalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.CRITICAL_VERIFIED.toString());
	}

	public Long getUnderReviewMajorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.MAJOR.toString());
	}

	public Long getUnderReviewMajorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.MAJOR_VERIFIED.toString());
	}

	public Long getUnderReviewMinorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.MINOR.toString());
	}

	public Long getUnderReviewMinorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.MINOR_VERIFIED.toString());
	}

	public Long getUnderReviewUndefinedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.UNDEFINED.toString());
	}

	public Long getUnderReviewUndefinedVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.UNDER_REVIEW.toString()
				+ ReqCoverageByTestStatType.UNDEFINED_VERIFIED.toString());
	}

	public Long getApprovedTotalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
	}

	public Long getApprovedTotalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
	}

	public Long getApprovedCriticalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.CRITICAL.toString());
	}

	public Long getApprovedCriticalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.CRITICAL_VERIFIED.toString());
	}

	public Long getApprovedMajorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.MAJOR.toString());
	}

	public Long getApprovedMajorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.MAJOR_VERIFIED.toString());
	}

	public Long getApprovedMinorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.MINOR.toString());
	}

	public Long getApprovedMinorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.MINOR_VERIFIED.toString());
	}

	public Long getApprovedUndefinedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.UNDEFINED.toString());
	}

	public Long getApprovedUndefinedVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.APPROVED.toString()
				+ ReqCoverageByTestStatType.UNDEFINED_VERIFIED.toString());
	}

	public Long getObsoleteTotalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.TOTAL.toString());
	}

	public Long getObsoleteTotalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.TOTAL_VERIFIED.toString());
	}

	public Long getObsoleteCriticalRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.CRITICAL.toString());
	}

	public Long getObsoleteCriticalVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.CRITICAL_VERIFIED.toString());
	}

	public Long getObsoleteMajorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.MAJOR.toString());
	}

	public Long getObsoleteMajorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.MAJOR_VERIFIED.toString());
	}

	public Long getObsoleteMinorRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.MINOR.toString());
	}

	public Long getObsoleteMinorVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.MINOR_VERIFIED.toString());
	}

	public Long getObsoleteUndefinedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.UNDEFINED.toString());
	}

	public Long getObsoleteUndefinedVerifiedRequirementNumber() {
		return requirementStatusNumbers.get(RequirementStatus.OBSOLETE.toString()
				+ ReqCoverageByTestStatType.UNDEFINED_VERIFIED.toString());
	}

	public Byte getWorkInProgressGlobalRequirementCoverage() {
		return workInProgressGlobalRequirementCoverage;
	}

	public Byte getWorkInProgressCriticalRequirementCoverage() {
		return workInProgressCriticalRequirementCoverage;
	}

	public Byte getWorkInProgressMajorRequirementCoverage() {
		return workInProgressMajorRequirementCoverage;
	}

	public Byte getWorkInProgressMinorRequirementCoverage() {
		return workInProgressMinorRequirementCoverage;
	}

	public Byte getWorkInProgressUndefinedRequirementCoverage() {
		return workInProgressUndefinedRequirementCoverage;
	}

	public Byte getUnderReviewGlobalRequirementCoverage() {
		return underReviewGlobalRequirementCoverage;
	}

	public Byte getUnderReviewCriticalRequirementCoverage() {
		return underReviewCriticalRequirementCoverage;
	}

	public Byte getUnderReviewMajorRequirementCoverage() {
		return underReviewMajorRequirementCoverage;
	}

	public Byte getUnderReviewMinorRequirementCoverage() {
		return underReviewMinorRequirementCoverage;
	}

	public Byte getUnderReviewUndefinedRequirementCoverage() {
		return underReviewUndefinedRequirementCoverage;
	}

	public Byte getApprovedGlobalRequirementCoverage() {
		return approvedGlobalRequirementCoverage;
	}

	public Byte getApprovedCriticalRequirementCoverage() {
		return approvedCriticalRequirementCoverage;
	}

	public Byte getApprovedMajorRequirementCoverage() {
		return approvedMajorRequirementCoverage;
	}

	public Byte getApprovedMinorRequirementCoverage() {
		return approvedMinorRequirementCoverage;
	}

	public Byte getApprovedUndefinedRequirementCoverage() {
		return approvedUndefinedRequirementCoverage;
	}

	public Byte getObsoleteGlobalRequirementCoverage() {
		return obsoleteGlobalRequirementCoverage;
	}

	public Byte getObsoleteCriticalRequirementCoverage() {
		return obsoleteCriticalRequirementCoverage;
	}

	public Byte getObsoleteMajorRequirementCoverage() {
		return obsoleteMajorRequirementCoverage;
	}

	public Byte getObsoleteMinorRequirementCoverage() {
		return obsoleteMinorRequirementCoverage;
	}

	public Byte getObsoleteUndefinedRequirementCoverage() {
		return obsoleteUndefinedRequirementCoverage;
	}

	public void setWorkInProgressGlobalRequirementCoverage(Byte workInProgressGlobalRequirementCoverage) {
		this.workInProgressGlobalRequirementCoverage = workInProgressGlobalRequirementCoverage;
	}

	public void setWorkInProgressCriticalRequirementCoverage(Byte workInProgressCriticalRequirementCoverage) {
		this.workInProgressCriticalRequirementCoverage = workInProgressCriticalRequirementCoverage;
	}

	public void setWorkInProgressMajorRequirementCoverage(Byte workInProgressMajorRequirementCoverage) {
		this.workInProgressMajorRequirementCoverage = workInProgressMajorRequirementCoverage;
	}

	public void setWorkInProgressMinorRequirementCoverage(Byte workInProgressMinorRequirementCoverage) {
		this.workInProgressMinorRequirementCoverage = workInProgressMinorRequirementCoverage;
	}

	public void setWorkInProgressUndefinedRequirementCoverage(Byte workInProgressUndefinedRequirementCoverage) {
		this.workInProgressUndefinedRequirementCoverage = workInProgressUndefinedRequirementCoverage;
	}

	public void setUnderReviewGlobalRequirementCoverage(Byte underReviewGlobalRequirementCoverage) {
		this.underReviewGlobalRequirementCoverage = underReviewGlobalRequirementCoverage;
	}

	public void setUnderReviewCriticalRequirementCoverage(Byte underReviewCriticalRequirementCoverage) {
		this.underReviewCriticalRequirementCoverage = underReviewCriticalRequirementCoverage;
	}

	public void setUnderReviewMajorRequirementCoverage(Byte underReviewMajorRequirementCoverage) {
		this.underReviewMajorRequirementCoverage = underReviewMajorRequirementCoverage;
	}

	public void setUnderReviewMinorRequirementCoverage(Byte underReviewMinorRequirementCoverage) {
		this.underReviewMinorRequirementCoverage = underReviewMinorRequirementCoverage;
	}

	public void setUnderReviewUndefinedRequirementCoverage(Byte underReviewUndefinedRequirementCoverage) {
		this.underReviewUndefinedRequirementCoverage = underReviewUndefinedRequirementCoverage;
	}

	public void setApprovedGlobalRequirementCoverage(Byte approvedGlobalRequirementCoverage) {
		this.approvedGlobalRequirementCoverage = approvedGlobalRequirementCoverage;
	}

	public void setApprovedCriticalRequirementCoverage(Byte approvedCriticalRequirementCoverage) {
		this.approvedCriticalRequirementCoverage = approvedCriticalRequirementCoverage;
	}

	public void setApprovedMajorRequirementCoverage(Byte approvedMajorRequirementCoverage) {
		this.approvedMajorRequirementCoverage = approvedMajorRequirementCoverage;
	}

	public void setApprovedMinorRequirementCoverage(Byte approvedMinorRequirementCoverage) {
		this.approvedMinorRequirementCoverage = approvedMinorRequirementCoverage;
	}

	public void setApprovedUndefinedRequirementCoverage(Byte approvedUndefinedRequirementCoverage) {
		this.approvedUndefinedRequirementCoverage = approvedUndefinedRequirementCoverage;
	}

	public void setObsoleteGlobalRequirementCoverage(Byte obsoleteGlobalRequirementCoverage) {
		this.obsoleteGlobalRequirementCoverage = obsoleteGlobalRequirementCoverage;
	}

	public void setObsoleteCriticalRequirementCoverage(Byte obsoleteCriticalRequirementCoverage) {
		this.obsoleteCriticalRequirementCoverage = obsoleteCriticalRequirementCoverage;
	}

	public void setObsoleteMajorRequirementCoverage(Byte obsoleteMajorRequirementCoverage) {
		this.obsoleteMajorRequirementCoverage = obsoleteMajorRequirementCoverage;
	}

	public void setObsoleteMinorRequirementCoverage(Byte obsoleteMinorRequirementCoverage) {
		this.obsoleteMinorRequirementCoverage = obsoleteMinorRequirementCoverage;
	}

	public void setObsoleteUndefinedRequirementCoverage(Byte obsoleteUndefinedRequirementCoverage) {
		this.obsoleteUndefinedRequirementCoverage = obsoleteUndefinedRequirementCoverage;
	}

	public Map<String, Long> getRequirementStatusNumbers() {
		return requirementStatusNumbers;
	}

	@Override
	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}



}
