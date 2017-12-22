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
package org.squashtest.tm.web.internal.controller.users;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.user.TeamModificationService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.administration.UserModel;
import org.squashtest.tm.web.internal.controller.project.ProjectModel;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;
import org.squashtest.tm.web.internal.model.json.JsonGeneralInfo;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

/**
 * @author mpagnon
 *
 */
@Controller
@RequestMapping("/administration/teams")
public class TeamController extends PartyControllerSupport {
	/**
	 *
	 */
	private static final String TEAM_ID = "teamId";

	@Inject
	private TeamModificationService service;

	@Inject
	private PermissionEvaluationService permissionEvaluationService;


	private static final String TEAM_ID_URL = "/{teamId}";

	private DatatableMapper<String> teamsMapper = new NameBasedMapper(9)
	.mapAttribute(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "name", Team.class)
	.mapAttribute("description", "description", Team.class)
	.mapAttribute("nb-associated-users", "members.size", Team.class)
	.mapAttribute("created-on", "audit.createdOn", Team.class)
	.mapAttribute("created-by", "audit.createdBy", Team.class)
	.mapAttribute("last-mod-on", "audit.lastModifiedOn", Team.class)
	.mapAttribute("last-mod-by", "audit.lastModifiedBy", Team.class);

	private DatatableMapper<String> membersMapper = new NameBasedMapper(1).map("user-name", "firstName");

	private DatatableMapper<String> permissionMapper = new NameBasedMapper(2)
	.mapAttribute(DataTableModelConstants.PROJECT_NAME_KEY, "project.name", ProjectPermission.class)
	.mapAttribute("permission-name", "permissionGroup.qualifiedName", ProjectPermission.class);

	private static final Logger LOGGER = LoggerFactory.getLogger(TeamController.class);

