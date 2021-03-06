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
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;

import java.util.Collection;

public class FetchTestListTask implements TestAutomationConnectorTask<TestAutomationProjectContent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	private TestAutomationConnectorRegistry connectorRegistry;
	private TestAutomationProject project;
	private String username;

	public FetchTestListTask(TestAutomationConnectorRegistry connectorRegistry, TestAutomationProject project, String username) {
		super();
		this.connectorRegistry = connectorRegistry;
		this.project = project;
		this.username = username;
	}

	@Override
	public TestAutomationProjectContent call() throws Exception {

		TestAutomationServer server = project.getServer();
		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());

		Collection<AutomatedTest> allTests = connector.listTestsInProject(project, username);
		boolean orderGuaranteed = connector.testListIsOrderGuaranteed(allTests);
		return new TestAutomationProjectContent(project, allTests, orderGuaranteed);
	}

	@Override
	public TestAutomationProjectContent buildFailedResult(Exception thrownException) {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error(
					"TestAutomationConnector : the task 'fetch test list' failed for project '" + project.getLabel()
					+ "' on server '" + project.getServer().getUrl() + "', caused by :", thrownException);
		}
		return new TestAutomationProjectContent(project, thrownException);
	}

}
