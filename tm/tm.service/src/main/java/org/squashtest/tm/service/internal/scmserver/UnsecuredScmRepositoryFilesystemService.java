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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.service.internal.library.PathService;
import org.squashtest.tm.service.internal.testcase.event.TestCaseGherkinLocationChangeEvent;
import org.squashtest.tm.service.scmserver.ScmRepositoryFilesystemService;
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest;
import org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy;

import javax.inject.Inject;
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

	private static final String TEST_CASE_PATH_ILLEGAL_PATTERN = "[^a-zA-Z0-9\\_\\-\\/]";

	private static final boolean USE_HIERARCHY = true;

	@Inject
	private ApplicationEventPublisher eventPublisher;

	@Inject
	private PathService pathService;

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
	public void createOrUpdateScriptFile(ScmRepository scm, Collection<ScriptedTestCase> testCases) {
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
						File testFile = locateOrMoveOrCreateTestFile(manifest, testCase);
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

	/**
	 * Attempt to locate the physical file corresponding to the given Test Case.
	 * If the file was located, check if it needs to be move and/or renamed according to the Test Case state.
	 * If the file does not exist yet, try to create it according to the Test Case, first attempting to write it with
	 * a standard name, then with a backup name if it failed.
	 * @param manifest
	 * @param testCase
	 * @return
	 * @throws IOException
	 */
	public File locateOrMoveOrCreateTestFile(ScmRepositoryManifest manifest, TestCase testCase) throws IOException{
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("attempting to locate physical script file for test '{}' in the scm", testCase.getId());
		}
		File testfile = null;
		Optional<File> maybeTestFile = manifest.locateTest(testCase);
		if (maybeTestFile.isPresent()){
			testfile = maybeTestFile.get();
			LOGGER.trace("found file : '{}'", testfile.getAbsolutePath());
			testfile = moveAndRenameFileIfNeeded(testCase, testfile, manifest.getScm());
		} else {
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
	 * Check if the given TestCase has been renamed and/or moved since it was last transmitted and written into the File.
	 * If so, try to move the File to the correct destination. First attempting to write the standard name, then with
	 * the backup name if it failed.
	 * @param testCase The Test Case being transmitted
	 * @param originalFile The File corresponding to the given Test Case that was found (but potentially at the wrong place)
	 * @param scmRepository The Scm Repository in which operations are occurring
	 * @return The moved/renamed File if such operations were needed. The original File it was not moved/renamed
	 */
	private File moveAndRenameFileIfNeeded(TestCase testCase, File originalFile, ScmRepository scmRepository) {

		File workingDirectory = scmRepository.getWorkingFolder();
		// Determine CORRECT folders path
		String foldersPath = getFoldersPath(testCase);
		// Determine CORRECT relative path of the given TestCase with the STANDARD name
		String correctStandardRelativePath = buildTestCaseStandardRelativePath(foldersPath, testCase);
		// Determine CORRECT relative path of the given TestCase with the BACKUP name
		String correctBackupRelativePath = buildTestCaseBackUpRelativePath(foldersPath, testCase);
		// Get the CURRENT relative path
		String currentPath = workingDirectory.toURI().relativize(originalFile.toURI()).toString();

		// Compare them
		if(!correctStandardRelativePath.equals(currentPath) && !correctBackupRelativePath.equals(currentPath)) {
			// Try to move/rename with the standard name
			File targetStandardFile = new File(workingDirectory, correctStandardRelativePath);

			try {
				tryMoveFile(originalFile, targetStandardFile);
				eventPublisher.publishEvent(
					new TestCaseGherkinLocationChangeEvent(
						testCase.getId(),
						scmRepository.getScmServer().getUrl() + "/" + scmRepository.getName() + "/" + correctStandardRelativePath));
				return targetStandardFile;
			} catch (IOException ex) {
				// Operation failed, try with the backup name
				File targetBackUpFile = new File(workingDirectory, correctBackupRelativePath);
				try {
					tryMoveFile(originalFile, targetBackUpFile);
					eventPublisher.publishEvent(
					new TestCaseGherkinLocationChangeEvent(
						testCase.getId(),
						scmRepository.getScmServer().getUrl() + "/" + scmRepository.getName() + "/" + correctBackupRelativePath));
					return targetBackUpFile;
				} catch (IOException ioEx) {
					throw new RuntimeException(ex);
				}
			}
		}
		// If the test case was not moved/rename, file remains the same
		return originalFile;
	}

	/**
	 * Given a TestCase and its corresponding file's foldersPath, build the file's relative path with the STANDARD name.
	 * @param foldersPath The folders path of the Test Case's corresponding File
	 * @param testCase The Test Case which path is to build
	 * @return The relative path with the Standard name
	 */
	private String buildTestCaseStandardRelativePath(String foldersPath, TestCase testCase) {
		ScriptToFileStrategy strategy = ScriptToFileStrategy.strategyFor(TestCaseKind.GHERKIN);
		String standardName = strategy.createFilenameFor(testCase);
		return foldersPath + standardName;
	}

	/**
	 * Same method as {@link #buildTestCaseStandardRelativePath(String, TestCase)} but with the BackUp name.
	 * @param foldersPath The folders path of the Test Case's corresponding File
	 * @param testCase The Test Case which path is to build
	 * @return The relative path with the Standard name
	 */
	private String buildTestCaseBackUpRelativePath(String foldersPath, TestCase testCase) {
		ScriptToFileStrategy strategy = ScriptToFileStrategy.strategyFor(TestCaseKind.GHERKIN);
		String standardName = strategy.backupFilenameFor(testCase);
		return foldersPath + standardName;
	}

	/**
	 * Given a Test Case, compute its corresponding file's folders path.
	 * The folders path is the relative path from the working directory but not containing the name of the file,
	 * so it only contains the folders.
	 * Ex: A test case in 'Project_2/main_folder/sub_folder/test_case_7' will compute the path 'main_folder/sub_folder/'
	 * Ex: A test case in the root of a library will compute an empty string
	 * @param testCase The Test Case which path is to compute
	 * @return The folders path of the given Test Case
	 */
	private String getFoldersPath(TestCase testCase) {
		if(!testCase.getProject().isUseTreeStructureInScmRepo()) {
			// If flat structure is used, no need of folders path
			return "";
		} else {
			// If the tree structure of TM is used
			String testCaseFoldersPath = pathService.buildTestCaseFoldersPath(testCase.getId());
			if (testCaseFoldersPath != null) {
				return normalizeFilePath(testCaseFoldersPath) + "/";
			} else {
				return "";
			}
		}
	}

	/**
	 * Remove accents and replace illegal characters by '_'.
	 */
	private String normalizeFilePath(String path) {
		return StringUtils.stripAccents(path).replaceAll(TEST_CASE_PATH_ILLEGAL_PATTERN, "_");
	}

	/**
	 * Attempts to create the test file with the preferred name.
	 *
	 * @param testCase
	 * @return
	 * @throws IOException
	 */
	private File createTestNominal(ScmRepository scm, TestCase testCase) throws IOException{
		String foldersPath = getFoldersPath(testCase);
		String fileRelativePath = buildTestCaseStandardRelativePath(foldersPath, testCase);
		return doCreateTestFile(scm, fileRelativePath);
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
		String foldersPath = getFoldersPath(testCase);
		String fileRelativePath = buildTestCaseBackUpRelativePath(foldersPath, testCase);
		return doCreateTestFile(scm, fileRelativePath);
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
			tryCreateFolders(newFile.getParentFile());
			newFile.createNewFile();
			LOGGER.trace("new file created : '{}'", newFile.getAbsolutePath());
		}
		return newFile;

	}

	private void printToFile(File dest, TestCase testCase) throws IOException{
		//TODO: strategy is not necessary
		ScriptToFileStrategy strategy = strategyFor(TestCaseKind.GHERKIN);
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
	 * @param folder The abstract folder to create
	 * @throws IOException If the folder could not be created
	 */
	private void tryCreateFolders(File folder) throws IOException {
		if(!folder.mkdirs()) {
			if(folder.isDirectory()) {
				LOGGER.trace("directory at path {} already exists.", folder.toString());
			} else {
				throw new IOException("directory could not be created at path " + folder.toString());
			}
		} else {
			LOGGER.trace("directory at path {} has been created.", folder.toString());
		}
	}

	/**
	 * Try to move a File from a location to another, taking charge of renaming the file if needed.
	 * If the target File path contains non-existent folders, try to create them.
	 * @param sourceFile The source file to move and/or rename
	 * @param targetFile The target file
	 * @return The moved File.
	 * @throws IOException If an error occurrend during the operation
	 */
	private File tryMoveFile(File sourceFile, File targetFile) throws IOException {
		File targetFileParent = targetFile.getParentFile();
		tryCreateFolders(targetFileParent);
		if(sourceFile.renameTo(targetFile)) {
			LOGGER.trace("file with path {} has been renamed to {}", sourceFile, targetFile);
			return targetFile;
		} else {
			throw new IOException("file with path " + sourceFile + " could not be move/renamed to file with path " + targetFile);
		}
	}

}
