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
package org.squashtest.tm.service.testautomation.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;

public class TestAutomationProjectContent {

	private final TestAutomationProject project;

	private final List<AutomatedTest> tests;

	private Exception knownProblem = null;

	private boolean orderGuaranteed ;

	public TestAutomationProject getProject() {
		return project;
	}

	/**
	 * 
	 * @return an **copy** of the test list.
	 */
	public List<AutomatedTest> getTests() {
		return Collections.unmodifiableList(tests);
	}

	public Exception getKnownProblem() {
		return knownProblem;
	}

	public boolean hadKnownProblems() {
		return knownProblem != null;
	}

	public void setKnownProblem(Exception knownProblem) {
		this.knownProblem = knownProblem;
	}
	public boolean isOrderGuaranteed() {
		return orderGuaranteed;
	}
	public void setOrderGuaranteed(boolean orderGuaranteed) {
		this.orderGuaranteed = orderGuaranteed;
	}
	public TestAutomationProjectContent(TestAutomationProject project) {
		super();
		this.project = project;
		this.tests = new ArrayList<>();
	}

	public TestAutomationProjectContent(TestAutomationProject project, Exception knownProblem) {
		super();
		this.project = project;
		this.tests = Collections.emptyList();
		this.knownProblem = knownProblem;
	}

	/**
	 * @param project
	 * @param tests
	 */
	public TestAutomationProjectContent(TestAutomationProject project, Collection<AutomatedTest> tests) {
		this(project);
		appendTests(tests);
	}

	public TestAutomationProjectContent(TestAutomationProject project, Collection<AutomatedTest> tests,
			boolean orderGuaranteed) {
		this(project, tests);
		this.orderGuaranteed = orderGuaranteed;
	}

	/**
	 * Adds a test without params
	 * 
	 * @param test
	 */
	public void appendTest(AutomatedTest test) {
		doAppendTest(test);
	}
	/**
	 * @see #appendTest(AutomatedTest)
	 * @param test
	 */
	private void doAppendTest(AutomatedTest test){
		tests.add(test);

	}

	/**
	 * Adds a batch of tests without params. Tests are added in the order of the given colleciton.
	 * 
	 * @param tests
	 */
	public final void appendTests(Collection<AutomatedTest> tests) {
		for (AutomatedTest test : tests) {
			doAppendTest(test);
		}
	}

}
