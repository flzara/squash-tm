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
package org.squashtest.tm.service.scmserver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.core.foundation.lang.Wrapped;
import org.squashtest.tm.core.scm.api.exception.ScmException;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.KeywordTestCase;
import org.squashtest.tm.domain.testcase.ScriptedTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.domain.testcase.TestCaseVisitor;
import org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * p>Encapsulates the operations aimed at searching a file on the scm.  </p>
 *
 * <p>It can work by direct I/O calls, or using a cache if limited I/O operations are desired. The cache option
 * is the default.</p>
 *
 */
public final class ScmRepositoryManifest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmRepositoryManifest.class);

	private static final String MISSING_WORKING_FOLDER_LOG_ERROR_MESSAGE =
		"Attempted to write files in the working folder of repository {} at path {} but could not find it. " +
			"\nPlease check that the repository is well initialized on local server.";

	private static final String MISSING_WORKING_FOLDER_ERROR_MESSAGE =
		"Attempted to write in the working folder of repository '%s' but could not find it." +
			"\nPlease contact an administrator to check that this repository is well initialized on the local server.";

	private ScmRepository scm;
	private boolean useCache = true;

	// contains all the file paths relative to the working folder
	private Set<String> pathCache = new HashSet<>();

	private final Path workFolderPath;

	public ScmRepositoryManifest(ScmRepository scm){
		this(scm, true);
	}

	public ScmRepository getScm() {
		return scm;
	}

	public boolean isUseCache() {
		return useCache;
	}

	ScmRepositoryManifest(ScmRepository scm, boolean useCache){
		this.scm = scm;
		this.workFolderPath = initWorkingFolderPath();
		this.useCache = useCache;
		if (useCache){
			initCache();
		}
	}

	/**
	 * Attends to retrieve the test file in the repository for a given test.
	 * The result is returned as an Optional.
	 *
	 * @param pattern the test file pattern
	 * @return
	 */
	public Optional<File> locateTest(String pattern, Long tcId){

		Collection<File> files;
		if (useCache){
			files = searchInCache(pattern);
		}
		else{
			files = searchOnDrive(pattern);
		}

		// check for the validity of the result
		if (files.size() > 1){
			LOGGER.warn("found two files more more that are possible candidates for test '{}'. This is an unexpected situation. " +
							"The commit routine will proceed with the first file in lexicographic order.", tcId);
		}

		return files.stream().sorted(Comparator.comparing(File::getName)).findFirst();

	}

	/**
	 * Returns the list of paths of the tests in the working folder of that repository, relative to
	 * that workfolder, as a Stream. The Unix separator is used regardless of the underlying OS.
	 *
	 * @return
	 */
	public Stream<String> streamTestsRelativePath() throws IOException{

		Stream<String> stream;

		if (useCache){
			stream = pathCache.stream();
		}
		else{
			stream = scm.listWorkingFolderContent()
				.stream()
				.map(this::getRelativePath);
		}

		return stream;
	}

	/**
	 * Returns path of this file, relative to the working folder of the repository.
	 * The file separator is the Unix separator '/'
	 *
	 * @param file
	 * @return
	 */
	public String getRelativePath(File file){
		Path testPath = Paths.get(file.getAbsolutePath());

		Path relative = workFolderPath.relativize(testPath);
		return FilenameUtils.normalizeNoEndSeparator(relative.toString(), true);
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

	private Collection<File> searchInCache(String regex){
		Pattern pattern = Pattern.compile(regex+"$");
		return pathCache.stream()
			.filter(entry -> pattern.matcher(entry).find())
			.map(relPath -> new File(scm.getWorkingFolder(), relPath))
			.collect(Collectors.toList());
	}

	private final void initCache(){
		try {
			// the pathcache maps a File by its filename
			pathCache = scm.listWorkingFolderContent()
							.stream()
							.map(this::getRelativePath)
							.collect(Collectors.toSet());
		}
		catch (IOException ex){
			throw new RuntimeException("cannot list content of scm '"+scm.getName()+"'", ex);
		} catch (IllegalArgumentException ex) {
			LOGGER.error(MISSING_WORKING_FOLDER_LOG_ERROR_MESSAGE, scm.getName(), scm.getWorkingFolder().toString());
			throw new ScmException(
				String.format(MISSING_WORKING_FOLDER_ERROR_MESSAGE, scm.getName()), ex);
		}
	}

	private final Path initWorkingFolderPath(){
		String baseDir = scm.getRepositoryPath();
		if (baseDir == null){
			throw new IllegalArgumentException("the repository '"+scm.getName()+"' has no base directory defined !");
		}
		String folderPath = StringUtils.defaultIfBlank(scm.getWorkingFolderPath(), "");
		return Paths.get(baseDir, folderPath);
	}



	private static class FilenamePatternFilter implements IOFileFilter {

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
