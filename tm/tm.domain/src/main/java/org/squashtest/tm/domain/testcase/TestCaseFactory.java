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
package org.squashtest.tm.domain.testcase;

public class TestCaseFactory {
	/**
	 * Private constructor.
	 */
	private TestCaseFactory() {
	}
	/**
	 * Given a {@linkplain TestCaseKind} as a String and a name,
	 * returns a new instance of a TestCase with the corresponding type initialized with its name.
	 * @param testCaseKindAsString A String corresponding to the requested {@linkplain TestCaseKind}
	 * @param testCaseName The name of the TestCase
	 * @return A new instance of a TestCase with the requested type and initialized with its name.
	 */
	public static TestCase getTestCase(String testCaseKindAsString, String testCaseName) {
		TestCaseKind testCaseKind = TestCaseKind.valueOf(testCaseKindAsString);
		TestCase testCase;
		switch (testCaseKind) {
			case STANDARD:
				testCase = new TestCase();
				testCase.setName(testCaseName);
				break;
			case KEYWORD:
				testCase = new KeywordTestCase();
				testCase.setName(testCaseName);
				break;
			case GHERKIN:
				// this constructor must be used to initialize the script
				testCase = new ScriptedTestCase(testCaseName);
				break;
			default:
				throw new IllegalArgumentException(
					"The Kind " + testCaseKind.name() + " is not supported by the TestCaseFactory.");
		}
		return testCase;
	}
}
