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

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.*;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;
import org.squashtest.tm.service.testautomation.spi.UnreadableResponseException;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

@Service("plugin.testautomation.jenkins.connector")
public class TestAutomationJenkinsConnector implements TestAutomationConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	private static final String CONNECTOR_KIND = "jenkins";
	private static final int DEFAULT_SPAM_INTERVAL_MILLIS = 5000;

	@Inject
	private TaskScheduler taskScheduler;
	@Inject
	private HttpClientProvider clientProvider;

	private JsonParser jsonParser = new JsonParser();
	private HttpRequestFactory requestFactory = new HttpRequestFactory();

	@Value("${tm.test.automation.pollinterval.millis}")
	private int spamInterval = DEFAULT_SPAM_INTERVAL_MILLIS;

	private RequestExecutor requestExecutor = RequestExecutor.getInstance();

	// ****************************** let's roll ****************************************

	@Override
	public String getConnectorKind() {
		return CONNECTOR_KIND;
	}

	@Override
	public boolean checkCredentials(TestAutomationServer server) {

		CloseableHttpClient client = clientProvider.getClientFor(server);

		HttpGet credCheck = requestFactory.newCheckCredentialsMethod(server);

		requestExecutor.execute(client, credCheck);

		// if everything went fine, we may return true. Or else let the exception go.
		return true;

	}

	@Override
	public Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server)
		throws TestAutomationException {

		CloseableHttpClient client = clientProvider.getClientFor(server);

		HttpGet getJobsMethod = requestFactory.newGetJobsMethod(server);

		String response = requestExecutor.execute(client, getJobsMethod);

		try {
			return jsonParser.readJobListFromJson(response);
		} catch (UnreadableResponseException ex) {// NOSONAR (GRF) call stack broken on purpose, i guess
			throw new UnreadableResponseException("Test automation - jenkins : server '" + server
				+ "' returned malformed response : ", ex.getCause()); // NOSONAR (GRF) call stack broken on purpose, i guess
		}

	}

	@Override
	public Collection<AutomatedTest> listTestsInProject(TestAutomationProject project) throws
		TestAutomationException {

		// first we try an optimistic approach
		try {
			OptimisticTestList otl = new OptimisticTestList(clientProvider, project);
			return otl.run();
		}

		// if the file isn't available we regenerate the file
		catch (Exception ex) {// NOSONAR the exception is handled

			CloseableHttpClient client = clientProvider.getClientFor(project.getServer());

			FetchTestListBuildProcessor processor = new FetchTestListBuildProcessor();

			processor.setClient(client);
			processor.setProject(project);
			processor.setBuildAbsoluteId(new BuildAbsoluteId(project.getJobName(), generateNewId()));
			processor.setDefaultReschedulingDelay(spamInterval);

			processor.run();

			return processor.getResult();

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

			new StartTestExecution(buildDef, clientProvider, externalId).run();

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

				jobDefs.add(new BuildDef(entry.getKey(), entry.getValue(), firstEntry.getA1().getNodeName()));
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

	/**
	 * @see TestAutomationJenkinsConnector#findTestAutomationProjectURL(TestAutomationProject)
	 */
	@Override
	public URL findTestAutomationProjectURL(TestAutomationProject testAutomationProject) {
		TestAutomationServer server = testAutomationProject.getServer();
		String projectUrl = server.getBaseURL().toString() + "/job/" + testAutomationProject.getJobName();
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

	public class TestAutomationProjectMalformedURLException extends RuntimeException {

		/**
		 *
		 */
		private static final long serialVersionUID = -4904491027261699261L;

		public TestAutomationProjectMalformedURLException(String projectUrl, Exception e) {
			super("The test automation project url : " + projectUrl + ", is malformed", e);
		}

	}

}
