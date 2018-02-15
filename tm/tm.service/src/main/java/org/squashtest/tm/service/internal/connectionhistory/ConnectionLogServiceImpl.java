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
package org.squashtest.tm.service.internal.connectionhistory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.squashtest.tm.service.internal.repository.ConnectionLogDao;
import org.squashtest.tm.service.connectionhistory.ConnectionLogService;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author aguilhem
 */
@Service("squashtest.tm.service.ConnectionLogService")
@Transactional
public class ConnectionLogServiceImpl implements ConnectionLogService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionLogServiceImpl.class);

	@Inject
	private ConnectionLogDao dao;

	@Override
	public ConnectionLog addSuccessfulConnectionLog(String login) {
		return initConnectionLog(login,true);
	}

	@Override
	public ConnectionLog addFailedConnectionLog(String login) {
		return initConnectionLog(login,false);
	}

	/**
	 * Initiate and persist a {@link ConnectionLog}
	 * @param login the login to set for the {@link ConnectionLog}
	 * @param success the success status of the {@link ConnectionLog}
	 * @return the {@link ConnectionLog} persisted without the id
	 */
	private ConnectionLog initConnectionLog(String login, boolean success) {
		ConnectionLog connectionLog = new ConnectionLog();
		connectionLog.setLogin(login);
		connectionLog.setConnectionDate(new Date());
		connectionLog.setSuccess(success);
		dao.save(connectionLog);
		return connectionLog;
	}
}
