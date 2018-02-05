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

import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.squashtest.tm.service.internal.foundation.collection.JpaPagingUtils;
import org.squashtest.tm.service.internal.foundation.collection.SortingUtils;
import org.squashtest.tm.service.internal.repository.CustomConnectionLogDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * @author aguilhem 
 */
public class ConnectionDaoImpl implements CustomConnectionLogDao {

	private static final String HQL_FIND_CONNECTION_LOGS_BASE = "from ConnectionLog ConnectionLog ";

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<ConnectionLog> findSortedTeams(PagingAndSorting paging, Filtering filtering) {
		StringBuilder sQuery = new StringBuilder(HQL_FIND_CONNECTION_LOGS_BASE);

		SortingUtils.addOrder(sQuery, paging);

		Query hQuery = entityManager.createQuery(sQuery.toString());

		JpaPagingUtils.addPaging(hQuery, paging);

		return hQuery.getResultList();
	}
}
