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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.api.plugin.EntityReference;
import org.squashtest.tm.api.plugin.EntityType;
import org.squashtest.tm.api.plugin.PluginValidationException;
import org.squashtest.tm.api.wizard.WorkspaceWizard;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.AdministrableProject;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.project.GenericProjectFinder;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.testautomation.TestAutomationProjectFinderService;
import org.squashtest.tm.service.testautomation.TestAutomationServerManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.generic.ServiceAwareAttachmentTableModelHelper;
import org.squashtest.tm.web.internal.controller.project.ProjectPluginModel;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.plugins.manager.wizard.WorkspaceWizardManager;

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
	private BugTrackerFinderService bugtrackerFinderService;
	@Inject
	private InternationalizationHelper internationalizationHelper;

	@Inject
	private TestAutomationServerManagerService taServerService;
	@Inject
	private TestAutomationProjectFinderService taProjectFinderService;

	@Inject
	private WorkspaceWizardManager pluginManager;

	@Inject
	private ServiceAwareAttachmentTableModelHelper attachmentsHelper;


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

		AdministrableProject adminProject = projectFinder.findAdministrableProjectById(projectId);

		// user permissions data
		List<PartyProjectPermissionsBean> partyProjectPermissionsBean = projectFinder
				.findPartyPermissionsBeanByProject(new DefaultPagingAndSorting("login", 25),
						DefaultFiltering.NO_FILTERING, projectId).getPagedItems();
		Collection<Object> partyPermissions = new PartyPermissionDatatableModelHelper(locale, internationalizationHelper)
		.buildRawModel(partyProjectPermissionsBean,1);

		List<PermissionGroup> availablePermissions = projectFinder.findAllPossiblePermission();

		// test automation data
		Collection<TestAutomationServer> availableTAServers = taServerService.findAllOrderedByName();
		Map<String, URL> jobUrls = taProjectFinderService.findProjectUrls(adminProject.getProject().getTestAutomationProjects());
		// bugtracker data
		Map<Long, String> comboDataMap = createComboDataForBugtracker(locale);

		// execution status data
		CampaignLibrary cl = adminProject.getCampaignLibrary();
		Map<String, Boolean> allowedStatuses = new HashMap<>();
		allowedStatuses.put(ExecutionStatus.SETTLED.toString(), cl.allowsStatus(ExecutionStatus.SETTLED));
		allowedStatuses.put(ExecutionStatus.UNTESTABLE.toString(), cl.allowsStatus(ExecutionStatus.UNTESTABLE));

		// populating model
		ModelAndView mav = new ModelAndView("page/projects/project-info");
		mav.addObject("isAdmin", permissionEvaluationService.hasRole("ROLE_ADMIN"));
		mav.addObject("jobUrls", jobUrls);
		mav.addObject("adminproject", adminProject);
		mav.addObject("availableTAServers", availableTAServers);
		mav.addObject("bugtrackersList", JsonHelper.serialize(comboDataMap));
		mav.addObject("bugtrackersListEmpty", comboDataMap.size() == 1);
		mav.addObject("userPermissions", partyPermissions);
		mav.addObject("availablePermissions", availablePermissions);
		mav.addObject("attachments", attachmentsHelper.findAttachments(adminProject.getProject()));
		mav.addObject("allowedStatuses", allowedStatuses);
		mav.addObject("allowTcModifDuringExec", adminProject.allowTcModifDuringExec());


		return mav;
	}

	private Map<Long, String> createComboDataForBugtracker(Locale locale) {
		Map<Long, String> comboDataMap = new HashMap<>();
		for (BugTracker b : bugtrackerFinderService.findAll()) {
			comboDataMap.put(b.getId(), b.getName());
		}
		comboDataMap.put(-1L, internationalizationHelper.internationalize(PROJECT_BUGTRACKER_NAME_UNDEFINED, locale));
		return comboDataMap;

	}


	// ********************** Plugin administration section ************


	@RequestMapping(value = "{projectId}/plugins")
	public String getPluginsManager(@PathVariable(RequestParams.PROJECT_ID) Long projectId, Model model, HttpServletRequest request) {

		GenericProject project = projectFinder.findById(projectId);

		Collection<WorkspaceWizard> plugins = pluginManager.findAll();

		Collection<String> enabledPlugins = new ArrayList<>();
		enabledPlugins.addAll(project.getTestCaseLibrary().getEnabledPlugins());
		enabledPlugins.addAll(project.getRequirementLibrary().getEnabledPlugins());
		enabledPlugins.addAll(project.getCampaignLibrary().getEnabledPlugins());

		String context = request.getServletContext().getContextPath();
		Collection<ProjectPluginModel> models = toPluginModel(context, projectId, plugins, enabledPlugins);

		model.addAttribute("plugins", models);
		model.addAttribute(RequestParams.PROJECT_ID, projectId);

		return "project-tabs/plugins-tab.html";

	}


	private Collection<ProjectPluginModel> toPluginModel(String servContext, long projectId, Collection<WorkspaceWizard> plugins, Collection<String> enabledPlugins) {

		List<ProjectPluginModel> output = new ArrayList<>(plugins.size());

		EntityReference context = new EntityReference(EntityType.PROJECT, projectId);

		int loop=1;
		for (WorkspaceWizard plugin : plugins) {

			boolean enabled = enabledPlugins.contains(plugin.getId());

			ProjectPluginModel model = new ProjectPluginModel(plugin);

			model.setIndex(loop++);
			model.setEnabled(enabled);

			String url = plugin.getConfigurationPath(context);

			if(url != null) {
				url = url.startsWith("/") ? url : "/" + url;

				model.setConfigUrl(servContext + url);
			}

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
