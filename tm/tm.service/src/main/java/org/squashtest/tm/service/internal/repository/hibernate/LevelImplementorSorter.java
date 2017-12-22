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
package org.squashtest.tm.service.internal.repository.hibernate;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.squashtest.tm.core.foundation.collection.DefaultSorting;
import org.squashtest.tm.core.foundation.collection.PagingAndMultiSorting;
import org.squashtest.tm.core.foundation.collection.Sorting;
import org.squashtest.tm.domain.Level;

/**
 *
 * @author Frederic,bsiri
 *
 * Enables sorting of objects according to Enums that implements {@link Level}. Note : the correct type should something like &lt;? extends Level & Enum &gt;
 * but I cannot make java comply with such type declaration, so the type checking will happen at runtime.
 *
 */
public final class LevelImplementorSorter implements PagingAndMultiSorting{

	private PagingAndMultiSorting multiSorting;

	@SuppressWarnings("rawtypes")
	private Map<String, Class<? extends Enum>> levelClassByAttributes = new HashMap<>();

	public LevelImplementorSorter(PagingAndMultiSorting sorting){
		this.multiSorting = sorting;
	}

	public void map(String attributeName, Class<? extends Enum<?>> levelClass){
		if (! Level.class.isAssignableFrom(levelClass)){
			throw new IllegalArgumentException("LevelImplementorSorter : attempted to map incompatible class '"+levelClass+"' : "+
					"it must both be an Enum and implement org.squashtest.tm.domain.Level");
		}
		levelClassByAttributes.put(attributeName, levelClass);
	}


	@Override
	public int getFirstItemIndex() {
		return multiSorting.getFirstItemIndex();
	}

	@Override
	public int getPageSize() {
		return multiSorting.getPageSize();
	}

	@Override
	public boolean shouldDisplayAll() {
		return multiSorting.shouldDisplayAll();
	}

	@Override
	public List<Sorting> getSortings() {
		List<Sorting> newSortings = new ArrayList<>(multiSorting.getSortings());

		ListIterator<Sorting> iterSorting = newSortings.listIterator();

		while (iterSorting.hasNext()){

			Sorting sort = iterSorting.next();
			String attribute = sort.getSortedAttribute();

			if (levelClassByAttributes.containsKey(attribute)){

				String stmt = buildCaseStmt(attribute);

				DefaultSorting newSorting =  new DefaultSorting();
				newSorting.setSortedAttribute(stmt);
				newSorting.setSortOrder(sort.getSortOrder());

				iterSorting.remove();
				iterSorting.add(newSorting);
			}

		}

		return newSortings;
	}


	@SuppressWarnings({"rawtypes", "unchecked"})
	private String buildCaseStmt(String attribute){

		Class<? extends Enum> enumClass = levelClassByAttributes.get(attribute);
		EnumSet<? extends Enum> enums = EnumSet.allOf(enumClass);

		StringBuilder builder = new StringBuilder();
		builder.append("CASE ").append(attribute).append(" ");
		for (Enum e : enums){
			builder.append("when '").append(e.name()).append("' then ").append(((Level) e).getLevel()).append(" ");
		}
		builder.append("END ");

		return builder.toString();
	}

}
