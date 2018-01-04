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

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.service.internal.repository.CustomInfoListDao;
import org.squashtest.tm.service.internal.repository.InfoListDao;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
public class InfoListDaoImpl implements CustomInfoListDao {

	@Inject
	//must have lazy loading to inject the generated dao into custom one, to avoid circular dep. Maybe because of entity manger also injected here ?
	@Lazy
	private InfoListDao infoListDao;

	@PersistenceContext
	private EntityManager em;

	@Override
	public boolean isUsedByOneOrMoreProject(long infoListId) {
		Query query = em.createNamedQuery("infoList.findProjectUsingInfoList");
		query.setParameter("id", infoListId);
		return !query.getResultList().isEmpty();
	}

	@Override
	public void unbindFromProject(long infoListId) {
		InfoList defaultReqCatList = infoListDao.findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode());
		execUpdateQuery(infoListId, "infoList.project.setReqCatListToDefault", defaultReqCatList);
		InfoList defaultTcNatList = infoListDao.findByCode(SystemInfoListCode.TEST_CASE_NATURE.getCode());
		execUpdateQuery(infoListId, "infoList.project.setTcNatListToDefault", defaultTcNatList);
		InfoList defaultTcTypeList = infoListDao.findByCode(SystemInfoListCode.TEST_CASE_TYPE.getCode());
		execUpdateQuery(infoListId, "infoList.project.setTcTypeListToDefault", defaultTcTypeList);
	}

	private void execUpdateQuery(long infoListId, String queryName, Object defaultParam) {
		Query query = em.createNamedQuery(queryName);
		query.setParameter("default", defaultParam);
		query.setParameter("id", infoListId);
		query.executeUpdate();
	}

}
