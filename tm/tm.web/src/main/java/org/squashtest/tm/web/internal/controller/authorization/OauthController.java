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
package org.squashtest.tm.web.internal.controller.authorization;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.service.security.OAuth2ClientService;

@Controller
@SessionAttributes("authorizationRequest")
public class OauthController {

	@Inject
	private OAuth2ClientService oAuth2ClientService ;

	@RequestMapping("/oauth/confirm_access")
	public ModelAndView getAccessConfirmation(Map<String, Object> model, Principal principal) throws Exception {
		AuthorizationRequest clientAuth = (AuthorizationRequest) model.remove("authorizationRequest");
		ClientDetails client = oAuth2ClientService.findClientDetailsById(clientAuth.getClientId());
		List<String> roles = new ArrayList<>();
		for (GrantedAuthority item : client.getAuthorities()) {
			roles.add(item.getAuthority());
		}
		model.put("auth_request", clientAuth);
		model.put("client", client);
		model.put("roles", roles);
		return new ModelAndView("page/oauth/access_confirmation", model);
	}

	@RequestMapping("/oauth/error")
	public String handleError(Map<String, Object> model) throws Exception {
		// We can add more stuff to the model here for JSP rendering. If the client was a machine then
		// the JSON will already have been rendered.
		model.put("message", "There was a problem with the OAuth2 protocol");
		return "page/oauth/oauth_error";
	}
}
