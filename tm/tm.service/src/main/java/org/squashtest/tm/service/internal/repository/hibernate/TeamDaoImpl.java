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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.jooq.DSLContext;
import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.Team;
import org.squashtest.tm.domain.users.User;
import org.squashtest.tm.service.internal.foundation.collection.JpaPagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomTeamDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.squashtest.tm.jooq.domain.Tables.CORE_TEAM;
import static org.squashtest.tm.jooq.domain.Tables.CORE_TEAM_MEMBER;

public class TeamDaoImpl implements CustomTeamDao {

	private static final String HQL_FIND_TEAMS_BASE = "from Team Team ";
	private static final String HQL_FIND_TEAMS_FILTER = "where Team.name like :filter or Team.audit.createdBy like :filter or Team.audit.lastModifiedBy like :filter ";

	@PersistenceContext
	private EntityManager entityManager;

	@Inject
	private DSLContext DSL;

	@Override
	public List<Team> findSortedTeams(PagingAndSorting paging, Filtering filter) {

		StringBuilder sQuery = new StringBuilder(HQL_FIND_TEAMS_BASE);

		if (filter.isDefined()) {
			sQuery.append(HQL_FIND_TEAMS_FILTER);
		}

		SortingUtils.addOrder(sQuery, paging);

		Query hQuery = entityManager.createQuery(sQuery.toString());

		if (filter.isDefined()) {
			hQuery.setParameter("filter", "%" + filter.getFilter() + "%");
		}

		JpaPagingUtils.addPaging(hQuery, paging);

		return hQuery.getResultList();

	}

	/**
	 * @see CustomTeamDao#findSortedAssociatedTeams(long, PagingAndSorting, Filtering)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Team> findSortedAssociatedTeams(long userId, PagingAndSorting paging, Filtering filtering) {


		Criteria crit = entityManager.unwrap(Session.class)
			.createCriteria(User.class, "User")
			.add(Restrictions.eq("User.id", userId))
			.createCriteria("User.teams", "Team")
			.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);

		/* add ordering */
		String sortedAttribute = paging.getSortedAttribute();
		if (sortedAttribute != null) {
			SortingUtils.addOrder(crit, paging);
		}

		/* add filtering */
		if (filtering.isDefined()) {
			crit = crit.add(filterAssociatedTeams(filtering));
		}

		/* result range */
		PagingUtils.addPaging(crit, paging);

		return collectFromMapList(crit.list(), "Team");
	}

	private Criterion filterAssociatedTeams(Filtering filtering) {
		String filter = filtering.getFilter();
		return Restrictions.disjunction().add(Restrictions.like("Team.name", filter, MatchMode.ANYWHERE));
	}

	private <X> List<X> collectFromMapList(List<X> hibernateResult, String alias){
		List<X> collected = new ArrayList<>(hibernateResult.size());
		for (Map<String, X> result : (List<Map<String, X>>) hibernateResult){
			collected.add(result.get(alias));
}
		return collected;
	}

	@Override
	public List<Long> findTeamIds(Long userId) {
		return DSL.select(CORE_TEAM.PARTY_ID)
			.from(CORE_TEAM)
			.join(CORE_TEAM_MEMBER).on(CORE_TEAM_MEMBER.TEAM_ID.eq(CORE_TEAM.PARTY_ID))
			.where(CORE_TEAM_MEMBER.USER_ID.eq(userId))
			.fetch(CORE_TEAM.PARTY_ID, Long.class);
	}

}
