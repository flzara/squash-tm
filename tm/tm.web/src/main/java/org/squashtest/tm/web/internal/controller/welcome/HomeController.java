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
package org.squashtest.tm.web.internal.controller.welcome;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.Workspace;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.customreport.CustomReportDashboardService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.service.user.PartyPreferenceService;
import org.squashtest.tm.web.internal.helper.I18nLevelEnumInfolistHelper;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

// XSS OK
@Controller
public class HomeController {

	private AdministrationService administrationService;

	@Inject
	private ProjectFinder projectFinder;

	@Inject
	private BugTrackerFinderService bugtrackerService;

	@Inject
	private PartyPreferenceService partyPreferenceService;

	@Inject
	private I18nLevelEnumInfolistHelper i18nLevelEnumInfolistHelper;

	@Inject
	private CustomReportDashboardService customReportDashboardService;

	@Inject
	private ConfigurationService configurationService;


	@Inject
	public void setAdministrationService(AdministrationService administrationService) {
		this.administrationService = administrationService;
	}

	@RequestMapping("/home-workspace")
	public ModelAndView home(Locale locale) {

		String welcomeMessage = administrationService.findWelcomeMessage();
		Map<String, String> userPrefs = partyPreferenceService.findPreferencesForCurrentUser();
		boolean canShowDashboard = customReportDashboardService.canShowDashboardInWorkspace(Workspace.HOME);
		boolean shouldShowDashboard = customReportDashboardService.shouldShowFavoriteDashboardInWorkspace(Workspace.HOME);

		ModelAndView model = new ModelAndView("home-workspace.html");

		model.addObject("welcomeMessage", HTMLCleanupUtils.cleanHtml(welcomeMessage));
		model.addObject("userPrefs", userPrefs);
		model.addObject("canShowDashboard", canShowDashboard);
		model.addObject("shouldShowDashboard", shouldShowDashboard);

		// put the available bugtrackers too
		List<Project> projects = projectFinder.findAllReadable();
		List<Long> projectsIds = IdentifiedUtil.extractIds(projects);
		List<BugTracker> visibleBugtrackers = bugtrackerService.findDistinctBugTrackersForProjects(projectsIds);

		model.addObject("visibleBugtrackers", visibleBugtrackers);
		model.addObject("defaultInfoLists", i18nLevelEnumInfolistHelper.getInternationalizedDefaultList(locale));
		model.addObject("testCaseImportance", i18nLevelEnumInfolistHelper.getI18nLevelEnum(TestCaseImportance.class, locale));
		model.addObject("testCaseStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(TestCaseStatus.class, locale));
		model.addObject("requirementStatus", i18nLevelEnumInfolistHelper.getI18nLevelEnum(RequirementStatus.class, locale));
		model.addObject("requirementCriticality", i18nLevelEnumInfolistHelper.getI18nLevelEnum(RequirementCriticality.class, locale));
		model.addObject("executionStatus",
			i18nLevelEnumInfolistHelper.getI18nLevelEnum(ExecutionStatus.class, locale));

		// License information
		String userLicenseInformation = configurationService.findConfiguration(ConfigurationService.Properties.ACTIVATED_USER_EXCESS);
		String dateLicenseInformation = configurationService.findConfiguration(ConfigurationService.Properties.PLUGIN_LICENSE_EXPIRATION);

		model.addObject("userLicenseInformation", userLicenseInformation);
		model.addObject("dateLicenseInformation", (dateLicenseInformation == null || dateLicenseInformation.isEmpty()) ? null : Integer.valueOf(dateLicenseInformation));

		return model;
	}

	@ResponseBody
	@RequestMapping(value = "/home-workspace/choose-message", method = RequestMethod.POST)
	public void chooseWelcomeMessageAsHomeContent() {
		partyPreferenceService.chooseWelcomeMessageAsHomeContentForCurrentUser();
	}

	@ResponseBody
	@RequestMapping(value = "/home-workspace/choose-dashboard", method = RequestMethod.POST)
	public void chooseFavoriteDashboardAsHomeContent() {
		partyPreferenceService.chooseFavoriteDashboardAsHomeContentForCurrentUser();
	}

	@ResponseBody
	@RequestMapping(value = "/administration/modify-welcome-message", method = RequestMethod.POST)
	public String modifyWelcomeMessage(@RequestParam(VALUE) String welcomeMessage) {
		administrationService.modifyWelcomeMessage(welcomeMessage);
		return HTMLCleanupUtils.cleanHtml(welcomeMessage);
	}

	@ResponseBody
	@RequestMapping(value = "/administration/modify-login-message", method = RequestMethod.POST)
	public String modifyLoginMessage(@RequestParam(VALUE) String loginMessage) {
		administrationService.modifyLoginMessage(loginMessage);
		return HTMLCleanupUtils.cleanHtml(loginMessage);
	}

	@RequestMapping("/administration/welcome-message")
	public ModelAndView welcomeMessagePage() {
		String welcomeMessage = administrationService.findWelcomeMessage();
		ModelAndView mav = new ModelAndView("page/administration/welcome-message-workspace");
		mav.addObject("welcomeMessage", welcomeMessage);
		return mav;
	}

	@RequestMapping("/administration/login-message")
	public ModelAndView loginMessagePage() {
		String loginMessage = administrationService.findLoginMessage();
		ModelAndView mav = new ModelAndView("page/administration/login-message-workspace");
		mav.addObject("loginMessage", loginMessage);
		return mav;
	}

}
