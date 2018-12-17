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
package org.squashtest.tm.service.internal.testautomation;

import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 *
 * @author bsiri
 *
 */
@Transactional
@Service("squashtest.tm.service.AutomatedTestService")
public class AutomatedTestManagerServiceImpl implements UnsecuredAutomatedTestManagerService {

	private static final int DEFAULT_THREAD_TIMEOUT = 30000; // timeout as milliseconds

	private int timeoutMillis = DEFAULT_THREAD_TIMEOUT;

	private static final Logger LOGGER = LoggerFactory.getLogger(AutomatedTestManagerServiceImpl.class);

	@Inject
	private TestAutomationProjectDao projectDao;

	@Inject
	private AutomatedTestDao testDao;

	@Inject
	private TestAutomationConnectorRegistry connectorRegistry;


	private TestAutomationTaskExecutor executor;

	@Inject
    @Transactional(propagation = SUPPORTS) // Injection method should not trigger a tx but should not care either
	public void setAsyncTaskExecutor(AsyncTaskExecutor executor) {
		this.executor = new TestAutomationTaskExecutor(executor);
	}

	// ******************** Entity Management ************************

	@Override
	public TestAutomationProject findProjectById(long projectId) {
		return projectDao.findById(projectId);
	}


	@Override
	public AutomatedTest persistOrAttach(AutomatedTest newTest) {
		return testDao.persistOrAttach(newTest);
	}

	@Override
	public void removeIfUnused(AutomatedTest test) {
		testDao.removeIfUnused(test);
	}


	// **************************** Remote Calls ***********************

	@Override
	public Collection<TestAutomationProjectContent> listTestsInProjects(Collection<TestAutomationProject> projects) {

		// 1 : request tests published on automation servers
		Collection<TestAutomationProjectContent> fromServers = listTestsFromServer(projects);

		// 2 : request tests published in SCM
		Optional<TestAutomationProjectContent> maybeFromScm = listTestsFromScm(projects);

		// if some content was found in SCM and defined, merge the content
		if (maybeFromScm != null){
			TestAutomationProjectContent fromScm = maybeFromScm.get();

			// lookup in the "fromServers" list which project is referenced there
			Optional<TestAutomationProjectContent> maybeFromServer = fromServers.stream()
					.filter(proj -> proj.getProject().equals(fromScm.getProject()))
					.findFirst();

			if (maybeFromServer.isPresent()){
				TestAutomationProjectContent fromServer = maybeFromServer.get();

				fromServer.mergeContent(fromScm);
			}
		}

		// now we can return
		return fromServers;
	}

	// ************************* method set : fetch tests from server  ************************************

	private Collection<TestAutomationProjectContent> listTestsFromServer(Collection<TestAutomationProject> projects){

		LOGGER.debug("listing tests on remote test automation projects ");
		if (LOGGER.isTraceEnabled()){
			Collection<String> projectNames = projects.stream().map(TestAutomationProject::getJobName).collect(toList());
			LOGGER.trace("projects are : {}", projectNames);
		}

		// 1 : prepare and submit all the tasks
		Collection<FetchTestListFuture> futures = projects.stream()
													// prepare the tasks
												  	.map(project -> new FetchTestListTask(connectorRegistry, project))
													// submit the tasks
													.map(executor::sumbitFetchTestListTask)
													// gather the futures
													.collect(toList());

		// 2 : harvest the results
		return futures.stream()
						.map(this::extractFromFuture)
						.collect(toList());


	}

	private TestAutomationProjectContent extractFromFuture(FetchTestListFuture future){
		try {
			return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
		} catch (Exception ex) {
			return future.getTask().buildFailedResult(ex);
		}
	}



	// ************************* method set : fetch tests from server  ************************************

	/**
	 * <p>
	 *     Collects all the automated tests contained in the repositories, and returns them paired to the first Gherkin-able
	 * automation project in the list.
	 * </p>
	 *
	 * <p>
	 *     Returns an empty option if no SCM or no Gherkin-able TestAutomationProject is available.
	 * </p>
	 *
	 * @param projects
	 * @return
	 */
	private Optional<TestAutomationProjectContent> listTestsFromScm(Collection<TestAutomationProject> projects){

		// 1 : get the SCMs
		Collection<ScmRepository> repos = gatherRepositories(projects);


		// 2 : locate the first Gherkin-able project
		Optional<TestAutomationProject> maybeGherkinProject = projects.stream()
																  .filter(TestAutomationProject::isCanRunGherkin)
																  .sorted(Comparator.comparing(TestAutomationProject::getLabel))
																  .findFirst();

		// go if there is at least one repo and one gherkin project
		if (! repos.isEmpty() && maybeGherkinProject.isPresent()){

			TestAutomationProject gherkinProject = maybeGherkinProject.get();

			// 3 : gather all automated tests, as test belonging to the selected gherkin project
			Collection<AutomatedTest> allTests = repos.stream()
													 .flatMap(this::collectTestRelativePath)
													 .map(path -> new AutomatedTest(path, gherkinProject))
													 .collect(toList());

			// 4 : return the result
			return Optional.of(new TestAutomationProjectContent(gherkinProject, allTests));
		}
		else{
			return Optional.empty();
		}

	}

	private Collection<ScmRepository> gatherRepositories(Collection<TestAutomationProject> projects){
		return projects.stream()
				   .map(taProj -> taProj.getTmProject().getScmRepository())
				   .filter(scm -> scm != null)
				   .distinct()
				   .collect(toList());
	}



	/*
	 * Returns paths of the content of the working directory of the repository as a stream of String.
	 * The returned paths are relative to the base repository directory and use the Unix separator
	 * regardless of the underlying OS or filesystem.
	 */
	private Stream<String> collectTestRelativePath(ScmRepository repository){
		Path baseAsPath = Paths.get(repository.getRepositoryPath());

		return getScmContentOrLog(repository).stream()
		   .map(testFile -> {
				Path testPath = Paths.get(testFile.getAbsolutePath());
				return baseAsPath.relativize(testPath);
			})
			.map(Path::toString)
			.map(path -> FilenameUtils.normalizeNoEndSeparator(path, true));
	}

	private Collection<File> getScmContentOrLog(ScmRepository repo){
		try{
			return repo.listWorkingFolderContent();
		}
		catch(IOException io){
			LOGGER.error("error while listing content of repository "+repo.getName(), io);
			return Collections.emptyList();
		}
	}




}
