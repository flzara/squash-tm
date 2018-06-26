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
package org.squashtest.tm.web.internal.controller.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponents;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.core.foundation.collection.*;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectTemplate;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.exception.NameAlreadyInUseException;
import org.squashtest.tm.exception.NoBugTrackerBindingException;
import org.squashtest.tm.exception.user.LoginDoNotExistException;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.testautomation.TestAutomationProjectFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.administration.PartyPermissionDatatableModelHelper;
import org.squashtest.tm.web.internal.helper.JEditablePostParams;
import org.squashtest.tm.service.internal.project.ProjectHelper;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.*;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;
import org.squashtest.tm.web.internal.model.json.JsonUrl;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;
import org.squashtest.tm.web.internal.plugins.manager.wizard.WorkspaceWizardManager;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URL;
import java.util.*;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

/**
 * @author Gregory Fouquet
 */

// XSS OK
@Controller
@RequestMapping("/generic-projects")
public class GenericProjectController {

	private static final String LABEL = "label";
	private static final String ACTIVE = "active";
	private static final String HABILITATION = "habilitation";
	private static final String BUGTRACKER = "bugtracker";
	private static final String AUTOMATION = "automation";

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private GenericProjectManagerService projectManager;

	@Inject
	private BugTrackerFinderService bugtrackerFinderService;

	@Inject
	private TestAutomationProjectFinderService testAutomationProjectFinder;

	@Inject
	private WorkspaceWizardManager pluginManager;

	@Inject
	private TaskExecutor taskExecutor;
	private static final Logger LOGGER = LoggerFactory.getLogger(GenericProjectController.class);

	private static final String PROJECT_ID_URL = "/{" + RequestParams.PROJECT_ID + "}";
	private static final String PROJECT_BUGTRACKER_NAME_UNDEFINED = "project.bugtracker.name.undefined";

	private static final String VALUES = "values[]";

	private DatatableMapper<String> allProjectsMapper = new NameBasedMapper(9)
		.map(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "name")
		.map(LABEL, LABEL)
		.map(ACTIVE, ACTIVE)
		.map(DataTableModelConstants.DEFAULT_CREATED_ON_KEY, DataTableModelConstants.DEFAULT_CREATED_ON_VALUE)
		.map(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, DataTableModelConstants.DEFAULT_CREATED_BY_VALUE)
		.map("last-mod-on", DataTableModelConstants.DEFAULT_LAST_MODIFIED_ON_VALUE)
		.map("last-mod-by", DataTableModelConstants.DEFAULT_LAST_MODIFIED_BY_VALUE)
		.map(HABILITATION, HABILITATION)
		.map(BUGTRACKER, BUGTRACKER)
		.map(AUTOMATION, AUTOMATION);
	private DatatableMapper<String> partyPermissionMapper = new NameBasedMapper(5).map("party-index", "index")
		.map("party-id", "id").map("party-name", "name").map("party-type", "type")
		.map("permission-group.qualifiedName", "qualifiedName");

	@ResponseBody
	@RequestMapping(value = "", params = RequestParams.S_ECHO_PARAM, method = RequestMethod.GET)
	public DataTableModel getProjectsTableModel(final DataTableDrawParameters params, final Locale locale) {

		final PagingAndMultiSorting sorter = new DataTableMultiSorting(params, allProjectsMapper);
		Filtering filtering = new DataTableFiltering(params);
		PagedCollectionHolder<List<GenericProject>> holder = projectManager.findSortedProjects(sorter, filtering);

		return new ProjectDataTableModelHelper(locale, messageSource, projectManager).buildDataModel(holder, params.getsEcho());

	}

	@ResponseBody
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public void createNewProject(@Valid @RequestBody Project project) {
		try {
			projectManager.persist(project);
		} catch (NameAlreadyInUseException ex) {
			ex.setObjectName("add-project-from-template");
			throw ex;
		}
	}

	@ResponseBody
	@ResponseStatus(value = HttpStatus.CREATED)
	@RequestMapping(value = "/new-template", method = RequestMethod.POST)
	public JsonUrl createNewTemplate(@RequestBody @Valid ProjectTemplate template) {
		try {
			projectManager.persist(template);
		} catch (NameAlreadyInUseException ex) {
			ex.setObjectName("add-template");
			throw ex;
		}
		return getUrlToProjectInfoPage(template);
	}


