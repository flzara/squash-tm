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
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.testcase.scripted.ScriptedTestCaseEventListener;
import org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

	private ScmRepository scm;
	private boolean useCache = true;

	// maps filename by File
	private Map<String, File> pathCache = new HashMap<>();

	public ScmRepositoryManifest(ScmRepository scm){
		this.scm = scm;
		initCache();
	}

	public ScmRepository getScm() {
		return scm;
	}

	public boolean isUseCache() {
		return useCache;
	}

	ScmRepositoryManifest(ScmRepository scm, boolean useCache){
		this.scm = scm;
		this.useCache = useCache;
		if (useCache){
			initCache();
		}
	}

	/**
	 * Attends to retrieve the test file in the repository for a given test.
	 * The result is returned as an Optional.
	 *
	 * @param testCase
	 * @return
	 */
	public Optional<File> locateTest(TestCase testCase){

		ScriptToFileStrategy strategy = ScriptToFileStrategy.strategyFor(testCase.getKind());
		String pattern = strategy.buildFilenameMatchPattern(testCase);

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
							"The commit routine will proceed with the first file in lexicographic order.", testCase.getId());
		}

		return files.stream().sorted(Comparator.comparing(File::getName)).findFirst();

	}

	/**
	 * Returns the list of paths of the tests in the working folder of that repository, relative to the root of the
	 * repository, as a Stream. The Unix separator is used regardless of the underlying OS.
	 *
	 * @return
	 */
	public Stream<String> streamTestsRelativePath() throws IOException{
		Path workfolderAsPath = Paths.get(scm.getRepositoryPath(), scm.getWorkingFolderPath());

		Stream<File> fileStream = (useCache) ? pathCache.values().stream() : scm.listWorkingFolderContent().stream();

		return fileStream
				   .map(testFile -> {
					   Path testPath = Paths.get(testFile.getAbsolutePath());
					   return workfolderAsPath.relativize(testPath);
				   })
				   .map(Path::toString)
				   .map(path -> FilenameUtils.normalizeNoEndSeparator(path, true));
	}

	/**
	 * Returns path of this file, relative to the working folder of the repository.
	 * The file separator is the Unix separator '/'
	 *
	 * @param file
	 * @return
	 */
	public String getRelativePath(File file){
		Path baseAsPath = Paths.get(scm.getRepositoryPath(), scm.getWorkingFolderPath());
		Path testPath = Paths.get(file.getAbsolutePath());

		Path relative = baseAsPath.relativize(testPath);

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

	private Collection<File> searchInCache(String pattern){
		return pathCache.entrySet().stream()
			.filter(entry -> entry.getKey().matches(pattern))
			.map(Map.Entry::getValue)
			.collect(Collectors.toList());
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
