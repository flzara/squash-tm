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
 Note : Jackson should not serialize this as is because of some getters sucha as the methods getXReference only make sense
 in the Java world (the client doesn't need to know them). Along with validation, those methods have a meaning in Java World only.
 Choose carefully what you want to serialize.
 */
public class AutomatedSuiteCreationSpecification {

	/**
	 * The test plan of the future automated suite (ie define the set of items
	 * in the test plan of an iteration, or the list of items directly).
	 * It accepts either of the following  :
	 * <ul>
	 *     <li>Exactly one entity reference to an iteration : the whole test plan of the iteration is used.</li>
	 *     <li>Exactly one entity reference to a test suite : the whole test plan of the suite is used.</li>
	 *     <li>One or several references to iteration test plan items : only these items will be ran. The ordering is defined by the property 'context', see below.</li>
	 * </ul>
	 *
	 * Other situations are considered erroneous and will be rejected.
	 */
	private List<EntityReference> testPlan = new ArrayList<>();

	/**
	 * The context of the test plan execution. When the property 'testPlan' is set to a collection of items, the context
	 * says in which order they should run, and if left to null the default context is the iteration than contains the
	 * first item of the list.
	 *
	 * When the 'testPlan' is set to other values, the context has no meaning.
	 *
	 */
	private EntityReference context = null;

	/**
	 * Additional information related to execution. Currently it merely specifies on which slave node a test automation
	 * project should run.
	 */
	private Collection<SuiteExecutionConfiguration>  executionConfigurations = new ArrayList<>();




	public List<EntityReference> getTestPlan() {
		return testPlan;
	}

	public void setTestPlan(List<EntityReference> testPlan) {
		this.testPlan = testPlan;
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

		if (testPlan.isEmpty()){
			isValid = false;
		}
		else if (testPlan.size() == 1){
			// if a null value has been inserted the thread deserves to abort outright with a NPE.
			EntityType type = testPlan.get(0).getType();
			isValid = (type == EntityType.ITERATION || type == EntityType.TEST_SUITE);
		}
		else{
			isValid = testPlan.stream().allMatch(ref -> ref.getType() == EntityType.ITEM_TEST_PLAN);
		}

		if (! isValid){
			throw new IllegalArgumentException("testPlan is invalid : the reference(s) point neither to an iteration, nor a test suite, nor test plan items");
		}
	}

	public EntityType getSourceType(){
		validate();
		return testPlan.get(0).getType();
	}

	/**
	 * Returns the single entity reference that point to an iteration. Will throw if the reference is not an Iteration.
	 *
	 * @return
	 * @throws IllegalStateException
	 */
	public EntityReference getIterationReference(){
		EntityReference reference = testPlan.get(0);
		if (reference.getType() != EntityType.ITERATION){
			throw new IllegalStateException("entity reference expected to be an Iteration, but was pointing instead to "+reference.getType());
		}
		return reference;
	}

	/**
	 * Returns the single entity reference that point to a test suite. Will throw if the reference is not a test suite.
	 *
	 * @return
	 * @throws IllegalStateException
	 */
	public EntityReference getTestSuiteReference(){
		EntityReference reference = testPlan.get(0);
		if (reference.getType() != EntityType.TEST_SUITE){
			throw new IllegalStateException("entity reference expected to be a TestSuite, but was pointing instead to "+reference.getType());
		}
		return reference;
	}

	/**
	 * Returns the multiple entity references that all point to items. Will throw if any reference is not about an item.
	 *
	 * @return
	 * @throws IllegalStateException
	 */
	public List<EntityReference> getItemReferences(){
		if (testPlan.stream().anyMatch(ref -> ref.getType() != EntityType.ITEM_TEST_PLAN)){
			throw new IllegalStateException("entity references expected to all be Items, but not all of them were");
		}
		return testPlan;
	}


}
