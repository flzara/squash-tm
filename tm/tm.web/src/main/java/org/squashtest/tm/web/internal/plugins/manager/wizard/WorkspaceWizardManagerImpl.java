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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.domain.library.PluginReferencer;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.service.project.GenericProjectFinder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;

/**
 * @author Gregory Fouquet
 */
@Service
public class WorkspaceWizardManagerImpl implements WorkspaceWizardManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceWizardManagerImpl.class);

	/**
	 * List of known wizards
	 * @Inject does not allow optional values but we may have no wizard. The simplest way is to us e@Autowired with
	 * an empty list as the default.
	 */
	@Autowired(required = false)
	private Collection<WorkspaceWizard> wizards = Collections.emptyList();

	private final MultiValueMap wizardsByWorkspace = new MultiValueMap();

	@Inject
	private GenericProjectFinder projectFinder;

	public WorkspaceWizardManagerImpl() {
	}

	@PostConstruct
	public void registerWizards() {
		for (WorkspaceWizard wizard : wizards) {
			LOGGER.info("Registering workspace wizard {} for workspace {}", wizard, wizard.getDisplayWorkspace());
			wizardsByWorkspace.put(wizard.getDisplayWorkspace(), wizard);
		}
	}


	/**
	 * @see WorkspaceWizardManager#findAllByWorkspace(WorkspaceType)
	 */
	@Override
	public Collection<WorkspaceWizard> findAllByWorkspace(WorkspaceType workspace) {
		Collection<WorkspaceWizard> collection = wizardsByWorkspace.getCollection(workspace);
		if (collection == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableCollection(collection); // ensures that the original collection won't be altered
	}

	@Override
	public WorkspaceWizard findById(String wizardId) {
		for (WorkspaceWizard wizard : findAll()) {
			if (wizard.getId().equals(wizardId)) {
				return wizard;
			}
		}
		throw new NoSuchElementException("cannot find WorkspaceWizard with id " + wizardId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<WorkspaceWizard> findAll() {
		return Collections.unmodifiableCollection(wizards);
	}

	@Override
	public Collection<WorkspaceWizard> findEnabledWizards(long projectId) {
		return findEnabledWizards(projectId, WorkspaceType.TEST_CASE_WORKSPACE, WorkspaceType.REQUIREMENT_WORKSPACE,
				WorkspaceType.CAMPAIGN_WORKSPACE);
	}

	@Override
	public Collection<WorkspaceWizard> findEnabledWizards(long projectId, WorkspaceType workspace) {

		Collection<WorkspaceWizard> wizards = findAllByWorkspace(workspace);
		Collection<String> enabledWizardIds = findEnabledWizardIds(projectId, workspace);

		Predicate predicate = new BelongsToList(enabledWizardIds);

		return filterWizards(wizards, predicate);
	}

	private Collection<WorkspaceWizard> filterWizards(Collection<WorkspaceWizard> wizards, Predicate predicate) {
		Collection<WorkspaceWizard> res = new ArrayList<>(wizards); // 'wizards' is immutable
		CollectionUtils.filter(res, predicate);
		return res;
	}

	@Override
	public Collection<WorkspaceWizard> findEnabledWizards(long projectId, WorkspaceType... workspaces) {
		Collection<WorkspaceWizard> allWizards = new HashSet<>(wizards.size());
		for (WorkspaceType workspace : workspaces) {
			allWizards.addAll(findEnabledWizards(projectId, workspace));
		}
		return allWizards;
	}

	@Override
	public Collection<WorkspaceWizard> findDisabledWizards(long projectId) {
		return findDisabledWizards(projectId, WorkspaceType.TEST_CASE_WORKSPACE, WorkspaceType.REQUIREMENT_WORKSPACE,
				WorkspaceType.CAMPAIGN_WORKSPACE);
	}

	@Override
	public Collection<WorkspaceWizard> findDisabledWizards(long projectId, WorkspaceType workspace) {
		Collection<WorkspaceWizard> wizards = findAllByWorkspace(workspace);
		Collection<String> enabledWizardIds = findEnabledWizardIds(projectId, workspace);

		Predicate predicate = new AbsentFromList(enabledWizardIds);

		return filterWizards(wizards, predicate);

	}

	@Override
	public Collection<WorkspaceWizard> findDisabledWizards(long projectId, WorkspaceType... workspaces) {
		Collection<WorkspaceWizard> allWizards = new HashSet<>();

		for (WorkspaceType workspace : workspaces) {
			allWizards.addAll(findDisabledWizards(projectId, workspace));
		}
		return allWizards;
	}

// ******************************** private stuffs *************************

	private PluginReferencer findLibrary(long projectId, WorkspaceType workspace) {
		GenericProject project = projectFinder.findById(projectId);

		switch (workspace) {
			case TEST_CASE_WORKSPACE:
				return project.getTestCaseLibrary();
			case REQUIREMENT_WORKSPACE:
				return project.getRequirementLibrary();
			case CAMPAIGN_WORKSPACE:
				return project.getCampaignLibrary();
			default:
				throw new IllegalArgumentException("WorkspaceType " + workspace + " is unknown and not covered by this class");
		}
	}

	private Collection<String> findEnabledWizardIds(long projectId, WorkspaceType workspace) {
		return findLibrary(projectId, workspace).getEnabledPlugins();
	}

	private static final class BelongsToList implements Predicate {

		private Collection<String> wizardIds;

		public BelongsToList(Collection<String> wizardIds) {
			this.wizardIds = wizardIds;
		}

		@Override
		public boolean evaluate(Object wizz) {
			String id = ((WorkspaceWizard) wizz).getId();
			return wizardIds.contains(id);
		}

	}

	private static final class AbsentFromList implements Predicate {

		private Collection<String> wizardIds;

		public AbsentFromList(Collection<String> wizardIds) {
			this.wizardIds = wizardIds;
		}

		@Override
		public boolean evaluate(Object wizz) {
			String id = ((WorkspaceWizard) wizz).getId();
			return !wizardIds.contains(id);
		}

	}

}
