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
package org.squashtest.tm.web.internal.controller.milestone;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneRange;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.helper.LevelLabelFormatter;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

@Controller
@RequestMapping("/milestones/{milestoneId}")
public class MilestoneModificationController {

	@Inject
	private MilestoneManagerService milestoneManager;

	@Inject
	private  AdministrationService adminManager;

	@Inject
	private UserAccountService userService;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;

	@Inject
	private Provider<MilestoneStatusComboDataBuilder> statusComboDataBuilderProvider;

	@Inject
	private Provider<MilestoneRangeComboDataBuilder> rangeComboDataBuilderProvider;

	@Inject
	private Provider<LevelLabelFormatter> levelLabelFormatterProvider;

	private static final Logger LOGGER = LoggerFactory.getLogger(MilestoneModificationController.class);

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long milestoneId, Locale locale) {
		Milestone milestone = milestoneManager.findById(milestoneId);
		ModelAndView mav = new ModelAndView("page/milestones/milestone-info");
		mav.addObject("milestoneStatus", statusComboDataBuilderProvider.get().useLocale(locale).buildMarshalled());
		mav.addObject("milestone", milestone);
		mav.addObject("milestoneStatusLabel", formatStatus(locale, milestone.getStatus()));
		mav.addObject("milestoneRangeLabel", formatRange(locale, milestone.getRange()));
		mav.addObject("milestoneRange", rangeComboDataBuilderProvider.get().useLocale(locale).buildMarshalled());
		mav.addObject("userList", buildMarshalledUserMap(adminManager.findAllAdminOrManager()));
		mav.addObject("canEdit", milestoneManager.canEditMilestone(milestoneId));
		mav.addObject("isAdmin", permissionEvaluationService.hasRole("ROLE_ADMIN"));
		mav.addObject("currentUser", StringEscapeUtils.escapeEcmaScript(userService.findCurrentUser().getLogin()));
		return mav;
	}

	private Object buildMarshalledUserMap(List<User> activeUsersOrderedByLogin) {

		HashMap<String, String> map = new HashMap<>();
		for (User user : activeUsersOrderedByLogin){
			map.put(user.getLogin(), user.getName());
		}

		return 	JsonHelper.serialize(map);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=milestone-description", VALUE })
	@ResponseBody
	public String changeDescription(@PathVariable long milestoneId, @RequestParam(VALUE) String newDescription) {
		milestoneManager.verifyCanEditMilestone(milestoneId);
		milestoneManager.changeDescription(milestoneId, newDescription);
		LOGGER.debug("Milestone modification : change milestone {} description = {}", milestoneId, newDescription);
		return  newDescription;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=milestone-status", VALUE })
	@ResponseBody
	public String changeStatus(@PathVariable long milestoneId, @RequestParam(VALUE) MilestoneStatus newStatus, Locale locale) {
		milestoneManager.verifyCanEditMilestone(milestoneId);
		milestoneManager.changeStatus(milestoneId, newStatus);
		LOGGER.debug("Milestone modification : change milestone {} Status = {}", milestoneId, newStatus);
		return  formatStatus(locale, newStatus);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=milestone-range", VALUE })
	@ResponseBody
	public String changeRange(@PathVariable long milestoneId, @RequestParam(VALUE) MilestoneRange newRange, Locale locale) {
		milestoneManager.verifyCanEditMilestoneRange();
		milestoneManager.changeRange(milestoneId, newRange);
		LOGGER.debug("Milestone modification : change milestone {} Range = {}", milestoneId, newRange);
		return  formatRange(locale, newRange);
	}


	private String formatStatus(Locale locale, MilestoneStatus status){
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(status);
	}

	private String formatRange(Locale locale, MilestoneRange newRange) {
		return levelLabelFormatterProvider.get().useLocale(locale).formatLabel(newRange);
	}



	@RequestMapping(method = RequestMethod.POST, params = { "newEndDate" })
	@ResponseBody
	public Date changeEndDate(@PathVariable long milestoneId, @RequestParam @DateTimeFormat(pattern = "yy-MM-dd") Date newEndDate) {
		milestoneManager.verifyCanEditMilestone(milestoneId);
		milestoneManager.changeEndDate(milestoneId, newEndDate);
		LOGGER.debug("Milestone modification : change milestone {} end date = {}", milestoneId, newEndDate);
		return  newEndDate;
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=milestone-owner", VALUE })
	@ResponseBody
	public String changeOwner(@PathVariable long milestoneId, @RequestParam(VALUE) String login) {
		User newOwner = adminManager.findByLogin(login);
		milestoneManager.verifyCanEditMilestone(milestoneId);
		milestoneManager.changeOwner(milestoneId, newOwner);
		LOGGER.debug("Milestone modification : change milestone {} owner = {}", milestoneId, newOwner);
		return  newOwner.getName();
	}

	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object changeName(@PathVariable long milestoneId, @RequestParam String newName) {
		milestoneManager.verifyCanEditMilestone(milestoneId);
		milestoneManager.changeLabel(milestoneId, newName);
		LOGGER.debug("Milestone modification : change milestone {} label = {}", milestoneId, newName);
		return new RenameModel(newName);
	}


	@RequestMapping(method = RequestMethod.GET,  params = { "isBoundToTemplate" })
	@ResponseBody
	public boolean isBoundToATemplate(@PathVariable long milestoneId) {

		return milestoneManager.isBoundToATemplate(milestoneId);
	}

	@RequestMapping(method = RequestMethod.GET,  params = { "isBoundToAtleastOneObject" })
	@ResponseBody
	public boolean isBoundToAtleastOneObject(@PathVariable long milestoneId) {

		return milestoneManager.isBoundToAtleastOneObject(milestoneId);
	}

	@RequestMapping(value = "/unbindallobjects", method = RequestMethod.DELETE)
	@ResponseBody
	public void unbindAllObjects(@PathVariable long milestoneId) {

		milestoneManager.unbindAllObjects(milestoneId);
	}


}
