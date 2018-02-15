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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.tm.core.foundation.collection.ColumnFiltering;
import org.squashtest.tm.domain.users.ConnectionLog;
import org.squashtest.tm.service.connectionhistory.ConnectionLogExportService;
import org.squashtest.tm.service.internal.repository.ConnectionLogDao;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.StringJoiner;

import static org.squashtest.tm.service.security.Authorizations.HAS_ROLE_ADMIN;

/**
 * @author aguilhem
 */
@Service("ConnectionLogExportService")
@PreAuthorize(HAS_ROLE_ADMIN)
@Transactional
public class ConnectionLogExportServiceImpl implements ConnectionLogExportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionLogExportServiceImpl.class);

	private static final String ID_COLUMN = "Id";
	private static final String LOGIN_COLUMN = "Login";
	private static final String CONNECTION_DATE_COLUMN = "Connection Date";
	private static final String SUCCESS_COLUMN = "Success";
	@Inject
	private ConnectionLogDao connectionLogDao;

	@Override
	public File exportConnectionLogsToCsv(ColumnFiltering filtering) {

		List<ConnectionLog> list = connectionLogDao.findFilteredConnections(filtering);

		File file;
		PrintWriter writer = null;
		try {
			file = File.createTempFile("export-connection-history", "tmp");
			file.deleteOnExit();


			writer = new PrintWriter(file);

			writer.write(getColumnTitles() + "\n");

			PrintWriter finalWriter = writer;
			list.stream().forEach(connectionLog -> {
				String line = buildLine(connectionLog);
				finalWriter.write(line+"\n");
			});

			writer.close();

			return file;
		} catch (IOException e) {
			LOGGER.error("connection history export : I/O failure while creating the temporary file : " + e.getMessage());
			throw new RuntimeException(e);
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private String getColumnTitles(){
		StringJoiner joiner = new StringJoiner(";");
		joiner.add(ID_COLUMN).add(LOGIN_COLUMN).add(CONNECTION_DATE_COLUMN).add(SUCCESS_COLUMN);
		return joiner.toString();
	}

	private String buildLine(ConnectionLog connectionLog){
		StringJoiner joiner = new StringJoiner(";");
		joiner.add(connectionLog.getId().toString())
			.add(connectionLog.getLogin())
			.add(connectionLog.getConnectionDate().toString())
			.add(connectionLog.getSuccess().toString());
		return joiner.toString();
	}
}
