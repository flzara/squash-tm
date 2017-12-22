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

import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.service.internal.foundation.collection.PagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomRequirementVersionLinkTypeDao;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jlor on 13/06/2017.
 */
public class RequirementVersionLinkTypeDaoImpl extends HibernateEntityDao<RequirementVersionLink> implements CustomRequirementVersionLinkTypeDao {

	@Override
	public List<RequirementVersionLinkType> getAllPagedAndSortedReqVersionLinkTypes(PagingAndSorting pagingAndSorting) {
		org.hibernate.Query baseQuery = currentSession().getNamedQuery("RequirementVersionLinkType.getAllPagedAndSortedReqVersionLinkTypes");
		String queryString = baseQuery.getQueryString();
		queryString = SortingUtils.addOrder(queryString, pagingAndSorting);

		org.hibernate.Query finalQuery = currentSession().createQuery(queryString);
		PagingUtils.addPaging(finalQuery, pagingAndSorting);

		return finalQuery.list();
	}

	@Override
	public boolean doesCodeAlreadyExist(String code) {
		Query existQuery = entityManager.createNamedQuery("RequirementVersionLinkType.codeAlreadyExists");
		existQuery.setParameter("code", code);
		return (Long)existQuery.getSingleResult() > 0;
	}

	@Override
	public boolean doesCodeAlreadyExist(String code, Long linkTypeId) {
		Query existQuery = entityManager.createNamedQuery("RequirementVersionLinkType.codeAlreadyExistsByAnotherType");
		existQuery.setParameter("code", code);
		existQuery.setParameter("linkTypeId", linkTypeId);
		return (Long)existQuery.getSingleResult() > 0;
	}
}
