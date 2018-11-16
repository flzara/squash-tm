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

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.service.scmserver.ScmServerManagerService;

import javax.inject.Inject;

@Controller
@RequestMapping("/administration/scm-server/{scmServerId}")
public class ScmServerModificationController {

	private static final String NAME = "name";
	@Inject
	private ScmServerManagerService scmServerManager;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showInfos(@PathVariable long scmServerId) {
		ScmServer scmServer = scmServerManager.findScmServer(scmServerId);
		ModelAndView mav = new ModelAndView("scm-servers/scm-server-details.html");
		mav.addObject("scmServer", scmServer);
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = NAME)
	@ResponseBody
	public String updateName(@PathVariable long scmServerId, @RequestParam String newName) {
		return scmServerManager.updateName(scmServerId, newName);
	}

}
