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
package org.squashtest.tm.service.internal.project;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.plugin.PluginType;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.core.foundation.exception.ActionException;
import org.squashtest.tm.domain.actionword.ActionWordLibrary;
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeDefinition;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.bdd.BddImplementationTechnology;
import org.squashtest.tm.domain.bdd.BddScriptLanguage;
import org.squashtest.tm.domain.bugtracker.BugTrackerBinding;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStatusReport;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.library.PluginReferencer;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.project.AutomationWorkflowType;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.LibraryPluginBinding;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectForCustomCompare;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.synchronisation.RemoteSynchronisation;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseAutomatable;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequest;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestLibrary;
import org.squashtest.tm.domain.tf.automationrequest.AutomationRequestStatus;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.exception.CompositeDomainException;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.exception.project.LockedParameterException;
import org.squashtest.tm.exception.testautomation.DuplicateTMLabelException;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.infolist.InfoListFinderService;
import org.squashtest.tm.service.internal.repository.ActionWordLibraryNodeDao;
import org.squashtest.tm.service.internal.repository.AutomationRequestDao;
import org.squashtest.tm.service.internal.repository.BugTrackerBindingDao;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.PartyDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.ProjectTemplateDao;
import org.squashtest.tm.service.internal.repository.RemoteAutomationRequestExtenderDao;
import org.squashtest.tm.service.internal.repository.RemoteSynchronisationDao;
import org.squashtest.tm.service.internal.repository.RequirementFolderSyncExtenderDao;
import org.squashtest.tm.service.internal.repository.RequirementSyncExtenderDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateRequirementDao;
import org.squashtest.tm.service.project.CustomGenericProjectFinder;
import org.squashtest.tm.service.project.CustomGenericProjectManager;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.ObjectIdentityService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;
import org.squashtest.tm.service.testcase.CustomTestCaseModificationService;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER_OR_TA_API_CLIENT;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;


@Service("CustomGenericProjectManager")
@Transactional
public class CustomGenericProjectManagerImpl implements CustomGenericProjectManager {

	@Inject
	private GenericProjectDao genericProjectDao;
	@Inject
	private ProjectDao projectDao;
	@Inject
	private ProjectTemplateDao templateDao;
	@Inject
	private BugTrackerBindingDao bugTrackerBindingDao;
	@Inject
	private BugTrackerDao bugTrackerDao;
	@PersistenceContext
	private EntityManager em;
	@Inject
	private RemoteSynchronisationDao remoteSynchronisationDao;
	@Inject
	private PartyDao partyDao;
	@Inject
	private ExecutionDao executionDao;
	@Inject
	private ObjectIdentityService objectIdentityService;
	@Inject
	private Provider<GenericToAdministrableProject> genericToAdministrableConvertor;
	@Inject
	private ProjectsPermissionManagementService permissionsManager;
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private ProjectDeletionHandler projectDeletionHandler;
	@Inject
	private ExecutionProcessingService execProcessing;
	@Inject
	private TestAutomationServerManagerService taServerService;
	@Inject
	private TestAutomationProjectManagerService taProjectService;
	@Inject
	private InfoListFinderService infoListService;
	@Inject
	private CustomFieldBindingModificationService customFieldBindingModificationService;
	@Inject
	private CustomReportLibraryNodeDao customReportLibraryNodeDao;
	@Inject
	private ActionWordLibraryNodeDao actionWordLibraryNodeDao;
	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private CustomTestCaseModificationService customTestCaseModificationService;
	@Inject
	private RequirementFolderSyncExtenderDao requirementFolderSyncExtenderDao;
	@Inject
	private RequirementSyncExtenderDao requirementSyncExtenderDao;
	@Inject
	private HibernateRequirementDao hibernateRequirementDao;
	@Inject
	private AutomationRequestDao automationRequestDao;
	@Inject
	private RemoteAutomationRequestExtenderDao remoteAutomationRequestExtenderDao;

