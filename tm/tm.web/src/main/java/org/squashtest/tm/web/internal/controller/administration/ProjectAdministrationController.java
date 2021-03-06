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
package org.squashtest.tm.web.internal.controller.administration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.plugin.EntityReference;
import org.squashtest.tm.api.plugin.EntityType;
import org.squashtest.tm.api.plugin.PluginType;
import org.squashtest.tm.api.plugin.PluginValidationException;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.bdd.BddImplementationTechnology;
import org.squashtest.tm.domain.bdd.BddScriptLanguage;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.LibraryPluginBinding;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.internal.project.ProjectHelper;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.project.GenericProjectFinder;
import org.squashtest.tm.service.project.ProjectTemplateFinder;
import org.squashtest.tm.service.scmserver.ScmServerManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testautomation.TestAutomationProjectFinderService;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.project.ProjectPluginModel;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.plugins.manager.automationworkflow.AutomationWorkflowPluginManager;
import org.squashtest.tm.web.internal.plugins.manager.wizard.WorkspaceWizardManager;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/administration/projects")
public class ProjectAdministrationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProjectAdministrationController.class);

	private static final String STATUS_ERROR = "ERROR";
	private static final String STATUS_OK = "OK";
	/**
	 * Finder service for generic project. We manage here both projects and templates !
	 */
	@Inject
	private PermissionEvaluationService permissionEvaluationService;
	@Inject
	private GenericProjectFinder projectFinder;
	@Inject
	private ProjectTemplateFinder templateFinder;
	@Inject
	private BugTrackerFinderService bugtrackerFinderService;
	@Inject
	private InternationalizationHelper internationalizationHelper;

	@Inject
	private TestAutomationServerManagerService taServerService;
	@Inject
	private TestAutomationProjectFinderService taProjectFinderService;

	@Inject
	private ScmServerManagerService scmServerService;

	@Inject
	private WorkspaceWizardManager pluginManager;

	@Inject
	private AutomationWorkflowPluginManager workflowPluginManager;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private ProjectDao projectDao;

	private static final String PROJECT_BUGTRACKER_NAME_UNDEFINED = "project.bugtracker.name.undefined";

	@ModelAttribute("projectsPageSize")
	public long populateProjectsPageSize() {
		return Pagings.DEFAULT_PAGING.getPageSize();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showProjects() {
		ModelAndView mav = new ModelAndView("page/projects/show-projects");
		mav.addObject("projects", projectFinder.findAllOrderedByName(Pagings.DEFAULT_PAGING));
		return mav;
	}

	@RequestMapping(value = "{projectId}", method = RequestMethod.GET)
	public ModelAndView showProjectEditor(@PathVariable long projectId, Locale locale) {
		return getProjectInfos(projectId, locale);
	}

	@RequestMapping(value = "{projectId}/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long projectId, Locale locale) {
		boolean pluginAutomHasConf;
		AdministrableProject adminProject = projectFinder.findAdministrableProjectById(projectId);

		// user permissions data
		List<PartyProjectPermissionsBean> partyProjectPermissionsBean = projectFinder
				.findPartyPermissionsBeanByProject(new DefaultPagingAndSorting("login", 25),
						DefaultFiltering.NO_FILTERING, projectId).getPagedItems();
		Collection<Object> partyPermissions = new PartyPermissionDatatableModelHelper(locale, internationalizationHelper)
		.buildRawModel(partyProjectPermissionsBean,1);

		List<PermissionGroup> availablePermissions = projectFinder.findAllPossiblePermission();
		availablePermissions.sort(Comparator.comparing(it -> internationalizationHelper.internationalize(
			"user.project-rights." + it.getSimpleName() + ".label", locale)));

		// BDD technologies and language


		// Automation workflows
		Map<String, String> automationWorkflows = getAvailableWorkflows(projectId, locale);
		//if the automation plugin used checks if there is a configuration
		LibraryPluginBinding lpb = projectDao.findPluginForProject(projectId, PluginType.AUTOMATION);
		WorkspaceWizard plugin = lpb != null ? pluginManager.findAll().stream().filter(it -> it.getId().equals(lpb.getPluginId())).findAny().orElse(null) : null;
		pluginAutomHasConf = plugin != null ? pluginManager.isHasConfiguration(plugin, projectId) : false;

		// test automation data
		Collection<TestAutomationServer> availableTAServers = taServerService.findAllOrderedByName();
		Map<String, URL> jobUrls = taProjectFinderService.findProjectUrls(adminProject.getProject().getTestAutomationProjects());

		// source code management
		Collection<ScmServer> availableScmServers = scmServerService.findAllOrderByName();

		// bugtracker data
		Map<Long, String> comboDataMap = createComboDataForBugtracker(locale);

		// execution status data
		CampaignLibrary cl = adminProject.getCampaignLibrary();
		Map<String, Boolean> allowedStatuses = new HashMap<>();
		allowedStatuses.put(ExecutionStatus.SETTLED.toString(), cl.allowsStatus(ExecutionStatus.SETTLED));
		allowedStatuses.put(ExecutionStatus.UNTESTABLE.toString(), cl.allowsStatus(ExecutionStatus.UNTESTABLE));

		// list of templates
		List<NamedReference> templatesList = templateFinder.findAllReferences();

		// License information
		String userLicenseInformation = configurationService.findConfiguration(ConfigurationService.Properties.ACTIVATED_USER_EXCESS);

		// populating model
		ModelAndView mav = new ModelAndView("page/projects/project-info");
		mav.addObject("isAdmin", permissionEvaluationService.hasRole("ROLE_ADMIN"));
		mav.addObject("jobUrls", jobUrls);
		mav.addObject("templatesList", templatesList);
		mav.addObject("userLicenseInformationData", userLicenseInformation);
		// information panel
		mav.addObject("adminproject", adminProject);
		// bugtracker panel
		mav.addObject("bugtrackersList", JsonHelper.serialize(comboDataMap));
		mav.addObject("bugtrackersListEmpty", comboDataMap.size() == 1);
		// permission panel
		mav.addObject("userPermissions", partyPermissions);
		mav.addObject("availablePermissions", availablePermissions);
		// execution option panel
		mav.addObject("allowTcModifDuringExec", adminProject.allowTcModifDuringExec());
		// status option panel
		mav.addObject("allowedStatuses", allowedStatuses);
		// test automation management panel
		mav.addObject("availableBddImplTechnologies", getAvailableBddImplTechnologies(locale));
		mav.addObject("chosenBddImplTechnology", adminProject.getProject().getBddImplementationTechnology().name());
		mav.addObject("availableBddScriptLanguages", getAvailableBddScriptLanguages(locale));
		mav.addObject("chosenBddScriptLanguage", adminProject.getProject().getBddScriptLanguage().name());
		mav.addObject("pluginAutomHasConf", pluginAutomHasConf);
		mav.addObject("allowAutomationWorkflow", adminProject.allowAutomationWorkflow());
		mav.addObject("chosenAutomationWorkflow", adminProject.getAutomationWorkflowType().getI18nKey());
		mav.addObject("availableAutomationWorkflows", automationWorkflows);
		mav.addObject("useTreeStructureInScmRepo", adminProject.useTreeStructureInScmRepo());
		mav.addObject("availableTAServers", availableTAServers);
		mav.addObject("availableScmServers", availableScmServers);
		mav.addObject("automatedSuitesLifetime", adminProject.getAutomatedSuitesLifetime());
		// attachment panel
		mav.addObject("attachments", attachmentsHelper.findAttachments(adminProject.getProject()));

		return mav;
	}

	/**
	 * Get all the available BddImplementationTechnologies as a Map.
	 * The Keys are the Enum {@linkplain BddImplementationTechnology} names
	 * and the values are the PascalCase translated values.
	 * @param locale The locale for translation
	 * @return The Map of the available BddImplementationTechnologies
	 */
	private Map<String, String> getAvailableBddImplTechnologies(Locale locale) {
		return Arrays
			.stream(BddImplementationTechnology.values())
			.collect(Collectors.toMap(
				BddImplementationTechnology::name,
				bddImplTech -> internationalizationHelper.internationalize(
					bddImplTech.getI18nKey(),
					locale)));
	}

	/**
	 * Get all the available BddScriptLanguages as a Map.
	 * The Keys are the Enum {@linkplain BddScriptLanguage} names
	 * and the values are the PascalCase translated values.
	 * @param locale The locale for translation
	 * @return The Map of the available BddScriptLanguages
	 */
	private Map<String, String> getAvailableBddScriptLanguages(Locale locale) {
		return Arrays
			.stream(BddScriptLanguage.values())
			.collect(Collectors.toMap(
				BddScriptLanguage::name,
				bddScriptLanguage -> internationalizationHelper.internationalize(
					bddScriptLanguage.getI18nKey(),
					locale
				)
			));
	}
	/**
	 *  Get all the available activated AutomationWorkflow Plugins for the given Project.
	 * @param projectId The Project Id.
	 * @param locale The Locale (used to translate 'Native' and 'None')
	 * @return The Map of available activated AutomatioWorkflow Plugins
	 */
	private Map<String, String> getAvailableWorkflows(Long projectId, Locale locale) {
		Collection<String> activePlugins =
			pluginManager.findEnabledWizards(projectId)
				.stream().map(WorkspaceWizard::getId).collect(Collectors.toList());

		return workflowPluginManager.getAutomationWorkflowsTypeFilteredByIds(activePlugins, locale);
	}

	@RequestMapping(value = "{projectId}/workflows")
	@ResponseBody
	public Map<String, String> createComboDataForWorkflows(
		@PathVariable(RequestParams.PROJECT_ID) Long projectId, Locale locale) {
		return getAvailableWorkflows(projectId, locale);
	}

	private Map<Long, String> createComboDataForBugtracker(Locale locale) {
		Map<Long, String> comboDataMap = new HashMap<>();
		for (BugTracker b : bugtrackerFinderService.findAll()) {
			if(!b.getKind().equals("jira.xsquash")) {
				comboDataMap.put(b.getId(), HtmlUtils.htmlEscape(b.getName()));
			}
		}
		comboDataMap.put(-1L, internationalizationHelper.internationalize(PROJECT_BUGTRACKER_NAME_UNDEFINED, locale));
		return comboDataMap;
	}


	// ********************** Plugin administration section ************


	@RequestMapping(value = "{projectId}/plugins")
	public String getPluginsManager(@PathVariable(RequestParams.PROJECT_ID) Long projectId, Model model, HttpServletRequest request) {

		GenericProject project = projectFinder.findById(projectId);

		Collection<WorkspaceWizard> plugins = pluginManager.findAll();

		Boolean isTemplate = ProjectHelper.isTemplate(project);

		Collection<String> enabledPlugins = new ArrayList<>();
		enabledPlugins.addAll(project.getTestCaseLibrary().getEnabledPlugins());
		enabledPlugins.addAll(project.getRequirementLibrary().getEnabledPlugins());
		enabledPlugins.addAll(project.getCampaignLibrary().getEnabledPlugins());

		String context = request.getServletContext().getContextPath();
		Collection<ProjectPluginModel> models = toPluginModel(context, projectId, plugins, isTemplate);

		model.addAttribute("plugins", models);
		model.addAttribute(RequestParams.PROJECT_ID, projectId);

		return "project-tabs/plugins-tab.html";

	}


	private Collection<ProjectPluginModel> toPluginModel(String servContext, long projectId, Collection<WorkspaceWizard> plugins, Boolean isTemplate) {

		List<ProjectPluginModel> output = new ArrayList<>(plugins.size());

		EntityReference context = new EntityReference(EntityType.PROJECT, projectId);

		int loop=1;
		for (WorkspaceWizard plugin : plugins) {

			boolean enabled = pluginManager.isActivePlugin(plugin, projectId);
			boolean hasConf = pluginManager.isHasConfiguration(plugin, projectId);
			ProjectPluginModel model = new ProjectPluginModel(plugin);

			model.setIndex(loop++);
			model.setEnabled(enabled);
			model.setHasConf(hasConf);

			model.setPluginType(plugin.getPluginType());

			String url = plugin.getConfigurationPath(context);

			if(url != null && !isTemplate) {
				url = url.startsWith("/") ? url : "/" + url;

				model.setConfigUrl(servContext + url);
			}else model.setConfigUrl("");

			// that should be refactored too once the API is updated
			try{
				plugin.validate(context);
				model.setStatus(STATUS_OK);
			}
			catch(PluginValidationException damnit){
				model.setStatus(STATUS_ERROR);
				LOGGER.debug("Plugin validation failed for Plugin " + plugin.getName(), damnit);
			}

			output.add(model);
		}

		return output;
	}



}
