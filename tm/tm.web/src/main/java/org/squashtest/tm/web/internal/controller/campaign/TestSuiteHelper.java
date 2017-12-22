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
package org.squashtest.tm.web.internal.controller.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.squashtest.tm.domain.campaign.TestSuite;

/**
 * 
 * 
 */
public final class TestSuiteHelper {
	/**
	 * 
	 */
	private TestSuiteHelper() {
		super();
	}
	
	public static String buildSuiteNameList(List<TestSuite> unsortedSuites){
		return buildNameList(unsortedSuites).toString();
	}
	
	public static String buildEllipsedSuiteNameList(List<TestSuite> unsortedSuites, int maxLength) {		
		StringBuilder testSuiteNames = buildNameList(unsortedSuites);
		return ellipseString(testSuiteNames, maxLength);
	}
	
	private static StringBuilder buildNameList(List<TestSuite> unsortedSuites){
		List<TestSuite> sortedSuites = new ArrayList<>(unsortedSuites);
		Collections.sort(sortedSuites, new Comparator<TestSuite>(){
			@Override
			public int compare(TestSuite o1, TestSuite o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		if (sortedSuites.isEmpty()) {
			return new StringBuilder("");
		}

		StringBuilder testSuiteNames = new StringBuilder();
		if (!sortedSuites.isEmpty()) {
			int i = 0;
			while (i < sortedSuites.size() - 1) {
				testSuiteNames.append(sortedSuites.get(i).getName()).append(", ");
				i++;
			}
			testSuiteNames.append(sortedSuites.get(i).getName());
		}
		return testSuiteNames; 
	}

	private static String ellipseString(StringBuilder builder, int maxLength) {
		String res;
		if (builder.length() > maxLength) {
			res = builder.substring(0, maxLength - 4) + "...";
			// rem : extracted from other class, not sure why max-4 instead of max-3
		} else {
			res = builder.toString();
		}
		return res;
	}
	
}
