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
package org.squashtest.tm.service.internal.testcase;

import java.util.HashSet;
import java.util.Set;

import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.CallTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.domain.testcase.TestStepVisitor;

public class ParameterNamesFinder implements TestStepVisitor{
	
	private Set<String> result = new HashSet<>();
	
	public Set<String> findParametersNamesInActionAndExpectedResult(TestStep step){
		step.accept(this);
		return result;
	}

	@Override
	public void visit(ActionTestStep visited) {
		this.result.addAll(visited.findUsedParametersNames());
	}

	@Override
	public void visit(CallTestStep visited) {
		//nope
	}
	
	
}
