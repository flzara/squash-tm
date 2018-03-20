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

public class ATSAfterUpdate extends TriggerAdapter {

	private static final String SQL =
		"UPDATE " +
			"TEST_CASE_LIBRARY_NODE TCLN " +
		"SET " +
			"TCLN.LAST_MODIFIED_BY = (SELECT ATS.LAST_MODIFIED_BY FROM ACTION_TEST_STEP ATS WHERE ATS.TEST_STEP_ID = ?), " +
			"TCLN.LAST_MODIFIED_ON = (SELECT ATS.LAST_MODIFIED_ON FROM ACTION_TEST_STEP ATS WHERE ATS.TEST_STEP_ID = ?) " +
		"WHERE " +
			"TCLN.TCLN_ID = (SELECT TCS.TEST_CASE_ID FROM TEST_CASE_STEPS TCS WHERE TCS.STEP_ID = ?);";


	@Override
	public void fire(Connection connection, ResultSet oldRow, ResultSet newRow) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(SQL);

		Long stepId = newRow.getLong(1);
		stmt.setLong(1, stepId);
		stmt.setLong(2, stepId);
		stmt.setLong(3, stepId);

		stmt.execute();
	}
}
