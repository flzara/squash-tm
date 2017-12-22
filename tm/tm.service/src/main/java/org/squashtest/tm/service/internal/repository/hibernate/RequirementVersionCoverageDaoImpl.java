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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.SortOrder;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomRequirementVersionCoverageDao;

public class RequirementVersionCoverageDaoImpl extends HibernateEntityDao<RequirementVersionCoverage> implements
CustomRequirementVersionCoverageDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersionCoverage> findAllByTestCaseId(long testCaseId, PagingAndSorting pas) {

		// we have to fetch our query and modify the hql a bit, hence the weird operation below
		Query namedquery = currentSession().getNamedQuery("RequirementVersionCoverage.findAllByTestCaseId");
		String hql = namedquery.getQueryString();
		hql = SortingUtils.addOrder(hql, pas);

		Query q = currentSession().createQuery(hql);
		if(!pas.shouldDisplayAll()){
			PagingUtils.addPaging(q, pas);
		}

		q.setParameter("testCaseId", testCaseId);

		List<Object[]> raw = q.list();

		// now we have to collect from the result set the only thing
		// we want : the coverages
		List<RequirementVersionCoverage> res = new ArrayList<>(raw.size());
		for (Object[] tuple : raw){
			res.add((RequirementVersionCoverage)tuple[0]);
		}

		return res;
	}




	@SuppressWarnings("unchecked")
	@Override
	public List<RequirementVersion> findDistinctRequirementVersionsByTestCases(Collection<Long> testCaseIds,
			PagingAndSorting pagingAndSorting) {


		if (testCaseIds.isEmpty()) {
			return Collections.emptyList();
		}

		// we have to fetch our query and modify the hql a bit, hence the weird operation below
		Query namedquery = currentSession().getNamedQuery("RequirementVersion.findDistinctRequirementVersionsByTestCases");
		String hql = namedquery.getQueryString();
		hql = SortingUtils.addOrder(hql, pagingAndSorting);

		Query q = currentSession().createQuery(hql);
		if(!pagingAndSorting.shouldDisplayAll()){
			PagingUtils.addPaging(q, pagingAndSorting);
		}

		q.setParameterList("testCaseIds", testCaseIds, LongType.INSTANCE);

		List<Object[]> raw = q.list();

		// now we have to collect from the result set the only thing
		// we want : the RequirementVersions
		List<RequirementVersion> res = new ArrayList<>(raw.size());
		for (Object[] tuple : raw){
			res.add((RequirementVersion)tuple[0]);
		}
		if ("endDate".equals(pagingAndSorting.getSortedAttribute())){
			 Collections.sort(res, new Comparator<RequirementVersion>() {
				@Override
				public int compare(RequirementVersion req1, RequirementVersion req2) {
					return compareReqMilestoneDate(req1, req2);
				}
			});

			if (pagingAndSorting.getSortOrder() == SortOrder.ASCENDING){
				Collections.reverse(res);
			}
		}
		return res;

	}

private int compareReqMilestoneDate(RequirementVersion req1, RequirementVersion req2){

		boolean isEmpty1 = req1.getMilestones().isEmpty();
		boolean isEmpty2 = req2.getMilestones().isEmpty();

		if (isEmpty1 && isEmpty2){
			return 0;
		} else if (isEmpty1){
			return 1;
		} else if (isEmpty2){
			return -1;
		} else {
			return getMinDate(req1).before(getMinDate(req2)) ?  getMinDate(req1).after(getMinDate(req2))? 0 : 1 : -1;
		}
	}

	private Date getMinDate(RequirementVersion req){
		return Collections.min(req.getMilestones(), new Comparator<Milestone>(){
			@Override
			public int compare(Milestone m1, Milestone m2) {
				return m1.getEndDate().before(m2.getEndDate()) ? -1 : 1;
			}
		}).getEndDate();
	}


	@Override
	public List<RequirementVersion> findDistinctRequirementVersionsByTestCases(Collection<Long> testCaseIds) {
		PagingAndSorting pas = new DefaultPagingAndSorting("RequirementVersion.name", true);
		return findDistinctRequirementVersionsByTestCases(testCaseIds, pas);
	}

	/*
	 * Hibernate won't f***ing do it the normal way so I'll shove SQL up it until it begs me to stop
	 *
	 * (non-Javadoc)
	 * @see org.squashtest.tm.service.internal.repository.CustomRequirementVersionCoverageDao#delete(org.squashtest.tm.domain.testcase.RequirementVersionCoverage)
	 */
	@Override
	public void delete(RequirementVersionCoverage requirementVersionCoverage) {


		Session s = currentSession();

		String sql = NativeQueries.REQUIREMENT_SQL_REMOVE_TEST_STEP_BY_COVERAGE_ID;

		Query q = s.createSQLQuery(sql);
		q.setParameter("covId", requirementVersionCoverage.getId());
		q.executeUpdate();

		s.flush();

		s.delete(requirementVersionCoverage);


	}


}
