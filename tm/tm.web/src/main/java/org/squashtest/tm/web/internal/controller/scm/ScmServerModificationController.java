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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.domain.scm.ScmRepository;
import org.squashtest.tm.domain.scm.ScmServer;
import org.squashtest.tm.domain.servers.AuthenticationPolicy;
import org.squashtest.tm.domain.servers.AuthenticationProtocol;
import org.squashtest.tm.service.internal.scmserver.ScmConnectorRegistry;
import org.squashtest.tm.service.scmserver.ScmRepositoryManagerService;
import org.squashtest.tm.service.scmserver.ScmServerCredentialsService;
import org.squashtest.tm.service.scmserver.ScmServerManagerService;
import org.squashtest.tm.service.servers.ManageableCredentials;
import org.squashtest.tm.service.servers.ServerAuthConfiguration;
import org.squashtest.tm.web.internal.controller.thirdpartyserver.ThirdPartyServerCredentialsManagementBean;
import org.squashtest.tm.web.internal.controller.thirdpartyserver.ThirdPartyServerCredentialsManagementHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.squashtest.tm.web.internal.controller.RequestParams.S_ECHO_PARAM;
import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;
import static org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY;

@Controller
@RequestMapping("/administration/scm-server/{scmServerId}")
public class ScmServerModificationController {

	private static final String SERVER_ID = "scmServerId";

	private static final String NAME = "name";
	private static final String URL = "url";
	private static final String COMMITTER_MAIL = "committerMail";
	private static final String PATH = "path";
	private static final String REPOSITORY_PATH = "repositoryPath";
	private static final String FOLDER = "folder";
	private static final String WORKING_FOLDER_PATH = "workingFolderPath";
	private static final String BRANCH = "branch";
	private static final String WORKING_BRANCH = "workingBranch";

	private static final DatatableMapper<String> scmRepositoryTableMapper = new NameBasedMapper(4)
		.map(DEFAULT_ENTITY_NAME_KEY, DEFAULT_ENTITY_NAME_KEY)
		.map(PATH, REPOSITORY_PATH)
		.map(FOLDER, WORKING_FOLDER_PATH)
		.map(BRANCH, WORKING_BRANCH);

	@Inject
	private ScmServerManagerService scmServerManager;
	@Inject
	private ScmConnectorRegistry scmServerRegistry;
	@Inject
	private ScmRepositoryManagerService scmRepositoryManager;
	@Inject
	private ScmServerCredentialsService credentialsService;
	@Inject
	private ThirdPartyServerCredentialsManagementHelper credentialsBeanHelper;
	@Inject
	private InternationalizationHelper i18nHelper;


	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showInfos(@PathVariable long scmServerId, Locale locale) {

		ScmServer scmServer = scmServerManager.findScmServer(scmServerId);
		Set<String> scmServerKinds = scmServerRegistry.getRegisteredScmKinds();
		List<ScmRepository> scmRepositories = scmRepositoryManager.findByScmServerOrderByPath(scmServerId);
		ThirdPartyServerCredentialsManagementBean authConf = makeAuthBean(scmServer, locale);

		ModelAndView mav = new ModelAndView("scm-servers/scm-server-details.html");
		mav.addObject("scmServer", scmServer);
		mav.addObject("scmServerKinds", scmServerKinds);
		mav.addObject("scmRepositories", scmRepositories);
		mav.addObject("authConf", authConf);

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

	@RequestMapping(method = RequestMethod.POST, params = COMMITTER_MAIL)
	@ResponseBody
	public String updateCommitterMail(@PathVariable long scmServerId, @RequestParam String committerMail) {
		return scmServerManager.updateCommitterMail(scmServerId, committerMail);
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
	public void createNewScmRepository(@PathVariable long scmServerId, @Valid ScmRepository scmRepository) throws IOException {
		scmRepositoryManager.createNewScmRepository(scmServerId, scmRepository);
	}


	// **************************** credentials management ******************************


	@RequestMapping(value = "/authentication-protocol", method = RequestMethod.POST, params = VALUE)
	@ResponseBody
	public void changeAuthProtocol(@PathVariable(SERVER_ID) long bugtrackerId, @RequestParam(VALUE) AuthenticationProtocol protocol){
		credentialsService.changeAuthenticationProtocol(bugtrackerId, protocol);
	}


	@RequestMapping(value = "/authentication-protocol/configuration", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public void saveAuthConfiguration(@PathVariable(SERVER_ID) long bugtrackerId,  @Valid @RequestBody ServerAuthConfiguration configuration){
		credentialsService.storeAuthConfiguration(bugtrackerId, configuration);
	}



	@RequestMapping(value = "/credentials", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public void storeCredentials(@PathVariable(SERVER_ID) long bugtrackerId ,@RequestBody ManageableCredentials credentials){
		credentialsService.storeCredentials(bugtrackerId, credentials);
	}


	// ********************** internal *****************************************


	private ThirdPartyServerCredentialsManagementBean makeAuthBean(ScmServer server, Locale locale){
		
		ThirdPartyServerCredentialsManagementBean bean = credentialsBeanHelper.initializeFor(server, locale);

		AuthenticationProtocol[] availableProtos = credentialsService.getSupportedProtocols(server);
		bean.setAvailableProtos(Arrays.asList(availableProtos));
		
		// force auth policy to app level, no other is supported
		bean.setAuthPolicy(AuthenticationPolicy.APP_LEVEL);
		bean.setFeatureAuthPolicySelection(false);
		
		//also the credentials are not optional, and are not testable
		bean.setFeatureTestCredentialsButton(false);
		bean.setAppLevelCredsAreOptional(false);

		return bean;

	}

	private class ScmRepositoryDataTableModelHelper extends DataTableModelBuilder<ScmRepository> {
		@Override
		protected Object buildItemData(ScmRepository item) {

			Map<String, String> row = new HashMap<>();

			row.put("repository-id", item.getId().toString());
			row.put("repository-index", Long.toString(getCurrentIndex()));
			row.put("name", item.getName());
			row.put("path", item.getRepositoryPath());
			row.put("folder", item.getWorkingFolderPath());
			row.put("branch", item.getWorkingBranch());
			row.put("empty-delete-holder", null);

			return row;
		}
	}
}
