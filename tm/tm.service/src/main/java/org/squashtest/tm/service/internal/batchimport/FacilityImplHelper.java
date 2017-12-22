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
package org.squashtest.tm.service.internal.batchimport;

import java.util.Map;
import java.util.Map.Entry;

import org.squashtest.tm.domain.Sizes;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;

final class FacilityImplHelper {

	static final int STD_TRUNCATE_SIZE = 255;

	private EntityFacilitySupport facility;
	
	FacilityImplHelper(EntityFacilitySupport facility) {
		super();
		this.facility = facility;
	}


	/**
	 * fix the potentially null values with default values. In some case it's completely superfluous since null values
	 * (eg for the name) are eliminatory.
	 */

	/*
	 * Feat 1108 :
	 * The nullity regarding the Nature and Type will be handled by
	 * the TestCaseLibraryNavigationService itself. No need to
	 * worry about them here and now.
	 */
	void fillNullWithDefaults(TestCase testCase) {

		if (testCase.getName() == null) {
			testCase.setName("");
		}

		if (testCase.getReference() == null) {
			testCase.setReference("");
		}

		if (testCase.getDescription() == null) {
			testCase.setDescription("");
		}

		if (testCase.getPrerequisite() == null) {
			testCase.setPrerequisite("");
		}

		if (testCase.getImportance() == null) {
			testCase.setImportance(TestCaseImportance.LOW);
		}

		if (testCase.getStatus() == null) {
			testCase.setStatus(TestCaseStatus.WORK_IN_PROGRESS);
		}

		if (testCase.isImportanceAuto() == null) {
			testCase.setImportanceAuto(Boolean.FALSE);
		}

	}

	/*
	 * Feat 1108 :
	 * The nullity regarding the Nature and Type will be handled by
	 * the TestCaseLibraryNavigationService itself. No need to
	 * worry about them here and now.
	 *
	 */
	void fillNullWithDefaults(RequirementVersion reqVersion) {

		if (reqVersion.getName() == null) {
			reqVersion.setName("");
		}

		if (reqVersion.getReference() == null) {
			reqVersion.setReference("");
		}

		if (reqVersion.getDescription() == null) {
			reqVersion.setDescription("");
		}

		if (reqVersion.getCriticality()==null){
			reqVersion.setCriticality(RequirementCriticality.MINOR);
		}

		if (reqVersion.getStatus() == null) {
			reqVersion.setStatus(RequirementStatus.WORK_IN_PROGRESS);
		}

	}

	void fillNullWithDefaults(ActionTestStep step) {
		if (step.getAction() == null) {
			step.setAction("");
		}
		if (step.getExpectedResult() == null) {
			step.setExpectedResult("");
		}
	}

	void fillNullWithDefaults(Parameter param) {
		if (param.getName() == null) {
			param.setName("");
		}
		if (param.getDescription() == null) {
			param.setDescription("");
		}
	}

	void fillNullWithDefaults(Dataset ds) {
		if (ds.getName() == null) {
			ds.setName("");
		}
	}

	/*
	 * Truncates string content that would exceed their maximum length
	 */
	void truncate(TestCase testCase, Map<String, String> cufValues) {
		String name = testCase.getName();
		testCase.setName(truncate(name, Sizes.NAME_MAX));
		
		String ref = testCase.getReference();
		testCase.setReference(truncate(ref, TestCase.MAX_REF_SIZE));

		truncateCustomfields(cufValues);
	}

	/*
	 * Truncates string content that would exceed their maximum length
	 */
	void truncate(RequirementVersion reqVersion, Map<String, String> cufValues) {
		String ref = reqVersion.getReference();
		reqVersion.setReference(truncate(ref, RequirementVersion.MAX_REF_SIZE));

		truncateCustomfields(cufValues);
	}

	/*
	 * Truncates string content that would exceed their maximum length
	 */
	void truncate(ActionTestStep step, Map<String, String> cufValues) {
		truncateCustomfields(cufValues);
	}
	

	/*
	 * Truncates string content that would exceed their maximum length
	 */
	void truncate(Parameter param) {
		String name = param.getName();
		param.setName(truncate(name, Parameter.MAX_NAME_SIZE));
	}

	/*
	 * Truncates string content that would exceed their maximum length
	 */
	void truncate(Dataset ds) {
		String name = ds.getName();
		ds.setName(truncate(name, Dataset.MAX_NAME_SIZE));
	}

	/**
	 * Will truncate the input str if it is longer than the given cap value.
	 *
	 * @param str
	 *            : the string to truncate
	 * @param cap
	 *            : the maximum number of characters to live in the truncated string
	 * @return : the truncated string or {@code null} if str was {@code null}.
	 */
	String truncate(String str, int cap) {
		if (str != null) {
			return str.substring(0, Math.min(str.length(), cap));
		} else {
			return null;
		}
	}

	
	String truncate(String str){
		return truncate(str, STD_TRUNCATE_SIZE);
	}

	
	void truncateCustomfields(Map<String, String> cufValues){
		for (Entry<String, String> cuf : cufValues.entrySet()) {
			InputType type = facility.getInputTypeFor(cuf.getKey());
			if (type != InputType.RICH_TEXT){
				String value = cuf.getValue();
				cuf.setValue(truncate(value, CustomFieldValue.MAX_SIZE));
			}
		}
	}
}
