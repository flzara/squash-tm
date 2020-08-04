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

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.actionword.ConsumerForActionWordFragmentVisitor;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.domain.bdd.ActionWordFragment;
import org.squashtest.tm.domain.bdd.ActionWordParameter;
import org.squashtest.tm.domain.bdd.ActionWordParameterValue;
import org.squashtest.tm.domain.bdd.Keyword;
import org.squashtest.tm.domain.execution.ExecutionStep;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.persistence.EnumType.STRING;
import static org.squashtest.tm.domain.bdd.util.ActionWordUtil.updateNumberValue;

@Entity
@PrimaryKeyJoinColumn(name = "TEST_STEP_ID")
public class KeywordTestStep extends TestStep {

	@NotNull
	@Enumerated(STRING)
	@Column(name = "KEYWORD")
	private Keyword keyword;

	@NotNull
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
	@JoinColumn(name = "ACTION_WORD_ID")
	private ActionWord actionWord;

	@NotNull
	@OneToMany(mappedBy = "keywordTestStep", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ActionWordParameterValue> paramValues = new ArrayList<>();

	@Transient
	private boolean hasTCParam = false;

	public KeywordTestStep() {
	}

	public KeywordTestStep(Keyword paramKeyword, ActionWord paramActionWord) {
		if (paramKeyword == null) {
			throw new IllegalArgumentException("Keyword cannot be null.");
		}
		if (paramActionWord == null) {
			throw new IllegalArgumentException("Action word cannot be null.");
		}
		this.keyword = paramKeyword;
		this.actionWord = paramActionWord;
	}

	@Override
	public TestStep createCopy() {
		return new KeywordTestStep(this.getKeyword(), this.getActionWord());
	}

	@Override
	public void accept(TestStepVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public List<ExecutionStep> createExecutionSteps(Dataset dataset, MessageSource messageSource, Locale locale) {
		List<ExecutionStep> res = new ArrayList<>(1);
		ExecutionStep executionStep = new ExecutionStep(this, dataset, messageSource, locale);
		res.add(executionStep);
		return res;
	}

	public String writeTestStepActionWordScript(boolean escapeArrows) {
		ActionWord actionWord = getActionWord();
		List<ActionWordFragment> fragments = actionWord.getFragments();
		List<ActionWordParameterValue> paramValues = getParamValues();
		return generateStepScriptFromActionWordFragments(fragments, paramValues, escapeArrows);
	}

	private String generateStepScriptFromActionWordFragments(List<ActionWordFragment> fragments, List<ActionWordParameterValue> paramValues, boolean escapeArrows) {
		StringBuilder stepBuilder = new StringBuilder();
		Consumer<ActionWordParameter> consumer = parameter ->
			appendParamValueToGenerateScript(parameter, paramValues, stepBuilder, escapeArrows);

		ConsumerForActionWordFragmentVisitor visitor = new ConsumerForActionWordFragmentVisitor(consumer, stepBuilder);

		for (ActionWordFragment fragment : fragments) {
			fragment.accept(visitor);
		}
		return stepBuilder.toString();
	}

	private void appendParamValueToGenerateScript(ActionWordParameter param, List<ActionWordParameterValue> paramValues, StringBuilder stepBuilder, boolean escapeArrows) {
		Optional<ActionWordParameterValue> paramValue =
			paramValues.stream().filter(pv -> pv.getActionWordParam() != null && pv.getActionWordParam().getId().equals(param.getId())).findAny();
		paramValue.ifPresent(
			actionWordParameterValue -> updateBuilderWithParamValue(stepBuilder, actionWordParameterValue, escapeArrows)
		);
	}

	private void updateBuilderWithParamValue(StringBuilder builder, ActionWordParameterValue actionWordParameterValue, boolean escapeArrows) {
		String paramValue = actionWordParameterValue.getValue();
		if ("\"\"".equals(paramValue)) {
			builder.append(paramValue);
			return;
		}

		Pattern pattern = Pattern.compile("<[^\"]+>");
		Matcher matcher = pattern.matcher(paramValue);
		if (matcher.matches()) {
			hasTCParam = true;
			if(escapeArrows) {
				String replaceHTMLCharactersStr = StringEscapeUtils.escapeHtml4(paramValue);
				builder.append(replaceHTMLCharactersStr);
			} else {
				builder.append(paramValue);
			}
			return;
		}
		String updatedParamValue = updateNumberValue(paramValue);
		builder.append(updatedParamValue);
	}

	public boolean hasTCParam() {
		return hasTCParam;
	}


	@Override
	public void setTestCase(@NotNull TestCase testCase) {

		TestCaseVisitor testCaseVisitor = new TestCaseVisitor() {
			@Override
			public void visit(TestCase testCase) {
				throw new IllegalArgumentException("Cannot add a Keyword Test Step outside a Keyword Test Case");
			}

			@Override
			public void visit(KeywordTestCase keywordTestCase) {
			}

			@Override
			public void visit(ScriptedTestCase scriptedTestCase) {
				throw new IllegalArgumentException("Cannot add a Keyword Test Step outside a Keyword Test Case");
			}
		};
		testCase.accept(testCaseVisitor);
		super.setTestCase(testCase);
	}

	public Keyword getKeyword() {
		return keyword;
	}

	public ActionWord getActionWord() {
		return actionWord;
	}

	public void setKeyword(Keyword keyword) {
		this.keyword = keyword;
	}

	public void setActionWord(ActionWord actionWord) {
		this.actionWord = actionWord;
	}

	public List<ActionWordParameterValue> getParamValues() {
		return paramValues;
	}

	public void setParamValues(List<ActionWordParameterValue> paramValues) {
		this.paramValues = paramValues;
	}

	public void addParamValues(ActionWordParameterValue value) {
		this.paramValues.add(value);
	}

}
