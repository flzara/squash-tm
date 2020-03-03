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
package org.squashtest.tm.domain.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.Wrapped;
import org.squashtest.tm.domain.testcase.ConsumerForScriptedTestCaseVisitor;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseLanguage;
import org.squashtest.tm.domain.testcase.TestCase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author Julien Thebault
 */
@Entity
@Table(name = "SCRIPTED_EXECUTION_EXTENDER")
public class ScriptedExecutionExtender {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedExecutionExtender.class);
	private static final String CLASS_NAME = "org.squashtest.tm.domain.execution.ScriptedExecutionExtender";
	private static final String SIMPLE_CLASS_NAME = "ScriptedExecutionExtender";

	@Id
	@Column(name = "SCRIPTED_EXECUTION_EXTENDER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "scripted_execution_extender_scripted_execution_extender_id_seq")
	@SequenceGenerator(name = "scripted_execution_extender_scripted_execution_extender_id_seq", sequenceName = "scripted_execution_extender_scripted_execution_extender_id_seq", allocationSize = 1)
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	private ScriptedTestCaseLanguage language;

	private String scriptName = "";

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(name = "EXECUTION_ID")
	private Execution execution;

	public ScriptedExecutionExtender() {
	}

	public ScriptedExecutionExtender(Execution execution) {
		this.execution = execution;
		TestCase referencedTestCase = execution.getReferencedTestCase();

		Wrapped<ScriptedTestCaseLanguage> script = new Wrapped<>();

		ConsumerForScriptedTestCaseVisitor testCaseVisitor = new ConsumerForScriptedTestCaseVisitor(
			scriptedTestCase -> {}, // NOOP
			new IllegalArgumentException("Can't create an execution extender if test case doesn't exist or is not scripted."));
		referencedTestCase.accept(testCaseVisitor);
		this.language = ScriptedTestCaseLanguage.GHERKIN;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ScriptedTestCaseLanguage getLanguage() {
		return language;
	}

	public void setLanguage(ScriptedTestCaseLanguage language) {
		this.language = language;
	}

	public Execution getExecution() {
		return execution;
	}

	public void setExecution(Execution execution) {
		this.execution = execution;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
}

