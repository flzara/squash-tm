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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.domain.servers.ThirdPartyServer;
import org.squashtest.tm.service.internal.scmserver.ScmConnectorRegistry;
import org.squashtest.tm.service.scmserver.ScmServerCredentialsService;
import org.squashtest.tm.service.scmserver.ScmServerManagerService;
import org.squashtest.tm.service.servers.EncryptionKeyChangedException;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.MissingEncryptionKeyException;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.thirdpartyserver.ThirdPartyServerCredentialsManagementBean;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.*;

import static org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY;

@Controller
@RequestMapping("/administration/scm-servers")
public class ScmServerManagementAdminController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScmServerManagementAdminController.class);

	private static final String URL = "url";
	private static final String KIND = "kind";
	private static final String ID_EQUALS_IS_BOUND = "id=is-bound";

	private final DatatableMapper<String> scmServerTableMapper = new NameBasedMapper()
			.map(DEFAULT_ENTITY_NAME_KEY, DEFAULT_ENTITY_NAME_KEY)
			.map(KIND, KIND)
			.map(URL, URL);

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

	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getScmServersTableModel(final DataTableDrawParameters params) {
		Pageable pageable = SpringPagination.pageable(params, scmServerTableMapper);
		Page<ScmServer> scmServers = scmServerManager.findAllSortedScmServers(pageable);
		return new ScmServerDataTableModelHelper().buildDataModel(scmServers, params.getsEcho());
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ScmServer createNewScmServer(@Valid ScmServer newScmServer) {
		return scmServerManager.createNewScmServer(newScmServer);
	}

	@RequestMapping(value = "/{scmServerIds}", method = RequestMethod.GET, params = ID_EQUALS_IS_BOUND)
	@ResponseBody
	public boolean isOneServerBoundToProject(@PathVariable List<Long> scmServerIds) {
		return scmServerManager.isOneServerBoundToProject(scmServerIds);
	}

	@RequestMapping(value = "/{scmServerIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteScmServers(@PathVariable List<Long> scmServerIds) {
		scmServerManager.deleteScmServers(scmServerIds);
	}



	private class ScmServerDataTableModelHelper extends DataTableModelBuilder<ScmServer> {
		@Override
		protected Object buildItemData(ScmServer item) {
			Map<String, String> row = new HashMap<>(6);

			row.put("server-id", item.getId().toString());
			row.put("server-index", Long.toString(getCurrentIndex()));
			row.put("name", item.getName());
			row.put("kind", item.getKind());
			row.put("url", item.getUrl());
			row.put("empty-delete-holder", null);

			return row;
		}
	}

}
