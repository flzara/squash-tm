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
package org.squashtest.tm.web.internal.controller.bugtracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.core.BugTrackerNoCredentialsException;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackerModificationService;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.web.internal.controller.thirdpartyserver.ThirdPartyServerCredentialsManagementBean;
import org.squashtest.tm.web.internal.controller.thirdpartyserver.ThirdPartyServerCredentialsManagementHelper;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

// XSS OK - bflessel
@Controller
@RequestMapping("/bugtracker/{bugtrackerId}")
public class BugTrackerModificationController {
	private static final String BUGTRACKER_ID = "bugtrackerId";

	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerModificationController.class);

	@Inject
	private BugTrackerModificationService bugtrackerModificationService;

	@Inject
	private InternationalizationHelper i18nHelper;

	@Inject
	private BugTrackerFinderService bugtrackerFinder;
	
	@Inject
	private ThirdPartyServerCredentialsManagementHelper credentialsBeanHelper;



	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long bugtrackerId, Locale locale) {

		BugTracker bugTracker = bugtrackerFinder.findById(bugtrackerId);
		String jsonBugtrackerKinds = findJsonBugTrackerKinds();
		ThirdPartyServerCredentialsManagementBean authBean = makeAuthBean(bugTracker, locale);
		ModelAndView mav = new ModelAndView("page/bugtrackers/bugtracker-info");
		mav.addObject("bugtracker", bugTracker);
		mav.addObject("bugtrackerKinds", jsonBugtrackerKinds);
		mav.addObject("authConf", authBean);
		return mav;
	}



	@RequestMapping(method = RequestMethod.POST, params = { "newName" })
	@ResponseBody
	public Object changeName(@PathVariable long bugtrackerId, @RequestParam String newName) {
		bugtrackerModificationService.changeName(bugtrackerId, newName);
		LOGGER.debug("BugTracker modification : change bugtracker {} name = {}", bugtrackerId, newName);
		return new RenameModel(newName);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=bugtracker-url", VALUE })
	@ResponseBody
	public String changeUrl(@PathVariable long bugtrackerId, @RequestParam(VALUE) String newUrl) {
		bugtrackerModificationService.changeUrl(bugtrackerId, newUrl);
		LOGGER.debug("BugTracker modification : change bugtracker {} url = {}", bugtrackerId, newUrl);
		return HTMLCleanupUtils.cleanHtml(newUrl);
	}

	@RequestMapping(method = RequestMethod.POST, params = { "isIframeFriendly" })
	@ResponseBody
	public Object changeIframeFriendly(@PathVariable long bugtrackerId,
									   @RequestParam boolean isIframeFriendly) {
		bugtrackerModificationService.changeIframeFriendly(bugtrackerId, isIframeFriendly);
		LOGGER.debug("BugTracker modification : change bugtracker {} is iframe-friendly = {}", bugtrackerId,
				isIframeFriendly);
		return new IframeFriendly(isIframeFriendly);
	}

	private static final class IframeFriendly {
		private Boolean isIframeFriendly;

		private IframeFriendly(boolean iframeFriendly) {
			this.isIframeFriendly = iframeFriendly;
		}

		@SuppressWarnings("unused")
		public Boolean isIframeFriendly() {
			return isIframeFriendly;
		}
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=bugtracker-kind", VALUE })
	@ResponseBody
	public String changeKind(@RequestParam(VALUE) String kind, @PathVariable long bugtrackerId) {
		LOGGER.debug("BugTracker modification : change bugtracker {} kind = {}", bugtrackerId, kind);
		bugtrackerModificationService.changeKind(bugtrackerId, kind);
		return HTMLCleanupUtils.cleanHtml(kind);
	}


	// **************************** credentials management ******************************

	@RequestMapping(value = "/authentication-policy", method = RequestMethod.POST, params = VALUE)
	@ResponseBody
	public void changeAuthPolicy(@PathVariable(BUGTRACKER_ID) long bugtrackerId, @RequestParam(VALUE) AuthenticationPolicy policy){
		bugtrackerModificationService.changeAuthenticationPolicy(bugtrackerId, policy);
	}

	@RequestMapping(value = "/authentication-protocol", method = RequestMethod.POST, params = VALUE)
	@ResponseBody
	public void changeAuthProtocol(@PathVariable(BUGTRACKER_ID) long bugtrackerId, @RequestParam(VALUE) AuthenticationProtocol protocol){
		bugtrackerModificationService.changeAuthenticationProtocol(bugtrackerId, protocol);
	}


	@RequestMapping(value = "/authentication-protocol/configuration", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public void saveAuthConfiguration(@PathVariable(BUGTRACKER_ID) long bugtrackerId,  @Valid @RequestBody ServerAuthConfiguration configuration){
		bugtrackerModificationService.storeAuthConfiguration(bugtrackerId, configuration);
	}


	@RequestMapping(value= "/credentials/validator", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public void testCredentials(@PathVariable(BUGTRACKER_ID) long bugtrackerId ,@RequestBody ManageableCredentials credentials){
		/*
		 * catch BugTrackerNoCredentialsException, let fly the others
		 */
		try{
			bugtrackerModificationService.testCredentials(bugtrackerId, credentials);
		}
		catch(BugTrackerNoCredentialsException ex){
			// need to rethrow the same exception, with a message in the expected user language
			LOGGER.debug("server-app credentials test failed : ", ex);
			String message = i18nHelper.internationalize("thirdpartyserver.admin.messages.testcreds.fail", LocaleContextHolder.getLocale());
			throw new BugTrackerNoCredentialsException(message, ex);
		}
	}

	@RequestMapping(value = "/credentials", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public void storeCredentials(@PathVariable(BUGTRACKER_ID) long bugtrackerId ,@RequestBody ManageableCredentials credentials){
		bugtrackerModificationService.storeCredentials(bugtrackerId, credentials);
	}


	// ********************** more private stuffs ******************


	private String findJsonBugTrackerKinds() {
		Set<String> bugtrackerKinds = bugtrackerFinder.findBugTrackerKinds();
		Map<String, String> mapKinds = new HashMap<>(bugtrackerKinds.size());
		for (String kind : bugtrackerKinds) {
			kind = HTMLCleanupUtils.cleanHtml(kind);
			mapKinds.put(kind, kind);
		}
		return JsonHelper.serialize(mapKinds);
	}


	private ThirdPartyServerCredentialsManagementBean makeAuthBean(BugTracker bugTracker, Locale locale){
		
		ThirdPartyServerCredentialsManagementBean bean = credentialsBeanHelper.initializeFor(bugTracker, locale);

		AuthenticationProtocol[] availableProtos = bugtrackerModificationService.getSupportedProtocols(bugTracker);
		bean.setAvailableProtos(Arrays.asList(availableProtos));
		
		return bean;

	}


}