	@RequestMapping(value = PROJECT_ID_URL, method = RequestMethod.POST, params = {"id=project-label", VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String changeLabel(@RequestParam(VALUE) String projectLabel, @PathVariable long projectId) {
		projectManager.changeLabel(projectId, projectLabel);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("project " + projectId + ": updated label to " + projectLabel);
		}
		return HtmlUtils.htmlEscape(projectLabel);
	}

	@RequestMapping(value = PROJECT_ID_URL, method = RequestMethod.POST, params = {"newName"})
	@ResponseBody
	public Object changeName(@PathVariable long projectId, @RequestParam String newName) {

		projectManager.changeName(projectId, newName);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Project modification : renaming {} as {}", projectId, newName);
		}
		return new RenameModel(HtmlUtils.htmlEscape(newName));
	}

	@RequestMapping(value = PROJECT_ID_URL + "/associate-template", method = RequestMethod.POST)
	@ResponseBody
	public void associateTemplate(@PathVariable long projectId, @RequestParam long templateId) {
		projectManager.associateToTemplate(projectId, templateId);
		LOGGER.info("Project modification : associating {} with template {}", projectId, templateId);
	}

	@RequestMapping(value = PROJECT_ID_URL + "/disassociate-template", method = RequestMethod.DELETE)
	@ResponseBody
	public void disassociateTemplate(@PathVariable long projectId) {
		projectManager.disassociateFromTemplate(projectId);
		LOGGER.info("Disassociating project of ID {} from its template.", projectId);
	}


