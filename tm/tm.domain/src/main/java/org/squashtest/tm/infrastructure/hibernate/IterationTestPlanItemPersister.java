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

import java.util.Iterator;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;

/*
    ABOUT SONAR : it says this class has been copypasted from TestStepPersister, 
and should be refactored. Sure, go ahdead. I'm watching.
*/

public class IterationTestPlanItemPersister extends SingleTableEntityPersister {

	private static final String NONFORMATTED_TABLE_NAME = "ITEM_TEST_PLAN_LIST";
	private static final String NONFORMATTED_COLUMN_NAME = "ITEM_TEST_PLAN_ID";

	private String formattedTableName;
	private String[] formattedColumnName = new String[1];

	/*
	 * At first, when Hibernate invokes getSubclassTableKeyColumns(int) we must test if
	 * the override applies by comparing the string names of the requested table.
	 *
	 * In order to prevent systematic and expensive string comparison we later on
	 * cache the index of that data, once it is known to us.
	 */
	private int _cachedIndex=-1;

      
	public IterationTestPlanItemPersister(PersistentClass persistentClass,
			EntityRegionAccessStrategy cacheAccessStrategy,
			NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy,
			PersisterCreationContext creationContext)
					throws HibernateException {

		super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, creationContext);
                
		init(persistentClass, creationContext.getSessionFactory());
	}



	/*
	 * This override is the very reason of that class
	 * @see org.hibernate.persister.entity.SingleTableEntityPersister#getSubclassTableKeyColumns(int)
	 */
	@Override
	public String[] getSubclassTableKeyColumns(int j) {
		if (isTheJoinTable(j)){
			return formattedColumnName;
		}
		else{
			return super.getSubclassTableKeyColumns(j);
		}
	}

	/*
	 * @See org.hibernate.persister.entity.AbstractEntityPersister#isInverseTable(int)
	 */
	@Override
	protected boolean isInverseTable(int j){
		if (isTheJoinTable(j)){
			return true;
		}
		else{
			return super.isInverseTable(j);
		}
	}



	private boolean isTheJoinTable(int index){
		if (_cachedIndex==-1){
			boolean isTheOne = getSubclassTableName(index).equals(formattedTableName);
			if (isTheOne){
				_cachedIndex=index;
			}
			return isTheOne;
		}
		else{
			return _cachedIndex == index;
		}
	}


	// **************************** init **************************

	private void init(PersistentClass persistentClass, SessionFactoryImplementor factory){
		createTableNamePattern(persistentClass, factory);
		createColumnName(factory);

	}


	private void createTableNamePattern(PersistentClass persistentClass, SessionFactoryImplementor factory){
		Iterator joinIter = persistentClass.getJoinClosureIterator();
		while (joinIter.hasNext()){
			Table tab = ((Join) joinIter.next()).getTable();
			if (tab.getName().equalsIgnoreCase(NONFORMATTED_TABLE_NAME)) {
				formattedTableName = tab.getQualifiedName(factory.getDialect(),
						factory.getSettings().getDefaultCatalogName(),
						factory.getSettings().getDefaultSchemaName());
				return;
			}
		}
		throw new IllegalArgumentException("FixIterationToItemPersister : could not find the join table "+NONFORMATTED_TABLE_NAME);
	}


	private void createColumnName(SessionFactoryImplementor factory){
		Column column = new Column(NONFORMATTED_COLUMN_NAME);
		formattedColumnName[0] = column.getQuotedName(factory.getDialect());
	}

}

