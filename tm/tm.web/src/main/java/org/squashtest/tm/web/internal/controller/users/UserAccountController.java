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
package org.squashtest.tm.web.internal.controller.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.core.BugTrackerRemoteException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyPreference;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.exception.bugtracker.CannotConnectBugtrackerException;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;
import org.squashtest.tm.service.internal.security.AuthenticationProviderContext;
import org.squashtest.tm.service.internal.servers.ManageableBasicAuthCredentials;
import org.squashtest.tm.service.internal.servers.UserOAuth1aToken;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.project.ProjectFinder;
import org.squashtest.tm.service.project.ProjectsPermissionFinder;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.OAuth1aTemporaryTokens;
import org.squashtest.tm.service.servers.StoredCredentialsManager;
import org.squashtest.tm.service.user.PartyPreferenceService;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.controller.bugtracker.BugTrackerModificationController;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.*;

import static org.squashtest.tm.domain.servers.OAuth1aCredentials.SignatureMethod;
import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

//XSS ok bflessel
@Controller
@RequestMapping("/user-account")
public class UserAccountController {

	private static final String SQUASH_BUGTRACKER_MODE= "squash.bug.tracker.mode";

	private UserAccountService userService;

	@Inject
	private MilestoneManagerService milestoneManager;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	private ProjectsPermissionFinder permissionFinder;

	@Inject
	private AuthenticationProviderContext authenticationProviderContext;

	@Inject
	private PartyPreferenceService partyPreferenceService;

	@Inject
	private StoredCredentialsManager credManager;

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	public void setProjectsPermissionFinderService(ProjectsPermissionFinder permissionFinder) {
		this.permissionFinder = permissionFinder;
	}

	@Inject
	public void setUserAccountService(UserAccountService service){
		this.userService=service;
	}

	public PartyPreferenceService getPartyPreferenceService() {
		return partyPreferenceService;
	}

	public void setPartyPreferenceService(PartyPreferenceService partyPreferenceService) {
		this.partyPreferenceService = partyPreferenceService;
	}

