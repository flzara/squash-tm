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
package org.squashtest.csp.h2.triggers;

import org.h2.tools.TriggerAdapter;

import java.sql.*;

/**
 *  Triggered after a new TestCaseLibraryNode had been inserted. It will insert into the relationship
 *  closure table the self-reference (the distance of a node with itself is always 0).
 *
 *
 * @author bsiri
 *
 */
public class TCLNAfterDeletingParameters extends TriggerAdapter {

	private static final String SQL =
		"UPDATE " +
			"TEST_CASE_LIBRARY_NODE TCLN " +
			"SET " +
			"TCLN.LAST_MODIFIED_ON = NOW() " +
			"WHERE " +
			"TCLN.TCLN_ID = ?;";

	@Override
	public void fire(Connection conn, ResultSet oldRow, ResultSet newRow)
		throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(SQL);
		Long id = oldRow.getLong("TEST_CASE_ID");
		stmt.setLong(1, id);
		stmt.execute();
	}
}
