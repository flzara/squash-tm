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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;
import org.squashtest.tm.service.testautomation.spi.TestAutomationConnector;

public class FetchTestListTask implements TestAutomationConnectorTask<TestAutomationProjectContent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestAutomationConnector.class);

	private TestAutomationConnectorRegistry connectorRegistry;
	private TestAutomationProject project;

	public FetchTestListTask(TestAutomationConnectorRegistry connectorRegistry, TestAutomationProject project) {
		super();
		this.connectorRegistry = connectorRegistry;
		this.project = project;
	}

	@Override
	public TestAutomationProjectContent call() throws Exception {
		TestAutomationServer server = project.getServer();
		TestAutomationConnector connector = connectorRegistry.getConnectorForKind(server.getKind());

		Collection<AutomatedTest> allTests = connector.listTestsInProject(project);
		boolean orderGuaranteed = connector.testListIsOrderGuaranteed(allTests);
		return new TestAutomationProjectContent(project, allTests, orderGuaranteed);
	}

	@Override
	public TestAutomationProjectContent buildFailedResult(Exception thrownException) {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error(
					"TestAutomationConnector : the task 'fetch test list' failed for project '" + project.getLabel()
					+ "' on server '" + project.getServer().getBaseURL() + "', caused by :", thrownException);
		}
		return new TestAutomationProjectContent(project, thrownException);
	}

}
