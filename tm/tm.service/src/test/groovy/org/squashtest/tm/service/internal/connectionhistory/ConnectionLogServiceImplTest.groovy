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
package org.squashtest.tm.service.internal.connectionhistory

import org.squashtest.tm.domain.users.ConnectionLog
import org.squashtest.tm.service.internal.repository.ConnectionLogDao
import spock.lang.Specification

class ConnectionLogServiceImplTest extends Specification{

	ConnectionLogServiceImpl service = new ConnectionLogServiceImpl();

	ConnectionLogDao connectionLogDao = Mock();

	def setup(){
		service.dao = connectionLogDao;
	}

	def "should persist a successful connection log"(){
		given : String login = "test";

		when :
		ConnectionLog res = service.addSuccessfulConnectionLog(login)

		then :
		res.login == login
		res.connectionDate != null
		res.success == true
		1 * connectionLogDao.save(!null)

	}

	def "should persist a failed connection log"(){
		given : String login = "test";

		when :
		ConnectionLog res = service.addFailedConnectionLog(login)

		then :
		res.login == login
		res.connectionDate != null
		res.success == false
		1 * connectionLogDao.save(!null)

	}
}
