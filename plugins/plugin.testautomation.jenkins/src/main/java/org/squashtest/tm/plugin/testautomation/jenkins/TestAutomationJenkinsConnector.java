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
package org.squashtest.tm.plugin.testautomation.jenkins;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.squashtest.csp.core.bugtracker.core.UnsupportedAuthenticationModeException;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.core.scm.api.exception.ScmNoCredentialsException;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.BuildDef;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.FetchTestListBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.OptimisticTestList;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.StartTestExecution;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.service.servers.CredentialsProvider;
import org.squashtest.tm.service.servers.UserCredentialsCache;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;
import org.squashtest.tm.service.testautomation.spi.TestAutomationServerNoCredentialsException;
import org.squashtest.tm.service.testautomation.spi.UnreadableResponseException;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import static org.squashtest.tm.domain.servers.AuthenticationProtocol.BASIC_AUTH;

@Service("plugin.testautomation.jenkins.connector")
public class TestAutomationJenkinsConnector implements TestAutomationConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	private static final String CONNECTOR_KIND = "jenkins";
	private static final int DEFAULT_SPAM_INTERVAL_MILLIS = 5000;

	private final static String GIVEN_PROTOCOL_NOT_SUPPORTED = "The given protocol %s is not supported.";

	@Inject
	private TaskScheduler taskScheduler;
	@Inject
	private HttpClientProvider clientProvider;

	private JsonParser jsonParser = new JsonParser();

	@Inject
	private HttpRequestFactory requestFactory;

	@Inject
	private CredentialsProvider credentialsProvider;

	@Inject
	private MessageSource i18nHelper;

	private String getMessage(String i18nKey) {
		Locale locale = LocaleContextHolder.getLocale();
		return i18nHelper.getMessage(i18nKey, null, locale);
	}

	@Value("${tm.test.automation.pollinterval.millis}")
	private int spamInterval = DEFAULT_SPAM_INTERVAL_MILLIS;

	private RequestExecutor requestExecutor = RequestExecutor.getInstance();

	// ****************************** let's roll ****************************************

	@Override
	public String getConnectorKind() {
		return CONNECTOR_KIND;
	}

	@Override
	public boolean checkCredentials(TestAutomationServer server, String login, String password) throws TestAutomationException{

		CloseableHttpClient client = clientProvider.getClientFor(server, login, password);

		HttpGet credCheck = requestFactory.newCheckCredentialsMethod(server, login, password);

		requestExecutor.execute(client, credCheck);

		// if everything went fine, we may return true. Or else let the exception go.
		return true;

	}

	@Override
	public boolean checkCredentials(TestAutomationServer server, Credentials credentials) throws TestAutomationException{

		BasicAuthenticationCredentials basicCredentials = (BasicAuthenticationCredentials) credentials;

		CloseableHttpClient client = clientProvider.getClientFor(server, basicCredentials.getUsername(), String.valueOf(basicCredentials.getPassword()));

		HttpGet credCheck = requestFactory.newCheckCredentialsMethod(server,basicCredentials.getUsername(), String.valueOf(basicCredentials.getPassword()));

		requestExecutor.execute(client, credCheck);

		// if everything went fine, we may return true. Or else let the exception go.
		return true;

	}

	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server, String login, String password)
		throws TestAutomationException {

		CloseableHttpClient client = clientProvider.getClientFor(server, login, password);

		HttpGet getJobsMethod = requestFactory.newGetJobsMethod(server);

		String response = requestExecutor.execute(client, getJobsMethod);

		try {
			return jsonParser.readJobListFromJson(response);
		} catch (UnreadableResponseException ex) {
			throw new UnreadableResponseException("Test automation - jenkins : server '" + server
				+ "' returned malformed response : ", ex);
		}

	}

	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server, Credentials credentials)
		throws TestAutomationException {

		BasicAuthenticationCredentials basicCredentials = (BasicAuthenticationCredentials) credentials;

		CloseableHttpClient client = clientProvider.getClientFor(server, basicCredentials.getUsername(), String.valueOf(basicCredentials.getPassword()));

		HttpGet getJobsMethod = requestFactory.newGetJobsMethod(server);

		String response = requestExecutor.execute(client, getJobsMethod);

		try {
			return jsonParser.readJobListFromJson(response);
		} catch (UnreadableResponseException ex) {
			throw new UnreadableResponseException("Test automation - jenkins : server '" + server
				+ "' returned malformed response : ", ex);
		}

	}

	@Override
	public Collection<AutomatedTest> listTestsInProject(TestAutomationProject project, String username) throws
		TestAutomationException {

		initializeCredentialsCache(username);
		BasicAuthenticationCredentials basicCredentials = getAutomationServerCredentials(project.getServer());

		// first we try an optimistic approach
		try {

			OptimisticTestList otl = new OptimisticTestList(clientProvider, project, basicCredentials);
			return otl.run();
		}

		// if the file isn't available we regenerate the file
		catch (Exception ex) {// NOSONAR the exception is handled
			LOGGER.error("Error while fetching job list for project {}.",project);
			LOGGER.error(ex.toString());
			CloseableHttpClient client = clientProvider.getClientFor(project.getServer(), basicCredentials.getUsername(), String.valueOf(basicCredentials.getPassword()));

			FetchTestListBuildProcessor processor = new FetchTestListBuildProcessor();

			processor.setClient(client);
			processor.setProject(project);
			processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getJobName(), generateNewId()));
			processor.setDefaultReschedulingDelay(spamInterval);

			processor.run();

			return processor.getResult();

		} finally {
			LOGGER.debug("TestAutomationJenkinsCOnnector : completed test fetching for autoamtion project '{}'", project.getLabel());
			credentialsProvider.unloadCache();
		}

	}

	/**
	 * @see org.squashtest.tm.service.testautomation.spi.TestAutomationConnector#executeParameterizedTests(java.util.Collection,
	 * java.lang.String, org.squashtest.tm.service.testautomation.TestAutomationCallbackService)
	 */
	@Override
	public void executeParameterizedTests(
		Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> parameterizedExecutions,
		String externalId, TestAutomationCallbackService callbackService) {

		MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> execsByProject = reduceToParamdExecsByProject(parameterizedExecutions);

		List<BuildDef> buildDefs = mapToJobDefs(execsByProject);

		for (BuildDef buildDef : buildDefs) {

			new StartTestExecution(buildDef, clientProvider, requestFactory, externalId).run();

		}

	}

	// ************************************ other private stuffs **************************

	private String generateNewId() {
		return Long.toString(System.currentTimeMillis());
	}

	private List<BuildDef> mapToJobDefs(
		MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> execsByProject) {
		ArrayList<BuildDef> jobDefs = new ArrayList<>(execsByProject.size());

		for (Entry<TestAutomationProject, List<Couple<AutomatedExecutionExtender, Map<String, Object>>>> entry : execsByProject
			.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				// fetch the name of the slave node if any
				Couple<AutomatedExecutionExtender, Map<String, Object>> firstEntry = entry.getValue().get(0);

				TestAutomationServer automationServer = entry.getKey().getServer();

				BasicAuthenticationCredentials credentials = getAutomationServerCredentials(automationServer);

				jobDefs.add(new BuildDef(entry.getKey(), credentials, entry.getValue(), firstEntry.getA1().getNodeName()));
			}
		}
		return jobDefs;
	}

	private MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> reduceToParamdExecsByProject(
		Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> parameterizedExecutions) {
		MultiValueMap<TestAutomationProject, Couple<AutomatedExecutionExtender, Map<String, Object>>> execsByProject = new LinkedMultiValueMap<>();

		for (Couple<AutomatedExecutionExtender, Map<String, Object>> paramdExec : parameterizedExecutions) {
			execsByProject.add(paramdExec.getA1().getAutomatedProject(), paramdExec);
		}

		return execsByProject;
	}

	private BasicAuthenticationCredentials getAutomationServerCredentials(TestAutomationServer automationServer){
		Optional<Credentials> maybeCredentials = credentialsProvider.getAppLevelCredentials(automationServer);
		Supplier<TestAutomationServerNoCredentialsException> throwIfNull = () -> {
			throw new TestAutomationServerNoCredentialsException(
				String.format(
					getMessage("message.testAutomationServer.noCredentials"),
					automationServer.getName()));
		};
		Credentials credentials = maybeCredentials.orElseThrow(throwIfNull);

		AuthenticationProtocol protocol = credentials.getImplementedProtocol();
		if(!this.supports(protocol)) {
			throw new UnsupportedAuthenticationModeException(protocol.toString());
		}
		return (BasicAuthenticationCredentials) credentials;
	}

	/**
	 * @see TestAutomationJenkinsConnector#findTestAutomationProjectURL(TestAutomationProject)
	 */
	@Override
	public URL findTestAutomationProjectURL(TestAutomationProject testAutomationProject) {
		String projectUrl = getJobPath(testAutomationProject);
		try {
			return new URL(projectUrl);
		} catch (MalformedURLException e) {
			throw new TestAutomationProjectMalformedURLException(projectUrl, e);
		}
	}

	/**
	 * @see TestAutomationJenkinsConnector#testListIsOrderGuaranteed(Collection)
	 */
	@Override
	public boolean testListIsOrderGuaranteed(Collection<AutomatedTest> tests) {
		if (tests.isEmpty()) {
			return true;
		}
		Iterator<AutomatedTest> iterator = tests.iterator();
		String firstPath = iterator.next().getPath();
		for (AutomatedTest test : tests) {
			String path = test.getPath();
			if (!firstPath.equals(path)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean supports(AuthenticationProtocol authenticationProtocol) {
		switch(authenticationProtocol) {
			case BASIC_AUTH:
				return true;
			case OAUTH_1A:
				return false;
			default:
				throw new IllegalArgumentException(
					String.format(GIVEN_PROTOCOL_NOT_SUPPORTED, authenticationProtocol.toString()));
		}
	}

	@Override
	public AuthenticationProtocol[] getSupportedProtocols() {
		return new AuthenticationProtocol[]{BASIC_AUTH};
	}

	public static String getJobPath(TestAutomationProject testAutomationProject) {
		TestAutomationServer server = testAutomationProject.getServer();
		String baseUrl = server.getUrl();
		String jobName = getJobSubPath(testAutomationProject);
		return baseUrl + jobName;
	}

	public static String getJobSubPath(TestAutomationProject testAutomationProject) {
		String jobName = StringUtils.prependIfMissing(testAutomationProject.getJobName(),"/");
		jobName = jobName.replace("/","/job/");
		return jobName;
	}


	public class TestAutomationProjectMalformedURLException extends RuntimeException {

		/**
		 *
		 */
		private static final long serialVersionUID = -4904491027261699261L;

		public TestAutomationProjectMalformedURLException(String projectUrl, Exception e) {
			super("The test automation project url : " + projectUrl + ", is malformed", e);
		}

	}


	// will create the user credentials and store them into the credentials provider
	private void initializeCredentialsCache(String username){

		LOGGER.debug("TestAutomationJenkinsConnector : initializing the credentials cache");

		UserCredentialsCache credentials = new UserCredentialsCache(username);

		credentialsProvider.restoreCache(credentials);

	}

}
