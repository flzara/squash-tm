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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseLanguage;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.service.internal.repository.AutomatedTestDao;
import org.squashtest.tm.service.internal.repository.TestAutomationProjectDao;
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testcase.scripted.ScriptToFileStrategy;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

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
		Collection<TestAutomationProjectContent> listedFromServers = listTestsFromRemoteServers(projects);

		// 2 : request tests published in SCM
		Collection<TestAutomationProjectContent> listedFromScms = listTestsFromScm(projects);

		// if some content was found in SCM and defined, merge the content
		for (TestAutomationProjectContent fromScm : listedFromScms){

			// lookup in the "fromServers" list which project is referenced there
			Optional<TestAutomationProjectContent> maybeFromServer = listedFromServers.stream()
					.filter(proj -> proj.getProject().equals(fromScm.getProject()))
					.findFirst();

			if (maybeFromServer.isPresent()){
				TestAutomationProjectContent fromServer = maybeFromServer.get();

				fromServer.mergeContent(fromScm);
			}
		}

		return listedFromServers;
	}

	// ************************* method set : fetch tests from server  ************************************

	@Override
	public Collection<TestAutomationProjectContent> listTestsFromRemoteServers(Collection<TestAutomationProject> projects){

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



	// ************************* method set : fetch tests from scm  ************************************

	@Override
	public Collection<TestAutomationProjectContent> listTestsFromScm(Collection<TestAutomationProject> autoprojects){

		List<TestAutomationProjectContent> allContent = new ArrayList<>();

		LOGGER.debug("retrieving test automation project content from repositories");
		if (LOGGER.isTraceEnabled()){
			List<String> taNames = autoprojects.stream().map(TestAutomationProject::getLabel).collect(toList());
			LOGGER.trace("automation projects : {}", taNames);
		}

		/*
		 * Here we first group the test automation projects by repositories. Automation projects that belongs to TM projects that have no repository are discarded.
		 * With each repository :
		 *  - we list the content of the repository
		 * 	- automation projects are classified by technology
		 * 	- tests are classified by technology
		 * 	- for each technology, tests and projects are paired together
		 *
		 * Shortcoming : if a scm is shared between multiple TM projects, the file listing will include more results than
		 * intended. Typically it can lead to inconsistencies where an automation projects for a TM project A will list
		 * scripts among which some of them will corresponds to test cases of a TM project B. The solution would be to
		 * double check with the content of a given project in the database. This would be relatively expensive and for
		 * now we won't perform such check.
		 *
		 */
		Map<ScmRepository, List<TestAutomationProject>> byTmProject =
			automationProjectsGroupByScm(autoprojects);

		for (Map.Entry<ScmRepository, List<TestAutomationProject>> groupedAutoprojects : byTmProject.entrySet()){

			ScmRepository scm = groupedAutoprojects.getKey();
			List<TestAutomationProject> taProjects = groupedAutoprojects.getValue();

			Collection<TestAutomationProjectContent> content = processAutoprojectSubset(scm, taProjects);

			allContent.addAll(content);

		}

		return allContent;

	}

	private Map<ScmRepository, List<TestAutomationProject>> automationProjectsGroupByScm(Collection<TestAutomationProject> autoprojects) {
		return autoprojects
			.stream()
			.filter(auto -> auto.getTmProject().getScmRepository() != null)
			.collect(Collectors.groupingBy(auto -> auto.getTmProject().getScmRepository()));
	}


	private Collection<TestAutomationProjectContent> processAutoprojectSubset(ScmRepository scm, Collection<TestAutomationProject> projects){

		if (LOGGER.isTraceEnabled()) {
			List<String> taNames = projects.stream().map(TestAutomationProject::getLabel).collect(toList());
			LOGGER.trace("inspecting content of repository '{}' and automation projects {}", scm.getName(), taNames);
		}

		try {

			List<TestAutomationProjectContent> allContent = new ArrayList<>();

			Map<TestCaseKind, List<String>> testsByTech = groupTestsByTechnology(scm);
			Map<TestCaseKind, List<TestAutomationProjectContent>> projectContentsByTech = groupProjectsByTechnology(projects);

			for (TestCaseKind kind : testsByTech.keySet()) {

				List<String> testPathsForKind = testsByTech.getOrDefault(kind, Collections.emptyList());
				List<TestAutomationProjectContent> contentForKind = projectContentsByTech.getOrDefault(kind, Collections.emptyList());

				populateProjectContents(testPathsForKind, contentForKind);

				allContent.addAll(contentForKind);

			}


			return allContent;

		}
		catch(Exception exception){
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Error while retrieving tests from repository '"+scm.getName()+"', " +
								 "which prevented to populate the automation projects test list", exception);
			}

			return buildFailedContent(exception, projects);
		}

	}

	private Collection<TestAutomationProjectContent> buildFailedContent(Exception ex, Collection<TestAutomationProject> projects){
		return projects.stream().map(proj -> new TestAutomationProjectContent(proj, ex)).collect(toList());
	}

	
	private void populateProjectContents(List<String> testPathsForKind, List<TestAutomationProjectContent> contentForKind) {
		for (TestAutomationProjectContent content : contentForKind) {
			TestAutomationProject taProject = content.getProject();
			for (String path : testPathsForKind) {
				AutomatedTest taTest = new AutomatedTest(path, taProject);
				content.appendTest(taTest);
			}
		}
	}



	// TODO: actually identifying the technology by file extension is weak. We should either
	// check for metadata in the tests themselves, or double check with the ScriptedTestCaseExtender
	// that goes along.
	// Note : the TestCaseKind "STANDARD" corresponds to unidentified technologies
	private Map<TestCaseKind, List<String>> groupTestsByTechnology(ScmRepository scm) throws IOException{
		return new ScmRepositoryManifest(scm)
				   .streamTestsRelativePath()
				   .sorted()
				   .collect(Collectors.groupingBy(this::identifyTestKind));

	}

	private Map<TestCaseKind, List<TestAutomationProjectContent>> groupProjectsByTechnology(Collection<TestAutomationProject> autoprojects){
		return autoprojects
				   .stream()
				   .sorted(Comparator.comparing(TestAutomationProject::getLabel))
				   .map(TestAutomationProjectContent::new)
				   .collect(Collectors.groupingBy(this::identifyProjectTechnology));
	}

	// naive classifier here !
	private TestCaseKind identifyTestKind(String testPath){
		if(testPath.endsWith(ScriptToFileStrategy.GHERKIN_STRATEGY.getExtension())) {
			return TestCaseKind.GHERKIN;
		} else if(testPath.endsWith(ScriptToFileStrategy.ROBOT_STRATEGY.getExtension())) {
			return TestCaseKind.ROBOT;
		} else {
			return TestCaseKind.STANDARD;
		}
	}

	private TestCaseKind identifyProjectTechnology(TestAutomationProjectContent projectContent){
		TestAutomationProject testAutoProject = projectContent.getProject();
		if(!testAutoProject.isCanRunScript()) {
			return TestCaseKind.STANDARD;
		} else {
			ScriptedTestCaseLanguage scriptLanguage = testAutoProject.getTmProject().getTcScriptType();
			return TestCaseKind.valueOf(scriptLanguage.name());
		}
	}


}
