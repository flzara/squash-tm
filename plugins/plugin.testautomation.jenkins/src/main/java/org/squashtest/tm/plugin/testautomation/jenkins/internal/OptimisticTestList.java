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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.squashtest.tm.domain.servers.BasicAuthenticationCredentials;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.plugin.testautomation.jenkins.beans.TestListElement;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpClientProvider;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.HttpRequestFactory;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.net.RequestExecutor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GatherTestList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Rip off {@link GatherTestList}
 *
 * @author bsiri
 *
 */
public class OptimisticTestList {

	private HttpClientProvider clientProvider;
	private TestAutomationProject project;
	private JsonParser parser = new JsonParser();
	private BasicAuthenticationCredentials basicAuthenticationCredentials;


	public OptimisticTestList(HttpClientProvider clientProvider, TestAutomationProject project, BasicAuthenticationCredentials basicAuthenticationCredentials) {
		super();
		this.clientProvider = clientProvider;
		this.project = project;
		this.basicAuthenticationCredentials = basicAuthenticationCredentials;
	}

	public Collection<AutomatedTest> run() {

		CloseableHttpClient client = clientProvider.getClientFor(project.getServer(), basicAuthenticationCredentials.getUsername(), String.valueOf(basicAuthenticationCredentials.getPassword()));

		HttpGet method = new HttpRequestFactory().newGetJsonTestList(project);

		try {
			String response = RequestExecutor.getInstance().execute(client, method);

			TestListElement testList = parser.getTestListFromJson(response);


			Collection<AutomatedTest> tests = new LinkedList<>();
			Map<String, List<String>> testNamesWithLinkTCMap = testList.collectAllTestNamesWithLinkedTestCases();
			testNamesWithLinkTCMap.forEach((testName, linkedTestCases) -> {
				AutomatedTest test = new AutomatedTest(testName, project, linkedTestCases);
				tests.add(test);
			});

			return tests;
		} finally {
			method.releaseConnection();
		}

	}

}
