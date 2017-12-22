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
package org.squashtest.tm.service.internal.user;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.AdministrationStatistics;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.domain.users.UsersGroup;
import org.squashtest.tm.exception.user.ActiveUserDeleteException;
import org.squashtest.tm.exception.user.ChartOwnerDeleteException;
import org.squashtest.tm.exception.user.LoginAlreadyExistsException;
import org.squashtest.tm.exception.user.MilestoneOwnerDeleteException;
import org.squashtest.tm.security.UserContextHolder;
import org.squashtest.tm.service.chart.ChartModificationService;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.internal.repository.AdministrationDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TeamDao;
import org.squashtest.tm.service.internal.repository.UserDao;
import org.squashtest.tm.service.internal.repository.UsersGroupDao;
import org.squashtest.tm.service.internal.security.UserBuilder;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.security.AdministratorAuthenticationService;
import org.squashtest.tm.service.security.acls.model.ObjectAclService;
import org.squashtest.tm.service.user.AdministrationService;
import org.squashtest.tm.service.user.AuthenticatedUser;
import org.squashtest.tm.service.user.UserAccountService;

/**
 *
 * @author bsiri
 *
 */
@Service("squashtest.tm.service.AdministrationService")
@Transactional
public class AdministrationServiceImpl implements AdministrationService {

	@Inject
	private UserAccountService userAccountService;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private UserDao userDao;

	@Inject
	private UsersGroupDao groupDao;

	@Inject
	private AdministrationDao adminDao;

	@Inject
	private ConfigurationService configurationService;

	@Inject
	private TeamDao teamDao;

	@Inject
	private ObjectAclService aclService;

	@Inject
	private AdministratorAuthenticationService adminAuthentService;

	@Inject
	private MilestoneManagerService milestoneManagerService;

	@Inject
	private ChartModificationService chartModificationService;

	@Inject private FeatureManager features;

	@PersistenceContext
	private EntityManager em;

	private static final String WELCOME_MESSAGE_KEY = "WELCOME_MESSAGE";
	private static final String LOGIN_MESSAGE_KEY = "LOGIN_MESSAGE";
	private static final String REQUIREMENT_INDEXING_DATE_KEY = "lastindexing.requirement.date";
	private static final String TESTCASE_INDEXING_DATE_KEY = "lastindexing.testcase.date";
	private static final String CAMPAIGN_INDEXING_DATE_KEY = "lastindexing.cimgNameampaign.date";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");

