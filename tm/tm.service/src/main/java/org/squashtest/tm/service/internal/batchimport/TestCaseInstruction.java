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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.testcase.TestCase;

public class TestCaseInstruction extends Instruction<TestCaseTarget> implements CustomFieldHolder, Milestoned {
	private final TestCase testCase;
	private final Map<String, String> customFields = new HashMap<>();
	private final String[] milestones = {};

	public TestCaseInstruction(TestCaseTarget target, TestCase testCase) {
		super(target);
		this.testCase = testCase;
	}

	@Override
	protected LogTrain executeUpdate(Facility facility) {
		LogTrain execLogTrain;
		execLogTrain = facility.updateTestCase(this);
		return execLogTrain;
	}

	@Override
	protected LogTrain executeDelete(Facility facility) {
		LogTrain execLogTrain;
		execLogTrain = facility.deleteTestCase(getTarget());
		return execLogTrain;
	}

	@Override
	protected LogTrain executeCreate(Facility facility) {
		LogTrain execLogTrain;
		execLogTrain = facility.createTestCase(this);
		return execLogTrain;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public Map<String, String> getCustomFields() {
		return customFields;
	}

	@Override
	public void addCustomField(String code, String value) {
		customFields.put(code, value);
	}

	@Override
	public List<String> getMilestones() {
		return Arrays.asList(milestones);
	}
}
