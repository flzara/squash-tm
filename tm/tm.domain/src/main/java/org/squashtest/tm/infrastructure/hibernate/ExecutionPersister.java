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
package org.squashtest.tm.infrastructure.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;

import java.util.Iterator;

/*
 This class works around a bug on the reverse mapping between the Executions and the TestCase that own them.
 See TestStepPersister class for a complete explanation.
  */
public class ExecutionPersister extends JoinedSubclassEntityPersister {

	private static final String NONFORMATTED_TABLE_NAME = "ITEM_TEST_PLAN_EXECUTION";
	private static final String NONFORMATTED_COLUMN_NAME = "EXECUTION_ID";

	private static final String ERROR_MESSAGE_JOIN_TABLE_NOT_FOUND = "ExecutionPersister : could not find the join table " + NONFORMATTED_TABLE_NAME;

	private String formattedTableName;
	private String[] formattedColumnName = new String[1];

	private int cachedIndex = -1;

	public ExecutionPersister(
		PersistentClass persistentClass,
		EntityRegionAccessStrategy cacheAccessStrategy,
		NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy,
		PersisterCreationContext creationContext)
		throws HibernateException {

		super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, creationContext);
		init(persistentClass, creationContext.getSessionFactory());
	}

	/*
	 * This override is the very reason of that class
	 * @see org.hibernate.persister.entity.JoinedSubclassEntityPersister#getSubclassTableKeyColumns(int)
	 */
	@Override
	protected String[] getSubclassTableKeyColumns(int j) {
		if (isTheJoinTable(j)){
			return formattedColumnName;
		}
		else{
			return super.getSubclassTableKeyColumns(j);
		}
	}

	private boolean isTheJoinTable(int index){
		if (cachedIndex == -1) {
			boolean isTheOne = getSubclassTableName(index).equals(formattedTableName);
			if (isTheOne) {
				cachedIndex = index;
			}
			return isTheOne;
		}
		else{
			return cachedIndex == index;
		}
	}

	/*
	 * @See org.hibernate.persister.entity.AbstractEntityPersister#isInverseTable(int)
	 */
	@Override
	protected boolean isInverseTable(int j) {
		if (isTheJoinTable(j)) {
			return true;
		} else {
			return super.isInverseTable(j);
		}
	}

	// *** Initialization *** //

	private void init(PersistentClass persistentClass, SessionFactoryImplementor factory){
		createTableNamePattern(persistentClass, factory);
		createColumnName(factory);
	}

	private void createTableNamePattern(PersistentClass persistentClass, SessionFactoryImplementor factory){
		Iterator joinIterator = persistentClass.getJoinClosureIterator();
		while (joinIterator.hasNext()) {
			Table table = ((Join) joinIterator.next()).getTable();
			if (NONFORMATTED_TABLE_NAME.equalsIgnoreCase(table.getName())) {
				formattedTableName = table.getQualifiedName(factory.getDialect(),
					factory.getSettings().getDefaultCatalogName(),
					factory.getSettings().getDefaultSchemaName());
				return;
			}
		}
		throw new IllegalArgumentException(ERROR_MESSAGE_JOIN_TABLE_NOT_FOUND);
	}

	private void createColumnName(SessionFactoryImplementor factory){
		Column column = new Column(NONFORMATTED_COLUMN_NAME);
		formattedColumnName[0] = column.getQuotedName(factory.getDialect());
	}
}
