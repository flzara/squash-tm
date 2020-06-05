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

import org.squashtest.tm.domain.bdd.ActionWordParameterValue;

import java.util.List;

public class ParameterNameInStepUpdater implements TestStepVisitor {

	private String oldParamName;
	private String newParamName;

	public ParameterNameInStepUpdater(String oldParamName, String newParamName) {
		this.oldParamName = oldParamName;
		this.newParamName = newParamName;
	}

	private String replace(String content) {
		return content.replace(Parameter.getParamStringAsUsedInStep(oldParamName),
				Parameter.getParamStringAsUsedInStep(newParamName));
	}

	@Override
	public void visit(ActionTestStep visited) {
		if (visited.getAction() != null) {
			visited.setAction(replace(visited.getAction()));
		}
		if (visited.getExpectedResult() != null) {
			visited.setExpectedResult(replace(visited.getExpectedResult()));
		}
	}

	@Override
	public void visit(CallTestStep visited) {
		// NOOP
	}

	@Override
	public void visit(KeywordTestStep visited) {
		List<ActionWordParameterValue> paramValues = visited.getParamValues();
		if (paramValues != null) {
			for (ActionWordParameterValue paramValue : paramValues) {
				if (paramValue.getValue() != null && paramValue.getValue().startsWith("=")) {
					paramValue.setValue(paramValue.getValue().replace(oldParamName, newParamName));
				}
			}
		}
	}
}
