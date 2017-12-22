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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import org.squashtest.tm.domain.execution.ExecutionStep;

@Entity
@PrimaryKeyJoinColumn(name = "TEST_STEP_ID")
public class CallTestStep extends TestStep {

	@ManyToOne(optional = false)
	@JoinColumn(name = "CALLED_TEST_CASE_ID")
	private TestCase calledTestCase;

	@ManyToOne
	@JoinColumn(name="CALLED_DATASET")
	private Dataset calledDataset;

	@Column(name="DELEGATE_PARAMETER_VALUES")
	private boolean delegateParameterValues = false;

	@Override
	public CallTestStep createCopy() {
		CallTestStep copy = new CallTestStep();
		copy.calledDataset = this.calledDataset;
		copy.calledTestCase = this.calledTestCase;
		return copy;
	}

	@Override
	public void accept(TestStepVisitor visitor) {
		visitor.visit(this);

	}


	public void setCalledTestCase(TestCase calledTestCase) {
		this.calledTestCase = calledTestCase;
	}

	public TestCase getCalledTestCase() {
		return calledTestCase;
	}



	public Dataset getCalledDataset() {
		return calledDataset;
	}

	public void setCalledDataset(Dataset calledDataset) {

		if (calledDataset != null && ! calledTestCase.getDatasets().contains(calledDataset)){
			throw new IllegalArgumentException("attempted to bind to a call step a dataset that doesn't belong to the called test case");
		}
		this.calledDataset = calledDataset;
	}

	public boolean isDelegateParameterValues() {
		return delegateParameterValues;
	}

	public void setDelegateParameterValues(boolean delegateParameterValues) {
		this.delegateParameterValues = delegateParameterValues;
	}

	public ParameterAssignationMode getParameterAssignationMode(){
		if (calledDataset != null){
			return ParameterAssignationMode.CALLED_DATASET;
		}
		else if (delegateParameterValues){
			return ParameterAssignationMode.DELEGATE;
		} else {
			return ParameterAssignationMode.NOTHING;
		}
	}


	@Override
	public List<ExecutionStep> createExecutionSteps(Dataset dataset){

		List<TestStep> testSteps = this.getCalledTestCase().getSteps();
		List<ExecutionStep> returnList = new ArrayList<>(testSteps.size());


		Dataset effective;
		switch(getParameterAssignationMode()){
		case CALLED_DATASET : effective = calledDataset; break;
		case DELEGATE : effective = dataset; break;
		case NOTHING : effective = null;break;
		default : effective = dataset;
		}


		for (TestStep testStep : testSteps) {
			returnList.addAll(testStep.createExecutionSteps(effective));
		}

		return returnList;
	}

}
