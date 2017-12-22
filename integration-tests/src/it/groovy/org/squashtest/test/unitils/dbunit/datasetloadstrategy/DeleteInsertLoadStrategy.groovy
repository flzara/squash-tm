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
package org.squashtest.test.unitils.dbunit.datasetloadstrategy

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;
import org.unitils.dbunit.datasetloadstrategy.impl.BaseDataSetLoadStrategy;
import org.unitils.dbunit.util.DbUnitDatabaseConnection;

class DeleteInsertLoadStrategy extends BaseDataSetLoadStrategy {
	static final DatabaseOperation DELETE_INSERT = new CompositeOperation(DatabaseOperation.DELETE, DatabaseOperation.INSERT)

	@Override
	protected void doExecute(DbUnitDatabaseConnection dbUnitDatabaseConnection, IDataSet dataSet)
	throws DatabaseUnitException, SQLException {
		DELETE_INSERT.execute dbUnitDatabaseConnection, dataSet
	}
}
