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

import org.squashtest.tm.core.foundation.collection.ColumnFiltering
import org.squashtest.tm.domain.users.ConnectionLog
import org.squashtest.tm.service.internal.repository.ConnectionLogDao
import spock.lang.Specification

class ConnectionLogExportServiceImplTest extends Specification{

	ConnectionLogExportServiceImpl service = new ConnectionLogExportServiceImpl();

	ConnectionLogDao connectionLogDao = Mock();

	def setup() {
		service.connectionLogDao = connectionLogDao
	}

    def "should create a file" () {
		given :
		ColumnFiltering filtering = ColumnFiltering.unfiltered();
		ConnectionLog connectionLog = new ConnectionLog();
		connectionLog.id = 1
		connectionLog.login = "test"
		connectionLog.connectionDate = new Date()
		connectionLog.success = true
		List<ConnectionLog> list = new ArrayList<>()
		list.add(connectionLog)

		when :
		File res = service.exportConnectionLogsToCsv(filtering)

		then :
		1 * connectionLogDao.findFilteredConnections(filtering) >> list
		res != null
	}
}
