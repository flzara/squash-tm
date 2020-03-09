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

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.tm.api.plugin.PluginType;
import org.squashtest.tm.api.wizard.AutomationWorkflow;
import org.squashtest.tm.core.scm.api.exception.ScmNoCredentialsException;
import org.squashtest.tm.core.scm.spi.ScmConnector;
import org.squashtest.tm.domain.IdCollector;
import org.squashtest.tm.domain.project.AutomationWorkflowType;
import org.squashtest.tm.domain.project.LibraryPluginBinding;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.QProject;
import org.squashtest.tm.domain.scm.QScmRepository;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.testautomation.QTestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testcase.QKeywordTestCase;
import org.squashtest.tm.domain.testcase.QScriptedTestCase;
import org.squashtest.tm.domain.testcase.QTestCase;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.tf.automationrequest.QAutomationRequest;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.ScmRepositoryDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.scmserver.ScmConnectorRegistry;
import org.squashtest.tm.service.internal.tf.event.AutomationRequestStatusChangeEvent;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.scmserver.ScmRepositoryFilesystemService;
import org.squashtest.tm.service.scmserver.ScmRepositoryManifest;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.testcase.TestCaseModificationService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Component
public class BDDTestCaseEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(BDDTestCaseEventListener.class);

	private static final String SPEL_ARSTATUS = "T(org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus)";

	private static final String TYPE_WORKFLOW = "T(org.squashtest.tm.domain.project.AutomationWorkflowType)";

	@PersistenceContext
	private EntityManager em;

	@Inject
	private CredentialsProvider credentialsProvider;

	@Inject
	private ScmConnectorRegistry scmConnectorRegistry;

	@Inject
	private ScmRepositoryFilesystemService scmService;

	@Inject
	private TestCaseModificationService tcService;

	@Inject
	private ScmRepositoryDao scmRepositoryDao;

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private GenericProjectManagerService projectManager;

	@Inject
	private MessageSource i18nHelper;

	@Autowired(required = false)
	Collection<AutomationWorkflow> plugins = Collections.EMPTY_LIST;

	private String getMessage(String i18nKey) {
		Locale locale = LocaleContextHolder.getLocale();
		return i18nHelper.getMessage(i18nKey, null, locale);
	}

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

		// Issue #TM-241: This event can occur after the modification of many AutomationRequests
		// If Hibernate entities were loaded before this modification, Hibernate cache may not have been updated
		em.clear();

		LOGGER.debug("request status changed : committing test scripts to repositories if needed");
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("changed request ids : '{}'", event.getAutomationRequestIds());
		}

		Collection<Long> requestIds = event.getAutomationRequestIds();

		Collection<Long> testCaseIds = findTestCaseIdsByAutomationRequestIds(requestIds);

		LOGGER.debug("committing test cases to their repositories");
		LOGGER.trace("test case ids : '{}'", testCaseIds);

		Map<ScmRepository, Set<TestCase>> scriptsGroupedByScm = scmRepositoryDao.findScriptedAndKeywordTestCasesGroupedByRepoById(testCaseIds);

		for (Map.Entry<ScmRepository, Set<TestCase>> entry : scriptsGroupedByScm.entrySet()) {

			ScmRepository scm = entry.getKey();
			Set<TestCase> testCases = entry.getValue();

			// Test existence of Credentials and test them
			Credentials credentials = testScmCredentials(scm);
			// Write files
			scmService.createOrUpdateScriptFile(scm, testCases);
			// Synchronize repository
			synchronizeRepository(scm, credentials);
		}

		/*Pour les projets avec REMOTE_WORKFLOW lancer le processuss de création de ticket jira */
		remoteRemoteTickets(new ArrayList<>(requestIds));
	}
	/**
	 * If Remote workflow automation is actif and newStatus is TRANSMITTED then create a new jira ticket and add remoteAutomationREquestExtender
	 **/

	private void remoteRemoteTickets(List<Long> requestIds) {
		String remoteIssueKey;

		LOGGER.debug("request status changed and type workflow isremote : create a new jira ticket and remoteRAE");
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("changed request ids : '{}'", requestIds);
		}

		List<TestCase> listTestCase = testCaseDao.findTestCaseByAutomationRequestIds(requestIds);
		for(TestCase tc: listTestCase){
			//workflow d'automatisation active
			if(tc.getProject().isAllowAutomationWorkflow() && AutomationWorkflowType.REMOTE_WORKFLOW.equals(tc.getProject().getAutomationWorkflowType())){
				//select plugin which is attach în project
				LibraryPluginBinding lpb= projectDao.findPluginForProject(tc.getProject().getId(), PluginType.AUTOMATION);
				for(AutomationWorkflow plugin: plugins){
					if(plugin.getPluginType().equals(lpb.getPluginType())){
						remoteIssueKey = plugin.createNewTicketRemoteServer(tc.getId());
						if (remoteIssueKey!=null){
							plugin.createRemoteAutomationRequestExtenderForTestCaseIfNotExist(remoteIssueKey, tc.getId());
						}
					}
				}
			}

		}
	}

	/**
	 * Check first if the credentials for the given ScmRepository have been set, if the protocol is valid,
	 * and then check the validity of the credentials for the remote repository.
	 * @param scm The ScmRepository whose connection is to test
	 * @return The credentials if everything is fine
	 */
	private Credentials testScmCredentials(ScmRepository scm) {
		ScmServer server = scm.getScmServer();
		ScmConnector connector = scmConnectorRegistry.createConnector(scm);

		Optional<Credentials> maybeCredentials = credentialsProvider.getAppLevelCredentials(server);

		Supplier<ScmNoCredentialsException> throwIfNull = () -> {
			throw new ScmNoCredentialsException(
				String.format(
					getMessage("message.scmRepository.noCredentials"),
					scm.getName())
			);
		};

		Credentials credentials = maybeCredentials.orElseThrow(throwIfNull);

		AuthenticationProtocol protocol = credentials.getImplementedProtocol();
		if(!connector.supports(protocol)) {
			throw new UnsupportedAuthenticationModeException(protocol.toString());
		}

		connector.testCredentials(credentials);

		return credentials;
	}

	/**
	 * Try to synchronise the local repository with the remote one using the given Credentials.
	 * @param scm The ScmRepository to synchronize
	 * @param credentials The Credentials to use
	 */
	private void synchronizeRepository(ScmRepository scm, Credentials credentials) {
		ScmConnector connector = scmConnectorRegistry.createConnector(scm);
		try {
			connector.synchronize(credentials);
		} catch(IOException ex) {
			LOGGER.error(ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Autobind a test case to an automated test for certain request transitions (if the automation workflow is on),
	 * if none is bound already, and the test case is a scripted or keyword test case.
	 *
	 * @param event
	 */
	@Order(100)
	@EventListener(classes = {AutomationRequestStatusChangeEvent.class}, condition = "#event.newStatus == " + SPEL_ARSTATUS + ".TRANSMITTED or " +
		"#event.newStatus == " + SPEL_ARSTATUS + ".AUTOMATED")
	public void autoBindWhenAvailable(AutomationRequestStatusChangeEvent event){

		LOGGER.debug("request status changed : autobinding test scripts if needed and possible");
		if (LOGGER.isTraceEnabled()){
			LOGGER.trace("changed request ids : '{}'", event.getAutomationRequestIds());
		}

		Collection<Long> requestIds = event.getAutomationRequestIds();

		/*
		 * Find the candidate test cases. As per the method findScriptedCandidatesForAutobind,
		 * the returned test cases are Gherkin-read (ie their project have a scm and a
		 * Gherkin-able test automation project)
		 */
		List<TestCase> candidates = findTCCandidatesForAutoBind(requestIds);

		// group them by Tm Project.
		Map<Project, List<TestCase>> testCasesByProjects = candidates.stream()
			.collect(Collectors.groupingBy(tc -> tc.getProject()));

		for (Map.Entry<Project, List<TestCase>> entry : testCasesByProjects.entrySet()){

			Project project = entry.getKey();

			// cannot be null per definition of a candidate TestCase (see above)
			ScmRepository scm = project.getScmRepository();

			List<TestCase> testCases = entry.getValue();

			try {
				scm.doWithLock(() -> {

					autoBindWithScm(scm, testCases);

					// no meaningful result to return
					return null;

				});
			}
			catch(IOException ex){
				LOGGER.error("Error while autobinding test cases from project '"+project.getName()+"'", ex);
				// do not let fail the whole operation here, proceed with the next batch
			}
		}

	}


	// ************* internals **********************

	/**
	 * Will attempt autobinding for the given test cases. The test cases and the SCM are assumed to
	 * belong to the same TM project.
	 *
	 * @param scm
	 * @param testCases
	 */
	private void autoBindWithScm(ScmRepository scm, List<TestCase> testCases){

		LOGGER.debug("autobinding test cases for scm '{}'", scm.getName());

		if (LOGGER.isTraceEnabled()) {
			List<Long> tcIds = IdCollector.collect(testCases);
			LOGGER.trace("test case ids : {}", tcIds);
		}

		ScmRepositoryManifest manifest = new ScmRepositoryManifest(scm);

		// look for a Gherkin-able project
		Optional<TestAutomationProject> maybeGherkin = findFirstGherkinProject(testCases);


		if (maybeGherkin.isPresent()){

			TestAutomationProject gherkinProject = maybeGherkin.get();

			testCases.forEach( tc -> {
				String pattern = scmService.createTestCasePatternForResearch(tc);
				Optional<File> maybeFile = manifest.locateTest(pattern,tc.getId());

				if (maybeFile.isPresent()) {

					File testFile = maybeFile.get();

					String normalizedName = manifest.getRelativePath(testFile);

					if (LOGGER.isTraceEnabled()){
						LOGGER.trace("autobinding test case [{}:{}] to script file '{}'", tc.getId(), tc.getName(),  normalizedName);
					}

					tcService.bindAutomatedTestAutomatically(tc.getId(), gherkinProject.getId(), normalizedName);

				}
				else{
					LOGGER.trace("no script found or test case [{}:{}]", tc.getId(), tc.getName());
				}

			});
		}
		else{
			// note : the actual program flow ensures that such an automation project will
			// always be found. The logging here is unlikely to occur
			LOGGER.debug("no automation project found, skipping");
		}


	}

	// assumes that all the test cases belong to the same tm project
	private Optional<TestAutomationProject> findFirstGherkinProject(List<TestCase> tcs) {
		if (tcs.isEmpty()){
			return Optional.empty();
		}

		TestCase tc = tcs.get(0);

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
	 * - cond 2 : are scripted test cases or keyword test cases
	 * - cond 3 : belong to a project that is connected to a SCM
	 * - cond 4 : belong to a project that have a (at least one) Gherkin-able TestAutomationProject
	 */
	private List<TestCase> findTCCandidatesForAutoBind(Collection<Long> automationRequestIds){

		if (automationRequestIds.isEmpty()){
			return Collections.emptyList();
		}

		QScriptedTestCase scriptedTestCase = QScriptedTestCase.scriptedTestCase;
		QKeywordTestCase keywordTestCase = QKeywordTestCase.keywordTestCase;

		List<TestCase> result = findScriptedAndKeywordTCForAutoBind(scriptedTestCase, scriptedTestCase.id, automationRequestIds);

		result.addAll(findScriptedAndKeywordTCForAutoBind(keywordTestCase, keywordTestCase.id, automationRequestIds));

		return result;

	}

	private <Y extends TestCase, T extends EntityPathBase<Y>> List<TestCase> findScriptedAndKeywordTCForAutoBind(T testCaseTable, NumberPath<Long> entityPathBaseId, Collection<Long> automationRequestIds) {
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
			.join(testCaseTable).on(entityPathBaseId.eq(testCase.id))	// condition 2
			.join(project.testAutomationProjects, automationProject)
			.join(project.scmRepository, scm) 								// condition 3
			.where(automationRequest.id.in(automationRequestIds) 			// condition 1
				.and(automationProject.canRunGherkin.isTrue())              // condition 4
			).fetch();
	}

}
