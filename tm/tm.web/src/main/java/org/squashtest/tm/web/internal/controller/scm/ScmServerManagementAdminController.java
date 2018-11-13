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
package org.squashtest.tm.web.internal.controller.scm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.service.internal.scmserver.ScmConnectorRegistry;
import org.squashtest.tm.service.scmserver.ScmServerManagerService;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/administration/scm-servers")
public class ScmServerManagementAdminController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmServerManagementAdminController.class);

	@Inject
	private ScmConnectorRegistry scmConnectorRegistry;

	@Inject
	private ScmServerManagerService scmServerManager;

	@RequestMapping(method = RequestMethod.GET)
		public ModelAndView showManager() {
		LOGGER.trace("Loading scm servers management page.");

		List<ScmServer> scmServers = scmServerManager.findAllOrderByName();
		Set<String> scmKinds = scmConnectorRegistry.getRegisteredScmKinds();

		ModelAndView mav = new ModelAndView("scm-servers/scm-servers-manager.html");
		mav.addObject("scmServers", scmServers);
		mav.addObject("scmKinds", scmKinds);
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ScmServer createNewScmServer(@Valid ScmServer newScmServer) {
		return scmServerManager.createNewScmServer(newScmServer);
	}


}
