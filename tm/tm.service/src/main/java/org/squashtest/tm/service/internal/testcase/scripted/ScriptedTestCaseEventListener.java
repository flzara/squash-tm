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
package org.squashtest.tm.service.internal.testcase.scripted;

import com.google.common.base.Functions;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
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
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.stream.Collectors;


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

			LOGGER.debug("committing test scripts to repositories if needed");

			Collection<Long> requestIds = event.getAutomationRequestIds();

			Map<ScmRepository, Set<TestCaseAndScript>> scriptsGroupedByScm = findScriptsGroupedByRepoFromRequestId(requestIds);

			for (Map.Entry<ScmRepository, Set<TestCaseAndScript>> entry : scriptsGroupedByScm.entrySet()){

				ScmRepository scm = entry.getKey();
				Set<TestCaseAndScript> tcAndScripts = entry.getValue();

				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("committing {} files to repository '{}'", tcAndScripts.size(), scm.getName());
				}

				exportToScm(scm, tcAndScripts);
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



	// TODO : mettre un vrai lock dessus, voir avec Johan
	private void doWithLock(ScmRepository scm, Action action){

		// acquire the lock

		try {
			// now do the work
			action.act();
		}
		finally{
			// finally, release the lock
			// also invoke the plugin for actual commit/push
		}


	}

	/**
	 * Will create the files in the scm if they don't exist, then update the content.
	 * A lock is acquired on the SCM for the whole operation beforehand.
	 *
	 * @param scm
	 * @param tcAndScripts
	 */
	private void exportToScm(ScmRepository scm, Set<TestCaseAndScript> tcAndScripts){

		doWithLock(scm, () ->{

			LOGGER.trace("committing tests to scm : '{}'", scm.getName());

			TestListing listing = new TestListing(scm);

			for (TestCaseAndScript pair : tcAndScripts){

				File testFile = listing.locateOrCreateTestFile(pair);

				// at this point the file is created without error
				// lets fill the file with the script content
				printToFile(testFile, pair);

			}
		});

	}


	/*
	 *	Returns all the ScriptedTestCaseExtender putatively targeted by the event, grouped by ScmRepository they should be committed into
	 */
	private Map<ScmRepository, Set<TestCaseAndScript>> findScriptsGroupedByRepoFromRequestId(Collection<Long> automationRequestIds){

		LOGGER.debug("looking for repositories and the scripts that should be commited into them");


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




	private void printToFile(File dest, TestCaseAndScript tcAndScript){
		ScriptToFileStrategy strategy = tcAndScript.getStrategy();
		String content = strategy.getWritableFileContent(tcAndScript.getTestCase());


		try {
			try {
				// try first with UTF-8
				FileUtils.write(dest, content, Charset.forName("UTF-8"));
			} catch (UnsupportedCharsetException ex) {
				// try again with default charset
				FileUtils.write(dest, content);
			}
		}
		catch(IOException ex){
			// waiting for specs about what to do with hardware or permission problems
			throw new RuntimeException(ex);
		}

	}


	// ********************************* internal classes etc **********************************

	// forces the load and group the test case and the script together
	// it may help prevent n+1 requests instead of calling script#getTestCase() or
	// reciprocally testCase.getScriptedTestCaseExtender()
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
			return ScriptToFileStrategy.strategyFor(testCase.getKind());
		}

	}

	/**
	 * p>Encapsulates the operations aimed at searching a file on the scm.  </p>
	 *
	 * <p>It can work by direct I/O calls, or using a cache if limited I/O operations are desired. The cache option
	 * is the default.</p>
	 *
	 * <p>Note : must be used within the scope of a repository lock.</p>
	 *
	 */
	private static final class TestListing{

		private ScmRepository scm;
		private boolean useCache = true;

		// maps filename by File
		private Map<String, File> pathCache;

		TestListing(ScmRepository scm){
			this.scm = scm;
			initCache();
		}

		TestListing(ScmRepository scm, boolean useCache){
			this.scm = scm;
			this.useCache = useCache;
			if (useCache){
				initCache();
			}
		}

		private final void initCache(){
			try {
				// the pathcache maps a File by its filename
				pathCache = scm.listWorkingFolderContent()
							   .stream()
								.collect(Collectors.toMap(
									f -> f.getName(),
									f -> f
								));
							   //.collect(Collectors.toMap(File::getName, Functions.identity())); // doesn't compile, dunnowhy
			}
			catch (IOException ex){
				throw new RuntimeException("cannot list content of scm '"+scm.getName()+"'", ex);
			}
		}


		private File locateOrCreateTestFile(TestCaseAndScript tcAndScript){
			
			if (LOGGER.isTraceEnabled()){
				LOGGER.trace("attempting to locate physical script file for test '{}' in the scm", tcAndScript.getTestCase().getId());
			}

			File testfile = null;

			Optional<File> maybeTestFile = locateTest(tcAndScript);

			if (maybeTestFile.isPresent()){
				
				testfile = maybeTestFile.get();

				LOGGER.trace("found file : '{}'", testfile.getAbsolutePath());

			}
			else{
				LOGGER.trace("file not found, attempting to create a new one");

				// roundtrip 1 : attempt the creation with the normal filename
				try{

					testfile = createTestNominal(tcAndScript);

				}
				catch(IOException ex){
					if (SystemUtils.IS_OS_WINDOWS){
						// maybe the failure is due to long absolute filename
						LOGGER.trace("failed to create file due to IOException, attempting with the backup filename");

						try{
							// try again with the backup name
							testfile = createTestBackup(tcAndScript);

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
		 * Attends to retrieve the test file in the repository for a given test.
		 * The result is returned as an Optional.
		 *
		 *
		 * @param tcAndScript
		 * @return
		 */
		private Optional<File> locateTest(TestCaseAndScript tcAndScript){

			TestCase testCase = tcAndScript.getTestCase();
			ScriptToFileStrategy strategy = tcAndScript.getStrategy();
			String pattern = strategy.buildFilenameMatchPattern(testCase);

			Collection<File> files;
			if (useCache){
				files = searchInCache(pattern);
			}
			else{
				files = searchOnDrive(pattern);
			}

			// check for the validity of the result
			if (files.size() > 2){
				LOGGER.warn("found two files that are possible candidates for test '{}'. This is an unexpected situation. " +
								"The commit routine will proceed with the first file in lexicographic order.", testCase.getId());
			}

			return files.stream().sorted(Comparator.comparing(File::getName)).findFirst();

		}

		/**
		 * Looks for a test
		 *
		 * @param pattern
		 * @return
		 */
		private Collection<File> searchOnDrive(String pattern){
			File workingFolder = scm.getWorkingFolder();
			return FileUtils.listFiles(workingFolder, new FilenamePatternFilter(pattern), FileFilterUtils.trueFileFilter());
		}

		private Collection<File> searchInCache(String pattern){
			return pathCache.entrySet().stream()
				.filter(entry -> entry.getKey().matches(pattern))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
		}

		/**
		 * Attempts to create the test file with the preferred name.
		 *
		 * @param tcAndScript
		 * @return
		 * @throws IOException
		 */
		private File createTestNominal(TestCaseAndScript tcAndScript) throws IOException{
			TestCase testCase = tcAndScript.getTestCase();
			ScriptToFileStrategy strategy = tcAndScript.getStrategy();

			String filename = strategy.createFilenameFor(testCase);

			return doCreateTest(scm, filename);
		}

		/**
		 * Attempts to create the test file with the backup name. Backup means : this is the filename we may need
		 * in the Windows world.
		 *
		 * @param tcAndScript
		 * @return
		 * @throws IOException
		 */
		private File createTestBackup(TestCaseAndScript tcAndScript) throws IOException{
			TestCase testCase = tcAndScript.getTestCase();
			ScriptToFileStrategy strategy = tcAndScript.getStrategy();

			String filename = strategy.backupFilenameFor(testCase);

			return doCreateTest(scm, filename);
		}



		/**
		 * Creates a new file with the given filename in the working folder of the scm
		 *
		 * @param scm
		 * @param filename
		 * @return
		 */
		private File doCreateTest(ScmRepository scm, String filename) throws IOException{

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