	public void setAdministratorAuthenticationService(AdministratorAuthenticationService adminService) {
		this.adminAuthentService = adminService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	/* **************** delegate user section, so is security ************ */

	@Override
	public void modifyUserFirstName(long userId, String newName) {
		userAccountService.modifyUserFirstName(userId, newName);
	}

	@Override
	public void modifyUserLastName(long userId, String newName) {
		userAccountService.modifyUserLastName(userId, newName);
	}

	@Override
	public void modifyUserLogin(long userId, String newLogin) {
		userAccountService.modifyUserLogin(userId, newLogin);
	}

	@Override
	public void modifyUserEmail(long userId, String newEmail) {
		userAccountService.modifyUserEmail(userId, newEmail);
	}

	/* ********************** proper admin section ******************* */

	private static final String IS_ADMIN_OR_MANAGER = "hasRole('ROLE_TM_PROJECT_MANAGER')" + OR_HAS_ROLE_ADMIN;

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public AuthenticatedUser findUserById(long userId) {
		User user = userDao.findOne(userId);
		boolean hasAuth = adminAuthentService.userExists(user.getLogin());
		return new AuthenticatedUser(user, hasAuth);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<User> findAllUsersOrderedByLogin() {
		return userDao.findAllUsersOrderedByLogin();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<User> findAllActiveUsersOrderedByLogin() {
		return userDao.findAllActiveUsersOrderedByLogin();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<User>> findAllUsersFiltered(PagingAndSorting sorter, Filtering filter) {
		List<User> list = userDao.findAllUsers(sorter, filter);
		long count = userDao.findAll().size();
		return new PagingBackedPagedCollectionHolder<>(sorter, count, list);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<UsersGroup> findAllUsersGroupOrderedByQualifiedName() {
		return groupDao.findAllGroupsOrderedByQualifiedName();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void addUser(User user, long groupId, String password) {
		// FIXME : check the auth login is available when time has come
		createUserWithoutCredentials(user, groupId);
		adminAuthentService.createNewUserPassword(user.getLogin(), password, user.getActive(), true, true, true,
				new ArrayList<GrantedAuthority>());
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void setUserGroupAuthority(long userId, long groupId) {
		UsersGroup group = groupDao.findOne(groupId);
		User user = userDao.findOne(userId);
		user.setGroup(group);
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deactivateUser(long userId) {
		User user = userDao.findOne(userId);
		checkActiveUser(user);
		userAccountService.deactivateUser(userId);
		adminAuthentService.deactivateAccount(user.getLogin());
		aclService.refreshAcls();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void activateUser(long userId) {
		userAccountService.activateUser(userId);
		User user = userDao.findOne(userId);
		adminAuthentService.activateAccount(user.getLogin());
		aclService.refreshAcls();
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deactivateUsers(Collection<Long> userIds) {
		for (Long id : userIds) {
			deactivateUser(id);
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void activateUsers(Collection<Long> userIds) {
		for (Long id : userIds) {
			activateUser(id);
		}
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deleteUsers(Collection<Long> userIds) {

		checkUsersOwnMilestones(userIds);
		checkUsersOwnCharts(userIds);

		for (Long id : userIds) {
			User user = userDao.findOne(id);
			checkActiveUser(user);
			userAccountService.deleteUser(id);
			adminAuthentService.deleteAccount(user.getLogin());
			userDao.delete(user);
		}
		aclService.refreshAcls();
	}

	private void checkUsersOwnCharts(Collection<Long> userIds) {
		if (chartModificationService.hasChart(new ArrayList<>(userIds))){
			throw new ChartOwnerDeleteException();
		}

	}

	private void checkUsersOwnMilestones(Collection<Long> userIds) {
		if (milestoneManagerService.hasMilestone(new ArrayList<>(userIds))) {
			throw new MilestoneOwnerDeleteException();
		}
	}

	private void checkActiveUser(User user) {
		String activeUserName = UserContextHolder.getUsername();
		if (user.getLogin().equals(activeUserName)) {
			throw new ActiveUserDeleteException();
		}
	}

	@Override
	public List<Project> findAllProjects() {
		return projectDao.findAll();
	}

	@Override
	public void modifyWelcomeMessage(String welcomeMessage) {
		if (configurationService.findConfiguration(WELCOME_MESSAGE_KEY) == null) {
			configurationService.createNewConfiguration(WELCOME_MESSAGE_KEY, welcomeMessage);
			return;
		}
		configurationService.updateConfiguration(WELCOME_MESSAGE_KEY, welcomeMessage);
	}

	@Override
	public void modifyLoginMessage(String loginMessage) {
		if (configurationService.findConfiguration(LOGIN_MESSAGE_KEY) == null) {
			configurationService.createNewConfiguration(LOGIN_MESSAGE_KEY, loginMessage);
			return;
		}
		configurationService.updateConfiguration(LOGIN_MESSAGE_KEY, loginMessage);
	}

	@Override
	public String findWelcomeMessage() {
		return configurationService.findConfiguration(WELCOME_MESSAGE_KEY);
	}

	@Override
	public String findLoginMessage() {
		return configurationService.findConfiguration(LOGIN_MESSAGE_KEY);
	}

	private Date findRequirementIndexingDate() {
		String date = configurationService.findConfiguration(REQUIREMENT_INDEXING_DATE_KEY);
		Date result = null;
		if (date != null) {
			try {
				result = dateFormat.parse(date);
			} catch (ParseException e) {

			}
		}
		return result;
	}

	private Date findTestCaseIndexingDate() {
		String date = configurationService.findConfiguration(TESTCASE_INDEXING_DATE_KEY);
		Date result = null;
		if (date != null) {
			try {
				result = dateFormat.parse(date);
			} catch (ParseException e) {

			}
		}
		return result;
	}

	private Date findCampaignIndexingDate() {
		String date = configurationService.findConfiguration(CAMPAIGN_INDEXING_DATE_KEY);
		Date result = null;
		if (date != null) {
			try {
				result = dateFormat.parse(date);
			} catch (ParseException e) {

			}
		}
		return result;
	}

	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void resetUserPassword(long userId, String newPassword) {
		User user = userDao.findOne(userId);
		adminAuthentService.resetUserPassword(user.getLogin(), newPassword);
	}

	/**
	 * @see AdministrationService#findAdministrationStatistics()
	 */
	@Override
	public AdministrationStatistics findAdministrationStatistics() {
		AdministrationStatistics statistics = adminDao.findAdministrationStatistics();
		statistics.setRequirementIndexingDate(findRequirementIndexingDate());
		statistics.setTestcaseIndexingDate(findTestCaseIndexingDate());
		statistics.setCampaignIndexingDate(findCampaignIndexingDate());
		return statistics;
	}

	/**
	 * @see AdministrationService#deassociateTeams(long, List)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void deassociateTeams(long userId, List<Long> teamIds) {
		User user = userDao.findOne(userId);
		user.removeTeams(teamIds);
		aclService.updateDerivedPermissions(userId);
	}

	/**
	 * @see AdministrationService#associateToTeams(long, List)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public void associateToTeams(long userId, List<Long> teamIds) {
		User user = userDao.findOne(userId);
		List<Team> teams = teamDao.findAll(teamIds);
		for (Team team : teams) {
			team.addMember(user);
			user.addTeam(team);
		}

		aclService.updateDerivedPermissions(userId);
	}

	/**
	 * @see AdministrationService#findSortedAssociatedTeams(long,
	 *      PagingAndSorting, Filtering)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<Team>> findSortedAssociatedTeams(long userId, PagingAndSorting paging,
			Filtering filtering) {
		List<Team> associatedTeams = teamDao.findSortedAssociatedTeams(userId, paging, filtering);
		long associatedTeamsTotal = teamDao.countAssociatedTeams(userId);
		return new PagingBackedPagedCollectionHolder<>(paging, associatedTeamsTotal, associatedTeams);

	}

	/**
	 * @see AdministrationService#findAllNonAssociatedTeams(long)
	 */
	@Override
	@PreAuthorize(HAS_ROLE_ADMIN)
	public List<Team> findAllNonAssociatedTeams(long userId) {
		return teamDao.findAllNonAssociatedTeams(userId);
	}

	/**
	 * This is not secured on purpose.
	 *
	 * @see org.squashtest.tm.service.user.AdministrationService#findByLogin(java.lang.String)
	 */
	@Override
	public User findByLogin(String login) {
		return userDao.findUserByLogin(login);
	}

	/**
	 * @see org.squashtest.tm.service.user.AdministrationService#createUserFromLogin(java.lang.String)
	 */
	@Override
	public User createUserFromLogin(@NotNull String login) throws LoginAlreadyExistsException {
		String loginTrim = login.trim();
		checkLoginAvailability(loginTrim);

		User user = User.createFromLogin(loginTrim);
		UsersGroup defaultGroup = groupDao.findByQualifiedName(UsersGroup.USER);
		user.setGroup(defaultGroup);

		userDao.save(user);
		return user;
	}

	/**
	 * @see org.squashtest.tm.service.user.AdministrationService#createUserWithoutCredentials(org.squashtest.tm.domain.users.User,
	 *      long)
	 */
	@Override
	public void createUserWithoutCredentials(User user, long groupId) {
		checkLoginAvailability(user.getLogin());

		UsersGroup group = groupDao.findOne(groupId);
		user.setGroup(group);

		userDao.save(user);
	}

	/**
	 * @see org.squashtest.tm.service.user.AdministrationService#createAuthentication(long,
	 *      java.lang.String)
	 */
	@Override
	public void createAuthentication(long userId, String password) throws LoginAlreadyExistsException {
		User user = userDao.findOne(userId);

		if (!adminAuthentService.userExists(user.getLogin())) {
			UserDetails auth = UserBuilder.forUser(user.getLogin()).password(password).active(user.getActive()).build();
			adminAuthentService.createUser(auth);

		} else {
			throw new LoginAlreadyExistsException("Authentication data for user '" + user.getLogin()
					+ "' already exists");
		}

	}

	@Override
	@PreAuthorize(IS_ADMIN_OR_MANAGER)
	public List<User> findAllAdminOrManager() {
		return userDao.findAllAdminOrManager();
	}

	/**
	 * @see org.squashtest.tm.service.user.UserManagerService#createAdministrator(org.squashtest.tm.domain.users.User,
	 *      java.lang.String)
	 */
	@Override
	public User createAdministrator(User user, String password) throws LoginAlreadyExistsException {
		UsersGroup admin = groupDao.findByQualifiedName(UsersGroup.ADMIN);
		user.normalize();
		addUser(user, admin.getId(), password);

		return user;
	}

	@Override
	public void checkLoginAvailability(String login) {
		boolean caseInsensitive = features.isEnabled(Feature.CASE_INSENSITIVE_LOGIN);

		if (caseInsensitive && userDao.findUserByCiLogin(login) != null || !caseInsensitive && userDao.findUserByLogin(login) != null) {
			throw new LoginAlreadyExistsException("User " + login + " cannot be created because it already exists");
		}
	}

	/**
	 * @see org.squashtest.tm.service.user.UserManagerService#findAllDuplicateLogins()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllDuplicateLogins() {
		return em.unwrap(Session.class).getNamedQuery("User.findAllDuplicateLogins").list();
	}

	/**
	 * @see org.squashtest.tm.service.user.UserManagerService#findCaseAwareLogin(java.lang.String)
	 */
	@Override
	public String findCaseAwareLogin(String login) {
		Query query = em.unwrap(Session.class).getNamedQuery("User.findCaseAwareLogin");
		query.setParameter("login", login);
		return (String) query.uniqueResult();
	}



}
