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
package org.squashtest.tm.web.internal.controller.testcase.steps;

import org.squashtest.tm.domain.testcase.BehaviorTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

import java.util.HashMap;
import java.util.Map;

public class BehaviorTestStepTableModelBuilder extends DataTableModelBuilder<TestStep> {
	@Override
	protected Object buildItemData(TestStep step) {
		BehaviorTestStep behaviorTestStep = (BehaviorTestStep) step;
		Map<String, String> item = new HashMap<>(3);
		item.put("step-index", String.valueOf(getCurrentIndex()));
		item.put("step-id", String.valueOf(behaviorTestStep.getId()));
		item.put("step-phrase", behaviorTestStep.getBehaviorPhrase().getPhrase());
		return item;
	}
}