	@Autowired(required = false)
	Collection<WorkspaceWizard> plugins = Collections.EMPTY_LIST;

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomGenericProjectManagerImpl.class);

	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	// ************************* finding projects wrt user role
	// ****************************

	/**
	 * @see org.squashtest.tm.service.project.CustomGenericProjectManager#findSortedProjects(PagingAndMultiSorting,
	 *      Filtering))
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public PagedCollectionHolder<List<GenericProject>> findSortedProjects(PagingAndMultiSorting sorting,
			Filtering filter) {

		Class<? extends GenericProject> type = permissionEvaluationService.hasRole(ROLE_ADMIN) ? GenericProject.class
				: Project.class;
		List<? extends GenericProject> resultset = genericProjectDao.findAllWithTextProperty(type, filter);

		// filter on permission
		List<? extends GenericProject> securedResultset = new LinkedList<>(resultset);
		CollectionUtils.filter(securedResultset, new IsManagerOnObject());

		// Consolidate projects with additional information needed to do the
		// sorting
		List<ProjectForCustomCompare> projects = consolidateProjects(securedResultset);
		sortProjects(projects, sorting);

		// manual paging
		int listsize = projects.size();
		int firstIdx = Math.min(listsize, sorting.getFirstItemIndex());
		int lastIdx = Math.min(listsize, firstIdx + sorting.getPageSize());
		projects = projects.subList(firstIdx, lastIdx);

		securedResultset = extractProjectList(projects);

		return new PagingBackedPagedCollectionHolder<>(sorting, listsize,
			(List<GenericProject>) securedResultset);

	}

	// ************************* finding projects wrt user role
	// ****************************

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void persist(GenericProject project) {

		// plug-in the default info lists
		assignDefaultInfolistToProject(project);

		if (genericProjectDao.countByName(project.getName()) > 0) {
			throw new NameAlreadyInUseException(project.getClass().getSimpleName(), project.getName());
		}

		CampaignLibrary cl = new CampaignLibrary();
		project.setCampaignLibrary(cl);
		em.persist(cl);

		RequirementLibrary rl = new RequirementLibrary();
		project.setRequirementLibrary(rl);
		em.persist(rl);

		TestCaseLibrary tcl = new TestCaseLibrary();
		project.setTestCaseLibrary(tcl);
		em.persist(tcl);

		CustomReportLibrary crl = new CustomReportLibrary();
		project.setCustomReportLibrary(crl);
		em.persist(crl);

		//add the tree node for the CustomReportLibrary as for custom report workspace library
		//object and their representation in tree are distinct entities
		CustomReportLibraryNode crlNode = new CustomReportLibraryNode(CustomReportTreeDefinition.LIBRARY, crl.getId(), project.getName(), crl);
		crlNode.setEntity(crl);
		em.persist(crlNode);

		AutomationRequestLibrary arl = new AutomationRequestLibrary();
		project.setAutomationRequestLibrary(arl);
		em.persist(arl);

		ActionWordLibrary awl = new ActionWordLibrary();
		project.setActionWordLibrary(awl);
		em.persist(awl);

		// add tree node for the ActionWordLibrary
		ActionWordLibraryNode awlNode = new ActionWordLibraryNode(ActionWordTreeDefinition.LIBRARY, awl.getId(), project.getName(), awl);
		awlNode.setEntity(awl);
		em.persist(awlNode);

		// now persist it
		em.persist(project);
		em.flush(); // otherwise ids not available

		objectIdentityService.addObjectIdentity(project.getId(), project.getClass());
		objectIdentityService.addObjectIdentity(tcl.getId(), tcl.getClass());
		objectIdentityService.addObjectIdentity(rl.getId(), rl.getClass());
		objectIdentityService.addObjectIdentity(cl.getId(), cl.getClass());
		objectIdentityService.addObjectIdentity(crl.getId(), crl.getClass());
		objectIdentityService.addObjectIdentity(arl.getId(), arl.getClass());
		objectIdentityService.addObjectIdentity(awl.getId(), awl.getClass());
	}

	private void assignDefaultInfolistToProject(GenericProject project) {
		InfoList defaultCategories = infoListService.findByCode("DEF_REQ_CAT");
		project.setRequirementCategories(defaultCategories);

		InfoList defaultNatures = infoListService.findByCode("DEF_TC_NAT");
		project.setTestCaseNatures(defaultNatures);

		InfoList defaultTypes = infoListService.findByCode("DEF_TC_TYP");
		project.setTestCaseTypes(defaultTypes);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void coerceProjectIntoTemplate(long projectId) {

		Project project = projectDao.getOne(projectId);

		projectDeletionHandler.checkProjectContainsOnlyFolders(projectId);
		projectDeletionHandler.deleteAllLibrariesContent(project);
		projectDeletionHandler.removeProjectFromFilters(project);

		ProjectTemplate template = genericProjectDao.coerceProjectIntoTemplate(projectId);

		objectIdentityService.addObjectIdentity(projectId, ProjectTemplate.class);
		permissionsManager.copyAssignedUsersFromProjectToTemplate(template, projectId);
		permissionsManager.removeAllPermissionsFromProject(projectId);
		objectIdentityService.removeObjectIdentity(projectId, Project.class);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void associateToTemplate(long projectId, long templateId) {
		Project project = projectDao.getOne(projectId);
		ProjectTemplate template = templateDao.getOne(templateId);
		project.setTemplate(template);
		synchronizeProjectFromTemplate(project, template);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void disassociateFromTemplate(long projectId) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		genericProject.setTemplate(null);
	}

	@Override
	@PreAuthorize("hasPermission(#projectId, 'org.squashtest.tm.domain.project.Project' , 'MANAGEMENT')"
			+ " or hasPermission(#projectId, 'org.squashtest.tm.domain.project.ProjectTemplate' , 'MANAGEMENT')"
			+ OR_HAS_ROLE_ADMIN)
	public AdministrableProject findAdministrableProjectById(long projectId) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(genericProject);
		return genericToAdministrableConvertor.get().convertToAdministrableProject(genericProject);
	}

	@Override
	public void addNewPermissionToProject(long userId, long projectId, String permission) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(genericProject);
		permissionsManager.addNewPermissionToProject(userId, projectId, permission);
	}

	@Override
	public void removeProjectPermission(long userId, long projectId) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(genericProject);
		permissionsManager.removeProjectPermission(userId, projectId);

	}

	@Override
	public List<PartyProjectPermissionsBean> findPartyPermissionsBeansByProject(long projectId) {
		return permissionsManager.findPartyPermissionsBeanByProject(projectId);
	}

	@Override
	public PagedCollectionHolder<List<PartyProjectPermissionsBean>> findPartyPermissionsBeanByProject(
			PagingAndSorting sorting, Filtering filtering, long projectId) {
		return permissionsManager.findPartyPermissionsBeanByProject(sorting, filtering, projectId);
	}

	@Override
	public List<PermissionGroup> findAllPossiblePermission() {
		return permissionsManager.findAllPossiblePermission();
	}

	@Override
	public List<Party> findPartyWithoutPermissionByProject(long projectId) {
		return permissionsManager.findPartyWithoutPermissionByProject(projectId);
	}

	@Override
	public Party findPartyById(long partyId) {
		return partyDao.getOne(partyId);
	}

	// ********************************** Test automation section
	// *************************************

	@Override
	public void bindTestAutomationServer(long tmProjectId, Long serverId) {
		GenericProject genericProject = genericProjectDao.getOne(tmProjectId);
		checkManageProjectOrAdmin(genericProject);

		taProjectService.deleteAllForTMProject(tmProjectId);

		TestAutomationServer taServer = null;
		if (serverId != null) {
			taServer = taServerService.findById(serverId);
		}

		genericProject.setTestAutomationServer(taServer);
	}

	@Override
	public void bindTestAutomationProject(long projectId, TestAutomationProject taProject) {

		GenericProject genericProject = genericProjectDao.getOne(projectId);
		bindTestAutomationProject(taProject, genericProject);
	}

	private void bindTestAutomationProject(TestAutomationProject taProject, GenericProject genericProject) {
		checkManageProjectOrAdmin(genericProject);

		TestAutomationServer server = genericProject.getTestAutomationServer();
		taProject.setServer(server);
		taProject.setTmProject(genericProject);

		taProjectService.persist(taProject);
		genericProject.bindTestAutomationProject(taProject);
	}

	@Override
	public void bindTestAutomationProjects(long projectId, Collection<TestAutomationProject> taProjects) {
		checkTAProjectNames(taProjects, projectId);
		for (TestAutomationProject p : taProjects) {
			bindTestAutomationProject(projectId, p);
		}
	}

	private void checkTAProjectNames(Collection<TestAutomationProject> taProjects, long projectId) {
		List<DuplicateTMLabelException> dnes = new ArrayList<>();
		List<String> taProjectNames = genericProjectDao.findBoundTestAutomationProjectLabels(projectId);
		for (TestAutomationProject taProject : taProjects) {
			try {
				checkTAProjecTName(taProject, taProjectNames);
			} catch (DuplicateTMLabelException dne) {
				LOGGER.error(dne.getMessage(), dne);
				dnes.add(dne);
			}
		}
		if (!dnes.isEmpty()) {
			throw new CompositeDomainException(dnes);
		}
	}

	private void checkTAProjecTName(TestAutomationProject taProject, List<String> projectNames) {
		if (projectNames.contains(taProject.getLabel())) {
			throw new DuplicateTMLabelException(taProject.getLabel());
		}

	}

	@Override
	public List<TestAutomationProject> findBoundTestAutomationProjects(long projectId) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(genericProject);
		return genericProjectDao.findBoundTestAutomationProjects(projectId);
	}

	@Override
	public void unbindTestAutomationProject(long projectId, long taProjectId) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(genericProject);
		genericProject.unbindTestAutomationProject(taProjectId);

	}

	/**
	 * @see CustomGenericProjectFinder#findAllAvailableTaProjects(long)
	 */
	@Override
	public Collection<TestAutomationProject> findAllAvailableTaProjects(long projectId) {
		TestAutomationServer server = genericProjectDao.findTestAutomationServer(projectId);
		if (server == null) {
			return Collections.emptyList();
		}
		Collection<TestAutomationProject> availableTaProjects = taProjectService.listProjectsOnServer(server);
		Collection<String> alreadyBoundProjectsJobNames = genericProjectDao
				.findBoundTestAutomationProjectJobNames(projectId);

		Iterator<TestAutomationProject> it = availableTaProjects.iterator();
		while (it.hasNext()) {
			TestAutomationProject taProject = it.next();
			if (alreadyBoundProjectsJobNames.contains(taProject.getJobName())) {
				it.remove();
			}
		}
		return availableTaProjects;
	}
	/**
	 * @see CustomGenericProjectFinder#findAllAvailableTaProjectsWithCredentials(long, String, String)
	 */
	@Override
	public Collection<TestAutomationProject> findAllAvailableTaProjectsWithCredentials(long projectId, String login, String password) {
		TestAutomationServer server = genericProjectDao.findTestAutomationServer(projectId);
		if(server == null) {
			return Collections.emptyList();
		}
		/* We don't want to manipulate the Persistent TestAutomationServer,
		so we create a Copy of it before setting the login and password. */
		TestAutomationServer transientServer = server.createCopy();

		Collection<TestAutomationProject> availableTaProjects = taProjectService.listProjectsOnServer(transientServer, login, password);
		Collection<String> alreadyBoundProjectsJobNames = genericProjectDao.findBoundTestAutomationProjectJobNames(projectId);
		Iterator<TestAutomationProject> it = availableTaProjects.iterator();
		while(it.hasNext()) {
			TestAutomationProject taProject = it.next();
			if(alreadyBoundProjectsJobNames.contains(taProject.getJobName())) {
				it.remove();
			}
		}
		return availableTaProjects;
	}

	/* ----- Scm Repository Section----- */

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void bindScmRepository(long projectId, long scmRepositoryId) {
		genericProjectDao.bindScmRepository(projectId, scmRepositoryId);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void unbindScmRepository(long projectId) {
		genericProjectDao.unbindScmRepository(projectId);
	}

	// ********************************** bugtracker section
	// *************************************

	@Override
	public void changeBugTracker(long projectId, Long newBugtrackerId) {

		GenericProject project = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(project);
		BugTracker newBugtracker = bugTrackerDao.getOne(newBugtrackerId);
		if (newBugtracker != null) {
			changeBugTracker(project, newBugtracker);
		} else {
			throw new UnknownEntityException(newBugtrackerId, BugTracker.class);
		}

	}

	@Override
	public void changeBugTracker(GenericProject project, BugTracker newBugtracker) {
		LOGGER.debug("changeBugTracker for project " + project.getId() + " bt: " + newBugtracker.getId());
		checkManageProjectOrAdmin(project);
		// the project doesn't have bug-tracker connection yet
		if (!project.isBugtrackerConnected()) {
			BugTrackerBinding bugTrackerBinding = new BugTrackerBinding(newBugtracker, project);
			bugTrackerBinding.addProjectName(project.getName());
			project.setBugtrackerBinding(bugTrackerBinding);
		}
		// the project has a bug-tracker connection
		else {
			// and the new one is different from the old one
			if (projectBugTrackerChanges(newBugtracker.getId(), project)) {
				project.getBugtrackerBinding().setBugtracker(newBugtracker);
			}
		}
	}

	private boolean projectBugTrackerChanges(Long newBugtrackerId, GenericProject project) {
		boolean change = true;
		BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
		long bugtrackerId = bugtrackerBinding.getBugtracker().getId();
		if (bugtrackerId == newBugtrackerId) {
			change = false;
		}
		return change;
	}

	@Override
	public void removeBugTracker(long projectId) {
		LOGGER.debug("removeBugTracker for project " + projectId);
		GenericProject project = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(project);
		if (project.isBugtrackerConnected()) {
			BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
			project.removeBugTrackerBinding();
			bugTrackerBindingDao.delete(bugtrackerBinding);
		}
	}



	// **************************** plugin section
	// **********************************

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void enablePluginForWorkspace(long projectId, WorkspaceType workspace, String pluginId, PluginType pluginType) {

		PluginReferencer<?> library = findLibrary(projectId, workspace);
		LibraryPluginBinding binding = library.getPluginBinding(pluginId);
		//verifier si le pluginID n'existe pas deja pr ce projet si c'est cas active = true
		if(binding!=null){
			binding.setActive(true);

			//if the plugin has a remote synchronization it must be activated
			List<RemoteSynchronisation> listRemoteSync = remoteSynchronisationDao.findByProjectIdAndKind(projectId,pluginId);
			listRemoteSync.forEach(remoteSync-> remoteSync.setSynchronisationEnable(true));

			//si c'est jirasync il faut mettre ?? jour  la valeur de active de  la ligne pour le workspace requirement
			if("squash.tm.plugin.jirasync".equals(pluginId)){
				PluginReferencer<?> libraryOther = findLibrary(projectId, WorkspaceType.REQUIREMENT_WORKSPACE);
				LibraryPluginBinding bindingOther = libraryOther.getPluginBinding(pluginId);
				if(bindingOther!=null) {
					bindingOther.setActive(true);
				}
			}
		}else{
			library.enablePlugin(pluginId);
			LibraryPluginBinding newBinding = library.getPluginBinding(pluginId);
			/*add pluginType*/
			if(pluginType!=null){
				newBinding.setPluginType(pluginType);
			}
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void disablePluginForWorkspace(long projectId, List<WorkspaceType> workspaces, String pluginId) {
		for (WorkspaceType workspace : workspaces) {
			PluginReferencer<?> library = findLibrary(projectId, workspace);
			library.disablePlugin(pluginId);

			//remoteSynchronisation
			List<RemoteSynchronisation> listRemoteSync = remoteSynchronisationDao.findByProjectIdAndKind(projectId,pluginId);
			listRemoteSync.forEach(remoteSync-> remoteSynchronisationDao.delete(remoteSync));
		}
	}

	@Override
	public void disablePluginAndSaveConf(long projectId, List<WorkspaceType> workspaces, String pluginId) {
		for (WorkspaceType workspace : workspaces) {
			PluginReferencer<?> library = findLibrary(projectId, workspace);
			LibraryPluginBinding binding = library.getPluginBinding(pluginId);

			if (binding != null) {
				binding.setActive(false);
			}
			//remoteSynchronisation
			List<RemoteSynchronisation> listRemoteSync = remoteSynchronisationDao.findByProjectIdAndKind(projectId,pluginId);
			listRemoteSync.forEach(remoteSync-> remoteSync.setSynchronisationEnable(false));
		}

	}

	@Override
	public boolean hasProjectRemoteSynchronisation(long projectId) {
		return remoteSynchronisationDao.findByProjectId(projectId).size() != 0;
	}

	@Override
	public void setAllSyncToDisable(long projectId){
		List<RemoteSynchronisation> syncList = remoteSynchronisationDao.findByProjectId(projectId);
		for (RemoteSynchronisation sync: syncList) {
			if(sync.isSynchronisationEnable()){
				sync.setSynchronisationEnable(false);
			}
		}
	}

	@Override
	public void deleteAllSync(long projectId){

		List<RemoteSynchronisation> rmList = remoteSynchronisationDao.findByProjectId(projectId);
		List<Long> ids = rmList.stream().map(RemoteSynchronisation::getId).collect(Collectors.toList());
		for (Long id : ids) {
			hibernateRequirementDao.updateManagementMode(id);
		}
		//deleteRequierement folder sync extender
		requirementFolderSyncExtenderDao.deleteByRemoteSynchronisationId(ids);
		//delete sync extender
		requirementSyncExtenderDao.deleteByRemoteSynchronisationId(ids);
		//delete sync
		remoteSynchronisationDao.deleteByProjectId(projectId);
	}

	@Override
	public void deleteAllRemoteAutomationRequestExtenders(long projectId) {
		List<AutomationRequest> automationRequests = automationRequestDao.findByProjectId(projectId);
		List<Long> automationRequestIds = automationRequests.stream().map(AutomationRequest::getId).collect(Collectors.toList());

		// delete remote automation request extenders
		if(!automationRequestIds.isEmpty()){
			remoteAutomationRequestExtenderDao.deleteByAutomationRequestIds(automationRequestIds);
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER_OR_TA_API_CLIENT)
	public Map<String, String> getPluginConfiguration(long projectId, WorkspaceType workspace, String pluginId) {
		return doGetPluginConfiguration(projectId, workspace, pluginId);
	}

	@Override
	public Map<String, String> getPluginConfigurationWithoutCheck(long projectId, WorkspaceType workspace, String pluginId) {
		return doGetPluginConfiguration(projectId, workspace, pluginId);
	}

	private Map<String, String> doGetPluginConfiguration(long projectId, WorkspaceType workspace, String pluginId) {
		PluginReferencer<?> library = findLibrary(projectId, workspace);
		LibraryPluginBinding binding = library.getPluginBinding(pluginId);
		if (binding != null) {
			return binding.getProperties();
		} else {
			return new HashMap<>();
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void setPluginConfiguration(long projectId, WorkspaceType workspace, String pluginId,
			Map<String, String> configuration) {

		PluginReferencer<?> library = findLibrary(projectId, workspace);
		if (!library.isPluginEnabled(pluginId)) {
			library.enablePlugin(pluginId);
		}

		LibraryPluginBinding binding = library.getPluginBinding(pluginId);
		binding.setProperties(configuration);
	}

	// ************************** status configuration section
	// ****************************

	@Override
	public void enableExecutionStatus(long projectId, ExecutionStatus executionStatus) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(genericProject);
		// Parameter is locked if it is a bound Project
		if(genericProject.isBoundToTemplate()) {
			throw new LockedParameterException();
		}
		doEnableExecutionStatus(genericProject, executionStatus);
	}

	@Override
	public void doEnableExecutionStatus(GenericProject genericProject, ExecutionStatus executionStatus) {
		genericProject.getCampaignLibrary().enableStatus(executionStatus);
		if (genericProjectDao.isProjectTemplate(genericProject.getId())) {
			/* TODO: Optimize with a request. */
			Collection<Project> boundProjects = projectDao.findAllBoundToTemplate(genericProject.getId());
			for (Project boundProject : boundProjects) {
				boundProject.getCampaignLibrary().enableStatus(executionStatus);
			}
		}
	}

	@Override
	public void disableExecutionStatus(long projectId, ExecutionStatus executionStatus) {
		GenericProject project = genericProjectDao.getOne(projectId);
		// Parameter is locked if the Project is bound to a Template
		if(project.isBoundToTemplate()) {
			throw new LockedParameterException();
		}
		checkManageProjectOrAdmin(project);
		doDisableExecutionStatus(project, executionStatus);
	}

	@Override
	public void doDisableExecutionStatus(GenericProject genericProject, ExecutionStatus executionStatus) {
		genericProject.getCampaignLibrary().disableStatus(executionStatus);
		/* If the GenericProject is a Template, propagate modification to bound Projects. */
		if (genericProjectDao.isProjectTemplate(genericProject.getId())) {
			Collection<Project> boundProjects = projectDao.findAllBoundToTemplate(genericProject.getId());
			for (Project boundProject : boundProjects) {
				boundProject.getCampaignLibrary().disableStatus(executionStatus);
			}
		}
	}

	@Override
	public Set<ExecutionStatus> enabledExecutionStatuses(long projectId) {
		GenericProject project = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(project);

		Set<ExecutionStatus> statuses = new HashSet<>();
		statuses.addAll(Arrays.asList(ExecutionStatus.values()));

		Set<ExecutionStatus> disabledStatuses = project.getCampaignLibrary().getDisabledStatuses();

		statuses.removeAll(disabledStatuses);
		statuses.removeAll(ExecutionStatus.TA_STATUSES_ONLY);

		return statuses;
	}

	@Override
	public Set<ExecutionStatus> disabledExecutionStatuses(long projectId) {
		GenericProject project = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(project);
		return project.getCampaignLibrary().getDisabledStatuses();
	}

	@Override
	public void replaceExecutionStepStatus(long projectId, ExecutionStatus source, ExecutionStatus target) {

		// save the ids of executions having steps with the source status
		List<Long> modifiedExecutionIds = executionDao.findExecutionIdsHavingStepStatus(projectId, source);

		// now modify the step statuses
		executionDao.replaceExecutionStepStatus(projectId, source, target);

		// now update the execution status
		for (Long id : modifiedExecutionIds) {
			ExecutionStatusReport report = execProcessing.getExecutionStatusReport(id);
			execProcessing.setExecutionStatus(id, report);
		}

		// finally update the item test plans
		executionDao.replaceTestPlanStatus(projectId, source, target);
	}

	@Override
	public boolean isExecutionStatusEnabledForProject(long projectId, ExecutionStatus executionStatus) {
		Set<ExecutionStatus> statuses = disabledExecutionStatuses(projectId);
		return !statuses.contains(executionStatus);
	}

	@Override
	public boolean projectUsesExecutionStatus(long projectId, ExecutionStatus executionStatus) {

		return executionDao.projectUsesExecutionStatus(projectId, executionStatus);
	}

	// **************** Custom comparator **************

	/**
	 * Get back the list of project from the list of consolidated project.
	 *
	 * @param projects
	 *            The list to be converted
	 * @return The list of generic projects
	 */
	private List<GenericProject> extractProjectList(final List<ProjectForCustomCompare> projects) {

		List<GenericProject> liste = new LinkedList<>();

		for (final ProjectForCustomCompare project : projects) {
			liste.add(project.getGenericProject());
		}
		return liste;
	}

	/**
	 * Create a list of ProjectForCustomCompare from a list of projects. The
	 * ProjectForCustomCompare contains the projects and some information needed
	 * to do the sorting.
	 *
	 * @param projects
	 *            The list of project to consolidate with additional data
	 * @return the list of consolidated data
	 */
	private List<ProjectForCustomCompare> consolidateProjects(final List<? extends GenericProject> projects) {

		List<ProjectForCustomCompare> consolidatedProjects = new LinkedList<>();

		for (final GenericProject project : projects) {

			ProjectForCustomCompare customProject = new ProjectForCustomCompare();
			customProject.setGenericProject(project);
			customProject.setAutomated(project.isTestAutomationEnabled());
			customProject.setBugtracker(project.isBugtrackerConnected() ? project.getBugtrackerBinding()
					.getBugtracker().getKind() : null);

			if (permissionsManager.findPartyPermissionsBeanByProject(project.getId()).isEmpty()) {
				customProject.setHabilitation(false);
			} else {
				customProject.setHabilitation(true);
			}

			consolidatedProjects.add(customProject);
		}

		return consolidatedProjects;
	}

	private void sortProjects(final List<ProjectForCustomCompare> securedResultset, final PagingAndMultiSorting sorter) {

		Collections.sort(securedResultset, new Comparator<ProjectForCustomCompare>() {

			@Override
			public int compare(ProjectForCustomCompare o1, ProjectForCustomCompare o2) {

				return buildProjectComparator(sorter, o1, o2).toComparison();
			}
		});
	}

	// **************** private stuffs **************

	/**
	 * Create the multiple sorter
	 *
	 * @param sorter
	 *            the rules used to sort
	 * @param o1
	 *            the first item to compare
	 * @param o2
	 *            the second item to compare
	 * @return the sorter
	 */
	private CompareToBuilder buildProjectComparator(final PagingAndMultiSorting sorter,
			final ProjectForCustomCompare o1, final ProjectForCustomCompare o2) {
		CompareToBuilder comp = new CompareToBuilder();
		GenericProject firstProject = o1.getGenericProject();
		GenericProject secondProject = o2.getGenericProject();

		AuditableMixin firstAudit = (AuditableMixin) firstProject;
		AuditableMixin secondAudit = (AuditableMixin) secondProject;
		for (final Sorting sorting : sorter.getSortings()) {

			Object first;
			Object second;
			String sortingAttribute = sorting.getSortedAttribute();

			switch (sortingAttribute) {
			case "name":
				first = firstProject.getName();
				second = secondProject.getName();
				break;

			case "label":
				first = firstProject.getLabel();
				second = secondProject.getLabel();
				break;

			case "active":
				first = firstProject.isActive();
				second = secondProject.isActive();
				break;

			case "audit.createdOn":
				first = firstAudit.getCreatedOn();
				second = secondAudit.getCreatedOn();
				break;

			case "audit.createdBy":
				first = firstAudit.getCreatedBy();
				second = secondAudit.getCreatedBy();
				break;

			case "audit.lastModifiedOn":
				first = firstAudit.getLastModifiedOn();
				second = secondAudit.getLastModifiedOn();
				break;

			case "audit.lastModifiedBy":
				first = firstAudit.getLastModifiedBy();
				second = secondAudit.getLastModifiedBy();
				break;

			case "habilitation":
				first = o1.hasHabilitation();
				second = o2.hasHabilitation();
				break;

			case "bugtracker":
				first = o1.getBugtracker();
				second = o2.getBugtracker();
				break;

			case "automation":
				first = o1.isAutomated();
				second = o2.isAutomated();
				break;

			default:
				throw new IllegalArgumentException("Sorting attribute" + sortingAttribute
						+ " is unknown and is not covered");
			}

			if (sorting.getSortOrder() == SortOrder.DESCENDING) {
				comp.append(first, second);
			} else {
				comp.append(second, first);
			}
		}

		return comp;
	}

	private PluginReferencer<?> findLibrary(long projectId, WorkspaceType workspace) {
		GenericProject project = genericProjectDao.getOne(projectId);

		switch (workspace) {
		case TEST_CASE_WORKSPACE:
			return project.getTestCaseLibrary();
		case REQUIREMENT_WORKSPACE:
			return project.getRequirementLibrary();
		case CAMPAIGN_WORKSPACE:
			return project.getCampaignLibrary();
		default:
			throw new IllegalArgumentException("WorkspaceType " + workspace + " is unknown and is not covered");
		}
	}

	private void checkManageProjectOrAdmin(GenericProject genericProject) {
		permissionEvaluationService.hasRoleOrPermissionOnObject(ROLE_ADMIN, "MANAGEMENT", genericProject);
	}

	private final class IsManagerOnObject implements Predicate {
		@Override
		public boolean evaluate(Object object) {
			return permissionEvaluationService.hasRoleOrPermissionOnObject(ROLE_ADMIN, "MANAGEMENT", object);
		}
	}

	/**
	 * @see org.squashtest.tm.service.project.CustomGenericProjectManager#changeName(long,
	 *      java.lang.String)
	 */
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	@Override
	public void changeName(long projectId, String newName) {
		GenericProject project = genericProjectDao.getOne(projectId);
		if (StringUtils.equals(project.getName(), newName)) {
			return;
		}
		if (genericProjectDao.countByName(newName) > 0) {
			throw new NameAlreadyInUseException(project.getClass().getSimpleName(), newName);
		}
		CustomReportLibrary crl = project.getCustomReportLibrary();
		CustomReportLibraryNode node = customReportLibraryNodeDao.findNodeFromEntity(crl);
		node.setName(newName);
		ActionWordLibrary awl = project.getActionWordLibrary();
		ActionWordLibraryNode actionWordLibraryNode = actionWordLibraryNodeDao.findNodeFromEntity(awl);
		actionWordLibraryNode.setName(newName);

		project.setName(newName);
	}

	private void copyMilestone(GenericProject target, GenericProject source) {

		List<Milestone> milestones = getOnlyBindableMilestones(source.getMilestones());

		target.bindMilestones(milestones);

		for (Milestone milestone: milestones){
			milestone.addProjectToPerimeter(target);
		}
	}

	private List<Milestone> getOnlyBindableMilestones(List<Milestone> milestones) {
		List<Milestone> bindableMilestones = new ArrayList<>();
		for (Milestone m : milestones){
			if (m.getStatus().isBindableToProject()){
				bindableMilestones.add(m);
			}
		}
		return bindableMilestones;
	}

	private void copyTestAutomationSettings(GenericProject target, GenericProject source) {

		target.setTestAutomationServer(source.getTestAutomationServer());

		for (TestAutomationProject automationProject : source.getTestAutomationProjects()) {
			TestAutomationProject taCopy = automationProject.createCopy();
			bindTestAutomationProject(target.getId(), taCopy);

		}
	}

	private void copyImplementationTechnologyAndScriptLanguage(GenericProject target, GenericProject source) {
		target.setBddImplementationTechnology(source.getBddImplementationTechnology());
		target.setBddScriptLanguage(source.getBddScriptLanguage());
	}

	private void copyAutomationWorkflowSettings(GenericProject target, GenericProject source) {
		target.setAllowAutomationWorkflow(source.isAllowAutomationWorkflow());
		target.setAutomationWorkflowType(source.getAutomationWorkflowType());
	}

	private void copyBugtrackerSettings(GenericProject target, GenericProject source) {
		if (source.isBugtrackerConnected()) {
			changeBugTracker(target, source.getBugtrackerBinding().getBugtracker());
		}
	}

	private void copyCustomFieldsSettings(GenericProject target, GenericProject source) {
		customFieldBindingModificationService.copyCustomFieldsSettingsFromTemplate(target, source);
	}

	private void copyAssignedUsers(GenericProject target, GenericProject source) {
		permissionsManager.copyAssignedUsers(target, source);
	}

	private void copyInfolists(GenericProject target, GenericProject source){
		target.setRequirementCategories(source.getRequirementCategories());
		target.setTestCaseNatures(source.getTestCaseNatures());
		target.setTestCaseTypes(source.getTestCaseTypes());
	}

	private void copyExecutionStatuses(GenericProject target, GenericProject source) {

		Set<ExecutionStatus> enabledStatuses = enabledExecutionStatuses(source.getId());
		Set<ExecutionStatus> disabledStatuses = disabledExecutionStatuses(source.getId());

		for(ExecutionStatus execStatusToEnable : enabledStatuses) {
			doEnableExecutionStatus(target, execStatusToEnable);
		}
		for(ExecutionStatus execStatusToDisable : disabledStatuses) {
			doDisableExecutionStatus(target, execStatusToDisable);
		}
	}

	private void copyPlugins(GenericProject target, GenericProject source) {
		PluginType pluginType;
		for(String pluginId : source.getRequirementLibrary().getEnabledPlugins()) {
			target.getRequirementLibrary().enablePlugin(pluginId);
		}
		for(String pluginId : source.getTestCaseLibrary().getEnabledPlugins()) {
			target.getTestCaseLibrary().enablePlugin(pluginId);
			LibraryPluginBinding lpb = source.getTestCaseLibrary().getPluginBinding(pluginId);
			if(lpb!=null) {
				pluginType = lpb.getPluginType();
				if (pluginType != null) {
					target.getTestCaseLibrary().getPluginBinding(pluginId).setPluginType(pluginType);
				}
			}
		}
		for(String pluginId : source.getCampaignLibrary().getEnabledPlugins()) {
			target.getCampaignLibrary().enablePlugin(pluginId);
		}
	}

	@PreAuthorize(HAS_ROLE_ADMIN)
	@Override
	public GenericProject synchronizeGenericProject(GenericProject target,
			GenericProject source, GenericProjectCopyParameter params) {

		if (params.isCopyPermissions()) {
			copyAssignedUsers(target, source);
		}
		if (params.isCopyCUF()) {
			copyCustomFieldsSettings(target, source);
		}
		if (params.isCopyBugtrackerBinding()) {
			copyBugtrackerSettings(target, source);
		}
		if (params.isCopyAutomatedProjects()) {
			copyImplementationTechnologyAndScriptLanguage(target, source);
			copyTestAutomationSettings(target, source);
			copyAutomationWorkflowSettings(target, source);
		}
		if (params.isCopyInfolists()) {
			copyInfolists(target, source);
		}
		if (params.isCopyMilestone()) {
			copyMilestone(target, source);
		}
		if (params.isCopyAllowTcModifFromExec()) {
			target.setAllowTcModifDuringExec(source.allowTcModifDuringExec());
		}
		if(params.isCopyOptionalExecStatuses()) {
			copyExecutionStatuses(target, source);
		}
		if(params.isCopyPlugins()) {
			copyPlugins(target, source);
		}

		return target;
	}

	@PreAuthorize(HAS_ROLE_ADMIN)
	@Override
	public GenericProject synchronizeProjectFromTemplate(Project target, ProjectTemplate source) {
		copyCustomFieldsSettings(target, source);
		copyInfolists(target, source);
		target.setAllowTcModifDuringExec(source.allowTcModifDuringExec());
		copyExecutionStatuses(target, source);
		copyPlugins(target, source);
		if(target.getBugtrackerBinding() == null) {
			copyBugtrackerSettings(target, source);
		}
		copyImplementationTechnologyAndScriptLanguage(target, source);
		copyAutomationWorkflowSettings(target, source);
		if(target.getTestAutomationServer() == null) {
			copyTestAutomationSettings(target, source);
		}
		return target;
	}

	@Override
	public void changeBugTrackerProjectName(long projectId, List<String> projectBugTrackerNames) {

		GenericProject project = genericProjectDao.getOne(projectId);
		checkManageProjectOrAdmin(project);
		if (project.isBugtrackerConnected()) {
			BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
			bugtrackerBinding.setProjectNames(projectBugTrackerNames);
		}

	}

	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	@Override
	public void changeAllowTcModifDuringExec(long projectId, boolean active) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		if(!genericProject.isBoundToTemplate()) {
			genericProject.setAllowTcModifDuringExec(active);
			/* If project is a Template, propagate on all the bound projects. */
			if (ProjectHelper.isTemplate(genericProject)) {
				templateDao.propagateAllowTcModifDuringExec(projectId, active);
			}
		} else {
			throw new LockedParameterException();
		}
	}

	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	@Override
	public boolean checkIfTcGherkinHaveTaScript(Long projectId) {
		boolean check = false;
		Integer number = testCaseDao.countScriptedTestCaseAssociatedToTAScriptByProject(projectId);
		if(number > 0) {
			check = true;
		}
		return check;
	}

	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	@Override
	public void changeAutomationWorkflow(long projectId, boolean active) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);

		genericProject.setAllowAutomationWorkflow(active);

		if (active) {
			List<Long> tcIds = testCaseDao.findAllTestCaseAssociatedToTAScriptByProject(projectId);
			createAutomationRequestForTc(tcIds);
		}
		/* If project is a Template, propagate on all the bound projects. */
		if (ProjectHelper.isTemplate(genericProject)) {
			templateDao.propagateAllowAutomationWorkflow(projectId, active);
		}
	}

	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	@Override
	public void changeAutomationWorkflow(long projectId, String automationWorkflow) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		genericProject.setAutomationWorkflowType(AutomationWorkflowType.valueOf(automationWorkflow));

		// Since allowAutomationWorkflow still exists, we have to update it consequently
		boolean active = !automationWorkflow.equals("NONE");
		changeAutomationWorkflow(projectId, active);
	}

	@Override
	public void changeUseTreeStructureInScmRepo(long projectId, boolean activated) {
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		genericProject.setUseTreeStructureInScmRepo(activated);
	}

	private void createAutomationRequestForTc(List<Long> tcIds) {

		for(int x = 0; x < tcIds.size(); x++) {
			Long tcId = tcIds.get(x);
			TestCase tc = testCaseDao.findById(tcId);
			tc.setAutomatable(TestCaseAutomatable.Y);
			customTestCaseModificationService.createRequestForTestCase(tcId, AutomationRequestStatus.AUTOMATED);
			if (x % 20 == 0) {
				em.flush();
				em.clear();
			}
		}
	}

	@Override
	public boolean isProjectUsingWorkflow(long projectId) {
		boolean isProjectUsingWorkflow=false;
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		String workflowType = genericProject.getAutomationWorkflowType().getI18nKey();
		if(workflowType.equals(AutomationWorkflowType.REMOTE_WORKFLOW.getI18nKey())){
			//check if the plugin exists
			for (WorkspaceWizard plugin : plugins) {
				if(PluginType.AUTOMATION.equals(plugin.getPluginType())){
					isProjectUsingWorkflow = true;
					break;
				}
			}
					}
		return isProjectUsingWorkflow;
	}

	@Override
	public void changeBddImplTechnology(long projectId, String bddImplTechnology) {
		BddImplementationTechnology newBddImplTechnology = BddImplementationTechnology.valueOf(bddImplTechnology);
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		genericProject.setBddImplementationTechnology(newBddImplTechnology);
		if(BddImplementationTechnology.ROBOT.equals(newBddImplTechnology)) {
			genericProject.setBddScriptLanguage(BddScriptLanguage.ENGLISH);
		}
	}

	@Override
	public void changeBddScriptLanguage(long projectId, String bddScriptLanguage) {
		BddScriptLanguage newBddScriptLanguage = BddScriptLanguage.valueOf(bddScriptLanguage);
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		BddImplementationTechnology currentBddImplTechnology = genericProject.getBddImplementationTechnology();
		if (BddImplementationTechnology.ROBOT.equals(currentBddImplTechnology)
			&& !BddScriptLanguage.ENGLISH.equals(newBddScriptLanguage)) {
			throw new IllegalArgumentException("No language other than English can be set for a Robot project.");
		}
		genericProject.setBddScriptLanguage(newBddScriptLanguage);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public Integer changeAutomatedSuitesLifetime(long projectId, String rawLifetime) {
		Integer lifetime;
		try {
			if (StringUtils.isBlank(rawLifetime)) {
				lifetime = null;
			} else {
				lifetime = new Integer(rawLifetime);
				if (lifetime < 0) {
					throw new IllegalArgumentException();
				}
			}
		} catch (IllegalArgumentException ex) {
			throw new WrongLifetimeFormatException(ex);
		}
		GenericProject genericProject = genericProjectDao.getOne(projectId);
		genericProject.setAutomatedSuitesLifetime(lifetime);
		return lifetime;
	}
}
