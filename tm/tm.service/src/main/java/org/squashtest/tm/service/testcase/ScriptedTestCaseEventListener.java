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
package org.squashtest.tm.service.testcase;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.project.QProject;
import org.squashtest.tm.domain.scm.QScmRepository;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.*;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.tf.automationrequest.QAutomationRequest;
import org.squashtest.tm.service.internal.tf.event.AutomationRequestStatusChangeEvent;
import org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy;

import static com.querydsl.core.group.GroupBy.*;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;


@Component
public class ScriptedTestCaseEventListener implements ApplicationListener<AutomationRequestStatusChangeEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedTestCaseEventListener.class);

	@PersistenceContext
	private EntityManager em;


	/**
	 * If the new status is suitable for commit (eg, TRANSMITTED),
	 * all the scripted test case will be committed to the scm repository.
	 * If there are repositories to commit to and scripts that needs it.
	 *
	 * @param event
	 */
	@Override
	public void onApplicationEvent(AutomationRequestStatusChangeEvent event) {

		LOGGER.debug("intercepted AutomationRequestStatusChangeEvent");
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("new status : '{}' for request ids : '{}'", event.getNewStatus(), event.getAutomationRequestIds());
		}

		if (newStatusRequiresCommit(event.getNewStatus())){

			Collection<Long> requestIds = event.getAutomationRequestIds();

			Map<ScmRepository, Set<TestCaseAndScript>> scriptsGroupedByScm = findScriptsGroupedByRepoFromRequestId(requestIds);

			for (Map.Entry<ScmRepository, Set<TestCaseAndScript>> entry : scriptsGroupedByScm.entrySet()){

				ScmRepository scm = entry.getKey();
				Set<TestCaseAndScript> tcAndScripts = entry.getValue();

				writeToScm(scm, tcAndScripts);
			}

		}
		else{
			LOGGER.debug("no action required");
		}


	}


	// ************* internals **********************


	// for now the only status considered is TRANSMITTED, but that could change in the future
	private boolean newStatusRequiresCommit(AutomationRequestStatus newStatus){
		return newStatus == AutomationRequestStatus.TRANSMITTED;
	}


	/**
	 * Will create the files in the scm if they don't exist, then update the content.
	 * A lock is acquired on the SCM for the whole operation beforehand.
	 *
	 * @param scm
	 * @param tcAndScripts
	 */
	private void writeToScm(ScmRepository scm, Set<TestCaseAndScript> tcAndScripts){

		doWithLock(scm, () ->{

			LOGGER.trace("committing tests to scm : '{}'", scm.getName());

			for (TestCaseAndScript pair : tcAndScripts){

				if (LOGGER.isTraceEnabled()){
					LOGGER.trace("looking for existing physical file for test case '{}'", pair.getTestCase().getId());
				}

				File testFile = locateOrCreateTest(scm, pair);

				// at this point the file is created without error

				// TODO : remplire le fichier
				throw new NotImplementedException("a faire");

			}
		});

	}


	// TODO : mettre un vrai lock dessus, voir avec Johan
	private void doWithLock(ScmRepository scm, Action action){

		// acquire the lock

		try {
			// now do the work
			action.act();
		}
		finally{
			// finally, release the lock
		}


	}

	/*
	 *returns all the ScriptedTestCaseExtender putatively targeted by the event, grouped by ScmRepository they should be committed into
	 */
	private Map<ScmRepository, Set<TestCaseAndScript>> findScriptsGroupedByRepoFromRequestId(Collection<Long> automationRequestIds){

		if (automationRequestIds.isEmpty()){
			return Collections.emptyMap();
		}

		QAutomationRequest automationRequest = QAutomationRequest.automationRequest;
		QTestCase testCase = QTestCase.testCase;
		QScriptedTestCaseExtender script = QScriptedTestCaseExtender.scriptedTestCaseExtender;
		QProject project = QProject.project1;
		QScmRepository scm = QScmRepository.scmRepository;


		return new JPAQueryFactory(em)
				   .select(scm, testCase, script)
				   .from(automationRequest)
				   .join(automationRequest.testCase, testCase)
				   .join(testCase.scriptedTestCaseExtender, script)
				   .join(testCase.project, project)
				   .join(project.scmRepository, scm)
				   .where(automationRequest.id.in(automationRequestIds))
				   .transform(groupBy(scm).as(set(
					   Projections.bean(TestCaseAndScript.class, testCase, testCase.scriptedTestCaseExtender))
				   ));

	}


	/********************************************************************************************************
	 * /!\ /!\/!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\
	 *
	 * The following methods below this line are intended to run within the scope of the scm filelock.
	 * This is hard to enforce so please be careful.
	 *
	 * /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\
	********************************************************************************************************/

	private File locateOrCreateTest(ScmRepository scm, TestCaseAndScript tcAndScript){
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("attempting to locate physical script file for test '{}' in the scm", tcAndScript.getTestCase().getId());
		}

		File testfile = null;

		Optional<File> maybeTestFile = locateTest(scm, tcAndScript);

		if (maybeTestFile.isPresent()){
			testfile = maybeTestFile.get();

			LOGGER.trace("found file : '{}'", testfile.getAbsolutePath());

		}
		else{
			LOGGER.trace("file not found, attempting to create a new one");

			// roundtrip 1 : attempt the creation with the normal filename
			try{

				testfile = createTestNominal(scm, tcAndScript);

			}
			catch(IOException ex){
				if (SystemUtils.IS_OS_WINDOWS){
					// maybe the failure is due to long absolute filename
					LOGGER.trace("failed to create file due to IOException, attempting with the backup filename");

					try{
						// try again with the backup name
						testfile = createTestBackup(scm, tcAndScript);

					}
					catch (IOException rex){
						// daaaaaamn, now we are in serious troubles
						throw new RuntimeException(ex);
					}

				}
				else{
					// weeell, what should I do ?
					throw new RuntimeException(ex);
				}
			}

		}

		return testfile;
	}

	/**
	 * Attends to retrieve the test file in the repository for a given test,
	 * using a predicate. The result is returned as an Optional.
	 *
	 *
	 * @param scm
	 * @param tcAndScript
	 * @return
	 */
	private Optional<File> locateTest(ScmRepository scm, TestCaseAndScript tcAndScript){

		TestCase testCase = tcAndScript.getTestCase();
		ScriptToFileStrategy strategy = tcAndScript.getStrategy();

		String pattern = strategy.buildFilenameMatchPattern(testCase);

		File workingFolder = scm.getWorkingFolder();

		Collection<File> files = FileUtils.listFiles(workingFolder, new FilenamePatternFilter(pattern), FileFilterUtils.trueFileFilter());

		if (files.size() > 2){
			LOGGER.warn("found two files that are possible candidates for test '{}'. This is an unexpected situation. " +
							"The commit routine will proceed with the first file in lexicographic order.", testCase.getId());
		}

		return files.stream().sorted(Comparator.comparing(File::getName)).findFirst();

	}


	/**
	 * Attempts to create the test file with the preferred name.
	 *
	 * @param scm
	 * @param tcAndScript
	 * @return
	 * @throws IOException
	 */
	private File createTestNominal(ScmRepository scm, TestCaseAndScript tcAndScript) throws IOException{
		TestCase testCase = tcAndScript.getTestCase();
		ScriptToFileStrategy strategy = tcAndScript.getStrategy();

		String filename = strategy.createFilenameFor(testCase);

		return createTest(scm, filename);
	}

	/**
	 * Attempts to create the test file with the backup name. Backup means : this is the filename we may need
	 * in the Windows world.
	 *
	 * @param scm
	 * @param tcAndScript
	 * @return
	 * @throws IOException
	 */
	private File createTestBackup(ScmRepository scm, TestCaseAndScript tcAndScript) throws IOException{
		TestCase testCase = tcAndScript.getTestCase();
		ScriptToFileStrategy strategy = tcAndScript.getStrategy();

		String filename = strategy.backupFilenameFor(testCase);

		return createTest(scm, filename);
	}



	/**
	 * Creates a new file with the given filename in the working folder of the scm
	 *
	 * @param scm
	 * @param filename
	 * @return
	 */
	private File createTest(ScmRepository scm, String filename) throws IOException{

		File workfolder = scm.getWorkingFolder();

		File newFile = new File(workfolder, filename);

		if (newFile.exists()){
			LOGGER.warn("retrieved physical file '{}' while in the file creation routine... it should have been detected earlier. This is an abnormal situation. " +
							"Anyway, this file ",
				newFile.getAbsolutePath());
		}
		else{
			newFile.createNewFile();
			LOGGER.trace("new file created : '{}'", newFile.getAbsolutePath());
		}

		return newFile;

	}



	// ********************************* internal classes etc **********************************

	// load and group the test case and the script together
	// it may help prevent n+1 requests instead of calling script#getTestCase()
	public static final class TestCaseAndScript{
		private TestCase testCase;
		private ScriptedTestCaseExtender scriptedTestCaseExtender;

		public TestCaseAndScript() {
			super();
		}

		public TestCaseAndScript(TestCase testCase, ScriptedTestCaseExtender scriptedTestCaseExtender) {
			this.testCase = testCase;
			this.scriptedTestCaseExtender = scriptedTestCaseExtender;
		}

		public TestCase getTestCase() {
			return testCase;
		}

		public void setTestCase(TestCase testCase) {
			this.testCase = testCase;
		}

		public ScriptedTestCaseExtender getScriptedTestCaseExtender() {
			return scriptedTestCaseExtender;
		}

		public void setScriptedTestCaseExtender(ScriptedTestCaseExtender scriptedTestCaseExtender) {
			this.scriptedTestCaseExtender = scriptedTestCaseExtender;
		}

		public ScriptToFileStrategy getStrategy(){
			return ScriptToFileStrategy.strategyFor(scriptedTestCaseExtender.getLanguage());
		}

	}


	@FunctionalInterface
	private interface Action{
		void act();
	}

	private static class FilenamePatternFilter implements IOFileFilter{

		private String pattern;

		FilenamePatternFilter(String pattern){
			this.pattern = pattern;
		}

		@Override
		public boolean accept(File file) {
			String filename = file.getName();
			return filename.matches(pattern);
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.matches(pattern);
		}
	}

}
