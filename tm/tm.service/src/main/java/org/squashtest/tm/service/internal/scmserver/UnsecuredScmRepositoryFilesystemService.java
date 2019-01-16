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
package org.squashtest.tm.service.internal.scmserver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.scmserver.ScmRepositoryFilesystemService;
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest;
import org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.Optional;

import static org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy.strategyFor;

@Service("ScmRepositoryFilesystemService")
@Transactional(readOnly = true)
public class UnsecuredScmRepositoryFilesystemService implements ScmRepositoryFilesystemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnsecuredScmRepositoryFilesystemService.class);

	@PersistenceContext
	private EntityManager em;

	@Override
	public void createWorkingFolderIfAbsent(ScmRepository scm) {
		File workingFolder = scm.getWorkingFolder();
		if(workingFolder.exists()) {
			LOGGER.trace("The working folder of repository '{}' already exists.", scm.getName());
			return;
		}
		try {
			scm.doWithLock(() -> {
				tryCreateFolders(workingFolder);
				return null;
			});
		} catch(IOException iOEx) {
			LOGGER.error("error while creating the working folder in the repository", iOEx);
			throw new RuntimeException(iOEx);
		}
	}
	@Override
	public void createOrUpdateScriptFile(ScmRepository scm, Collection<TestCase> testCases) {

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("committing {} files to repository '{}'", testCases.size(), scm.getName());
		}
		// exportToScm
		try {
			scm.doWithLock(() -> {

				LOGGER.trace("committing tests to scm : '{}'", scm.getName());

				try {
					ScmRepositoryManifest manifest = new ScmRepositoryManifest(scm);

					for (TestCase testCase : testCases) {

						File testFile = locateOrCreateTestFile(manifest, testCase);

						// at this point the file is created without error
						// lets fill the file with the script content
						printToFile(testFile, testCase);

					}

					return null;

				}
				catch (IOException ex) {
					LOGGER.error("error while creating/updating files in the repository", ex);
					throw new RuntimeException(ex);
				}
			});
		}
		catch (IOException ex){
			LOGGER.error("error while creating/updating files in the repository", ex);
			throw new RuntimeException(ex);
		}
	}


	// **************** internal routines ********************************


	/********************************************************************************************************
	 * /!\ /!\/!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\
	 *
	 * The following methods below this line are intended to run within the scope of the scm filelock.
	 * This is hard to enforce so please be careful.
	 *
	 * /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\ /!\
	 ********************************************************************************************************/

	public File locateOrCreateTestFile(ScmRepositoryManifest manifest, TestCase testCase) throws IOException{

		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("attempting to locate physical script file for test '{}' in the scm", testCase.getId());
		}

		File testfile = null;

		Optional<File> maybeTestFile = manifest.locateTest(testCase);

		if (maybeTestFile.isPresent()){

			testfile = maybeTestFile.get();

			LOGGER.trace("found file : '{}'", testfile.getAbsolutePath());

		}
		else{
			LOGGER.trace("file not found, attempting to create a new one");

			// roundtrip 1 : attempt the creation with the normal filename
			try{

				testfile = createTestNominal(manifest.getScm(), testCase);

			}
			catch(IOException ex){
				if (SystemUtils.IS_OS_WINDOWS) {
					// maybe the failure is due to long absolute filename
					LOGGER.trace("failed to create file due to IOException, attempting with the backup filename");

					// try again with the backup name
					testfile = createTestBackup(manifest.getScm(), testCase);
				}
			}

		}

		return testfile;
	}

	/**
	 * Attempts to create the test file with the preferred name.
	 *
	 * @param testCase
	 * @return
	 * @throws IOException
	 */
	private File createTestNominal(ScmRepository scm, TestCase testCase) throws IOException{
		ScriptToFileStrategy strategy = strategyFor(testCase.getKind());

		String filename = strategy.createFilenameFor(testCase);

		return doCreateTestFile(scm, filename);
	}

	/**
	 * Attempts to create the test file with the backup name. Backup means : this is the filename we may need
	 * in the Windows world.
	 *
	 * @param testCase
	 * @return
	 * @throws IOException
	 */
	public File createTestBackup(ScmRepository scm, TestCase testCase) throws IOException{
		ScriptToFileStrategy strategy = strategyFor(testCase.getKind());

		String filename = strategy.backupFilenameFor(testCase);

		return doCreateTestFile(scm, filename);
	}


	/**
	 * Creates a new file with the given filename in the working folder of the scm
	 *
	 * @param scm
	 * @param filename
	 * @return
	 */
	private File doCreateTestFile(ScmRepository scm, String filename) throws IOException{

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

	private void printToFile(File dest, TestCase testCase) throws IOException{
		ScriptToFileStrategy strategy = strategyFor(testCase.getKind());
		String content = strategy.getWritableFileContent(testCase);

		try {
			// try first with UTF-8
			FileUtils.write(dest, content, Charset.forName("UTF-8"));
		} catch (UnsupportedCharsetException ex) {
			// try again with default charset
			FileUtils.write(dest, content);
		}

	}

	/**
	 * Try to create the folder and all the absent parent folders represented by the given abstract pathname.
	 * @param wf The abstract folder to create
	 * @throws IOException If the folder could not be created
	 */
	private void tryCreateFolders(File wf) throws IOException {
		if(!wf.mkdirs()) {
			if(wf.isDirectory()) {
				LOGGER.trace("directory at path {} already exists.", wf.toString());
			} else {
				throw new IOException("directory could not be created at path " + wf.toString());
			}
		} else {
			LOGGER.trace("directory at path {} has been created.", wf.toString());
		}
	}

}
