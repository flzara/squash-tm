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
package org.squashtest.tm.service.project;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.exception.NameAlreadyInUseException;

/**
 * @author Gregory Fouquet
 *
 */
public interface CustomGenericProjectManager extends CustomGenericProjectFinder {
	/**
	 * Will find all Projects and Templates to which the user has management access to and return them ordered according
	 * to the given params.
	 *
	 * @param pagingAndSorting
	 *            the {@link PagingAndSorting} that holds order and paging params
	 * @param filter
	 *            the filter to apply on the result
	 * @return a {@link PagedCollectionHolder} containing all projects the user has management access to, ordered
	 *         according to the given params.
	 */
	PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndMultiSorting pagingAndSorting,
			Filtering filtering);

	/**
	 * @param project
	 */
	void persist(GenericProject project) throws NameAlreadyInUseException;

	/**
	 *
	 * @param templateId
	 */
	void coerceTemplateIntoProject(long templateId);

	/************************************************************************************************/
	void deleteProject(long projectId);

	void addNewPermissionToProject(long userId, long projectId, String permission);

	void removeProjectPermission(long userId, long projectId);

	Party findPartyById(long partyId);

	// **************************** test automation extension ********************

	/**
	 * Will bind a TM project to a test automation server. Both are identified by their ID.
	 * The serverId may be null, in which case the TM project is bound to nothing. It will be
	 * then treated as a non automated project.
	 *
	 * @param tmProjectId
	 * @param serverId
	 */
	void bindTestAutomationServer(long tmProjectId, Long serverId);

	/**
	 * Will bind the TM project to a TA project. Will persist it if necessary.
	 *
	 * @param TMprojectId
	 * @param TAproject
	 */
	void bindTestAutomationProject(long tmProjectId, TestAutomationProject taProject);

	void bindTestAutomationProjects(long tmProjectId, Collection<TestAutomationProject> taProjects);

	void unbindTestAutomationProject(long projectId, long taProjectId);

	// ****************************** bugtracker section ****************************

	/**
	 * Change the Bugtracker the Project is associated-to.<br>
	 * If the Project had no Bugtracker, will add a new association.<br>
	 * If the Project had a already a Bugtracker, it will keep the project-Name information
	 *
	 * @param projectId
	 * @param newBugtrackerId
	 */
	void changeBugTracker(long projectId, Long newBugtrackerId);

	/**
	 * Change the Bugtracker the Project is associated-to.<br>
	 * If the Project had no Bugtracker, will add a new association.<br>
	 * If the Project had a already a Bugtracker, it will keep the project-Name information
	 *
	 * @param project
	 *            : the concerned GenericProject
	 * @param bugtracker
	 *            : the bugtracker to bind the project to
	 */
	void changeBugTracker(GenericProject project, BugTracker bugtracker);

	/**
	 * Will remove the association the Project has to it's Bugtracker.
	 *
	 * @param projectId
	 */
	void removeBugTracker(long projectId);

	/**
	 * Will change a bugtracker connexion parameter : the names of the bugtracker's projects it's associated to.
	 *
	 * @param projectId
	 *            the concerned project
	 * @param projectBugTrackerNames
	 *            the names of the bugtracker's projects, the Project is connected to
	 */
	void changeBugTrackerProjectName(long projectId, List<String> projectBugTrackerNames);

	// ****************************** plugins management ***********************

	/**
	 * enables the given plugin for the given workspace of the given project
	 */
	void enablePluginForWorkspace(long projectId, WorkspaceType workspace, String pluginId);

	/**
	 * enables the given plugin for the given workspace of the given project
	 */
	void disablePluginForWorkspace(long projectId, WorkspaceType workspace, String pluginId);

	/**
	 * Returns the configuration of a given plugin for a given project. Returns an empty map if the plugin is not bound
	 * to this project.
	 */
	Map<String, String> getPluginConfiguration(long projectId, WorkspaceType workspace, String pluginId);

	/**
	 * Applies the given configuration to a plugin for a given project. If the plugin wasn't enabled for this project
	 * already, it will be during the process.
	 *
	 * @param projectId
	 * @param workspace
	 * @param pluginId
	 * @param configuration
	 */
	void setPluginConfiguration(long projectId, WorkspaceType workspace, String pluginId,
			Map<String, String> configuration);

	// ***************************** status management *************************

	/**
	 * Enables an execution status for a project
	 *
	 * @param projectId
	 * @param executionStatus
	 */
	void enableExecutionStatus(long projectId, ExecutionStatus executionStatus);

	/**
	 * Disables an execution status for a project
	 *
	 * @param projectId
	 * @param executionStatus
	 */
	void disableExecutionStatus(long projectId, ExecutionStatus executionStatus);

	/**
	 * Returns the list of enabled execution statuses given a project.
	 *
	 * @param projectId
	 * @return
	 */
	Set<ExecutionStatus> enabledExecutionStatuses(long projectId);

	/**
	 * Returns the list of disabled execution statuses given a project.
	 *
	 * @param projectId
	 * @return
	 */
	Set<ExecutionStatus> disabledExecutionStatuses(long projectId);

	/**
	 * Replaces an execution status with another within a project
	 *
	 * @param source
	 * @param target
	 */
	void replaceExecutionStepStatus(long projectId, ExecutionStatus source, ExecutionStatus target);

	/**
	 * Returns true if a given execution status is enabled for a given project, false otherwise
	 *
	 * @param projectId
	 * @param executionStatus
	 * @return
	 */
	boolean isExecutionStatusEnabledForProject(long projectId, ExecutionStatus executionStatus);

	boolean projectUsesExecutionStatus(long projectId,  ExecutionStatus executionStatus);

	void changeName(long projectId, String newName) throws NameAlreadyInUseException;

	GenericProject synchronizeGenericProject(GenericProject target,
			GenericProject source, GenericProjectCopyParameter params);
}