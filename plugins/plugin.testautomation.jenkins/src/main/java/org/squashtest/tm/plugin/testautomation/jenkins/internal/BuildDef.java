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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;

/**
 * Build definition i.e. all the informtion required to run a bunch of automated tests from the same squash ta project
 * (i.e. the same jenkins job)
 *
 * @author Gregory Fouquet
 *
 */
public class BuildDef {
	private final TestAutomationProject project;
	private final Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> parameterizedExecutions;
	private final String node;


	public BuildDef(@NotNull TestAutomationProject project,
			@NotNull List<Couple<AutomatedExecutionExtender, Map<String, Object>>> parameterizedExecutions, String node) {
		super();
		this.project = project;
		this.parameterizedExecutions = Collections.unmodifiableList(parameterizedExecutions);
		this.node = node;
	}

	/**
	 * @return the project
	 */
	public TestAutomationProject getProject() {
		return project;
	}

	/**
	 * @return the parameterizedExecutions
	 */
	public Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> getParameterizedExecutions() {
		return parameterizedExecutions;
	}

	public Collection<AutomatedTest> getTests() {
		ArrayList<AutomatedTest> res = new ArrayList<>(parameterizedExecutions.size());

		for (Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec : parameterizedExecutions) {
			res.add(paramdExec.getA1().getAutomatedTest());
		}

		return res;
	}

	public String getNode(){
		return node;
	}
}
