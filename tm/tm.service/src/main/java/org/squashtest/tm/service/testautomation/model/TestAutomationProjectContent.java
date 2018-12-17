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

import java.util.*;
import java.util.stream.Collectors;

import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;

public class TestAutomationProjectContent {

	private final TestAutomationProject project;

	private final List<AutomatedTest> tests;

	private Exception knownProblem = null;

	private boolean orderGuaranteed ;

	public TestAutomationProjectContent(TestAutomationProject project) {
		super();
		this.project = project;
		this.tests = new ArrayList<>();
	}

	public TestAutomationProjectContent(TestAutomationProject project, Exception knownProblem) {
		super();
		this.project = project;
		this.tests = new ArrayList<>();
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
	 * Adds a batch of tests without params. Tests are added in the order of the given collection.
	 *
	 * @param tests
	 */
	public final void appendTests(Collection<AutomatedTest> tests) {
		for (AutomatedTest test : tests) {
			doAppendTest(test);
		}
	}

	/**
	 * Appends the automated test of the otherContent into this instance. Duplicate automated tests won't be included.
	 * It is assumed that the TestAutomationProject of this instance and the otherContent are the same (instance equality), otherwise
	 * an {@link IllegalArgumentException} will be thrown.
	 *
	 * @param otherContent
	 * @throws IllegalArgumentException if this instance and the other instance reference different TestAutomationProjects
	 */
	public final void mergeContent(TestAutomationProjectContent otherContent){

		if (project != otherContent.getProject()){
			throw new IllegalArgumentException("attempted to merge automated tests from project '"+project.getLabel() +"' with project '"+otherContent.getProject().getLabel()+"' !");
		}

		Set<String> myTestPaths = getTests().stream().map(AutomatedTest::getName).collect(Collectors.toSet());

		otherContent.getTests().forEach(otherTest -> {
			if (! myTestPaths.contains(otherTest.getName())){
				doAppendTest(otherTest);
			}
		});
	}

}
