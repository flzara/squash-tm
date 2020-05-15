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

import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.bdd.ActionWordText;
import org.squashtest.tm.domain.testcase.KeywordTestStep;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeywordTestStepTableModelBuilder extends DataTableModelBuilder<TestStep> {
	@Override
	protected Object buildItemData(TestStep step) {
		KeywordTestStep keywordTestStep = (KeywordTestStep) step;
		Map<String, String> item = new HashMap<>(4);
		item.put("entity-id", step.getId().toString());
		item.put("step-index", String.valueOf(getCurrentIndex()));
		item.put("step-keyword", String.valueOf(keywordTestStep.getKeyword()));
		String actionWordWithParamValues = createActionWordWithParamValues(keywordTestStep);
		item.put("step-action-word", actionWordWithParamValues);
		item.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, null);
		return item;
	}

	private String createActionWordWithParamValues(KeywordTestStep keywordTestStep) {
		ActionWord actionWord = keywordTestStep.getActionWord();
		List<ActionWordFragment> fragments = actionWord.getFragments();
		List<ActionWordParameterValue> paramValues = keywordTestStep.getParamValues();
		return createWord(fragments, paramValues);
	}

	private String createWord(List<ActionWordFragment> fragments, List<ActionWordParameterValue> paramValues) {
		StringBuilder builder = new StringBuilder();
		int index = 0;
		for (ActionWordFragment fragment : fragments) {
			if (ActionWordText.class.isAssignableFrom(fragment.getClass())){
				ActionWordText text = (ActionWordText) fragment;
				builder.append(text.getText());
			} else {
				builder.append("\"").append(paramValues.get(index).getValue()).append("\"");
				++index;
			}
		}
		return builder.toString();
	}
}
