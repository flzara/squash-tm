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
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;


@Entity
@PrimaryKeyJoinColumn(name = "TCLN_ID")
public class ScriptedTestCase extends TestCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedTestCase.class);

	public static final String LANGUAGE_TAG = "# language: ";

	public static final String FEATURE_EXTENSION = "feature";

	private static final String ILLEGAL_PATTERN = "[^a-zA-Z0-9\\_\\-]";

	public static final int FILENAME_MAX_SIZE = 100;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String script = "";

	public ScriptedTestCase() {
		super();
	}

	public ScriptedTestCase(String name) {
		super();
		this.setName(name);
		populateInitialScript(LocaleContextHolder.getLocale().getLanguage());
	}

	//For SquashTM 1.18 this is for Gherkin only for now
	//if sometime another script language need to be handled, you will probably need to subclass with a discriminator column...
	public void populateInitialScript(String locale) {
		LOGGER.info("Try to populate script with script language {} and locale {}.", locale);
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

	@Override
	public ScriptedTestCase createCopy() {
		ScriptedTestCase copy = new ScriptedTestCase();
		populateCopiedTestCaseAttributes(copy);
		copy.setScript(this.getScript());
		return copy;
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
		sb.insert(0, "# Automation priority: " + this.getAutomationRequest().getAutomationPriority() + "\n");
		return sb.toString();
	}

	/**
	 * Creates a scripted test case which non-collection, non-primitive type fields are set to null.
	 *
	 * @return
	 * @param script
	 */
	public static ScriptedTestCase createBlankScriptedTestCase(String script) {
		ScriptedTestCase res = new ScriptedTestCase();
		setAttributesAsNullForBlankTestCase(res);
		res.script = script;
		return res;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	protected boolean isAutomatedInWorkflow() {
		return isActuallyAutomated() &&
			TestCaseAutomatable.Y.equals(automatable) &&
			AutomationRequestStatus.AUTOMATED.equals(automationRequest.getRequestStatus()) &&
			getProject().getScmRepository() != null;
	}

	@Override
	public void accept(TestCaseVisitor visitor) {
		visitor.visit(this);
	}

	// -- Transmission Methods --

	public String createFilename(){
		String normalized = baseNameFromTestCase();

		// make sure that the final filename will not exceed the MAX_SIZE limit once the extension is added
		String caped = StringUtils.substring(normalized, 0, FILENAME_MAX_SIZE - (FEATURE_EXTENSION.length() +1));

		return caped + "." + FEATURE_EXTENSION;
	}

	private String baseNameFromTestCase() {
		String name = this.getName();
		Long id = this.getId();
		String deAccented = StringUtils.stripAccents(name);
		String normalized = deAccented.replaceAll(ILLEGAL_PATTERN, "_");

		return id + "_" + normalized;
	}

	public String buildFilenameMatchPattern(){
		return String.format("%d(_.*)?\\.%s", getId(), FEATURE_EXTENSION);
	}

	public String createBackupFileName() {
		return getId() + "." + FEATURE_EXTENSION;
	}
}
