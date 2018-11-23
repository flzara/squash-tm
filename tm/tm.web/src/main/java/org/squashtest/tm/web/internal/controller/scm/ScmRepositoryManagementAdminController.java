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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.service.scmserver.ScmRepositoryManagerService;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping("/administration/scm-repositories")
public class ScmRepositoryManagementAdminController {

	private static final String PATH = "path";

	@Inject
	ScmRepositoryManagerService scmRepositoryManager;

	@RequestMapping(value = "/{scmRepositoriesIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteScmRepositories(@PathVariable List<Long> scmRepositoriesIds) {
		scmRepositoryManager.deleteScmRepositories(scmRepositoriesIds);
	}

	@RequestMapping(value = "/{scmRepositoryId}", method = RequestMethod.POST, params = PATH)
	@ResponseBody
	public String updatePath(@PathVariable long scmRepositoryId, String path) {
		return scmRepositoryManager.updatePath(scmRepositoryId, path);
	}
}
