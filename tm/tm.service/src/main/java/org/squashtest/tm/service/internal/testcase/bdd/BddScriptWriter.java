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
package org.squashtest.tm.service.internal.testcase.bdd;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.KeywordTestStep;

import java.util.Locale;

public interface BddScriptWriter {

	/**
	 * Given a KeywordTestCase, write its corresponding bdd script.
	 * @param testCase the test case
	 * @param messageSource the message source for potential translation
	 * @param escapeArrows whether to escape arrow symbols
	 * @return the bdd script of the given test case
	 */
	String writeBddScript(KeywordTestCase testCase, MessageSource messageSource, boolean escapeArrows);

	/**
	 * Given a KeywordTestStep, write its corresponding bdd script.
	 * @param testStep test step
	 * @param messageSource the message source for potential translation
	 * @param escapeArrows whether to escape arrow symbols
	 * @return the bdd script of the given test step
	 */
	String writeBddStepScript(KeywordTestStep testStep, MessageSource messageSource, Locale locale, boolean escapeArrows);

}
