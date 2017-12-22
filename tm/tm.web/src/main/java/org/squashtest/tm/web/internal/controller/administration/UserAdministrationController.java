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
package org.squashtest.tm.web.internal.controller.administration;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.api.security.authentication.AuthenticationProviderFeatures;
import org.squashtest.tm.core.foundation.collection.DefaultFiltering;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.audit.AuditableMixin;
import org.squashtest.tm.domain.project.ProjectPermission;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.service.user.TeamFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.controller.project.ProjectModel;
import org.squashtest.tm.web.internal.controller.users.PartyControllerSupport;
import org.squashtest.tm.web.internal.controller.users.PermissionGroupModel;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableFiltering;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;
import org.squashtest.tm.web.internal.security.authentication.AuthenticationProviderContext;

@Controller
@RequestMapping("/administration/users")
public class UserAdministrationController extends PartyControllerSupport {

	private static final String USER_ID = "userId";
	private static final Logger LOGGER = LoggerFactory.getLogger(UserAdministrationController.class);
	private static final String USER_URL = "/{userId}";
	private static final String USER_URLS = "/{userIds}";

	private static final PagingAndSorting TEAMS_DEFAULT_PAGING = new DefaultPagingAndSorting("name");
	private static final Filtering TEAMS_DEFAULT_FILTERING = DefaultFiltering.NO_FILTERING;

	@Inject
	private AdministrationService adminService;

	@Inject
	private TeamFinderService teamFinderService;

	@Inject
	private InternationalizationHelper messageSource;

	@Inject
	private AuthenticationProviderContext authenticationProviderContext;

	private DatatableMapper<String> userMapper = new NameBasedMapper(10).map("user-id", "id")
			.map("user-active", "active").map("user-login", "login").map("user-group", "group")
			.map("user-firstname", "firstName").map("user-lastname", "lastName").map("user-email", "email")
			.map("user-created-on", "audit.createdOn").map("user-created-by", "audit.createdBy")
			.map("user-modified-on", "audit.lastModifiedOn").map("user-modified-by", "audit.lastModifiedBy")
			.map("user-connected-on", "lastConnectedOn");

	private DatatableMapper<String> permissionMapper = new NameBasedMapper(2).mapAttribute(
			DataTableModelConstants.PROJECT_NAME_KEY, "project.name", ProjectPermission.class).mapAttribute(
					"permission-name", "permissionGroup.qualifiedName", ProjectPermission.class);

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ModelAndView getUserList(Locale locale) {

		ModelAndView mav = new ModelAndView("page/users/show-users");

		List<UsersGroup> list = adminService.findAllUsersGroupOrderedByQualifiedName();

		PagingAndSorting paging = new DefaultPagingAndSorting("User.login");
		Filtering filter = DefaultFiltering.NO_FILTERING;

		DataTableModel model = getTableModel(paging, filter, "noneed", locale);

		mav.addObject("usersGroupList", list);
		mav.addObject("userList", model.getAaData());

		PagedCollectionHolder<List<Team>> teams = teamFinderService.findAllFiltered(TEAMS_DEFAULT_PAGING,
				TEAMS_DEFAULT_FILTERING);
		mav.addObject("pagedTeams", teams);
		mav.addObject("teamsPageSize", TEAMS_DEFAULT_PAGING.getPageSize());
		return mav;
	}

	@ResponseBody
	@RequestMapping(value = "/table", params = RequestParams.S_ECHO_PARAM, method = RequestMethod.GET)
	public
	DataTableModel getTable(final DataTableDrawParameters params, final Locale locale) {
		LOGGER.trace("getTable called ");

		DataTableSorting sorting = createSorting(params, userMapper);
		Filtering filtering = new DataTableFiltering(params);

		return getTableModel(sorting, filtering, params.getsEcho(), locale);

	}

	private DataTableModel getTableModel(PagingAndSorting sorting, Filtering filtering, String sEcho, Locale locale) {
		PagedCollectionHolder<List<User>> holder = adminService.findAllUsersFiltered(sorting, filtering);

		return new UserDataTableModelBuilder(locale).buildDataModel(holder, sEcho);
	}

	@ResponseBody
	@RequestMapping(value = "/new", method = RequestMethod.POST, params = "password")
	public
	void addUser(@ModelAttribute("add-user") @Valid UserForm userForm) {
		if (!currentProviderFeatures().isManagedPassword()) {
			adminService.addUser(userForm.getUser(), userForm.getGroupId(), userForm.getPassword());

		} else {
			// If this happens, it's either a bug or a forged request
			LOGGER.warn(
					"Received a password while passwords are managed by auth provider. This is either a bug or a forged request. User form : {}",
					ToStringBuilder.reflectionToString(userForm));
			throw new IllegalArgumentException(
					"Received a password while passwords are managed by auth provider. This is either a bug or a forged request.");
		}
	}

