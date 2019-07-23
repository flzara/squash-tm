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
package org.squashtest.tm.service.internal.repository.hibernate;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jooq.DSLContext;
import org.jooq.Name;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.users.QUser;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.jooq.domain.tables.AclClass;
import org.squashtest.tm.jooq.domain.tables.AclObjectIdentity;
import org.squashtest.tm.jooq.domain.tables.AclResponsibilityScopeEntry;
import org.squashtest.tm.jooq.domain.tables.CoreGroupAuthority;
import org.squashtest.tm.jooq.domain.tables.CoreGroupMember;
import org.squashtest.tm.jooq.domain.tables.CoreTeamMember;
import org.squashtest.tm.jooq.domain.tables.CoreUser;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomUserDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.countDistinct;
import static org.squashtest.tm.api.security.acls.Roles.ROLE_ADMIN;
import static org.squashtest.tm.api.security.acls.Roles.ROLE_TA_API_CLIENT;
import static org.squashtest.tm.api.security.acls.Roles.ROLE_TM_PROJECT_MANAGER;
import static org.squashtest.tm.api.security.acls.Roles.ROLE_TM_USER;
import static org.squashtest.tm.jooq.domain.Tables.ACL_CLASS;
import static org.squashtest.tm.jooq.domain.Tables.ACL_OBJECT_IDENTITY;
import static org.squashtest.tm.jooq.domain.Tables.ACL_RESPONSIBILITY_SCOPE_ENTRY;
import static org.squashtest.tm.jooq.domain.Tables.CORE_GROUP_AUTHORITY;
import static org.squashtest.tm.jooq.domain.Tables.CORE_GROUP_MEMBER;
import static org.squashtest.tm.jooq.domain.Tables.CORE_PARTY;
import static org.squashtest.tm.jooq.domain.Tables.CORE_TEAM_MEMBER;
import static org.squashtest.tm.jooq.domain.Tables.CORE_USER;

public class UserDaoImpl implements CustomUserDao {

	private static final String FIND_ALL_MANAGER_AND_ADMIN =
			"SELECT  member.PARTY_ID FROM  CORE_GROUP_MEMBER member "
			+ "inner join CORE_GROUP_AUTHORITY cga on cga.GROUP_ID=member.GROUP_ID "
			+ "WHERE cga.AUTHORITY = '" + ROLE_ADMIN + "' "
			+ "UNION "
			+ "Select auth.PARTY_ID From  CORE_PARTY_AUTHORITY auth where auth.AUTHORITY = '" + ROLE_TM_PROJECT_MANAGER + "'";

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private DSLContext DSL;

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAllUsers(PagingAndSorting sorter, Filtering filter) {

		User example = new User();
		example.setActive(true);

		String sortedAttribute = sorter.getSortedAttribute();
		SortOrder order = sorter.getSortOrder();


		Criteria crit = entityManager.unwrap(Session.class).createCriteria(User.class, "User");


		/* create the query with respect to the filtering */
		if (filter.isDefined()) {
			crit = crit.add(filterUsers(filter));
		}

		/* add ordering */
		if (sortedAttribute != null) {
			if (order == SortOrder.ASCENDING) {
				crit.addOrder(Order.asc(sortedAttribute).ignoreCase());
			} else {
				crit.addOrder(Order.desc(sortedAttribute).ignoreCase());
			}
		}


		/* result range */
		crit.setFirstResult(sorter.getFirstItemIndex());
		crit.setMaxResults(sorter.getPageSize());

		return crit.list();

	}


