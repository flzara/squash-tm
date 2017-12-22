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

public final class ActionStepCollector implements TestStepVisitor {

	private List<ActionTestStep> actionSteps ;

	public List<ActionTestStep> collect(List<TestStep> steps) {
		this.actionSteps = new ArrayList<>();
		for (TestStep step : steps) {
			step.accept(this);
		}
		return actionSteps;
	}

	@Override
	public void visit(ActionTestStep visited) {
		actionSteps.add(visited);
	}

	@Override
	public void visit(CallTestStep visited) {

	}

}
