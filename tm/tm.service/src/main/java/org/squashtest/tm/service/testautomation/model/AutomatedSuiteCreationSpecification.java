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

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * That class describes how an AutomatedSuite should be created and executed.
 */

/*
 Note : Jackson should not serialize this as is because of the various methods getXReference. Along with validation,
 those methods have a meaning in Java World only. Choose carefully what you want to serialize.
 */
public class AutomatedSuiteCreationSpecification {

	/**
	 * The source from which the automated suite should be created (ie define the set of items
	 * in the test plan of an iteration, or the list of items directly).
	 * It accepts either of the following  :
	 * <ul>
	 *     <li>One entity reference to an iteration,</li>
	 *     <li>One entity reference to a test suite,</li>
	 *     <li>One or several references to iteration test plan items</li>
	 * </ul>
	 * Other situations are considered erroneous and will be rejected.
	 */
	private List<EntityReference> source = new ArrayList<>();

	/**
	 * Additional information related to execution. Currently it merely specifies on which slave node a test automation
	 * project should run.
	 */
	private Collection<SuiteExecutionConfiguration>  executionConfigurations = new ArrayList<>();


	public List<EntityReference> getSource() {
		return source;
	}

	public void setSource(List<EntityReference> source) {
		this.source = source;
	}

	public Collection<SuiteExecutionConfiguration> getExecutionConfigurations() {
		return executionConfigurations;
	}

	public void setExecutionConfigurations(Collection<SuiteExecutionConfiguration> executionConfigurations) {
		this.executionConfigurations = executionConfigurations;
	}

	/**
	 * Returns quietly if the source matches the definition of the javadoc above, or throws if not.
	 *
	 * @return
	 * @throws IllegalArgumentException if the source is invalid.
	 *
	 */
	public void validate(){
		boolean isValid = false;

		if (source.isEmpty()){
			isValid = false;
		}
		else if (source.size() == 1){
			// if a null value has been inserted the thread deserves to abort outright with a NPE.
			EntityType type = source.get(0).getType();
			isValid = (type == EntityType.ITERATION || type == EntityType.TEST_SUITE);
		}
		else{
			isValid = source.stream().allMatch( ref -> ref.getType() == EntityType.ITEM_TEST_PLAN);
		}

		if (! isValid){
			throw new IllegalArgumentException("source is invalid : the reference(s) point neither to an iteration, nor a test suite, nor test plan items");
		}
	}

	public EntityType getSourceType(){
		validate();
		return source.get(0).getType();
	}

	/**
	 * Returns the single entity reference that point to an iteration. Will throw if the reference is not an Iteration.
	 *
	 * @return
	 * @throws IllegalStateException
	 */
	public EntityReference getIterationReference(){
		EntityReference reference = source.get(0);
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
		EntityReference reference = source.get(0);
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
		if (source.stream().anyMatch(ref -> ref.getType() != EntityType.ITEM_TEST_PLAN)){
			throw new IllegalStateException("entity references expected to all be Items, but not all of them were");
		}
		return source;
	}

}
