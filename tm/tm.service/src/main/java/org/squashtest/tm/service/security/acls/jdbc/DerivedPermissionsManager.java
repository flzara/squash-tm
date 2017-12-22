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
package org.squashtest.tm.service.security.acls.jdbc;

import java.math.BigInteger;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.security.acls.CustomPermission;

@Service
@Transactional
class DerivedPermissionsManager {

	private static final String PROJECT_CLASS_NAME = Project.class.getName();

	private static final String PERM_MANAGEMENT = Integer.toString(CustomPermission.MANAGEMENT.getMask());


	private static final String REMOVE_CORE_PARTY_MANAGER_AUTHORITY = "delete from CORE_PARTY_AUTHORITY where PARTY_ID in (:ids) and AUTHORITY = 'ROLE_TM_PROJECT_MANAGER'";
	private static final String INSERT_CORE_PARTY_MANAGER_AUTHORITY = "insert into CORE_PARTY_AUTHORITY(PARTY_ID, AUTHORITY) values (:id, 'ROLE_TM_PROJECT_MANAGER')";


	private static final String CHECK_OBJECT_IDENTITY_EXISTENCE =
		"select aoi.ID from ACL_OBJECT_IDENTITY aoi " +
			"inner join ACL_CLASS acc on acc.ID = aoi.CLASS_ID " +
			"where aoi.IDENTITY = :id and acc.CLASSNAME = :class";

	private static final String CHECK_PARTY_EXISTENCE = "select PARTY_ID from CORE_PARTY where PARTY_ID = :id";

	private static final String FIND_ALL_USERS = "select PARTY_ID from CORE_USER";

	private static final String FIND_TEAM_MEMBERS_OR_USER =
		"select cu.PARTY_ID from CORE_USER cu " +
			"where cu.PARTY_ID = :id " +
			"UNION " +
			"select cu.PARTY_ID from CORE_USER cu " +
			"inner join CORE_TEAM_MEMBER ctm on ctm.USER_ID = cu.PARTY_ID " +
			"inner join CORE_TEAM ct on ct.PARTY_ID = ctm.TEAM_ID " +
			"where ct.PARTY_ID = :id";


	private static final String FIND_PARTIES_USING_IDENTITY =
		"select arse.PARTY_ID from ACL_RESPONSIBILITY_SCOPE_ENTRY arse " +
			"inner join ACL_OBJECT_IDENTITY aoi on arse.OBJECT_IDENTITY_ID = aoi.ID " +
			"inner join ACL_CLASS acc on aoi.CLASS_ID = acc.ID " +
			"inner join ACL_GROUP_PERMISSION acp on acp.ACL_GROUP_ID = arse.ACL_GROUP_ID " +
			"where acp.CLASS_ID = acc.ID " +
			"and aoi.IDENTITY = :id and acc.CLASSNAME = :class ";


	private static final String RETAIN_USERS_MANAGING_ANYTHING =
		"select arse.PARTY_ID from ACL_RESPONSIBILITY_SCOPE_ENTRY arse " +
			"inner join ACL_OBJECT_IDENTITY aoi on arse.OBJECT_IDENTITY_ID = aoi.ID " +
			"inner join ACL_CLASS acc on aoi.CLASS_ID = acc.ID " +
			"inner join ACL_GROUP_PERMISSION acp on acp.ACL_GROUP_ID = arse.ACL_GROUP_ID " +
			"where acp.CLASS_ID = acc.ID and acp.PERMISSION_MASK = " + PERM_MANAGEMENT + " " +
			"and acc.CLASSNAME in ('org.squashtest.tm.domain.project.Project', 'org.squashtest.tm.domain.project.ProjectTemplate') " +
			"and arse.PARTY_ID in (:ids)";


	private static final String RETAIN_MEMBERS_OF_TEAMS_MANAGING_ANYTHING =
		"select cu.PARTY_ID from CORE_USER cu " +
			"inner join CORE_TEAM_MEMBER ctm on ctm.USER_ID = cu.PARTY_ID " +
			"inner join ACL_RESPONSIBILITY_SCOPE_ENTRY arse on arse.PARTY_ID = ctm.TEAM_ID " +
			"inner join ACL_OBJECT_IDENTITY aoi on arse.OBJECT_IDENTITY_ID = aoi.ID " +
			"inner join ACL_CLASS acc on aoi.CLASS_ID = acc.ID " +
			"inner join ACL_GROUP_PERMISSION acp on acp.ACL_GROUP_ID = arse.ACL_GROUP_ID " +
			"where acp.CLASS_ID = acc.ID and acp.PERMISSION_MASK = " + PERM_MANAGEMENT + " " +
			"and acc.CLASSNAME in ('org.squashtest.tm.domain.project.Project', 'org.squashtest.tm.domain.project.ProjectTemplate') " +
			"and cu.PARTY_ID in (:ids)";

	@PersistenceContext
	private EntityManager em;


	void updateDerivedPermissions(ObjectIdentity identity) {
		updateDerivedAuths(identity);
	}

	void updateDerivedPermissions(long partyId) {
		updateDerivedAuths(partyId);
	}


	void updateDerivedPermissions(long partyId, ObjectIdentity identity) {

		// as for now we don't have a use for the identity argument yet. That might change later though.
		//updateDerivedAcl(partyId);
		updateDerivedAuths(partyId);
	}


