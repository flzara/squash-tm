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
package org.squashtest.tm.plugin.testautomation.jenkins.internal.net;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.FileParameter;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.Parameter;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.ParameterArray;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.JsonParser;
import org.squashtest.tm.service.testautomation.spi.BadConfiguration;
import org.squashtest.tm.service.testautomation.spi.TestAutomationException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * TODO Crudely migrated from httpclient 3 to httpclient 4. Test coverage was mostly null so when it breaks,
 * write some tests. Or use RestTemplate.
 */
public class HttpRequestFactory {

	private static final String JOB_PATH = "/job/";

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestFactory.class);

	private static final String API_URI = "/api/json";

	private static final String TREE = "tree";

	public static final String SYMBOLIC_BUILDFILENAME = "testsuite.json";
	public static final String MULTIPART_BUILDFILENAME = "file0";
	public static final String MULTIPART_JENKINSARGS = "json";

	private static final NameValuePair[] JOB_LIST_QUERY = new NameValuePair[]{
		new BasicNameValuePair(TREE, "jobs[name,color]")
	};

	private static final NameValuePair[] QUEUED_BUILDS_QUERY = new NameValuePair[]{
		new BasicNameValuePair(TREE, "items[id,actions[parameters[name,value]],task[name]]")
	};

	private static final NameValuePair[] EXISTING_BUILDS_QUERY = new NameValuePair[]{
		new BasicNameValuePair(TREE, "builds[building,number,actions[parameters[name,value]]]")
	};

	private static final NameValuePair[] SINGLE_BUILD_QUERY = new NameValuePair[]{
		new BasicNameValuePair(TREE, "building,number,actions[parameters[name,value]]")
	};

	private static final NameValuePair[] BUILD_RESULT_QUERY = new NameValuePair[]{
		new BasicNameValuePair(TREE, "suites[name,cases[name,status]]")
	};

	private final JsonParser jsonParser = new JsonParser();

	private final CallbackURLProvider callbackProvider = new CallbackURLProvider();

	public String newRandomId() {
		return Long.valueOf(System.currentTimeMillis()).toString();
	}

	public HttpGet newCheckCredentialsMethod(TestAutomationServer server) {
		URIBuilder builder = buildApiPath(server);

		HttpGet method = new HttpGet(build(builder));

		String logPass = server.getLogin() + ":" + server.getPassword();
		String auth = new String(Base64.encodeBase64(logPass.getBytes()));

		method.addHeader("Authorization", "Basic " + auth);

		return method;
	}

	private URIBuilder buildApiPath(TestAutomationServer server) {
		URIBuilder uriBuilder = uriBuilder(server);
		return concatPath(uriBuilder, API_URI);
	}
	
	private URIBuilder concatPath(URIBuilder builder, String path){
		return builder.setPath(builder.getPath() + path);
	}

	public HttpGet newGetJobsMethod(TestAutomationServer server) {
		URIBuilder builder = buildApiPath(server);
		builder.setParameters(JOB_LIST_QUERY);

		return new HttpGet(build(builder));
	}

	public HttpPost newStartFetchTestListBuild(TestAutomationProject project, String externalID) {

		ParameterArray params = new ParameterArray(
			new Parameter[]{
				Parameter.operationTestListParameter(),
				Parameter.newExtIdParameter(externalID)
			}
		);

		return newStartBuild(project, params);

	}

	public ParameterArray getStartTestSuiteBuildParameters(String externalID) {
		String strURL = callbackProvider.get().toExternalForm();

		return new ParameterArray(
			new Object[]{
				Parameter.operationRunSuiteParameter(),
				Parameter.newExtIdParameter(externalID),
				Parameter.newCallbackURlParameter(strURL),
				Parameter.testListParameter(),
				new FileParameter(Parameter.SYMBOLIC_FILENAME, MULTIPART_BUILDFILENAME)
			});
	}

	public ParameterArray getStartTestSuiteBuildParameters(String externalID, String executor) {
		String strURL = callbackProvider.get().toExternalForm();

		if (StringUtils.isBlank(executor)) {
			return getStartTestSuiteBuildParameters(externalID);

		} else {
			return new ParameterArray(
				new Object[]{
					Parameter.operationRunSuiteParameter(),
					Parameter.newExtIdParameter(externalID),
					Parameter.newCallbackURlParameter(strURL),
					Parameter.testListParameter(),
					Parameter.executorParameter(executor),
					new FileParameter(Parameter.SYMBOLIC_FILENAME, MULTIPART_BUILDFILENAME)
				});
		}
	}

	public HttpGet newCheckQueue(TestAutomationProject project) {
		TestAutomationServer server = project.getServer();
		URIBuilder builder = uriBuilder(server);
		concatPath(builder, "/queue" + API_URI);
		builder.setParameters(QUEUED_BUILDS_QUERY);
		return new HttpGet(build(builder));
	}

	private URIBuilder uriBuilder(TestAutomationServer server) {
		try {
			return new URIBuilder(server.getBaseURL().toURI());
		} catch (URISyntaxException ex) {
			throw handleUriException(ex);
		}
	}

	public HttpGet newGetBuildsForProject(TestAutomationProject project) {
		TestAutomationServer server = project.getServer();
		URIBuilder builder = uriBuilder(server);
		concatPath(builder, JOB_PATH + project.getJobName() + API_URI);
		builder.setParameters(EXISTING_BUILDS_QUERY);
		return new HttpGet(build(builder));

	}

	public HttpGet newGetBuild(TestAutomationProject project, int buildId) {
		TestAutomationServer server = project.getServer();
		URIBuilder builder = uriBuilder(server);
		concatPath(builder, JOB_PATH + project.getJobName() + '/' + buildId + '/' + API_URI);
		builder.setParameters(SINGLE_BUILD_QUERY);
		return new HttpGet(build(builder));
	}

	public HttpGet newGetBuildResults(TestAutomationProject project, int buildId) {
		TestAutomationServer server = project.getServer();
		URIBuilder builder = uriBuilder(server);
		concatPath(builder, JOB_PATH + project.getJobName() + '/' + buildId + "/testReport/" + API_URI);
		builder.setParameters(BUILD_RESULT_QUERY);
		return new HttpGet(build(builder));
	}

	public HttpGet newGetJsonTestList(TestAutomationProject project) {
		TestAutomationServer server = project.getServer();
		URIBuilder builder = uriBuilder(server);
		concatPath(builder, JOB_PATH + project.getJobName() + "/Test_list/testTree.json");
		return new HttpGet(build(builder));
	}

	public String buildResultURL(AutomatedTest test, Integer buildID) {

		TestAutomationProject project = test.getProject();

		String relativePath = toRelativePath(test);
		TestAutomationServer server = project.getServer();
		URIBuilder builder = uriBuilder(server);
		concatPath(builder, JOB_PATH + project.getJobName() + "/" + buildID + "/testReport/" + relativePath);
		return builder.toString();

	}

	protected HttpPost newStartBuild(TestAutomationProject project, ParameterArray params) {
		TestAutomationServer server = project.getServer();
		URIBuilder builder = uriBuilder(server);
		concatPath(builder, JOB_PATH + project.getJobName() + "/build");

		String jsonParam = jsonParser.toJson(params);

		builder.setParameter("json", jsonParam);

		return new HttpPost(build(builder));

	}

	/**
	 * Mostly softens exceptions from URIBuilder.build()
	 *
	 * @param builder the URIBuilder which shall be used to build URI
	 * @return the built URI
	 */
	private URI build(URIBuilder builder) {
		try {
			return builder.build();
		} catch (URISyntaxException ex) {
			throw handleUriException(ex);
		}
	}

	private TestAutomationException handleUriException(URISyntaxException ex) {
		LOGGER.error("HttpRequestFactory : the URI is invalid, and that was not supposed to happen.");
		return new TestAutomationException(ex);
	}

	private String toRelativePath(AutomatedTest test) {

		String name = "";

		if (test.isAtTheRoot()) {
			name = "(root)/";
		}

		name += test.getPath() + test.getShortName().replaceAll("[-\\.]", "_");

		return name;

	}

	private static class CallbackURLProvider {

		public URL get() {

			CallbackURL callback = CallbackURL.getInstance();
			String strURL = callback.getValue();

			try {
				return new URL(strURL);
			} catch (MalformedURLException ex) {
				BadConfiguration bc = new BadConfiguration(
					"Test Automation configuration : The test could not be started because the service is not configured properly. The url '" + strURL + "' specified at property '" + callback.getConfPropertyName()
						+ "' in configuration file 'tm.testautomation.conf.properties' is malformed. Please contact the administration team.", ex);

				bc.setPropertyName(callback.getConfPropertyName());

				throw bc;
			}

		}

	}

}
