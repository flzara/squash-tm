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
import org.squashtest.tm.service.internal.repository.CustomRequirementVersionLinkDao;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jlor on 19/05/2017.
 */
public class RequirementVersionLinkDaoImpl extends HibernateEntityDao<RequirementVersionLink> implements CustomRequirementVersionLinkDao {

	@Override
	public List<RequirementVersionLink> findAllByReqVersionId(long requirementVersionId, PagingAndSorting pagingAndSorting) {

		org.hibernate.Query baseQuery = currentSession().getNamedQuery("RequirementVersionLink.findAllByReqVersionId");
		String queryString = baseQuery.getQueryString();
		queryString = SortingUtils.addOrder(queryString, pagingAndSorting);

		org.hibernate.Query finalQuery = currentSession().createQuery(queryString);
		finalQuery.setParameter("requirementVersionId", requirementVersionId);

		PagingUtils.addPaging(finalQuery, pagingAndSorting);

		List<Object[]> resultList = finalQuery.list();
		List<RequirementVersionLink> reqVerLinkList = new ArrayList<>(resultList.size());
		for(Object[] object : resultList) {
			reqVerLinkList.add((RequirementVersionLink) object[0]);
		}
		return reqVerLinkList;

	}

	@Override
	public boolean linkAlreadyExists(Long reqVersionId, Long relatedReqVersionId) {
		Query existQuery = entityManager.createNamedQuery("RequirementVersionLink.linkAlreadyExists");
		existQuery.setParameter("reqVersionId", reqVersionId);
		existQuery.setParameter("relatedReqVersionId", relatedReqVersionId);
		return (Long)existQuery.getSingleResult() > 0;
	}

	@Override
	public RequirementVersionLink addLink(RequirementVersionLink requirementVersionLink) {
		/* Because of the particular model, for each RequirementVersionLink persisted, we persist another symmetrical one.
		* (Which RequirementVersions and LinkDirections were inverted). */
		entityManager.persist(requirementVersionLink);

		RequirementVersionLink symmetricalRequirementVersionLink =
			requirementVersionLink.createSymmetricalRequirementVersionLink();
		entityManager.persist(symmetricalRequirementVersionLink);

		return requirementVersionLink;
	}

	@Override
	public void addLinks(List<RequirementVersionLink> requirementVersionLinks) {
		persist(requirementVersionLinks);

		List<RequirementVersionLink> symmetricalLinks = new ArrayList<>();
		for(RequirementVersionLink link : requirementVersionLinks) {
			symmetricalLinks.add(link.createSymmetricalRequirementVersionLink());
		}
		persist(symmetricalLinks);
	}

	@Override
	public void setLinksTypeToDefault(RequirementVersionLinkType linkTypeToReplace,
									  RequirementVersionLinkType defaultLinkType) {
		Query updateQuery = entityManager.createNamedQuery("RequirementVersionLink.setLinksTypeToDefault");
		updateQuery.setParameter("formerLinkType", linkTypeToReplace);
		updateQuery.setParameter("defaultLinkType", defaultLinkType);
		updateQuery.executeUpdate();
	}
}
