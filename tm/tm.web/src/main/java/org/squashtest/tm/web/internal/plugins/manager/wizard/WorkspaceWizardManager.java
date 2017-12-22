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
package org.squashtest.tm.web.internal.plugins.manager.wizard;

import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * @author Gregory Fouquet
 *
 */
public interface WorkspaceWizardManager {


	WorkspaceWizard findById(String wizardId);


	Collection<WorkspaceWizard> findAll();

	/**
	 * Ftches the wizards for the given workspace.
	 *
	 * @param workspace
	 * @return a not null, not modifible collection of wizards
	 */
	Collection<WorkspaceWizard> findAllByWorkspace(@NotNull WorkspaceType workspace);


	/**
	 * returns all the wizards enabled for that project, regardless of the workspace type.
	 *
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findEnabledWizards(long projectId);

	/**
	 * returns all the wizards enabled for that project, restricted to those of the corresponding workspace type.
	 *
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findEnabledWizards(long projectId, WorkspaceType workspace);

	/**
	 * returns all the wizards enabled for that project, restricted to those of the corresponding workspace types.
	 *
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findEnabledWizards(long projectId, WorkspaceType... workspaces);



	/**
	 * returns all the wizards disabled for that project, regardless of the workspace type.
	 *
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findDisabledWizards(long projectId);

	/**
	 * returns all the wizards disabled for that project, restricted to those of the corresponding workspace type.
	 *
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findDisabledWizards(long projectId, WorkspaceType workspace);


	/**
	 * returns all the wizards disabled for that project, restricted to those of the corresponding workspace types.
	 *
	 * @param projectId
	 * @return
	 */
	Collection<WorkspaceWizard> findDisabledWizards(long projectId, WorkspaceType... workspaces);




}


