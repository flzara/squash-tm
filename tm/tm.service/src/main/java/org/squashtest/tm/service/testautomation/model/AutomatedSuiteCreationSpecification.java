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

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * That class describes how an AutomatedSuite should be created and executed.
 */

/*
 Tech Note : Jackson should not serialize this as is because of some getters sucha as the methods getXReference only make sense
 in the Java world (the client doesn't need to know them). Along with validation those methods have a meaning in Java World only,
 so it's best not to serialize them.
 */
public class AutomatedSuiteCreationSpecification {

	/**
	 * <p>
	 * This is the reference to the Entity that owns the test plan. It must references either Iteration or to a TestSuite.
	 * By default their whole test plan is scheduled for execution, unless {@link #testPlanSubsetIds} is not empty in which case
	 * only those items will be referenced.
	 * </p>
	 *
	 * <p>
	 *     Must references exactly one Iteration or exactly one TestSuite, otherwise the specification is invalid and will
	 *     be rejected.
	 * </p>
	 *
	 *
	 */
	private EntityReference context = null;

	/**
	 * <p>
	 *     Optional property : the test plan subset ids, if not empty, will define a subset of the test plan (defined by the
	 *     {@link #context}, see above) that is effectively scheduled. It consists of ids of test plan items.
	 *     If some item ids don't belong to the context test plan they will simply be ignored.
	 * </p>
	 */
	private List<Long> testPlanSubsetIds = new ArrayList<>();



	/**
	 * <p>
	 * 		Additional configuration about how the suite will be executed. Currently it merely specifies on which slave
	 * 		node a test automation project should run.
	 * </p>
	 */
	private Collection<SuiteExecutionConfiguration>  executionConfigurations = new ArrayList<>();


	public List<Long> getTestPlanSubsetIds() {
		return testPlanSubsetIds;
	}

	public void setTestPlanSubsetIds(List<Long> testPlanSubsetIds) {
		this.testPlanSubsetIds = testPlanSubsetIds;
	}

	public EntityReference getContext() {
		return context;
	}

	public void setContext(EntityReference context) {
		this.context = context;
	}

	public Collection<SuiteExecutionConfiguration> getExecutionConfigurations() {
		return executionConfigurations;
	}

	public void setExecutionConfigurations(Collection<SuiteExecutionConfiguration> executionConfigurations) {
		this.executionConfigurations = executionConfigurations;
	}

	/**
	 * Returns quietly if the testPlan matches the definition of the javadoc above, or throws if not.
	 *
	 * @return
	 * @throws IllegalArgumentException if the testPlan is invalid.
	 *
	 */
	public void validate(){
		boolean isValid = false;
		if (context != null){
			EntityType type = context.getType();
			isValid = (type == EntityType.ITERATION || type == EntityType.TEST_SUITE);
		}

		if (! isValid){
			throw new IllegalArgumentException("the context is invalid : it references neither an iteration nor a test suite");
		}
	}


}
