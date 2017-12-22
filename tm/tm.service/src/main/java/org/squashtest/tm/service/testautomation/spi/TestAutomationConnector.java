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
package org.squashtest.tm.service.testautomation.spi;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.squashtest.tm.core.foundation.lang.Couple;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.TestAutomationCallbackService;

public interface TestAutomationConnector {

	/**
	 * A String indicating which kind of connector it is
	 * 
	 * @return
	 */
	String getConnectorKind();

	/**
	 * Checks that the given server configuration (including credentials) actually works.
	 * 
	 * @param server
	 * @return true if the credentials work, false otherwise
	 */
	boolean checkCredentials(TestAutomationServer server);

	/**
	 * <p>
	 * Given a server (that contains everything you need to connect it), returns the collection of
	 * {@link TestAutomationProject} that it hosts.
	 * </p>
	 * 
	 * 
	 * @param server
	 * @return a Collection that may never be null if success
	 * @throws ServerConnectionFailed
	 *             if could not connect to the server
	 * @throws AccessDenied
	 *             if the server was reached but the used user could log in
	 * @throws UnreadableResponseException
	 *             if the server replied something that is not suitable for a response or otherwise replied not nicely
	 * @throws NotFoundException
	 *             if the server could not find its projects
	 * @Throws BadConfiguration if something went wrong due to the configuration
	 * @throws TestAutomationException
	 *             for anything that doesn't fit the exceptions above.
	 */
	Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server) throws ServerConnectionFailed,
	AccessDenied, UnreadableResponseException, NotFoundException, BadConfiguration, TestAutomationException;

	/**
	 * <p>
	 * Given a project (that contains everything you need to connect it), returns the collection of
	 * {@link AutomatedTest} that it contains
	 * </p>
	 * 
	 * @param project
	 * 
	 * @return a Collection possibly empty but never null of TestAutomationTest if success
	 * @throws ServerConnectionFailed
	 *             if could not connect to the server
	 * @throws AccessDenied
	 *             if the server was reached but the used user could log in
	 * @throws UnreadableResponseException
	 *             if the server replied something that is not suitable for a response or otherwise was rude to you
	 * @throws NotFoundException
	 *             if the tests in that project cannot be found
	 * @Throws BadConfiguration if something went wrong due to the configuration
	 * @throws TestAutomationException
	 *             for anything that doesn't fit the exceptions above.
	 */
	Collection<AutomatedTest> listTestsInProject(TestAutomationProject project) throws ServerConnectionFailed,
	AccessDenied, UnreadableResponseException, NotFoundException, BadConfiguration, TestAutomationException;

	/**
	 * <p>
	 * Given a bunch of tests, must tell the remote server to execute them. These particular executions of those tests
	 * are grouped and must be identifiable by a reference.
	 * </p>
	 * 
	 * <p>
	 * That method must return immediately after initiating the test start sequence, it must not wait for their
	 * completion. However it may possibly start a background task to oversee the remote executions from here.
	 * </p>
	 * 
	 * @param tests
	 *            the tests that must be executed
	 * @param externalId
	 *            a reference that index the resulting executions of those tests
	 * 
	 * @throws ServerConnectionFailed
	 *             if could not connect to the server
	 * @throws AccessDenied
	 *             if the server was reached but the used user could log in
	 * @throws UnreadableResponseException
	 *             if the server replied something that is not suitable for a response or otherwise thrown garbages at
	 *             you
	 * @throws NotFoundException
	 *             if the tests in that project cannot be found
	 * @Throws BadConfiguration if something went wrong due to the configuration
	 * @throws TestAutomationException
	 *             for anything that doesn't fit the exceptions above.
	 */
	void executeParameterizedTests(Collection<Couple<AutomatedExecutionExtender, Map<String, Object>>> tests,
			String externalId, TestAutomationCallbackService securedCallback);

	/**
	 * <p>
	 * Will build and return the URL to access to the given test automation project's.
	 * </P>
	 * 
	 * @param testAutomationProject
	 *            : the {@link TestAutomationProject} we want the URL of
	 * @return : the URL for the given {@link TestAutomationProject}
	 * @throws SE
	 */
	URL findTestAutomationProjectURL(TestAutomationProject testAutomationProject);

	/**
	 * Will say, depending on the tests ecosystems if the execution order of the given test list is guaranteed.
	 * 
	 * @param tests
	 * @return true if the test list execution order is guaranteed.
	 */
	boolean testListIsOrderGuaranteed(Collection<AutomatedTest> tests);
}
