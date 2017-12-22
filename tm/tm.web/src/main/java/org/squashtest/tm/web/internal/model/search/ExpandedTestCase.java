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
package org.squashtest.tm.web.internal.model.search;

import java.util.Collection;

import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

/***
 *
 * @author xpetitrenaud
 *
 *         This class is used to display the result of test case research by requirement. It calculates the strongest
 *         criticality
 */
public class ExpandedTestCase {

	private TestCase testCase;

	private RequirementCriticality criticality;

	/***
	 * the criticalities selected in the research.
	 */
	private Collection<RequirementCriticality> selectedCriticalities;

	public String getClassSimpleName() {
		return testCase.getClassSimpleName();
	}

	public void setTestCase(TestCase pTestCase) {
		this.testCase = pTestCase;
		this.criticality = calculateMaxCriticality();
	}

	public String getName() {
		return this.testCase.getName();
	}

	public String getReference() {
		return this.testCase.getReference();
	}
	
	public Project getProject() {
		return this.testCase.getProject();
	}

	public Long getId() {
		return this.testCase.getId();
	}

	public RequirementCriticality getCriticality() {
		return this.criticality;
	}

	public void setSelectedCriticalities(Collection<RequirementCriticality> pSelectedCriticalities) {
		this.selectedCriticalities = pSelectedCriticalities;
	}

	/***
	 * This method returns the strongest criticality in verified requirement list
	 *
	 * @return
	 */
	private RequirementCriticality calculateMaxCriticality() {
		// Each level is represented by a number. 3 is the lowest one
		// FIXME what if we add a criticality given this method is not covered by tests ?! max criticality should not be hardcoded !
		int level = 3;

		// check if there's a stronger criticality and if this criticality is part of the ones selected for the research
		for (RequirementVersion requirement : this.testCase.getVerifiedRequirementVersions()) {
			if (requirement.getCriticality().getLevel() < level
					&& (selectedCriticalities.isEmpty() || selectedCriticalities.contains(requirement.getCriticality()))) {
				level = requirement.getCriticality().getLevel();
			}
		}
		return RequirementCriticality.valueOf(level);
	}

}
