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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.*;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.squashtest.tm.service.internal.repository.ConnectionLogDao;
import org.squashtest.tm.service.user.ConnectionLogFinderService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

/**
 * @author aguilhem
 */
@Service("ConnectionLogFinderService")
@PreAuthorize(HAS_ROLE_ADMIN)
@Transactional
public class ConnectionLogFinderServiceImpl implements ConnectionLogFinderService{
	@Inject
	ConnectionLogDao connectionLogDao;
	@Override
	public List<ConnectionLog> findAll() {
		return connectionLogDao.findAll();
	}

	@Override
	public PagedCollectionHolder<List<ConnectionLog>> findAllFiltered(PagingAndSorting paging, ColumnFiltering columnFiltering) {

		List<ConnectionLog> connectionLogs = connectionLogDao.findSortedConnections(paging, columnFiltering);

		Long count = connectionLogDao.count();

		return new PagingBackedPagedCollectionHolder<>(paging, count, connectionLogs);
	}
}