	@ResponseBody
	@RequestMapping(value = "/new", method = RequestMethod.POST, params = "noPassword")
	public
	void addUserWithoutCredentials(@ModelAttribute("add-user") @Valid UserForm userForm) {
		if (currentProviderFeatures().isManagedPassword()) {
			adminService.createUserWithoutCredentials(userForm.getUser(), userForm.getGroupId());

		} else {
			// If this happens, it's either a bug or a forged request
			LOGGER.warn(
					"Received no password while passwords are managed by Squash. This is either a bug or a forged request. User form : {}",
					ToStringBuilder.reflectionToString(userForm));
			throw new IllegalArgumentException(
					"Received no password while passwords are managed by Squash. This is either a bug or a forged request.");
		}
	}

	@SuppressWarnings("rawtypes")
	private DataTableSorting createSorting(final DataTableDrawParameters params, final DatatableMapper mapper) {
		return new DataTableSorting(params, mapper);
	}

	@ResponseBody
	@RequestMapping(value = USER_URLS + "/deactivate", method = RequestMethod.POST)
	public
	void deactivateUsers(@PathVariable("userIds") List<Long> userIds) {
		adminService.deactivateUsers(userIds);
	}

	@ResponseBody
	@RequestMapping(value = USER_URLS + "/activate", method = RequestMethod.POST)
	public
	void activateUsers(@PathVariable("userIds") List<Long> userIds) {
		adminService.activateUsers(userIds);
	}

	@ResponseBody
	@RequestMapping(value = USER_URLS, method = RequestMethod.DELETE)
	public
	void deleteUsers(@PathVariable("userIds") List<Long> userIds) {
		adminService.deleteUsers(userIds);
	}

	/**
	 * Will return a view for the user of the given id
	 *
	 * @param userId
	 */
	@RequestMapping(value = USER_URL + "/info", method = RequestMethod.GET)
	public String getUserInfos(@PathVariable(USER_ID) long userId, Model model) {
		User user = adminService.findUserById(userId);
		List<UsersGroup> usersGroupList = adminService.findAllUsersGroupOrderedByQualifiedName();

		List<?> permissionModel = createPermissionTableModel(userId, new DefaultPagingAndSorting(),
				DefaultFiltering.NO_FILTERING, "").getAaData();

		List<PermissionGroupModel> pgm = getPermissionGroupModels();
		List<ProjectModel> pm = getProjectModels(userId);

		model.addAttribute("usersGroupList", usersGroupList);
		model.addAttribute("user", user);
		model.addAttribute("permissionList", pgm);
		model.addAttribute("myprojectList", pm);
		model.addAttribute("permissions", permissionModel);

		return "user-modification.html";
	}

