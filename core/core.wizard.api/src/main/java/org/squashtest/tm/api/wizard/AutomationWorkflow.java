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
package org.squashtest.tm.api.wizard;

import org.squashtest.tm.api.plugin.Plugin;
import org.squashtest.tm.bugtracker.definition.RemoteIssue;
import org.squashtest.tm.domain.testcase.TestCase;

public interface AutomationWorkflow extends Plugin {

	/**
	 * Get the name of this AutomationWorkflow.
	 * Will be displayed in SquashTM administration page to choose the type of Automation Workflow.
	 * @return The name of this AutomationWorkflow
	 */
	String getWorkflowName();

	/**
	 * Get the runnable task that will be scheduled if the polling method is chosen.
	 * @return The Runnable task to schedule
	 */
	Runnable getSynchronizationTask();

	/**
	 * Created new ticket in the remote bugtracker
	 * @return the Remote Issue Key
	 */

	String createNewTicketRemoteServer(TestCase tc);

	/**
	 * add new ligne in remoteAtomationRequestExtender table
	 */
	void createRemoteAutomationRequestExtenderForTestCaseIfNotExist(String remoteIssueKey, TestCase tc);

}