	// *************************** private ******************************


	private void updateDerivedAuths(ObjectIdentity identity) {

		flush();

		if (!isSortOfProject(identity)) {
			return;
		}

		if (doesExist(identity)) {
			Collection<Long> userIds = findUsers(identity);
			updateAuthsForThoseUsers(userIds);

		} else {
			// corner case : the target object doesn't exist anymore so we can't find
			// which users were using it. We must then update them all.
			updateDerivedAuths();
		}

	}

	private void updateDerivedAuths(long partyId) {

		flush();

		if (doesExist(partyId)) {
			Collection<Long> memberIds = findMembers(partyId);
			updateAuthsForThoseUsers(memberIds);
		} else {
			// corner case : the target party doesn't exist anymore so we don't know
			// which team members were part of it (in case of a team). We must then update
			// all the users.
			updateDerivedAuths();
		}

	}

	// will update all users, no exceptions
	private void updateDerivedAuths() {

		flush();

		Collection<Long> allUsers = findAllUsers();

		updateAuthsForThoseUsers(allUsers);
	}


	private void updateAuthsForThoseUsers(Collection<Long> userIds) {

		removeProjectManagerAuthorities(userIds);

		Collection<Long> managerIds = retainUsersManagingAnything(userIds);

		grantProjectManagerAuthorities(managerIds);
	}


	// ******************************** helpers ***********************************

	/*
	 *  will help to cut some uneeded computations and DB calls. For now only project-level permissions induces derived permissions.
	 *  also, remember that : DONT BE SHY and modify/remove it if the permission management specs ever changes, instead of working around !
	 */
	private boolean isSortOfProject(ObjectIdentity identity) {
		String type = identity.getType();
		return type.equals(PROJECT_CLASS_NAME);
	}

	private boolean doesExist(ObjectIdentity identity) {

		Query query = em.createNativeQuery(CHECK_OBJECT_IDENTITY_EXISTENCE);
		query.setParameter("id", identity.getIdentifier());
		query.setParameter("class", identity.getType());

		List<?> result = query.getResultList();
		return !result.isEmpty();
	}


	private boolean doesExist(long partyId) {

		Query query = em.createNativeQuery(CHECK_PARTY_EXISTENCE);
		query.setParameter("id", partyId);

		List<?> result = query.getResultList();
		return !result.isEmpty();
	}


	// will find all members of a team given its id. It the id actually refers to a user, that user id will be the only result.
	private Collection<Long> findMembers(long partyId) {

		Query query = em.createNativeQuery(FIND_TEAM_MEMBERS_OR_USER);
		query.setParameter("id", partyId);
		return executeRequestAndConvertIds(query);
	}

	// will find all the users
	private Collection<Long> findUsers(ObjectIdentity identity) {

		// first find the parties managing that thing
		Query query = em.createNativeQuery(FIND_PARTIES_USING_IDENTITY);
		query.setParameter("id", identity.getIdentifier());
		query.setParameter("class", identity.getType());

		Collection<Long> partyIds = executeRequestAndConvertIds(query);

		// then find the corresponding users
		Collection<Long> userIds = new HashSet<>();
		for (Long id : partyIds) {
			userIds.addAll(findMembers(id));
		}

		return userIds;

	}

	private Collection<Long> findAllUsers() {
		Query query = em.createNativeQuery(FIND_ALL_USERS);
		return executeRequestAndConvertIds(query);
	}

	private void removeProjectManagerAuthorities(Collection<Long> ids) {
		if (!ids.isEmpty()) {
			Query query = em.createNativeQuery(REMOVE_CORE_PARTY_MANAGER_AUTHORITY);
			query.setParameter("ids", ids);
			query.executeUpdate();
		}
	}

	private Collection<Long> retainUsersManagingAnything(Collection<Long> ids) {
		if (!ids.isEmpty()) {

			Set<Long> userIds = new HashSet<>();
			Collection<Long> buffer;

			// first, get users directly managing anything
			Query query = em.createNativeQuery(RETAIN_USERS_MANAGING_ANYTHING);
			query.setParameter("ids", ids);


			buffer = executeRequestAndConvertIds(query);
			userIds.addAll(buffer);

			// second, get users managing through teams or project leaders (which sounds quite silly I agree)
			query = em.createNativeQuery(RETAIN_MEMBERS_OF_TEAMS_MANAGING_ANYTHING);
			query.setParameter("ids", ids);

			buffer = executeRequestAndConvertIds(query);
			userIds.addAll(buffer);

			return userIds;
		} else {
			return Collections.emptyList();
		}
	}

	private void grantProjectManagerAuthorities(Collection<Long> ids) {
		Query query;
		for (Long id : ids) {
			query = em.createNativeQuery(INSERT_CORE_PARTY_MANAGER_AUTHORITY);
			query.setParameter("id", id);
			query.executeUpdate();
		}


	}

	private void flush() {
		em.flush();
	}

	private List<Long> executeRequestAndConvertIds(Query query){
		List<BigInteger> bigIntIds = query.getResultList();
		List<Long> longsIds = new ArrayList<>();
		for (BigInteger bigIntId : bigIntIds) {
			longsIds.add(bigIntId.longValue());
		}
		return longsIds;
	}

}
