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

import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.bdd.BehaviorPhrase;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@PrimaryKeyJoinColumn(name = "TEST_STEP_ID")
public class BehaviorTestStep extends TestStep {

	@NotNull
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "BEHAVIOR_PHRASE_ID")
	private BehaviorPhrase behaviorPhrase;

	BehaviorTestStep() {
	}

	public BehaviorTestStep(BehaviorPhrase paramBehaviorPhrase) {
		if(paramBehaviorPhrase == null) {
			throw new IllegalArgumentException("Behavior phrase cannot be null.");
		}
		this.behaviorPhrase = paramBehaviorPhrase;
	}

	@Override
	public TestStep createCopy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void accept(TestStepVisitor visitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ExecutionStep> createExecutionSteps(Dataset dataset) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTestCase(@NotNull TestCase testCase) {
		if(!testCase.isBehaviorTestCase()) {
			throw new IllegalArgumentException("Cannot add a Behavior Test Step");
		}
		super.setTestCase(testCase);
	}

	public BehaviorPhrase getBehaviorPhrase() {
		return behaviorPhrase;
	}
}
