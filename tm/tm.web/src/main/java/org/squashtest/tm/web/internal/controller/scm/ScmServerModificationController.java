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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.service.internal.scmserver.ScmConnectorRegistry;
import org.squashtest.tm.service.scmserver.ScmRepositoryManagerService;
import org.squashtest.tm.service.scmserver.ScmServerManagerService;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.squashtest.tm.web.internal.controller.RequestParams.S_ECHO_PARAM;
import static org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY;

@Controller
@RequestMapping("/administration/scm-server/{scmServerId}")
public class ScmServerModificationController {

	private static final String NAME = "name";
	private static final String URL = "url";
	private static final String KIND = "kind";
	private static final String PATH = "path";
	private static final String REPOSITORY_PATH = "repositoryPath";

	private static final String FOLDER = "folder";
	private static final String FOLDER_PATH = "folderPath";
	private static final String BRANCH = "branch";

	private static final DatatableMapper<String> scmRepositoryTableMapper = new NameBasedMapper(4)
		.map(DEFAULT_ENTITY_NAME_KEY, DEFAULT_ENTITY_NAME_KEY)
		.map(PATH, REPOSITORY_PATH)
		.map(FOLDER, FOLDER_PATH)
		.map(BRANCH, BRANCH);

	@Inject
	private ScmServerManagerService scmServerManager;
	@Inject
	private ScmConnectorRegistry scmServerRegistry;
	@Inject
	private ScmRepositoryManagerService scmRepositoryManager;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showInfos(@PathVariable long scmServerId) {
		ScmServer scmServer = scmServerManager.findScmServer(scmServerId);
		Set<String> scmServerKinds = scmServerRegistry.getRegisteredScmKinds();
		List<ScmRepository> scmRepositories = scmRepositoryManager.findByScmServerOrderByPath(scmServerId);
		ModelAndView mav = new ModelAndView("scm-servers/scm-server-details.html");
		mav.addObject("scmServer", scmServer);
		mav.addObject("scmServerKinds", scmServerKinds);
		mav.addObject("scmRepositories", scmRepositories);
		return mav;
	}

	@RequestMapping(method = RequestMethod.POST, params = NAME)
	@ResponseBody
	public String updateName(@PathVariable long scmServerId, @RequestParam String name) {
		return scmServerManager.updateName(scmServerId, name);
	}

	@RequestMapping(method = RequestMethod.POST, params = URL)
	@ResponseBody
	public String updateUrl(@PathVariable long scmServerId, @RequestParam String url) {
		return scmServerManager.updateUrl(scmServerId, url);
	}

	@RequestMapping(method = RequestMethod.POST, params = KIND)
	@ResponseBody
	public String updateKind(@PathVariable long scmServerId, @RequestParam String kind) {
		return scmServerManager.updateKind(scmServerId, kind);
	}

	@RequestMapping(value = "/repositories", method = RequestMethod.GET, params = S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getScmRepositoriesTableModel(@PathVariable long scmServerId, DataTableDrawParameters params) {
		Pageable pageable = SpringPagination.pageable(params, scmRepositoryTableMapper);
		Page<ScmRepository> scmRepositories = scmRepositoryManager.findPagedScmRepositoriesByScmServer(scmServerId, pageable);
		return new ScmRepositoryDataTableModelHelper().buildDataModel(scmRepositories, params.getsEcho());
	}

	@RequestMapping(value = "/repositories", method = RequestMethod.POST)
	@ResponseBody
	public ScmRepository createNewScmRepository(@Valid ScmRepository scmRepository) {
		return scmRepositoryManager.createNewScmRepository(scmRepository);
	}

	private class ScmRepositoryDataTableModelHelper extends DataTableModelBuilder<ScmRepository> {
		@Override
		protected Object buildItemData(ScmRepository item) {

			Map<String, String> row = new HashMap<>();

			row.put("repository-id", item.getId().toString());
			row.put("repository-index", Long.toString(getCurrentIndex()));
			row.put("path", item.getRepositoryPath());
			row.put("folder", item.getFolderPath());
			row.put("branch", item.getBranch());
			row.put("empty-delete-holder", null);

			return row;
		}
	}
}
