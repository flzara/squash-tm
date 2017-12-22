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

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN_OR_PROJECT_MANAGER;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.workspace.WorkspaceType;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
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
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.LibraryPluginBinding;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectForCustomCompare;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.exception.CompositeDomainException;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.exception.testautomation.DuplicateTMLabelException;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.customfield.CustomFieldBindingModificationService;
import org.squashtest.tm.service.execution.ExecutionProcessingService;
import org.squashtest.tm.service.infolist.InfoListFinderService;
import org.squashtest.tm.service.internal.repository.BugTrackerBindingDao;
import org.squashtest.tm.service.internal.repository.BugTrackerDao;
import org.squashtest.tm.service.internal.repository.CustomReportLibraryNodeDao;
import org.squashtest.tm.service.internal.repository.ExecutionDao;
import org.squashtest.tm.service.internal.repository.GenericProjectDao;
import org.squashtest.tm.service.internal.repository.PartyDao;
import org.squashtest.tm.service.milestone.MilestoneBindingManagerService;
import org.squashtest.tm.service.project.CustomGenericProjectFinder;
import org.squashtest.tm.service.project.CustomGenericProjectManager;
import org.squashtest.tm.service.project.GenericProjectCopyParameter;
import org.squashtest.tm.service.project.ProjectsPermissionManagementService;
import org.squashtest.tm.service.security.ObjectIdentityService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testautomation.TestAutomationProjectManagerService;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;

@Service("CustomGenericProjectManager")
@Transactional
public class CustomGenericProjectManagerImpl implements CustomGenericProjectManager {

	@Inject
	private GenericProjectDao genericProjectDao;

	@Inject
	private BugTrackerBindingDao bugTrackerBindingDao;
	@Inject
	private BugTrackerDao bugTrackerDao;
	@PersistenceContext
	private EntityManager em;

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

