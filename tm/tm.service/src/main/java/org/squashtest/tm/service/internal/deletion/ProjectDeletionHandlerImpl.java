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
package org.squashtest.tm.service.internal.deletion;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.project.ProjectVisitor;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.exception.library.CannotDeleteProjectException;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.internal.campaign.CampaignNodeDeletionHandler;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.project.ProjectDeletionHandler;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectFilterDao;
import org.squashtest.tm.service.internal.requirement.RequirementNodeDeletionHandler;
import org.squashtest.tm.service.internal.testcase.TestCaseNodeDeletionHandler;
import org.squashtest.tm.service.milestone.MilestoneBindingManagerService;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.ObjectIdentityService;

@Component("squashtest.tm.service.deletion.ProjectDeletionHandler")
public class ProjectDeletionHandlerImpl implements ProjectDeletionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDeletionHandlerImpl.class);

	@Inject
	private ProjectDao projectDao;
	@Inject
	private GenericProjectDao genericProjectDao;
	@Inject
	private CampaignNodeDeletionHandler campaignDeletionHandler;
	@Inject
	private TestCaseNodeDeletionHandler testCaseDeletionHandker;
	@Inject
	private RequirementNodeDeletionHandler requirementDeletionHandler;
	@Inject
	private ObjectIdentityService objectIdentityService;
	@Inject
	private ProjectsPermissionManagementService projectPermissionManagementService;
	@PersistenceContext
	private EntityManager em;
	@Inject
	private CustomReportLibraryNodeDao crlnDao;

	@Inject
	private CustomReportLibraryNodeService crlnService;

	@Inject
	private CustomFieldBindingModificationService bindingService;
	@Inject private MilestoneBindingManagerService milestoneBindingManager;

	@Inject
	ProjectFilterDao projectFilterDao;

	@Override
	public void deleteProject(long projectId) {
		GenericProject project = genericProjectDao.findById(projectId);

		project.accept(new ProjectVisitor() {
			@Override
			public void visit(ProjectTemplate projectTemplate) {
				// NOOP
			}

			@Override
			public void visit(Project project) {
				checkProjectContainsOnlyFolders(project);
			}
		});

		bindingService.removeCustomFieldBindings(projectId);
		milestoneBindingManager.unbindAllMilestonesFromProject(project);


		doDeleteProject(projectId);

	}

	@Override
	public void checkProjectContainsOnlyFolders(Project project) {
		Long nonFolder = projectDao.countNonFoldersInProject(project.getId());
		LOGGER.debug("The project #{} contains {} non folder library nodes", project.getId(), nonFolder);
		if (nonFolder > 0L) {
			throw new CannotDeleteProjectException("non-folders are found in the project");
		}

	}

	private void doDeleteProject(long projectId) {
		LOGGER.debug("The project #" + projectId + " is being deleted");
		GenericProject project = genericProjectDao.findById(projectId);

		CampaignLibrary campaignLibrary = project.getCampaignLibrary();
		deleteLibraryContent(campaignLibrary, campaignDeletionHandler);

		TestCaseLibrary testCaseLibrary = project.getTestCaseLibrary();
		deleteLibraryContent(testCaseLibrary, testCaseDeletionHandker);

		RequirementLibrary requirementLibrary = project.getRequirementLibrary();
		deleteLibraryContent(requirementLibrary, requirementDeletionHandler);

		//deleting the node associated to custom report library
		CustomReportLibrary customReportLibrary = project.getCustomReportLibrary();
		deleteCustomReportLibraryContent(customReportLibrary);


		em.unwrap(Session.class).evict(project);
		project = genericProjectDao.findById(projectId);
		project.accept(new ProjectVisitor() {
			@Override
			public void visit(ProjectTemplate projectTemplate) {
				// NOOP
			}

			@Override
			public void visit(Project project) {
				removeProjectFromFilters(project);
			}
		});

		removeACLsForProjectAndLibraries(project);
		genericProjectDao.delete(project);
	}

	private void deleteCustomReportLibraryContent(CustomReportLibrary customReportLibrary) {
		//1 delete library content ie folders as we cannot have other things because eit was checked early
		List<Long> descendantIds = crlnDao.findAllNodeIdsForLibraryEntity(customReportLibrary.getId());
		crlnService.delete(descendantIds);
		//2 Check if node exist because of Issue 6499 which was basically executing only part of the process (Thanks to the re-indexation optimization mess)
		if(crlnDao.countNodeFromEntity(customReportLibrary).equals(1L)){
			//3 delete the node representing the library. The deletion of the CustomReportLibrary entity will be handled by cascade delete on Project in the last part of the deletion process
			deleteCustomReportLibraryNode(customReportLibrary);
		}
	}

	private void deleteCustomReportLibraryNode(
			CustomReportLibrary customReportLibrary) {
		CustomReportLibraryNode node = crlnDao.findNodeFromEntity(customReportLibrary);
		node.setLibrary(null);
		node.setEntity(null);
		em.remove(node);
		em.flush();
	}

	private void removeACLsForProjectAndLibraries(GenericProject project) {
		long rlId = project.getRequirementLibrary().getId();
		long tclId = project.getTestCaseLibrary().getId();
		long clId = project.getCampaignLibrary().getId();
		long crlId = project.getCustomReportLibrary().getId();
		//remove arse for libraries
		projectPermissionManagementService.removeAllPermissionsFromObject(RequirementLibrary.class, rlId);
		projectPermissionManagementService.removeAllPermissionsFromObject(TestCaseLibrary.class, tclId);
		projectPermissionManagementService.removeAllPermissionsFromObject(CampaignLibrary.class, clId);
		projectPermissionManagementService.removeAllPermissionsFromObject(CustomReportLibrary.class, crlId);
		//remove aoi for libaries
		objectIdentityService.removeObjectIdentity(rlId, RequirementLibrary.class);
		objectIdentityService.removeObjectIdentity(tclId, TestCaseLibrary.class);
		objectIdentityService.removeObjectIdentity(clId, CampaignLibrary.class);
		objectIdentityService.removeObjectIdentity(crlId, CustomReportLibrary.class);
		//remove arse for project
		//and remove aoi for project
		project.accept(new ProjectVisitor() {
			@Override
			public void visit(ProjectTemplate projectTemplate) {
				projectPermissionManagementService.removeAllPermissionsFromObject(ProjectTemplate.class, projectTemplate.getId());
				objectIdentityService.removeObjectIdentity(projectTemplate.getId(), ProjectTemplate.class);
			}

			@Override
			public void visit(Project project) {
				projectPermissionManagementService.removeAllPermissionsFromObject(Project.class, project.getId());
				objectIdentityService.removeObjectIdentity(project.getId(), Project.class);
			}
		});
	}

	private void removeProjectFromFilters(Project project) {
		List<ProjectFilter> projectFilters = projectFilterDao.findByProjectsId(project.getId());
		for (ProjectFilter projectFilter : projectFilters) {
			projectFilter.removeProject(project);
		}
	}

	private void deleteLibraryContent(Library<? extends LibraryNode> library, NodeDeletionHandler<?, ?> deletionHandler) {
		List<? extends LibraryNode> folders = library.getContent();
		if (!folders.isEmpty()) {
			List<Long> nodesIds = retrieveNodesids(folders);
			deletionHandler.deleteNodes(nodesIds);
		}

	}

	private List<Long> retrieveNodesids(List<? extends LibraryNode> folders) {
		List<Long> result = new ArrayList<>();
		for (LibraryNode libraryNode : folders) {
			result.add(libraryNode.getId());
		}
		return result;
	}
}
