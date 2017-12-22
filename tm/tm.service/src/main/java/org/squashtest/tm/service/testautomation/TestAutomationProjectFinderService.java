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
package org.squashtest.tm.service.testautomation;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.testautomation.spi.AccessDenied;

public interface TestAutomationProjectFinderService {

	TestAutomationProject findProjectById(long projectId);

	/**
	 * <p>
	 * Given the name of a server, will return the list of project currently available on it. The credentials will be
	 * tested on the fly.
	 * </p>
	 * 
	 * @param serverURL
	 * @param login
	 * @param password
	 * 
	 * @return a collection of projects hosted on that server
	 * @throws AccessDenied
	 *             if the given credentials are invalid
	 */
	Collection<TestAutomationProject> listProjectsOnServer(String serverName);

	/**
	 * see {@link #listProjectsOnServer(URL, String, String)}, using its ID for argument
	 * 
	 * @param server
	 * @return
	 */
	Collection<TestAutomationProject> listProjectsOnServer(Long serverId);

	/**
	 * see {@link #listProjectsOnServer(URL, String, String)}, using a {@link TestAutomationServer} for argument
	 * 
	 * @param server
	 * @return
	 */
	Collection<TestAutomationProject> listProjectsOnServer(TestAutomationServer server);

	/**
	 * Will return the ta-project urls mapped by their jobName.
	 * 
	 * @param collection
	 *            : the {@link TestAutomationProject} to get the urls of
	 * @return : a map with
	 *         <ul>
	 *         <li>key : the project's jobName</li>
	 *         <li>value : the project's URL</li>
	 *         </ul>
	 */
	Map<String, URL> findProjectUrls(Collection<TestAutomationProject> collection);
	/**
	 * 
	 * @param projectId
	 * @return  <code>true</code> if the project have been executed, <code>false</code> otherwise
	 */
	boolean hasExecutedTests(long projectId);

	URL findProjectURL(TestAutomationProject project);

}
