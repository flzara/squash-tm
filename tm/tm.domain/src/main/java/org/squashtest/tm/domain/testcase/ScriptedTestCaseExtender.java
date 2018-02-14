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

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * @author Julien Thebault
 */
@Entity
@Table(name = "SCRIPTED_TC_EXTENDER")
@PrimaryKeyJoinColumn(name = "SCRIPTED_TC_EXTENDER_ID")
public class ScriptedTestCaseExtender {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedTestCaseExtender.class);
	private static final String CLASS_NAME = "org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender";
	private static final String SIMPLE_CLASS_NAME = "ScriptedTestCaseExtender";

	@Id
	@Column(name = "SCRIPTED_TC_EXTENDER_ID")
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "scripted_tc_extender_scripted_tc_extender_id_seq")
	@SequenceGenerator(name = "scripted_tc_extender_scripted_tc_extender_id_seq", sequenceName = "scripted_tc_extender_scripted_tc_extender_id_seq", allocationSize = 1)
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	private ScriptedTestCaseKind kind;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String script = "";

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(name = "TEST_CASE_ID")
	private TestCase testCase;

	public ScriptedTestCaseExtender() {
	}

	public ScriptedTestCaseExtender(TestCase testCase, ScriptedTestCaseKind kind) {
		this.testCase = testCase;
		this.kind = kind;
	}

	public ScriptedTestCaseExtender(TestCase testCase, String kind) {
		this.testCase = testCase;
		if (StringUtils.isNotBlank(kind)) {
			ScriptedTestCaseKind scriptedTestCaseKind = EnumUtils.getEnum(ScriptedTestCaseKind.class, kind);
			if (scriptedTestCaseKind == null) {
				throw new IllegalArgumentException("Unknown kind of scripted test case : " + kind);
			}
			this.kind = scriptedTestCaseKind;
		} else {
			throw new IllegalArgumentException("Scripted test case MUST have a not null kind");
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ScriptedTestCaseKind getKind() {
		return kind;
	}

	public void setKind(ScriptedTestCaseKind kind) {
		this.kind = kind;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public TestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(TestCase testCase) {
		this.testCase = testCase;
	}


}

