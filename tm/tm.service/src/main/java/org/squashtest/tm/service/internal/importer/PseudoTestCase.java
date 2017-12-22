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
package org.squashtest.tm.service.internal.importer;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.domain.testcase.TestStep;

/**
 * Factored out from {@link ExcelTestCaseParserImpl}
 * @author Benoit Siri
 * @author Gregory Fouquet
 *
 */
/* package-private */class PseudoTestCase {
	private Date createdOnDate = null;
	private String createdBy = null;
	private String createdOn = null;

	private String importance = "";
	private String nature = "";
	private String type = "";
	private String status = "";

	// the first element of the list is the description itself
	// others are complementary elements
	private final List<String[]> descriptionElements = new ArrayList<>();

	private final List<String> prerequisites = new ArrayList<>();

	private final List<String[]> stepElements = new LinkedList<>();

	/* ***************************** formatters *********************************** */

	public String formatDescription() {

		StringBuilder builder = new StringBuilder();

		List<String[]> elements = descriptionElements;

		if (!elements.isEmpty()) {
			// appending the description
			builder.append("<p>").append(elements.get(0)[1]).append("</p>");
		}

		// appending supplementary material if any;

		if (elements.size() > 1) {

			builder.append("<hr/>");
			builder.append("<ul>");

			for (int i = 1; i < elements.size(); i++) {
				String[] elt = elements.get(i);
				builder.append("<li>").append("<strong>").append(elt[0]).append(" :</strong> ").append(elt[1]).append("</li>");
			}

			builder.append("</ul>");

		}

		return builder.toString();

	}

	public TestCaseImportance formatImportance() {
		return TestCaseImportance.valueOf(importance);
	}


	/*
	 * Feat 1108 : for back compatibility purpose
	 * we must ensure that the former natures
	 * (the enum TestCaseNature) are still supported,
	 * eg NON_FUNCTIONAL must be translated to NAT_NON_FUNCTIONAL.
	 *
	 * Note that because of this we must treat the nominal case as an exception handling clause,
	 * and that sucks.
	 */
	public InfoListItem formatNature() {
		InfoListItem res;

		if (StringUtils.isBlank(nature)){
			// better off with a null nature than a blank one
			res = null;
		}
		else {
			try{
				// Feat 1108 : does this string reference an obsolete code for a nature ?
				TestCaseNature obsoleteNature = TestCaseNature.valueOf(nature);
				res = new ListItemReference("NAT_"+obsoleteNature.toString());
			}
			catch(IllegalArgumentException ex){
				// else, the code is assumed to be correct
				res = new ListItemReference(nature);
			}
		}

		return res;
	}


	/*
	 * Feat 1108 : for back compatibility purpose
	 * we must ensure that the former types
	 * (the enum testCaseType) are still supported,
	 * eg UNDEFINED must be translated to TYP_UNDEFINED.
	 *
	 * Note that because of this we must treat the nominal case as an exception handling clause,
	 * and that sucks.
	 */
	public InfoListItem formatType() {
		InfoListItem res;

		if (StringUtils.isBlank(type)){
			// better off with a null nature than a blank one
			res = null;
		}
		else {
			try{
				// Feat 1108 : does this string reference an obsolete code for a nature ?
				TestCaseNature obsoleteNature = TestCaseNature.valueOf(type);
				res = new ListItemReference("TYP_"+obsoleteNature.toString());
			}
			catch(IllegalArgumentException ex){
				// else, the code is assumed to be correct
				res = new ListItemReference(type);
			}
		}

		return res;
	}

	public TestCaseStatus formatStatus() {
		return TestCaseStatus.valueOf(status);
	}

	public String formatPreRequisites() {
		StringBuilder builder = new StringBuilder();

		if (!prerequisites.isEmpty()) {

			builder.append("<ol>");

			for (String string : prerequisites) {
				builder.append("<li>").append(string).append("</li>");
			}

			builder.append("</ol>");
		}

		return builder.toString();
	}

	public List<TestStep> formatSteps() {

		List<TestStep> steps = new LinkedList<>();

		for (String[] pseudoStep : stepElements) {
			ActionTestStep step = new ActionTestStep();
			step.setAction("<p>" + pseudoStep[0] + "</p>");
			step.setExpectedResult("<p>" + pseudoStep[1] + "</p>");
			steps.add(step);
		}

		return steps;

	}


	// ******** accessors ***********************

	public Date getCreatedOnDate() {
		return createdOnDate;
	}

	public void setCreatedOnDate(Date createdOnDate) {
		this.createdOnDate = createdOnDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getImportance() {
		return importance;
	}

	public void setImportance(String importance) {
		this.importance = importance;
	}

	public String getNature() {
		return nature;
	}


	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String[]> getDescriptionElements() {
		return descriptionElements;
	}

	public List<String> getPrerequisites() {
		return prerequisites;
	}

	public List<String[]> getStepElements() {
		return stepElements;
	}



}