	/**
	 * Creates a new Team
	 *
	 * @param team
	 *            : the given {@link Team} filled with a name and a description
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public void createNew(@Valid @ModelAttribute("add-team") Team team) {
		LOGGER.info(ToStringBuilder.reflectionToString(team));
		service.persist(team);
	}

	/**
	 * Return the DataTableModel to display the table of all teams.
	 *
	 * @param params
	 *            the {@link DataTableDrawParameters} for the teams table
	 * @return the {@link DataTableModel} with organized {@link Team} infos.
	 */
	@RequestMapping(method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getTableModel(final DataTableDrawParameters params, final Locale locale) {

		PagingAndSorting paging = new DataTableSorting(params, teamsMapper);
		Filtering filtering = new DataTableFiltering(params);

		PagedCollectionHolder<List<Team>> holder = service.findAllFiltered(paging, filtering);

		return new TeamsDataTableModelHelper(locale, messageSource).buildDataModel(holder, params.getsEcho());
	}

	/**
	 * Will delete the given team along with it's permissions. will not delete it's associated users
	 *
	 * @param teamId
	 */
	@RequestMapping(value = TEAM_ID_URL, method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTeam(@PathVariable List<Long> teamId) {
		service.deleteTeam(teamId);
	}

	/**
	 * Will return a view for the team of the given id
	 *
	 * @param teamId
	 */
	@RequestMapping(value = TEAM_ID_URL, method = RequestMethod.GET)
	public String showTeamModificationPage(@PathVariable Long teamId, Model model) {
		if (!permissionEvaluationService.hasRole("ROLE_ADMIN")) {
			throw new AccessDeniedException("Access is denied");
		}
		Team team = service.findById(teamId);
		List<?> permissionModel = createPermissionTableModel(teamId, new DefaultPagingAndSorting(),
				DefaultFiltering.NO_FILTERING, "").getAaData();

		List<?> userModel = createMembersTableModel(teamId, new DefaultPagingAndSorting(),
				DefaultFiltering.NO_FILTERING, "").getAaData();

		List<PermissionGroupModel> pgm = getPermissionGroupModels();
		List<ProjectModel> pm = getProjectModels(teamId);


		model.addAttribute("team", team);
		model.addAttribute("users", userModel);
		model.addAttribute("permissionList", pgm);
		model.addAttribute("myprojectList", pm);
		model.addAttribute("permissions", permissionModel);

		return "team-modification.html";
	}

	@RequestMapping(value = TEAM_ID_URL, method = RequestMethod.POST, params = "id=team-description")
	@ResponseBody
	public String changeDescription(@PathVariable Long teamId, @RequestParam String value) {
		service.changeDescription(teamId, value);
		return value;
	}

	@RequestMapping(value = TEAM_ID_URL + "/name", method = RequestMethod.POST)
	@ResponseBody
	public RenameModel changeName(@PathVariable Long teamId, @RequestParam String value) {
		service.changeName(teamId, value);
		return new RenameModel(value);
	}


	@RequestMapping(value = TEAM_ID_URL + "/general", method = RequestMethod.GET, produces=ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonGeneralInfo refreshGeneralInfos(@PathVariable(TEAM_ID) long teamId){
		Team team = service.findById(teamId);
		return new JsonGeneralInfo((AuditableMixin)team);

	}


	// ************************************ team members section ************************

	@RequestMapping(value = TEAM_ID_URL + "/members", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getMembersTableModel(DataTableDrawParameters params, @PathVariable(TEAM_ID) long teamId) {
		PagingAndSorting paging = new DataTableSorting(params, membersMapper);
		Filtering filtering = new DataTableFiltering(params);
		return createMembersTableModel(teamId, paging, filtering, params.getsEcho());
	}

	@RequestMapping(value = TEAM_ID_URL + "/members/{memberIds}", method = RequestMethod.DELETE)
	@ResponseBody
	public void removeMember(@PathVariable(TEAM_ID) long teamId, @PathVariable("memberIds") List<Long> memberIds) {
		service.removeMembers(teamId, memberIds);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = TEAM_ID_URL + "/non-members", headers = "Accept=application/json")
	@ResponseBody
	public Collection<UserModel> getNonMembers(@PathVariable(TEAM_ID) long teamId) {
		List<User> nonMembers = service.findAllNonMemberUsers(teamId);
		return CollectionUtils.collect(nonMembers, new UserModelCreator());
	}

	@RequestMapping(value = TEAM_ID_URL + "/members/{logins}", method = RequestMethod.PUT)
	@ResponseBody
	public void addMembers(@PathVariable(TEAM_ID) long teamId, @PathVariable("logins") List<String> userlogins) {
		service.addMembers(teamId, userlogins);
	}

	// **************************** team permission section ************************

	@RequestMapping(value = TEAM_ID_URL + "/add-permission", method = RequestMethod.POST)
	@ResponseBody
	public void addNewPermission(@RequestParam("project") long projectId, @PathVariable long teamId,
			@RequestParam String permission) {
		permissionService.addNewPermissionToProject(teamId, projectId, permission);
	}

	@RequestMapping(value = TEAM_ID_URL + "/remove-permission", method = RequestMethod.POST)
	@ResponseBody
	public void removePermission(@RequestParam("project") long projectId, @PathVariable long teamId) {
		permissionService.removeProjectPermission(teamId, projectId);
	}

	@RequestMapping(value = TEAM_ID_URL + "/permissions", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getPermissionTableModel(DataTableDrawParameters params, @PathVariable(TEAM_ID) long teamId) {
		PagingAndSorting paging = new DataTableSorting(params, permissionMapper);
		Filtering filtering = new DataTableFiltering(params);
		return createPermissionTableModel(teamId, paging, filtering, params.getsEcho());
	}

	// ******************************* private *************************************

	private DataTableModel createMembersTableModel(long teamId, PagingAndSorting paging, Filtering filtering,
			String secho) {
		PagedCollectionHolder<List<User>> holder = service.findAllTeamMembers(teamId, paging, filtering);
		return new MembersTableModelHelper().buildDataModel(holder, secho);
	}

	@RequestMapping(value = TEAM_ID_URL + "/permission-popup", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getPermissionPopup(@PathVariable long teamId) {
		return createPermissionPopupModel(teamId);
	}

	// ************************* private classes ***********************

	private static final class UserModelCreator implements Transformer {
		@Override
		public Object transform(Object user) {
			return new UserModel((User) user);
		}
	}

	private static final class TeamsDataTableModelHelper extends DataTableModelBuilder<Team> {
		private InternationalizationHelper messageSource;
		private Locale locale;

		private TeamsDataTableModelHelper(Locale locale, InternationalizationHelper messageSource) {
			this.locale = locale;
			this.messageSource = messageSource;
		}

		@Override
		public Map<String, Object> buildItemData(Team item) {
			final AuditableMixin auditable = (AuditableMixin) item;
			Map<String, Object> res = new HashMap<>();
			res.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
			res.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, item.getName());
			res.put("description", item.getDescription());
			res.put("nb-associated-users", item.getMembers().size());
			res.put(DataTableModelConstants.DEFAULT_CREATED_ON_KEY, messageSource.localizeDate(auditable.getCreatedOn(), locale));
			res.put(DataTableModelConstants.DEFAULT_CREATED_BY_KEY, auditable.getCreatedBy());
			res.put("last-mod-on", messageSource.localizeDate(auditable.getLastModifiedOn(), locale));
			res.put("last-mod-by", auditable.getLastModifiedBy());
			res.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return res;
		}
	}

	private static final class MembersTableModelHelper extends DataTableModelBuilder<User> {
		private MembersTableModelHelper() {
			super();
		}

		@Override
		protected Map<?, ?> buildItemData(User item) {
			Map<String, Object> res = new HashMap<>();
			res.put("user-id", item.getId());
			res.put("user-active", item.getActive());
			res.put("user-index", getCurrentIndex());
			res.put("user-name", item.getFirstName() + " " + item.getLastName() + " (" + item.getLogin() + ")");
			res.put("empty-delete-holder", null);
			return res;
		}
	}
}
