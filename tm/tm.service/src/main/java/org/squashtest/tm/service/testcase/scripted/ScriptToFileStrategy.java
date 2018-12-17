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
import org.squashtest.tm.domain.testcase.ScriptedTestCaseExtender;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseLanguage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseKind;


/**
 * These are the strategies that comes into play when a ScriptedTestCase is dumped to a file.
 *
 */
public enum ScriptToFileStrategy {

	GHERKIN_STRATEGY(){
		@Override
		public TestCaseKind getHandledKind() {
			return TestCaseKind.GHERKIN;
		}

		@Override
		public String getExtension(){
			return "feature";
		}

		@Override
		public String getWritableFileContent(TestCase testCase) {
			if (! canHandle(testCase)){
				throw new IllegalArgumentException("This strategy handles Gherkin test cases, but ");
			}

			ScriptedTestCaseExtender extender = testCase.getScriptedTestCaseExtender();

			// For now, no metadata are defined so we just return the script as is
			return extender.getScript();
		}
	};




	// ******************* public API ******************************


	public static final int FILENAME_MAX_SIZE = 100;




	/**
	 * Selects the correct instance of Strategy for the given language
	 *
	 * @param kind
	 * @return
	 */
	public static ScriptToFileStrategy strategyFor(TestCaseKind kind){
		ScriptToFileStrategy strategy = null;
		switch(kind){
			case GHERKIN: strategy = GHERKIN_STRATEGY; break;
			default : throw new IllegalArgumentException("unimplemented script dumping strategy for test case kind : '"+kind+"'");
		}
		return strategy;
	}



	 // ---- language-specific methods -------

	/**
	 * Returns the kind of TestCase this strategy is for.
	 *
	 * @return
	 */
	public abstract TestCaseKind getHandledKind();


	/**
	 * Returns the extension usually associated to files written in this language.
	 *
	 * @return
	 */
	public abstract String getExtension();


	/**
	 * <p>Returns the content of the script, possibly with additional metadata (eg comments)
	 * for Squash TM or Squash TA use.</p>
	 *
	 * <p>Throws an IllegalArgumentException if the TestCase is not a scripted test case, or if this
	 * strategy is not suitable for that test case.</p>
	 * @param testCase
	 * @return
	 */
	public abstract String getWritableFileContent(TestCase testCase);


	// --------- common methods --------------


	/**
	 * Returns whether this strategy can handle that test case (ie, the test case
	 * is a scripted test case and corresponds to the scripting language).
	 * Is equivalent to (this == ScriptToFileStrategy.strategyFor(testCase))
	 *
	 * @param testCase
	 * @return
	 */
	public boolean canHandle(TestCase testCase){
		TestCaseKind kind = testCase.getKind();
		return testCase.isScripted() &&
				   kind == getHandledKind();
	}

	/**
	 * Creates the String pattern that helps retrieving a file in a repository.
	 * The resulting expression will match strings that start with the test case id,
	 * followed by <i>possibly</i> an underscore and the rest of the filename, and
	 * then ends with .extension. The two possibilities corresponds to the nominal
	 * and backup filenames, see the filename generation methods for details.
	 *
	 * @param testCase
	 * @return
	 */
	public String buildFilenameMatchPattern(TestCase testCase){
		Long id = testCase.getId();
		String extension = getExtension();

		return String.format("^%d(_.*)?\\.%s", id, extension);

	}

	/**
	 * Generates a nominal filename for dumping a scripted test case to filesystem
	 *
	 * @param testCase
	 * @return
	 */
	public String createFilenameFor(TestCase testCase){

		String extension = getExtension();

		String normalized = baseNameFromTestCase(testCase);

		// make sure that the final filename will not exceed the MAX_SIZE limit once the extension is added
		String caped = StringUtils.substring(normalized, 0, FILENAME_MAX_SIZE - (extension.length() +1));

		return caped + "." + extension;

	}

	/**
	 * Generates a much shorter filename. It is used as a backup
	 * for Windows-based systems that cannot handle file absolute path
	 * length beyond 255.
	 *
	 * @param testCase
	 * @return
	 */
	public String backupFilenameFor(TestCase testCase){
		return testCase.getId() + "." + getExtension();
	}




	// ****************** private API **********************


	private static final String ILLEGAL_PATTERN = "[^a-zA-Z0-9\\_\\-]";

	/**
	 * <p>
	 *  Normalizes a TestCase name by replacing accented characters by their ascii counterpart, removing other otherwise
	 * 	illegal characters with an underscore and truncating to a decent size, and finally prefixing by its id.
	 * </p>
	 *
	 * <p>
	 *     Apologies to users that write in Russian, Chinese and other non latin alphabets, because the
	 *     generated filenames will probably consist of endless strings of underscores -_-
	 * </p>
	 *
	 * @param testCase
	 * @return
	 */
	private String baseNameFromTestCase(TestCase testCase){
		String name = testCase.getName();
		Long id = testCase.getId();
		String deaccented = StringUtils.stripAccents(name);
		String normalized = deaccented.replaceAll(ILLEGAL_PATTERN, "_");

		return id + "_" + normalized;
	}

}