	@ResponseBody
	@RequestMapping(value = USER_URL + "/change-group", method = RequestMethod.POST)
	public
	void changeUserGroup(@PathVariable long userId, @RequestParam long groupId) {
		adminService.setUserGroupAuthority(userId, groupId);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "id=user-login", VALUE }, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String updateLogin(@ModelAttribute @Valid UserLoginForm userLoginform, @PathVariable long userId) {
		String userLogin = userLoginform.getValue();
		adminService.modifyUserLogin(userId, userLogin);
		return HtmlUtils.htmlEscape(userLogin);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "id=user-first-name", VALUE }, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String updateFirstName(@RequestParam(VALUE) String firstName, @PathVariable long userId) {
		adminService.modifyUserFirstName(userId, firstName);
		return HtmlUtils.htmlEscape(firstName);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "id=user-last-name", VALUE }, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String updateLastName(@RequestParam(VALUE) String lastName, @PathVariable long userId) {
		adminService.modifyUserLastName(userId, lastName);
		return HtmlUtils.htmlEscape(lastName);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = { "id=user-email", VALUE }, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String updateEmail(@RequestParam(VALUE) String email, @PathVariable long userId) {
		adminService.modifyUserEmail(userId, email);
		return HtmlUtils.htmlEscape(email);
	}

	@RequestMapping(value = USER_URL, method = RequestMethod.POST, params = "password")
	@ResponseBody
	public void resetPassword(@ModelAttribute @Valid PasswordResetForm form, @PathVariable long userId) {
		LOGGER.trace("Reset password for user #" + userId);
		adminService.resetUserPassword(userId, form.getPassword());
	}

	@RequestMapping(value = USER_URL + "/authentication", method = RequestMethod.PUT, params = "password")
	@ResponseBody
	public void createAuthentication(@ModelAttribute @Valid PasswordResetForm form, @PathVariable long userId) {
		LOGGER.trace("Create authentication for user #" + userId);
		if (!currentProviderFeatures().isManagedPassword()) {
			adminService.createAuthentication(userId, form.getPassword());
		}
		// when password are managed, we should not create internal
		// authentications.
	}

	// *********************************************************************************
	@ResponseBody
	@RequestMapping(value = USER_URL + "/add-permission", method = RequestMethod.POST)
	public
	void addNewPermission(@RequestParam("project") long projectId, @PathVariable long userId,
			@RequestParam String permission) {
		permissionService.addNewPermissionToProject(userId, projectId, permission);
	}

	@ResponseBody
	@RequestMapping(value = USER_URL + "/remove-permission", method = RequestMethod.POST)
	public
	void removePermission(@RequestParam("project") List<Long> projectIds, @PathVariable(USER_ID) long userId) {
		for (Long projectId : projectIds) {
			permissionService.removeProjectPermission(userId, projectId);
		}
	}

	@ResponseBody
	@RequestMapping(value = USER_URL + "/permission-popup", method = RequestMethod.GET)
	public
	Map<String, Object> getPermissionPopup(@PathVariable(USER_ID) long userId) {
		return createPermissionPopupModel(userId);
	}

	@ResponseBody
	@RequestMapping(value = USER_URL + "/permissions", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	public
	DataTableModel getPermissionTableModel(DataTableDrawParameters params, @PathVariable(USER_ID) long userId) {
		PagingAndSorting paging = new DataTableSorting(params, permissionMapper);
		Filtering filtering = new DataTableFiltering(params);
		return createPermissionTableModel(userId, paging, filtering, params.getsEcho());
	}

	private String formatString(String arg, Locale locale) {
		if (arg == null) {
			return formatNoData(locale);
		} else {
			return arg;
		}
	}

	private String formatDate(Date date, Locale locale) {
		return messageSource.localizeDate(date, locale);

	}

	private String formatNoData(Locale locale) {
		return messageSource.noData(locale);
	}

	/**
	 * Builds datatable model for users table
	 */
	private final class UserDataTableModelBuilder extends DataTableModelBuilder<User> {
		/**
		 *
		 */
		private final Locale locale;

		/**
		 * @param locale
		 */
		private UserDataTableModelBuilder(Locale locale) {
			this.locale = locale;
		}

		@Override
		public Map<?, ?> buildItemData(User item) {
			AuditableMixin newP = (AuditableMixin) item;
			String group;

			if (item.getGroup() == null) {
				// just in case there is no group, even though it should not
				// happen. otherwise, breaks user admin page.
				group = "";
			} else {
				group = messageSource.internationalize("user.account.group." + item.getGroup().getQualifiedName()
						+ ".label", locale);
				if (group == null) {
					group = item.getGroup().getSimpleName();
				}
			}

			Map<Object, Object> result = new HashMap<>();
			result.put("user-id", item.getId());
			result.put("user-active", item.getActive());
			result.put("user-index", getCurrentIndex());
			result.put("user-login", item.getLogin());
			result.put("user-group", group);
			result.put("user-firstname", item.getFirstName());
			result.put("user-lastname", item.getLastName());
			result.put("user-email", item.getEmail());
			// Could be done with a SimpleDateFormat but substring works very well.
			result.put("user-created-on", formatDate(newP.getCreatedOn(), locale).substring(0, 10));
			result.put("user-created-by", formatString(newP.getCreatedBy(), locale));
			result.put("user-modified-on", formatDate(newP.getLastModifiedOn(), locale));
			result.put("user-modified-by", formatString(newP.getLastModifiedBy(), locale));
			// Feature 6763 - Add 'last connected on' column
			result.put("user-connected-on", formatDate(item.getLastConnectedOn(), locale));
			result.put("empty-delete-holder", null);

			return result;
		}
	}

	@ModelAttribute("authenticationProvider")
	public AuthenticationProviderFeatures getAuthenticationProviderModelAttribute() {
		return currentProviderFeatures();
	}

	private AuthenticationProviderFeatures currentProviderFeatures() {
		return authenticationProviderContext.getCurrentProviderFeatures();
	}

}