	private static final String BUGTRACKER_ID = "bugtrackerId";
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerModificationController.class);

	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView getUserAccountDetails() {
		User user = userService.findCurrentUser();
		Long idUser = user.getId();
		Party party = userService.getParty(idUser);
		Map<String, String> map  =  partyPreferenceService.findPreferences(party);
		String bugtrackerMode= map.get(SQUASH_BUGTRACKER_MODE);
		boolean hasLocalPassword = userService.hasCurrentUserPasswordDefined();

		Map<BugTracker, ManageableCredentials> bugtrackerMap = this.getPairedBugtrackerAndManagedCredentials();


		List<Milestone> milestoneList = milestoneManager.findAllVisibleToCurrentUser();

		List<ProjectPermission> projectPermissions = permissionFinder.findProjectPermissionByUserLogin(user.getLogin());

		Collections.sort(milestoneList, new SortMilestoneList());

		ModelAndView mav = new ModelAndView("page/users/user-account");
		mav.addObject("user", user);
		mav.addObject("milestoneList", milestoneList);
		mav.addObject("projectPermissions", projectPermissions);
		mav.addObject("bugtrackerMode", bugtrackerMode);
		mav.addObject("hasLocalPassword", hasLocalPassword);
		mav.addObject("bugtrackerCredentialsMap", bugtrackerMap);


		// also, active milestone
		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			JsonMilestone jsMilestone =
					new JsonMilestone(
					activeMilestone.get().getId(),
					HtmlUtils.htmlEscape(activeMilestone.get().getLabel()), activeMilestone.get().getStatus(),
					activeMilestone.get().getRange(), activeMilestone.get().getEndDate(),
					HtmlUtils.htmlEscape(activeMilestone.get().getOwner().getLogin())
							);
			mav.addObject("activeMilestone", jsMilestone);
		}
		
		// if the local password manageable ?
		boolean canManageLocalPassword = authenticationProviderContext.isInternalProviderEnabled();
		mav.addObject("canManageLocalPassword", canManageLocalPassword);

		return mav;

	}

	@RequestMapping(value="/update", method=RequestMethod.POST, params={"initializing", "oldPassword", "newPassword"})
	@ResponseBody
	public void changePassword(@ModelAttribute @Valid PasswordChangeForm form){
		if (form.isInitializing()){
			userService.setCurrentUserPassword(form.getNewPassword());
		}
		else{
			userService.setCurrentUserPassword(form.getOldPassword(), form.getNewPassword());
		}
	}

	@RequestMapping(value="/update", method=RequestMethod.POST, params={"id=user-account-email", VALUE}, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String updateUserEmail(@RequestParam(VALUE) String email){
		userService.setCurrentUserEmail(email);
		return HtmlUtils.htmlEscape(email);
	}



	public class SortMilestoneList implements Comparator<Milestone> {
		@Override
		public int compare(Milestone m1, Milestone m2) {
			return m1.getLabel().compareTo(m2.getLabel());
		}
	}

	@RequestMapping(value = "/update", method= RequestMethod.POST)
	@ResponseBody
	public PartyPreference changeUserBugtrackerMode (@RequestParam(VALUE) String bugtrackerMode){
		partyPreferenceService.addOrUpdatePreferenceForCurrentUser(SQUASH_BUGTRACKER_MODE,bugtrackerMode);
		return partyPreferenceService.findPreferenceForCurrentUser(SQUASH_BUGTRACKER_MODE);
	}


	/**
	 * For all bugtrackers that the current user can access, map the bugtracker to an instance of manageable
	 * credentials of the appropriate protocol. If the current user has no credentials for that bugtracker,
	 * a default, empty instance will be supplied instead (ie the values are never null).
	 *
	 * @return
	 */
	private Map<BugTracker, ManageableCredentials> getPairedBugtrackerAndManagedCredentials(){

		Map<BugTracker, ManageableCredentials> bugtrackerMap = new LinkedHashMap<>();
		List<BugTracker> bugtrackers = userService.findAllUserBugTracker();

		bugtrackers.sort(Comparator.comparing((BugTracker::getAuthenticationProtocol)));
		for (BugTracker bugtracker : bugtrackers) {
			ManageableCredentials credentials = credManager.findCurrentUserCredentials(bugtracker.getId());

			if (credentials == null){
				credentials = createDefaultCredentials(bugtracker);
			}

			bugtrackerMap.put(bugtracker,credentials);

		}

		return bugtrackerMap;

	}

	/**
	 * This method creates default, empty instances of manageable credentials if none are defined
	 * for the current user for that server.
	 *
	 * @param bugtracker
	 * @return
	 */
	private ManageableCredentials createDefaultCredentials(BugTracker bugtracker) {
		ManageableCredentials credentials;
		switch(bugtracker.getAuthenticationProtocol()){

			case BASIC_AUTH: credentials = new ManageableBasicAuthCredentials("", "");
			break;

			case OAUTH_1A: credentials = new UserOAuth1aToken("", "");
			break;

			default:
				throw new IllegalArgumentException("AuthenticationProtocol '"+bugtracker.getAuthenticationProtocol()+"' not supported");

		}
		return credentials;
	}

	@RequestMapping(value = "bugtracker/{bugtrackerId}/credentials",params={ "username", "password"}, method =  RequestMethod.POST)
	@ResponseBody
	public void saveCurrentUserCredentials(@PathVariable(BUGTRACKER_ID) long bugtrackerId , @RequestParam String username,  @RequestParam char[] password){

		ManageableBasicAuthCredentials credentials = new ManageableBasicAuthCredentials(username, password);
			try {
				userAccountService.testCurrentUserCredentials(bugtrackerId, credentials);
				userAccountService.saveCurrentUserCredentials(bugtrackerId, credentials);
			} catch (BugTrackerRemoteException ex) {
				LOGGER.debug("server-app credentials test failed : ", ex);
				throw new CannotConnectBugtrackerException(ex);
			}


	}

	@RequestMapping(value= "bugtracker/{bugtrackerId}/credentials/validator", params = {"username", "password"}, method = RequestMethod.POST)
	@ResponseBody
	public void testCredentials(@PathVariable(BUGTRACKER_ID)  long bugtrackerId , @RequestParam String username, @RequestParam char[] password){
		/*
		 * catch BugTrackerNoCredentialsException, let fly the others
		 */
		ManageableBasicAuthCredentials credentials = new ManageableBasicAuthCredentials(username, password);

		try{
			userAccountService.testCurrentUserCredentials(bugtrackerId, credentials);
		}
		catch(BugTrackerRemoteException ex){
			// need to rethrow the same exception, with a message in the expected user language
			LOGGER.debug("server-app credentials test failed : ", ex);
			throw new CannotConnectBugtrackerException(ex);
		}
	}

	@RequestMapping(value="bugtracker/{bugtrackerId}/credentials", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteUserCredentials (@PathVariable (BUGTRACKER_ID) long bugtrackerId) {
		userAccountService.deleteCurrentUserCredentials(bugtrackerId);

	}


}
