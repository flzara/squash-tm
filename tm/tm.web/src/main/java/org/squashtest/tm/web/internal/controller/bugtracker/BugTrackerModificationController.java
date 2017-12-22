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

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.Credentials;
import org.squashtest.tm.service.bugtracker.BugTrackerFinderService;
import org.squashtest.tm.service.bugtracker.BugTrackerModificationService;
import org.squashtest.tm.service.servers.EncryptionKeyChangedException;
import org.squashtest.tm.service.servers.MissingEncryptionKeyException;
import org.squashtest.tm.web.internal.helper.JsonHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;

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
	
	
	
	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public ModelAndView getProjectInfos(@PathVariable long bugtrackerId, Locale locale) {

		BugTracker bugTracker = bugtrackerFinder.findById(bugtrackerId);
		String jsonBugtrackerKinds = findJsonBugTrackerKinds();
		BugtrackerCredentialsManagementBean authBean = makeAuthBean(bugTracker, locale);
		
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
		return newUrl;
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
		private Boolean iframeFriendly;

		private IframeFriendly(boolean iframeFriendly) {
			this.iframeFriendly = iframeFriendly;
		}

		@SuppressWarnings("unused")
		public Boolean isIframeFriendly() {
			return iframeFriendly;
		}
	}

	@RequestMapping(method = RequestMethod.POST, params = { "id=bugtracker-kind", VALUE })
	@ResponseBody
	public String changeKind(@RequestParam(VALUE) String kind, @PathVariable long bugtrackerId) {
		LOGGER.debug("BugTracker modification : change bugtracker {} kind = {}", bugtrackerId, kind);
		bugtrackerModificationService.changeKind(bugtrackerId, kind);

		return kind;
	}
	
	
	// **************************** credentials management ******************************
	
	@RequestMapping(method = RequestMethod.POST, params = {"id=bugtracker-auth-policy", VALUE})
	@ResponseBody
	public void changeAuthPolicy(@RequestParam(VALUE) AuthenticationPolicy policy, @PathVariable(BUGTRACKER_ID) long bugtrackerId){
		bugtrackerModificationService.changeAuthenticationPolicy(bugtrackerId, policy);
	}
	
	
	@RequestMapping(value= "/credentials/validator", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public void testCredentials(@PathVariable(BUGTRACKER_ID) long bugtrackerId ,@RequestBody Credentials credentials){
		/*
		 * no exception -> no problem
		 * exception	-> let it fly
		 */
		bugtrackerModificationService.testCredentials(bugtrackerId, credentials);
	}
	
	@RequestMapping(value = "/credentials", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public void storeCredentials(@PathVariable(BUGTRACKER_ID) long bugtrackerId ,@RequestBody Credentials credentials){
		bugtrackerModificationService.storeCredentials(bugtrackerId, credentials);
	}
	
	
	// ********************** more private stuffs ******************
	
	
	private String findJsonBugTrackerKinds() {
		Set<String> bugtrackerKinds = bugtrackerFinder.findBugTrackerKinds();
		Map<String, String> mapKinds = new HashMap<>(bugtrackerKinds.size());
		for (String kind : bugtrackerKinds) {
			mapKinds.put(kind, kind);
		}
		return JsonHelper.serialize(mapKinds);
	}
	
	
	private BugtrackerCredentialsManagementBean makeAuthBean(BugTracker bugTracker, Locale locale){
		AuthenticationProtocol[] availableProtos = bugtrackerModificationService.getSupportedProtocols(bugTracker);
		BugtrackerCredentialsManagementBean bean = new BugtrackerCredentialsManagementBean();
		
		// defaults 
		bean.setAuthPolicy(bugTracker.getAuthenticationPolicy());
		bean.setSelectedProto(AuthenticationProtocol.BASIC_AUTH);
		bean.setAvailableProtos(Arrays.asList(availableProtos));
		
		// now check against the credentials
		try{
			Credentials credentials = bugtrackerModificationService.findCredentials(bugTracker.getId());
			
			if (credentials != null){
				bean.setSelectedProto(credentials.getImplementedProtocol());
				bean.setCredentials(credentials);
			}
			
		}
		// no encryption key : blocking error, internationalizable
		catch(MissingEncryptionKeyException ex){
			String msg = i18nHelper.internationalize(ex, locale);
			bean.setFailureMessage(msg);
		}
		// key changed : recoverable error, internationalizable
		catch(EncryptionKeyChangedException ex){
			String msg = i18nHelper.internationalize(ex, locale);
			bean.setWarningMessage(msg);
		}
		// other exceptions are treated as non blocking, non internationalizable errors
		catch(Exception ex){
			LOGGER.error(ex.getMessage(), ex);
			bean.setWarningMessage(ex.getMessage());
		}
		
		return bean;
		
	}
	
	
	
	public static final class BugtrackerCredentialsManagementBean{
		
		// if those Strings remains to null it is a good thing
		private String failureMessage = null;
		private String warningMessage = null;
		
		// the rest is used if the above is null
		private AuthenticationPolicy authPolicy;
		private List<AuthenticationProtocol> availableProtos;
		private AuthenticationProtocol selectedProto; 
		private Credentials credentials;
		
		public AuthenticationPolicy getAuthPolicy() {
			return authPolicy;
		}
		public void setAuthPolicy(AuthenticationPolicy authPolicy) {
			this.authPolicy = authPolicy;
		}
		public List<AuthenticationProtocol> getAvailableProtos() {
			return availableProtos;
		}
		public void setAvailableProtos(List<AuthenticationProtocol> availableProtos) {
			this.availableProtos = availableProtos;
		}
		public AuthenticationProtocol getSelectedProto() {
			return selectedProto;
		}
		public void setSelectedProto(AuthenticationProtocol selectedProto) {
			this.selectedProto = selectedProto;
		}
		public Credentials getCredentials() {
			return credentials;
		}
		public void setCredentials(Credentials credentials) {
			this.credentials = credentials;
		}
		public String getFailureMessage() {
			return failureMessage;
		}
		public void setFailureMessage(String failureMessage) {
			this.failureMessage = failureMessage;
		}
		public String getWarningMessage() {
			return warningMessage;
		}
		public void setWarningMessage(String warningMessage) {
			this.warningMessage = warningMessage;
		}
		
		
		
		
	}
	

}
