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
package org.squashtest.tm.service.internal.repository.hibernate

import org.squashtest.tm.core.foundation.collection.DefaultSorting
import org.squashtest.tm.core.foundation.collection.DelegatePagingAndMultiSorting
import org.squashtest.tm.core.foundation.collection.MultiSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting
import org.squashtest.tm.core.foundation.collection.Pagings
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.testcase.TestCaseImportance;

import spock.lang.Specification

class LevelImplementorSorterTest extends Specification {

	def "should not modify a PagingAndMultiSorting if it doesn't need to"(){
		
		given :
			def mocksortings = createSorting("Bob.name asc, Robert.hobbies desc")
					
			PagingAndMultiSorting pas = new DelegatePagingAndMultiSorting(Pagings.DEFAULT_PAGING, mocksortings)
			LevelImplementorSorter wrapper = new LevelImplementorSorter(pas)
			wrapper.map("Mickael.level", DummyEnum.class)
		
		when :
			def unmodifiedSortings = wrapper.sortings
		
		then :
			unmodifiedSortings == mocksortings.sortings
		
	}
	
	
	def "should replace a hql clause flagged as a enum level"(){
		given :
			def mocksortings = createSorting("Bob.name asc, Mickael.level asc, Robert.hobbies desc")
					
			PagingAndMultiSorting pas = new DelegatePagingAndMultiSorting(Pagings.DEFAULT_PAGING, mocksortings)
			LevelImplementorSorter wrapper = new LevelImplementorSorter(pas)
			wrapper.map("Mickael.level", DummyEnum.class)
		
		when :
			def modifiedSortings = wrapper.sortings
		
		then :
			modifiedSortings.collect { it.sortOrder } == [SortOrder.ASCENDING, SortOrder.ASCENDING, SortOrder.DESCENDING]
			modifiedSortings.collect {it.sortedAttribute } == [
				"Bob.name",
				"CASE Mickael.level when 'LEVEL_ONE' then 1 when 'LEVEL_TWO' then 2 when 'LEVEL_THREE' then 3 END ",
				"Robert.hobbies"	
			]
		
	}
	
	
	def createSorting(String definition){
		def matcher = definition =~ /([^ ]+) (asc|desc)/
		
		def sortings = []
		
		matcher.each {
			String attr = it[1]
			String strOrder = it[2]
			SortOrder order = strOrder.toUpperCase() + "ENDING"
			
			DefaultSorting sorting = new DefaultSorting()
			sorting.setSortedAttribute attr
			sorting.setSortOrder order
			sortings << sorting
		}
		
		return [
			getSortings : { sortings }
		] as MultiSorting
	}
	
	
	private enum DummyEnum implements Level{
		
		LEVEL_ONE(1), LEVEL_TWO(2), LEVEL_THREE(3);
		
		private int level;
		
		private DummyEnum(int lvl){
			this.level = lvl;
		}
		
		public int getLevel(){
			return level
		}

		@Override
		public String getI18nKey() {
			return "key."+this.name()
		}
	}  
	
}