	private Criterion filterUsers(Filtering oFilter) {

		String filter = oFilter.getFilter();
		return Restrictions.disjunction()
			.add(Restrictions.ilike("login", filter, MatchMode.ANYWHERE))
			.add(Restrictions.ilike("firstName", filter, MatchMode.ANYWHERE))
			.add(Restrictions.ilike("lastName", filter, MatchMode.ANYWHERE))
			.add(Restrictions.ilike("email", filter, MatchMode.ANYWHERE))
			.add(Restrictions.ilike("audit.createdBy", filter, MatchMode.ANYWHERE))
			.add(Restrictions.ilike("audit.lastModifiedBy", filter, MatchMode.ANYWHERE));


	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAllTeamMembers(long teamId, PagingAndSorting paging,
		Filtering filtering) {
		Criteria crit = entityManager.unwrap(Session.class).createCriteria(Team.class, "Team")
			.add(Restrictions.eq("Team.id", teamId))
			.createCriteria("Team.members", "User")
			.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);

		/* add ordering */
		String sortedAttribute = paging.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, paging);
		}

		/* add filtering */
		if (filtering.isDefined()) {
			crit = crit.add(filterMembers(filtering));
		}

		/* result range */
		PagingUtils.addPaging(crit, paging);

		List<Map<String, User>> res = crit.list();
		List<User> collected = new ArrayList<>(res.size());
		for (Map<String, User> result : res) {
			collected.add(result.get("User"));
		}
		return collected;

	}

	private Criterion filterMembers(Filtering filtering) {
		String filter = filtering.getFilter();
		return Restrictions.disjunction()
			.add(Restrictions.like("User.firstName", filter, MatchMode.ANYWHERE))
			.add(Restrictions.like("User.lastName", filter, MatchMode.ANYWHERE))
			.add(Restrictions.like("User.login", filter, MatchMode.ANYWHERE));
	}


	// **************** private code ****************************

	@SuppressWarnings("unchecked")
	@Override
	public List<User> findAllAdminOrManager() {
		Query query = entityManager.unwrap(Session.class).createSQLQuery(FIND_ALL_MANAGER_AND_ADMIN);
		query.setResultTransformer(new SqLIdResultTransformer());
		List<Long> ids = query.list();

		return new JPAQueryFactory(entityManager)
			.selectFrom(QUser.user)
			.where(QUser.user.id.in(ids))
			.fetch();
	}

	@Override
	public Long findUserId(String login) {
		return DSL
			.select(CORE_USER.PARTY_ID)
			.from(CORE_USER)
			.where(CORE_USER.LOGIN.eq(login))
			.fetchOne(CORE_USER.PARTY_ID);
	}

	@Override
	public int countAllActiveUsersAssignedToAtLeastOneProject(){

		CoreUser coreUser = CORE_USER.as("coreUser");
		AclObjectIdentity objId = ACL_OBJECT_IDENTITY.as("objId");
		AclClass aclClass = ACL_CLASS.as("aclClass");
		AclResponsibilityScopeEntry responsibility = ACL_RESPONSIBILITY_SCOPE_ENTRY.as("responsibility");
		CoreGroupMember groupMembers = CORE_GROUP_MEMBER.as("groupMember");
		CoreGroupAuthority authorities = CORE_GROUP_AUTHORITY.as("groupAuthority");
		CoreTeamMember teamMembers = CORE_TEAM_MEMBER.as("teamMembers");


		return DSL.select(countDistinct(coreUser.PARTY_ID).as("userCount"))

				   // 1. first retrieve all ACL entries on either project or project_template
				   // we also left-join with the team membership table, that will be used in
				   // the where condition below
				   .from(aclClass)

				   .innerJoin(objId).on(aclClass.ID.eq(objId.CLASS_ID)
											.and(aclClass.CLASSNAME.in("org.squashtest.tm.domain.project.Project", "org.squashtest.tm.domain.project.ProjectTemplate")))

				   .innerJoin(responsibility).on(responsibility.OBJECT_IDENTITY_ID.eq(objId.ID))

				   .leftJoin(teamMembers).on(responsibility.PARTY_ID.eq(teamMembers.TEAM_ID))

				   // 2. now we retrieve all users that are either admin or regular TM users
				   .crossJoin(coreUser)
				   .innerJoin(groupMembers).on(coreUser.PARTY_ID.eq(groupMembers.PARTY_ID))

				   .innerJoin(authorities).on(groupMembers.GROUP_ID.eq(authorities.GROUP_ID)
												  .and(authorities.AUTHORITY.in(ROLE_ADMIN, ROLE_TM_USER)))

				   // 3. finally we filter on active users only, that have responsibilities
				   // either directly or as a member of a team
				   .where(coreUser.ACTIVE.eq(true))
				   .and(coreUser.PARTY_ID.eq(responsibility.PARTY_ID)
							.or(coreUser.PARTY_ID.eq(teamMembers.USER_ID)))

				   .fetchOne().get("userCount", Integer.class);


	}

}
