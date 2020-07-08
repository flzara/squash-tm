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

import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_CLOSE_GUILLEMET;
import static org.squashtest.tm.domain.bdd.ActionWord.ACTION_WORD_OPEN_GUILLEMET;

public class ParameterNameInStepUpdater implements TestStepVisitor {

	private String oldParamName;
	private String newParamName;

	public ParameterNameInStepUpdater(String oldParamName, String newParamName) {
		this.oldParamName = oldParamName;
		this.newParamName = newParamName;
	}

	private String replace(String content) {
		return content.replace(Parameter.getParamStringAsUsedInStandardTestCase(oldParamName),
				Parameter.getParamStringAsUsedInStandardTestCase(newParamName));
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
		String oldMatchingParamValue = Parameter.getParamStringAsUsedInKeywordTestCase(oldParamName);
		String newMatchingParamValue = Parameter.getParamStringAsUsedInKeywordTestCase(newParamName);

		if (paramValues != null) {
			for (ActionWordParameterValue paramValue : paramValues) {
				String currentValue = paramValue.getValue();
				if (oldMatchingParamValue.equals(currentValue)) {
					paramValue.setValue(newMatchingParamValue);
				}
			}
		}
	}
}
