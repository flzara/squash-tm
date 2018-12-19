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

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.project.QProject;
import org.squashtest.tm.domain.scm.QScmRepository;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.testautomation.QTestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.*;
import org.squashtest.tm.domain.tf.automationrequest.QAutomationRequest;
import org.squashtest.tm.service.internal.tf.event.AutomationRequestStatusChangeEvent;
import org.squashtest.tm.service.scmserver.ScmRepositoryFilesystemService;
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest;
import org.squashtest.tm.service.testcase.TestCaseModificationService;


import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class ScriptedTestCaseEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedTestCaseEventListener.class);

	private static final String SPEL_ARSTATUS = "T(org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus)";

	@PersistenceContext
	private EntityManager em;

	@Inject
	private ScmRepositoryFilesystemService scmService;

	@Inject
	private TestCaseModificationService tcService;


	/**
	 * If the new status is suitable for commit (eg, TRANSMITTED),
	 * all the scripted test case will be committed to the scm repository.
	 * If there are repositories to commit to and scripts that needs it.
	 *
	 * @param event
	 */
	@Order(10)	// must run before the handler that autoassociates the scripts
	@EventListener(classes = {AutomationRequestStatusChangeEvent.class}, condition = "#event.newStatus == " + SPEL_ARSTATUS + ".TRANSMITTED")
	public void commitWhenTransmitted(AutomationRequestStatusChangeEvent event) {

		LOGGER.debug("request status changed : committing test scripts to repositories if needed");
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("changed request ids : '{}'", event.getAutomationRequestIds());
		}

		Collection<Long> requestIds = event.getAutomationRequestIds();

		Collection<Long> testCaseIds = findTestCaseIdsByAutomationRequestIds(requestIds);

		scmService.createOrUpdateScriptFile(testCaseIds);

	}

	/**
	 * Autobind a test case to an automated test for certain request transitions (if the automation workflow is on),
	 * if none is bound already, and the test case is a scripted test case.
	 *
	 * @param event
	 */
	@Order(100)
	@EventListener(classes = {AutomationRequestStatusChangeEvent.class}, condition = "#event.newStatus == " + SPEL_ARSTATUS + ".TRANSMITTED or " +
																					 "#event.newStatus == " + SPEL_ARSTATUS + ".AUTOMATED")
	public void autoBindWhenAvailable(AutomationRequestStatusChangeEvent event){

		LOGGER.debug("request status changed : autoassociating test scripts if needed and possible");
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("changed request ids : '{}'", event.getAutomationRequestIds());
		}

		Collection<Long> requestIds = event.getAutomationRequestIds();

		// find the candidates
		List<TestCase> candidates = findCandidatesForAutobind(requestIds);

		// group them by scm
		Map<ScmRepository, List<TestCase>> testCasesByScm = candidates.stream().collect(Collectors.groupingBy(tc -> tc.getProject().getScmRepository()));

		for (Map.Entry<ScmRepository, List<TestCase>> entry : testCasesByScm.entrySet()){

			ScmRepository scm = entry.getKey();
			List<TestCase> testCases = entry.getValue();

			try {
				scm.doWithLock(() -> {

					autoBindWithScm(scm, testCases);

					return null;

				});
			}
			catch(IOException ex){
				LOGGER.error("Error while autobinding test cases", ex);
				// do not let fail the whole operation here, proceed with the next batch
			}
		}

	}


	// ************* internals **********************


	private void autoBindWithScm(ScmRepository scm, Collection<TestCase> testCases){

		ScmRepositoryManifest manifest = new ScmRepositoryManifest(scm);

		testCases.forEach( tc -> {

			// look for a Gherkin-able project
			Optional<TestAutomationProject> maybeGherkin = findFirstGherkinProject(tc);
			Optional<File> maybeFile = manifest.locateTest(tc);

			if (maybeFile.isPresent() && maybeGherkin.isPresent()) {

				File testFile = maybeFile.get();
				TestAutomationProject gherkinProject = maybeGherkin.get();

				String normalizedName = manifest.getRelativePath(testFile);

				tcService.bindAutomatedTest(tc.getId(), gherkinProject.getId(), normalizedName);

			}

		});
	}

	private Optional<TestAutomationProject> findFirstGherkinProject(TestCase tc) {
		return tc.getProject()
			   .getTestAutomationProjects()
			   .stream()
			   .filter(TestAutomationProject::isCanRunGherkin)
			   .sorted(Comparator.comparing(TestAutomationProject::getLabel))
			   .findFirst();
	}


	/**
	 * returns the test case ids concerned by the given automation request ids
	 *
	 * @param automationRequestIds
	 * @return
	 */
	private Collection<Long> findTestCaseIdsByAutomationRequestIds(Collection<Long> automationRequestIds){

		if (automationRequestIds.isEmpty()){
			return Collections.emptyList();
		}

		QAutomationRequest automationRequest = QAutomationRequest.automationRequest;
		QTestCase testCase = QTestCase.testCase;

		return new JPAQueryFactory(em)
				   .select(testCase.id)
					.from(automationRequest)
					.join(automationRequest.testCase, testCase)
					.where(automationRequest.id.in(automationRequestIds))
					.fetch();

	}


	/*
	 * Returns the test cases that :
	 * - cond 1 : are the object of the automation requests (in arguments),
	 * - cond 2 : are scripted test cases
	 * - cond 3 : don't have an AutomatedTest bound yet,
	 * - cond 4 : belong to a project that is connected to a SCM
	 * - cond 5 : belong to a project that have a (at least one) Gherkin-able TestAutomationProject
	 */
	private List<TestCase> findCandidatesForAutobind(Collection<Long> automationRequestIds){

		if (automationRequestIds.isEmpty()){
			return Collections.emptyList();
		}

		QAutomationRequest automationRequest = QAutomationRequest.automationRequest;
		QTestCase testCase = QTestCase.testCase;
		QProject project = QProject.project1;
		QTestAutomationProject automationProject = QTestAutomationProject.testAutomationProject;
		QScmRepository scm = QScmRepository.scmRepository;

		return new JPAQueryFactory(em)
				   .select(testCase)
				   .from(automationRequest)
				   .join(automationRequest.testCase, testCase)
				   .join(testCase.project, project)
				   .join(project.testAutomationProjects, automationProject)
				   .join(project.scmRepository, scm) 								// condition 4
					.where(automationRequest.id.in(automationRequestIds) 			// condition 1
							   .and(testCase.kind.ne(TestCaseKind.STANDARD))		// condition 2
							   .and(testCase.automatedTest.isNull())				// condition 3
								.and(automationProject.canRunGherkin.isTrue())				// condition 5
					)
					.fetch();

	}

}
