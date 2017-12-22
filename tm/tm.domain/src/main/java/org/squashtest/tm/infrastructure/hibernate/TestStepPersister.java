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
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;


/*
 * Since version 1.5.0 :
 *
 *
 * What
 * ======================
 *
 * This class works around a bug on the reverse mapping between the test steps and the test case that own them.
 *
 *
 * Why
 * ======================
 *
 * Hibernate 3.6.10 (and probably other releases) cannot process properly the following case :
 *
 * - bi-directional ManyToOne (owned by the 1 side),
 * - using a join table,
 * - using an index column,
 * - mapped in a superclass using the joined-sublasses strategy
 *
 * Specifically the problem lies in org.hibernate.persister.entity.JoinedSubclassEntityPersister constructor, line 277 -> 281.
 * In that section of the code, the foreign key between the master table (TEST_STEP) and the join table (TEST_CASE_STEPS) is
 * wrongly identified : it is assumed to be the the primary key of the join table regardless of what the annotations says
 * (and the primary key isn't right anyway).
 *
 * The consequence is that Hibernate believe it must join on TEST_CASE_ID, while the correct column is STEP_ID.
 *
 *
 * How
 * =======================
 *
 * To work around this we override the function #getSubclassTableKeyColumns(), that returns accepts an index as input and returns
 * the foreign key for this index. This method is invoked anytime the persister generates a join sql fragment.
 * When the overriden function is requested for the foreign key of the table TEST_CASE_STEPS, it will return the correct foreign key
 * (STEP_ID), instead of the wrong one (TEST_CASE_ID).
 *
 * This class reuses some bits of the initialization code in order to generate the final name of the table and column, according to
 * target database dialect.
 *
 *
 *
 */

/*
 * Update 08/03/13 : Issue 1980 (https://ci.squashtest.org/mantis/view.php?id=1980)
 *
 * First, I'd like you to know that I swear I've tried everything ( mapping using @SecondaryTable + inverse=true, custom @SQLInsert, orthodox and unorthodox mapping,
 * voodoo and else) before relying on this.
 *
 *
 * Why
 * ======================
 *
 * This issue is related to the cascade-persist of a test case and its steps. What should normally happen is the following :
 *
 *  1/ persist the data in table TEST_STEP
 *  2/ persist the data in the table subclasses
 *  3/ persist the other join tables
 *
 *  For each of those operations it's supposed to check that the join pointing to the current table is not an inverse relation. That information is normally
 *  supplied by the  . However, here is
 *  the default implementation straight from org.hibernate.persister.entity.AbstractEntityPersister :
 *
 *  [quote]
 *  protected boolean isInverseTable(int j) {
 *		return false;
 *	}
 *	[/quote]
 *
 *	And here is how Gavin King solved the problem : by delegating to me.
 *
 *  The consequence, regarding cascade persistence, is that the join table TEST_CASE_STEPS is handled by the CollectionPersister managing TestCase#steps but also by the
 *  TestStep persister, which has no clue of what index it should insert the TestStep. This leads to double-insertion in the database, with null data for the order column.
 *  But the TestStepPersister should never worry about TEST_CASE_STEPS in the first place !
 *
 *
 * How
 * =======================
 *
 * Override isInverseTable and return the information that should have been read from the metadata : the bloody TEST_CASE_STEPS table is an INVERTED TABLE.
 *
 *
 */

/*
    ABOUT SONAR : it says this class has been copypasted into IterationTestPlanItemPersister, 
and should be refactored. Sure, go ahdead. I'm watching.
*/
public class TestStepPersister extends JoinedSubclassEntityPersister {

	private static final String NONFORMATTED_TABLE_NAME = "TEST_CASE_STEPS";
	private static final String NONFORMATTED_COLUMN_NAME = "STEP_ID";

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

      
	public TestStepPersister(PersistentClass persistentClass,
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
		throw new IllegalArgumentException("TestStepPersister : could not find the join table "+NONFORMATTED_TABLE_NAME);
	}


	private void createColumnName(SessionFactoryImplementor factory){
		Column column = new Column(NONFORMATTED_COLUMN_NAME);
		formattedColumnName[0] = column.getQuotedName(factory.getDialect());
	}

}

