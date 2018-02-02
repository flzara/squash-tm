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
package org.squashtest.tm.web.internal.controller.testcase;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCaseExecutionMode;
import org.squashtest.tm.web.internal.helper.InternationalizableLabelFormatter;
import org.squashtest.tm.web.internal.model.builder.EnumJeditableComboDataBuilder;

import javax.inject.Inject;

// XSS OK
@Component
@Scope("prototype")
public class TestCaseModeJeditableComboDataBuilder extends EnumJeditableComboDataBuilder<TestCaseExecutionMode, TestCaseModeJeditableComboDataBuilder> {

	public TestCaseModeJeditableComboDataBuilder() {
		super();
		TestCaseExecutionMode[] modes = new TestCaseExecutionMode[2];
		modes[0] = TestCaseExecutionMode.AUTOMATED;
		modes[1] = TestCaseExecutionMode.MANUAL;
		setModel(modes);
	}

	@Inject
	public void setLabelFormatter(InternationalizableLabelFormatter formatter) {
		super.setLabelFormatter(formatter);
	}
}