	@RequestMapping(value = PROJECT_ID_URL, method = RequestMethod.POST, params = {"isActive"})
	@ResponseBody
	public Active changeActive(@PathVariable long projectId,
							   @RequestParam boolean isActive) {

		projectManager.changeActive(projectId, isActive);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Project modification : change project {} is active = {}", projectId, isActive);
		}
		return new Active(isActive);
	}

	private static final class Active {
		private Boolean isActive;

		private Active(Boolean active) {
			this.isActive = active;
		}

		@SuppressWarnings("unused")
		public Boolean isActive() {
			return isActive;
		}
	}

	@RequestMapping(value = PROJECT_ID_URL, method = RequestMethod.POST, params = {"id=project-bugtracker", VALUE})
	@ResponseBody
	public String changeBugtracker(@RequestParam(VALUE) Long bugtrackerId, @PathVariable long projectId, Locale locale) {
		String toReturn;
		if (bugtrackerId > 0) {
			toReturn = HtmlUtils.htmlEscape(bugtrackerFinderService.findBugtrackerName(bugtrackerId));
			projectManager.changeBugTracker(projectId, bugtrackerId);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Project {} : bugtracker changed, new value : {}", projectId, bugtrackerId);
			}
		} else {
			toReturn = messageSource.internationalize(PROJECT_BUGTRACKER_NAME_UNDEFINED, locale);
			projectManager.removeBugTracker(projectId);
		}
		return toReturn;
	}

	@RequestMapping(value = PROJECT_ID_URL, method = RequestMethod.POST, params = {
		"id=project-bugtracker-project-name", VALUES})
	@ResponseBody
	public List<String> changeBugtrackerProjectName(@RequestParam(VALUES) List<String> projectBugTrackerNames,
													@PathVariable long projectId) {
		projectManager.changeBugTrackerProjectName(projectId, projectBugTrackerNames);
		return projectBugTrackerNames;
	}

	@RequestMapping(value = PROJECT_ID_URL, method = RequestMethod.POST, params = {"id=project-description", VALUE})
	@ResponseBody
	public String changeDescription(@RequestParam(VALUE) String projectDescription, @PathVariable long projectId) {
		projectManager.changeDescription(projectId, projectDescription);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("project " + projectId + ": updated description to " + projectDescription);
		}
		return HTMLCleanupUtils.cleanHtml(projectDescription);
	}

	@RequestMapping(value = PROJECT_ID_URL + "/bugtracker/projectName", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getBugtrackerProject(@PathVariable long projectId) {
		GenericProject project = projectManager.findById(projectId);
		if (project.isBugtrackerConnected()) {
			return project.getBugtrackerBinding().getProjectNames();
		} else {
			throw new NoBugTrackerBindingException();
		}
	}

	@RequestMapping(value = PROJECT_ID_URL + "/general", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable long projectId) {

		GenericProject project = projectManager.findById(projectId);
		return new JsonGeneralInfo((AuditableMixin) project);
	}

	@RequestMapping(value = PROJECT_ID_URL, method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteProject(@PathVariable long projectId) {
		projectManager.deleteProject(projectId);
	}

	@RequestMapping(value = "/{projectId}/description", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public String getDescription(@PathVariable long projectId) {
		GenericProject genericProject = projectManager.findById(projectId);
		return HTMLCleanupUtils.cleanHtml(genericProject.getDescription());
	}

	// ********************************** Permission Popup *******************************

	@RequestMapping(value = PROJECT_ID_URL + "/unbound-parties", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String, Object>> getPermissionPopup(@PathVariable long projectId) {

		List<Party> partyList = projectManager.findPartyWithoutPermissionByProject(projectId);

		List<Map<String, Object>> partiesModel = new ArrayList<>(partyList.size());
		for (Party p : partyList) {
			Map<String, Object> newModel = new HashMap<>();
			newModel.put(LABEL, HTMLCleanupUtils.cleanAndUnescapeHTML(p.getName()));
			newModel.put(JEditablePostParams.VALUE, HTMLCleanupUtils.cleanAndUnescapeHTML(p.getName()));
			newModel.put("id", p.getId());
			partiesModel.add(newModel);
		}

		return partiesModel;

	}

	@ResponseBody
	@RequestMapping(value = PROJECT_ID_URL + "/parties/{partyId}/permissions/{permission}", method = RequestMethod.PUT)
	public void addNewPermissionWithPartyId(@PathVariable long partyId, @PathVariable long projectId,
											@PathVariable String permission) {

		Party party = projectManager.findPartyById(partyId);

		if (party == null) {
			throw new LoginDoNotExistException();
		}

		projectManager.addNewPermissionToProject(party.getId(), projectId, permission);

	}

	// ***************************** permission table *************************************

	@RequestMapping(value = PROJECT_ID_URL + "/party-permissions", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getPartyPermissionTable(DataTableDrawParameters params,
												  @PathVariable(RequestParams.PROJECT_ID) long projectId, final Locale locale) {

		PagingAndSorting sorting = new DataTableSorting(params, partyPermissionMapper);
		Filtering filtering = new DataTableFiltering(params);

		PagedCollectionHolder<List<PartyProjectPermissionsBean>> partyPermissions = projectManager
			.findPartyPermissionsBeanByProject(sorting, filtering, projectId);

		return new PartyPermissionDatatableModelHelper(locale, messageSource).buildDataModel(partyPermissions,
			params.getsEcho());
	}

	@ResponseBody
	@RequestMapping(value = PROJECT_ID_URL + "/parties/{partyId}/permissions/{permission}", method = RequestMethod.POST)
	public void addNewPartyPermission(@PathVariable long partyId, @PathVariable long projectId, @PathVariable String permission) {
		projectManager.addNewPermissionToProject(partyId, projectId, permission);
	}

	@ResponseBody
	@RequestMapping(value = PROJECT_ID_URL + "/parties/{partyId}/permissions", method = RequestMethod.DELETE)
	public void removePartyPermission(@PathVariable long partyId, @PathVariable long projectId) {
		projectManager.removeProjectPermission(partyId, projectId);
	}

	// ********************************** test automation ***********************************

	@RequestMapping(value = PROJECT_ID_URL + "/test-automation-server", method = RequestMethod.POST, params = "serverId")
	@ResponseBody
	public Long bindTestAutomationServer(@PathVariable(RequestParams.PROJECT_ID) long projectId,
										 @RequestParam("serverId") long serverId) {
		Long finalServerId = serverId == 0 ? null : serverId;
		projectManager.bindTestAutomationServer(projectId, finalServerId);
		return serverId;
	}

	// filtering and sorting not supported for now
	@RequestMapping(value = PROJECT_ID_URL + "/test-automation-projects", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getAutomatedProjectsTableModel(@PathVariable long projectId, final DataTableDrawParameters params) {
		List<TestAutomationProject> taProjects = projectManager.findBoundTestAutomationProjects(projectId);

		PagedCollectionHolder<List<TestAutomationProject>> holder = new SinglePageCollectionHolder<>(
			taProjects);
		Map<String, URL> jobUrls = testAutomationProjectFinder.findProjectUrls(taProjects);
		return new TestAutomationTableModel(jobUrls).buildDataModel(holder, params.getsEcho());

	}

	@RequestMapping(value = PROJECT_ID_URL + "/test-automation-projects/new", method = RequestMethod.POST)
	@ResponseBody
	public void addTestAutomationProject(@PathVariable long projectId,
										 @RequestBody TestAutomationProject[] projects) throws BindException {
		projectManager.bindTestAutomationProjects(projectId, Arrays.asList(projects));

	}

	@RequestMapping(value = PROJECT_ID_URL + "/test-automation-projects/{taProjectId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindProject(@PathVariable long projectId, @PathVariable long taProjectId) {
		projectManager.unbindTestAutomationProject(projectId, taProjectId);
	}

	@RequestMapping(value = PROJECT_ID_URL + "/available-ta-projects", method = RequestMethod.GET, params = {"login", "password"})
	@ResponseBody
	public Collection<TestAutomationProject> getAvailableTAProjectsWithCredentials(
		@RequestParam("login") String login, @RequestParam("password") String password, @PathVariable long projectId)
		throws BindException {
		return projectManager.findAllAvailableTaProjectsWithCredentials(projectId, login, password);

	}

	@RequestMapping(value = PROJECT_ID_URL + "/available-ta-projects", method = RequestMethod.GET)
	@ResponseBody
	public Collection<TestAutomationProject> getAvailableTAProjects(@PathVariable long projectId)
		throws BindException {
		return projectManager.findAllAvailableTaProjects(projectId);

	}
	// ************************* plugins administration ***********************

	@RequestMapping(value = PROJECT_ID_URL + "/plugins/{pluginId}", method = RequestMethod.POST)
	@ResponseBody
	public void enablePlugin(@PathVariable long projectId, @PathVariable String pluginId) {
		WorkspaceWizard wizard = pluginManager.findById(pluginId);
		projectManager.enablePluginForWorkspace(projectId, wizard.getDisplayWorkspace(), pluginId);

	}

	@RequestMapping(value = PROJECT_ID_URL + "/plugins/{pluginId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void disablePlugin(@PathVariable long projectId, @PathVariable String pluginId) {
		WorkspaceWizard plugin = pluginManager.findById(pluginId);
		projectManager.disablePluginForWorkspace(projectId, plugin.getDisplayWorkspace(), pluginId);
	}


	// ********************** other stuffs *****************************

	// ********************** private classes ***************************

	private static final class TestAutomationTableModel extends DataTableModelBuilder<TestAutomationProject> {

		private Map<String, URL> jobUrls;

		public TestAutomationTableModel(Map<String, URL> jobUrls) {
			this.jobUrls = jobUrls;
		}

		@Override
		protected Map<String, ?> buildItemData(TestAutomationProject item) {
			Map<String, Object> res = new HashMap<>();

			res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put(LABEL, HtmlUtils.htmlEscape(item.getLabel()));
			res.put("jobName", HtmlUtils.htmlEscape(item.getJobName()));
			res.put("slaves", HtmlUtils.htmlEscape(item.getSlaves()));
			res.put("url", jobUrls.get(item.getJobName()));
			res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			res.put(DataTableModelConstants.DEFAULT_EMPTY_EDIT_HOLDER_KEY, " ");
			return res;
		}
	}

	private static final class ProjectDataTableModelHelper extends DataTableModelBuilder<GenericProject> {


		private InternationalizationHelper messageSource;
		private Locale locale;
		private GenericProjectManagerService projectManager;

		private ProjectDataTableModelHelper(Locale locale, InternationalizationHelper messageSource, GenericProjectManagerService projectManager) {
			this.locale = locale;
			this.messageSource = messageSource;
			this.projectManager = projectManager;
		}

		@Override
		public Object buildItemData(GenericProject project) {
			Map<String, Object> data = new HashMap<>(14);

			final AuditableMixin auditable = (AuditableMixin) project;
			data.put("project-id", project.getId());
			data.put("index", getCurrentIndex());
			data.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, HtmlUtils.htmlEscape(project.getName()));
			data.put(ACTIVE, messageSource.internationalizeYesNo(project.isActive(), locale));
			data.put(LABEL, HtmlUtils.htmlEscape(project.getLabel()));
			data.put(DataTableModelConstants.DEFAULT_CREATED_ON_KEY, messageSource.localizeDate(auditable.getCreatedOn(), locale));
			data.put(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, HtmlUtils.htmlEscape(auditable.getCreatedBy()));
			data.put("last-mod-on", messageSource.localizeDate(auditable.getLastModifiedOn(), locale));
			data.put("last-mod-by", HtmlUtils.htmlEscape(auditable.getLastModifiedBy()));
			data.put("raw-type", ProjectHelper.isTemplate(project) ? "template" : "project");
			data.put("type", "&nbsp;");
			data.put(HABILITATION, messageSource.internationalizeYesNo(hasPermissions(project), locale));
			data.put(BUGTRACKER, getBugtrackerKind(project));
			data.put(AUTOMATION, messageSource.internationalizeYesNo(project.isTestAutomationEnabled(), locale));
			return data;
		}

		private boolean hasPermissions(final GenericProject project) {
			boolean hasPermissions = true;
			if (projectManager.findPartyPermissionsBeansByProject(project.getId()).isEmpty()) {
				hasPermissions = false;
			}
			return hasPermissions;
		}

		private String getBugtrackerKind(final GenericProject project) {

			String bugtrackerKind;
			if (project.isBugtrackerConnected()) {
				bugtrackerKind = project.getBugtrackerBinding().getBugtracker().getKind();
			} else {
				bugtrackerKind = messageSource.noData(locale);
			}
			return bugtrackerKind;
		}
	}

	@RequestMapping(value = PROJECT_ID_URL, method = RequestMethod.POST, params = VALUE)
	@ResponseBody
	public void changeEx(@PathVariable long projectId, @RequestParam(VALUE) boolean active) {
		projectManager.changeAllowTcModifDuringExec(projectId, active);
	}

	@RequestMapping(value = PROJECT_ID_URL + "/disable-execution-status/{executionStatus}", method = RequestMethod.POST)
	@ResponseBody
	public void disableExecutionStatusOnProject(@PathVariable long projectId, @PathVariable String executionStatus) {
		projectManager.disableExecutionStatus(projectId, ExecutionStatus.valueOf(executionStatus));
	}

	@RequestMapping(value = PROJECT_ID_URL + "/enable-execution-status/{executionStatus}", method = RequestMethod.POST)
	@ResponseBody
	public void enableExecutionStatusOnProject(@PathVariable long projectId, @PathVariable String executionStatus) {
		projectManager.enableExecutionStatus(projectId, ExecutionStatus.valueOf(executionStatus));
	}

	@RequestMapping(value = PROJECT_ID_URL + "/is-enabled-execution-status/{executionStatus}", method = RequestMethod.GET)
	@ResponseBody
	public boolean isExecutionStatusEnabledForProject(@PathVariable long projectId, @PathVariable String executionStatus) {
		return projectManager.isExecutionStatusEnabledForProject(projectId, ExecutionStatus.valueOf(executionStatus));
	}

	@RequestMapping(value = PROJECT_ID_URL + "/execution-status-is-used/{executionStatus}", method = RequestMethod.GET)
	@ResponseBody
	public boolean projectUsesExecutionStatus(@PathVariable long projectId, @PathVariable String executionStatus) {
		return projectManager.projectUsesExecutionStatus(projectId, ExecutionStatus.valueOf(executionStatus));
	}

	@RequestMapping(value = PROJECT_ID_URL + "/execution-status/{executionStatus}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getStatusPopup(@PathVariable long projectId, @PathVariable String executionStatus,
											  final Locale locale) {
		Set<ExecutionStatus> statuses = projectManager.enabledExecutionStatuses(projectId);
		ExecutionStatus status = ExecutionStatus.valueOf(executionStatus);
		statuses.remove(status);
		Map<String, Object> options = new HashMap<>();
		for (ExecutionStatus st : statuses) {
			options.put(messageSource.internationalize(st.getI18nKey(), locale), st.name());
		}
		return options;

	}

	@RequestMapping(value = PROJECT_ID_URL + "/replace-execution-status", method = RequestMethod.POST)
	@ResponseBody
	public void replaceStatusWithinProject(@PathVariable long projectId, @RequestParam String sourceExecutionStatus,
										   @RequestParam String targetExecutionStatus) {
		ExecutionStatus source = ExecutionStatus.valueOf(sourceExecutionStatus);
		ExecutionStatus target = ExecutionStatus.valueOf(targetExecutionStatus);
		Runnable replacer = new AsynchronousReplaceExecutionStatus(projectId, source, target);
		taskExecutor.execute(replacer);
	}

	private class AsynchronousReplaceExecutionStatus implements Runnable {

		private Long projectId;
		private ExecutionStatus sourceExecutionStatus;
		private ExecutionStatus targetExecutionStatus;

		public AsynchronousReplaceExecutionStatus(Long projectId, ExecutionStatus sourceExecutionStatus,
												  ExecutionStatus targetExecutionStatus) {
			super();
			this.projectId = projectId;
			this.sourceExecutionStatus = sourceExecutionStatus;
			this.targetExecutionStatus = targetExecutionStatus;
		}

		@Override
		public void run() {
			projectManager.replaceExecutionStepStatus(projectId, sourceExecutionStatus, targetExecutionStatus);
		}
	}

	private JsonUrl getUrlToProjectInfoPage(GenericProject project) {
		UriComponents uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/administration/projects/{id}/info")
			.buildAndExpand(project.getId());
		return new JsonUrl(uri.toString());
	}
}
