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

import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemInfoListItemCode;
import org.squashtest.tm.service.internal.repository.CustomInfoListItemDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class InfoListItemDaoImpl implements CustomInfoListItemDao {
	@PersistenceContext
	private EntityManager entityManager;

	private static final String ITEM_CODE = "itemCode";
	private static final String PROJECT_ID = "projectId";

	@Override
	public boolean isCategoryConsistent(long projectId, String itemCode) {
		Query q = entityManager.createNamedQuery("infoListItem.foundCategoryInProject");
		q.setParameter(PROJECT_ID, projectId);
		q.setParameter(ITEM_CODE, itemCode);
		return (Long) q.getSingleResult() == 1;
	}

	@Override
	public boolean isNatureConsistent(long projectId, String itemCode) {
		Query q = entityManager.createNamedQuery("infoListItem.foundNatureInProject");
		q.setParameter(PROJECT_ID, projectId);
		q.setParameter(ITEM_CODE, itemCode);
		return (Long) q.getSingleResult() == 1;
	}

	@Override
	public boolean isTypeConsistent(long projectId, String itemCode) {
		Query q = entityManager.createNamedQuery("infoListItem.foundTypeInProject");
		q.setParameter(PROJECT_ID, projectId);
		q.setParameter(ITEM_CODE, itemCode);
		return (Long) q.getSingleResult() == 1;
	}

	@Override
	public void unbindFromLibraryObjects(long infoListId) {
		InfoListItem defaultReqCat = findByCode(SystemInfoListItemCode.CAT_UNDEFINED.getCode());
		execUpdateQuery(infoListId, "infoList.setReqCatToDefault", defaultReqCat);
		InfoListItem defaultTcNat = findByCode(SystemInfoListItemCode.NAT_UNDEFINED.getCode());
		execUpdateQuery(infoListId, "infoList.setTcNatToDefault", defaultTcNat);
		InfoListItem defaultTcType = findByCode(SystemInfoListItemCode.TYP_UNDEFINED.getCode());
		execUpdateQuery(infoListId, "infoList.setTcTypeToDefault", defaultTcType);

	}

	private InfoListItem findByCode(String code) {
		return (InfoListItem) entityManager.createNamedQuery("InfoListItem.findByCode")
			.setParameter("code", code)
			.getSingleResult();
	}

	private void execUpdateQuery(long infoListId, String queryName, InfoListItem defaultParam) {
		Query query = entityManager.createNamedQuery(queryName);
		query.setParameter("default", defaultParam);
		query.setParameter("id", infoListId);
		query.executeUpdate();

	}

	@Override
	public boolean isUsed(long infoListItemId) {
		Query q = entityManager.createNamedQuery("infoListItem.isUsed");
		q.setParameter("id", infoListItemId);
		return (Long) q.getSingleResult() > 0;
	}

	@Override
	public void removeInfoListItem(long infoListItemId, InfoListItem defaultItem) {

		execUpdateQuery(infoListItemId, "infoListItem.setReqCatToDefault", defaultItem);
		execUpdateQuery(infoListItemId, "infoListItem.setTcNatToDefault", defaultItem);
		execUpdateQuery(infoListItemId, "infoListItem.setTcTypeToDefault", defaultItem);

	}

}
