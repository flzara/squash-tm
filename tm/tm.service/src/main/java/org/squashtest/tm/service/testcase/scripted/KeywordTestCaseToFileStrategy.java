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
package org.squashtest.tm.service.testcase.scripted;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.bdd.BddImplementationTechnology;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.service.internal.testcase.bdd.BddScriptWriter;
import org.squashtest.tm.service.internal.testcase.bdd.CucumberScriptWriter;
import org.squashtest.tm.service.internal.testcase.bdd.RobotScriptWriter;

/**
 * These are the strategies that comes into play when a KeywordTestCase is dumped to a file.
 *
 */
public enum KeywordTestCaseToFileStrategy {

	CUCUMBER_STRATEGY() {

		@Override
		public String getExtension() {
			return "feature";
		}

		@Override
		public BddScriptWriter getScriptWriter() {
			return new CucumberScriptWriter();
		}

	},
	ROBOT_STRATEGY() {

		@Override
		public String getExtension() {
			return "robot";
		}

		@Override
		public BddScriptWriter getScriptWriter() {
			return new RobotScriptWriter();
		}

	};

	// ******************* public API ******************************

	public static final int FILENAME_MAX_SIZE = 100;

	/**
	 * Selects the correct instance of Strategy for the given language.
	 *
	 * @param bddImplementationTechnology The {@linkplain BddImplementationTechnology}
	 * @return The corresponding Strategy
	 */
	public static KeywordTestCaseToFileStrategy strategyFor(BddImplementationTechnology bddImplementationTechnology) {
		KeywordTestCaseToFileStrategy strategy = null;
		switch(bddImplementationTechnology){
			case CUCUMBER:
				strategy = CUCUMBER_STRATEGY;
				break;
			case ROBOT:
				strategy = ROBOT_STRATEGY;
				break;
			default:
				throw new IllegalArgumentException("Unimplemented script dumping strategy for bdd implementation: '" +  bddImplementationTechnology + "'");
		}
		return strategy;
	}


	/* ----- Language-Specific Methods ----- */

	/**
	 * Returns the extension usually associated to files written in this language.
	 *
	 * @return The extension as a String.
	 */
	public abstract String getExtension();

	/**
	 * Returns a suitable {@linkplain BddScriptWriter} for this strategy.
	 * @return
	 */
	public abstract BddScriptWriter getScriptWriter();


	/* ----- Common Methods ----- */

	/**
	 * Creates the String pattern that helps retrieving a file in a repository.
	 * The resulting expression can identity strings that ends with the test case id
	 * followed by <i>possibly</i> an underscore and the rest of the filename, and
	 * then ends with .extension. The two possibilities corresponds to the nominal
	 * and backup filenames, see the filename generation methods for details.
	 *
	 * @param testCase the test case
	 * @return The Pattern as a String
	 */
	public String buildFilenameMatchPattern(KeywordTestCase testCase){
		Long id = testCase.getId();
		String extension = getExtension();
		return String.format("%d(_.*)?\\.%s", id, extension);
	}

	/**
	 * Generates a nominal file name for dumping a KeywordTestCase to filesystem
	 *
	 * @param testCase the test case
	 * @return the nominal file name
	 */
	public String createFilenameFor(KeywordTestCase testCase){
		String extension = getExtension();
		String normalized = baseNameFromTestCase(testCase);
		// make sure that the final filename will not exceed the MAX_SIZE limit once the extension is added
		String caped = StringUtils.substring(normalized, 0, FILENAME_MAX_SIZE - (extension.length() +1));
		return caped + "." + extension;
	}

	/**
	 * Generates a much shorter filename.<br\>
	 * It is used as a backup for Windows-based systems that cannot handle file absolute path length beyond 255.
	 *
	 * @param testCase the test case
	 * @return the backup file name
	 */
	public String backupFilenameFor(KeywordTestCase testCase){
		return testCase.getId() + "." + getExtension();
	}

	/**
	 * Returns the content of the script, possibly with additional metadata (eg comments)
	 * for Squash TM or Squash TF use.
	 *
	 * @param testCase the test case which script will be generated
	 * @param messageSource the message source for potential translation
	 * @param escapeArrows whether to escape arrow symbols (for html purpose)
	 * @return the content of the Script
	 */
	public String getWritableFileContent(KeywordTestCase testCase, MessageSource messageSource, boolean escapeArrows) {
		return getScriptWriter().writeBddScript(testCase, messageSource, escapeArrows);
	}

	/* ----- Private Api ----- */

	private static final String ILLEGAL_PATTERN = "[^a-zA-Z0-9\\_\\-]";

	/**
	 * <p>
	 *  Normalizes a KeywordTestCase name by replacing accented characters by their ascii counterpart,
	 *  removing other otherwise illegal characters with an underscore and truncating to a decent size,
	 *  and finally prefixing by its id.
	 * </p>
	 * <p>
	 *     Apologies to users that write in Russian, Chinese and other non latin alphabets, because the
	 *     generated filenames will probably consist of endless strings of underscores -_-
	 * </p>
	 *
	 * @param testCase the test case
	 * @return
	 */
	private String baseNameFromTestCase(KeywordTestCase testCase){
		String name = testCase.getName();
		Long id = testCase.getId();
		String deaccented = StringUtils.stripAccents(name);
		String normalized = deaccented.replaceAll(ILLEGAL_PATTERN, "_");
		return id + "_" + normalized;
	}

}