	@Inject private MilestoneBindingManagerService milestoneBindingManager;

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomGenericProjectManagerImpl.class);

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

		Class<? extends GenericProject> type = permissionEvaluationService.hasRole("ROLE_ADMIN") ? GenericProject.class
				: Project.class;
		List<? extends GenericProject> resultset = genericProjectDao.findAllWithTextProperty(type, filter);

		// filter on permission
		List<? extends GenericProject> securedResultset = new LinkedList<>(resultset);
		CollectionUtils.filter(securedResultset, new IsManagerOnObject());

		// Consolidate projects with additional information neeeded to do the
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

		// now persist it
		em.persist(project);
		em.flush(); // otherwise ids not available


		objectIdentityService.addObjectIdentity(project.getId(), project.getClass());
		objectIdentityService.addObjectIdentity(tcl.getId(), tcl.getClass());
		objectIdentityService.addObjectIdentity(rl.getId(), rl.getClass());
		objectIdentityService.addObjectIdentity(cl.getId(), cl.getClass());
		objectIdentityService.addObjectIdentity(crl.getId(), crl.getClass());

	}

	private void assignDefaultInfolistToProject(GenericProject project) {
		InfoList defaultCategories = infoListService.findByCode("DEF_REQ_CAT");
		project.setRequirementCategories(defaultCategories);

		InfoList defaultNatures = infoListService.findByCode("DEF_TC_NAT");
		project.setTestCaseNatures(defaultNatures);

		InfoList defaultTypes = infoListService.findByCode("DEF_TC_TYP");
		project.setTestCaseTypes(defaultTypes);
	}

	/**
	 * @see org.squashtest.tm.service.project.CustomGenericProjectManager#coerceTemplateIntoProject(long)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void coerceTemplateIntoProject(long templateId) {
		Project project = genericProjectDao.coerceTemplateIntoProject(templateId);

		objectIdentityService.addObjectIdentity(templateId, Project.class);
		permissionsManager.copyAssignedUsersFromTemplate(project, templateId);
		permissionsManager.removeAllPermissionsFromProjectTemplate(templateId);
		objectIdentityService.removeObjectIdentity(templateId, ProjectTemplate.class);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void deleteProject(long projectId) {
		projectDeletionHandler.deleteProject(projectId);
	}

	@Override
	@PreAuthorize("hasPermission(#projectId, 'org.squashtest.tm.domain.project.Project' , 'MANAGEMENT')"
			+ " or hasPermission(#projectId, 'org.squashtest.tm.domain.project.ProjectTemplate' , 'MANAGEMENT')"
			+ OR_HAS_ROLE_ADMIN)
	public AdministrableProject findAdministrableProjectById(long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		return genericToAdministrableConvertor.get().convertToAdministrableProject(genericProject);
	}

	@Override
	public void addNewPermissionToProject(long userId, long projectId, String permission) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		permissionsManager.addNewPermissionToProject(userId, projectId, permission);
	}

	@Override
	public void removeProjectPermission(long userId, long projectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
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
		return partyDao.findOne(partyId);
	}

	// ********************************** Test automation section
	// *************************************

	@Override
	public void bindTestAutomationServer(long tmProjectId, Long serverId) {
		GenericProject genericProject = genericProjectDao.findById(tmProjectId);
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

		GenericProject genericProject = genericProjectDao.findById(projectId);
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
		GenericProject genericProject = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(genericProject);
		return genericProjectDao.findBoundTestAutomationProjects(projectId);
	}

	@Override
	public void unbindTestAutomationProject(long projectId, long taProjectId) {
		GenericProject genericProject = genericProjectDao.findById(projectId);
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
	 * @see CustomGenericProjectFinder#findAllAvailableTaProjects(long, String, String)
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
		transientServer.setLogin(login);
		transientServer.setPassword(password);
		
		Collection<TestAutomationProject> availableTaProjects = taProjectService.listProjectsOnServer(transientServer);
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
	

	// ********************************** bugtracker section
	// *************************************

	@Override
	public void changeBugTracker(long projectId, Long newBugtrackerId) {

		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		BugTracker newBugtracker = bugTrackerDao.findOne(newBugtrackerId);
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
		GenericProject project = genericProjectDao.findById(projectId);
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
	public void enablePluginForWorkspace(long projectId, WorkspaceType workspace, String pluginId) {
		PluginReferencer<?> library = findLibrary(projectId, workspace);
		library.enablePlugin(pluginId);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public void disablePluginForWorkspace(long projectId, WorkspaceType workspace, String pluginId) {
		PluginReferencer<?> library = findLibrary(projectId, workspace);
		library.disablePlugin(pluginId);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	public Map<String, String> getPluginConfiguration(long projectId, WorkspaceType workspace, String pluginId) {
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
		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		project.getCampaignLibrary().enableStatus(executionStatus);
	}

	@Override
	public void disableExecutionStatus(long projectId, ExecutionStatus executionStatus) {
		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		project.getCampaignLibrary().disableStatus(executionStatus);
	}

	@Override
	public Set<ExecutionStatus> enabledExecutionStatuses(long projectId) {
		GenericProject project = genericProjectDao.findById(projectId);
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
		GenericProject project = genericProjectDao.findById(projectId);
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
		GenericProject project = genericProjectDao.findById(projectId);

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
		permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "MANAGEMENT", genericProject);
	}

	private final class IsManagerOnObject implements Predicate {
		@Override
		public boolean evaluate(Object object) {
			return permissionEvaluationService.hasRoleOrPermissionOnObject("ROLE_ADMIN", "MANAGEMENT", object);
		}
	}

	/**
	 * @see org.squashtest.tm.service.project.CustomGenericProjectManager#changeName(long,
	 *      java.lang.String)
	 */
	@PreAuthorize(HAS_ROLE_ADMIN_OR_PROJECT_MANAGER)
	@Override
	public void changeName(long projectId, String newName) {
		GenericProject project = genericProjectDao.findById(projectId);
		if (StringUtils.equals(project.getName(), newName)) {
			return;
		}
		if (genericProjectDao.countByName(newName) > 0) {
			throw new NameAlreadyInUseException(project.getClass().getSimpleName(), newName);
		}
		CustomReportLibrary crl = project.getCustomReportLibrary();
		CustomReportLibraryNode node = customReportLibraryNodeDao.findNodeFromEntity(crl);
		project.setName(newName);
		node.setName(newName);
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
			copyTestAutomationSettings(target, source);
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

		return target;
	}


	@Override
	public void changeBugTrackerProjectName(long projectId, List<String> projectBugTrackerNames) {

		GenericProject project = genericProjectDao.findById(projectId);
		checkManageProjectOrAdmin(project);
		if (project.isBugtrackerConnected()) {
			BugTrackerBinding bugtrackerBinding = project.getBugtrackerBinding();
			bugtrackerBinding.setProjectNames(projectBugTrackerNames);
		}

	}

}
