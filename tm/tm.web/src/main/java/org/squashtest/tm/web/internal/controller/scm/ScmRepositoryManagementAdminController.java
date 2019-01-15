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
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.service.scmserver.ScmRepositoryManagerService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping("/administration/scm-repositories")
public class ScmRepositoryManagementAdminController {

	private static final String SCM_SERVER_ID = "scmServerId";
	private static final String PATH = "path";
	private static final String FOLDER = "folder";
	private static final String BRANCH = "branch";
	private static final String ID_EQUAL_IS_BOUND = "id=is-bound";

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

	@RequestMapping(value = "/{scmRepositoryId}", method = RequestMethod.POST, params = FOLDER)
	@ResponseBody
	public String updateFolder(@PathVariable long scmRepositoryId, String folder) {
		return scmRepositoryManager.updateFolder(scmRepositoryId, folder);
	}

	@RequestMapping(value = "/{scmRepositoryId}", method = RequestMethod.POST, params = BRANCH)
	@ResponseBody
	public String updateBranch(@PathVariable long scmRepositoryId, String branch) throws IOException {
		return scmRepositoryManager.updateBranch(scmRepositoryId, branch);
	}

	@RequestMapping(method = RequestMethod.GET, params = SCM_SERVER_ID)
	@ResponseBody
	public List<ScmRepository> getScmRepositories(long scmServerId) {
		return scmRepositoryManager.findByScmServerOrderByPath(scmServerId);
	}

	@RequestMapping(value = "/{scmRepositoryIds}", method = RequestMethod.GET, params = ID_EQUAL_IS_BOUND)
	@ResponseBody
	public boolean isOneRepositoryBoundToProject(@PathVariable Collection<Long> scmRepositoryIds) {
		return scmRepositoryManager.isOneRepositoryBoundToProject(scmRepositoryIds);
	}


}
