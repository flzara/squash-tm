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

import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


@Entity
@DiscriminatorValue("S")
@SecondaryTable(name="SCRIPTED_TEST_CASE", pkJoinColumns={
	@PrimaryKeyJoinColumn(name="TCLN_ID", referencedColumnName="TCLN_ID")
})
public class ScriptedTestCase extends TestCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedTestCase.class);

	public static final String LANGUAGE_TAG = "# language: ";

	@NotNull
	@Enumerated(EnumType.STRING)
	private ScriptedTestCaseLanguage language = ScriptedTestCaseLanguage.GHERKIN;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String script = "";

	public ScriptedTestCase() {
		super();
	}

	public ScriptedTestCase(@NotNull ScriptedTestCaseLanguage language, String script) {
		this.language = language;
		this.script = script;
	}

	//	public ScriptedTestCase(String scriptLanguage, String locale) {
//		super();
//		if (StringUtils.isNotBlank(scriptLanguage)) {
//			ScriptedTestCaseLanguage scriptedTestCaseLanguage = EnumUtils.getEnum(ScriptedTestCaseLanguage.class, scriptLanguage);
//			if (scriptedTestCaseLanguage == null) {
//				throw new IllegalArgumentException("Unknown scriptLanguage of scripted test case : " + scriptLanguage);
//			}
//			this.language = scriptedTestCaseLanguage;
//			this.populateInitialScript(scriptLanguage, locale);
//		} else {
//			throw new IllegalArgumentException("Scripted test case MUST have a not null scriptLanguage");
//		}
//	}

	//For SquashTM 1.18 this is for Gherkin only for now
	//if sometime another script language need to be handled, you will probably need to subclass with a discriminator column...
	private void populateInitialScript(String scriptLanguage, String locale) {
		LOGGER.info("Try to populate script with script language {} and locale {}.", scriptLanguage, locale);
		StringBuilder sb = new StringBuilder();
		sb.append(LANGUAGE_TAG)
			.append(locale)
			.append("\n");

		GherkinDialectProvider gherkinDialectProvider = new GherkinDialectProvider(locale);
		GherkinDialect defaultDialect = gherkinDialectProvider.getDefaultDialect();
		String featureKeyword = defaultDialect.getFeatureKeywords().get(0);
		sb.append(featureKeyword)
			.append(": ")
			.append(this.getName());

		this.setScript(sb.toString());
	}


	public ScriptedTestCase createCopy() {
		throw new UnsupportedOperationException();
//		ScriptedTestCaseExtender copy = new ScriptedTestCaseExtender();
//		copy.setScript(this.getScript());
//		copy.setLanguage(this.getLanguage());
//		return copy;
	}

	/**
	 * Return this TestCase script appended with some needed metadata.
	 * <br/>
	 * Note: The metadata are not translated.
	 * If it is needed, this operation and the translation have to be done in the service layer.
	 * @return This TestCase script appended with metadata.
	 */
	public String computeScriptWithAppendedMetadata() {
		StringBuilder sb = new StringBuilder(this.script);
		sb.insert(0, "# Test case importance: " + this.getImportance() + "\n");
		sb.insert(0, "# Automation status: " + this.getAutomationRequest().getRequestStatus() + "\n");
		sb.insert(0, "# Automation priority: " + this.getAutomationRequest().getAutomationPriority() + "\n");
		return sb.toString();
	}

	/**
	 * Creates a scripted test case which non-collection, non-primitive type fields are set to null.
	 *
	 * @return
	 * @param language
	 * @param script
	 */
	public static ScriptedTestCase createBlankScriptedTestCase(ScriptedTestCaseLanguage language, String script) {
		ScriptedTestCase res = new ScriptedTestCase();
		res.importanceAuto = null;
		res.prerequisite = null;
		res.reference = null;
		res.nature = null;
		res.type = null;
		res.importance = null;
		res.status = null;
		res.automatable = null;
		res.uuid = null;
		res.language = language;
		res.script = script;
		return res;
	}

	public ScriptedTestCaseLanguage getLanguage() {
		return language;
	}

	public void setLanguage(ScriptedTestCaseLanguage language) {
		this.language = language;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	protected boolean isAutomatedInWorkflow() {
		boolean isAutomated =false;
		if (automatable.equals(TestCaseAutomatable.Y) && AutomationRequestStatus.AUTOMATED.equals(automationRequest.getRequestStatus())) {
			isAutomated = automatedTest != null && getProject().getScmRepository() != null;
		}
		return isAutomated;
	}

	@Override
	public void accept(TestCaseVisitor visitor) {
		visitor.visit(this);
	}
}
