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
package org.squashtest.tm.service.internal.advancedsearch;

import com.querydsl.jpa.impl.JPAQuery;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The {@link AdvancedSearchQueryModelToConfiguredQueryConverter} requires such an object that
 * helps him to retrieve which columns it needs by key. The keys are arbitrary and they just need
 * to match the possible attribute values in the supplied form. Such mapping needs to be supplied
 * for every processing because the same keys can refer to different column prototypes depending on the
 * search context in use.
 *
 * The mappings are split in three categories :
 * - the form mapping, which are the columns used by the search search,
 * - the custom field mapping, which are names of type of custom field in the interface mapped to the correct column prototype label
 * - the result mapping, which are the columns displayed in the result table.
 *
 * Each mapping comes with their column mapping by key.
 *
 * Also, sometimes a given key (eg an attribute
 * of the search form or the result table) cannot be mapped to a query column prototype label, because its too specific
 * or complicated for the query engine to use. In this case you still can map it as a QueryCustomizer, in which case
 * you will need to implement the desired query transformation.
 *
 */
public final class AdvancedSearchColumnMappings {

	final private ColumnMapping formMapping = new ColumnMapping();
	final private ColumnMapping resultMapping = new ColumnMapping();
	final private ColumnMapping cufMapping = new ColumnMapping();


	public ColumnMapping getFormMapping() {
		return formMapping;
	}


	public ColumnMapping getResultMapping() {
		return resultMapping;
	}


	public ColumnMapping getCufMapping(){
		return cufMapping;
	}
	// *************** internal definitions *******************

	public final class ColumnMapping{

		/**
		 * For regular columns, retrieve the label of the QueryColumnPrototype
		 * we need based on its assigned key. The key depends on the context of
		 * use (searching requirements, displaying searchs for requirements,
		 * etc).
		 */
		private Map<String, String> mappedColumnLabels = new HashMap<>();

		/**
		 * When a given data of the search or result panes cannot be handled
		 * by a regular column, put your query customization here.
		 */
		private Map<String, QueryCustomizer> specialHandlers = new HashMap<>();


		public ColumnMapping map(String key, String colLabel){
			mappedColumnLabels.put(key, colLabel);
			return this;
		}

		public ColumnMapping mapHandler(String key, QueryCustomizer customizer){
			specialHandlers.put(key, customizer);
			return this;
		}

		public Collection<String> getMappedKeys(){
			return mappedColumnLabels.keySet();
		}

		public boolean isMappedKey(String key){
			return mappedColumnLabels.containsKey(key);
		}

		/**
		 *
		 * @param key
		 * @return
		 * @throws NoSuchElementException if the key cannot be found
		 */
		public String findColumnLabel(String key){
			String label = mappedColumnLabels.get(key);
			if (label == null){
				throw new NoSuchElementException("Search Engine : cannot retrieve column for key '"+key+"'");
			}
			return label;
		}

		public Collection<String> getSpecialKeys(){
			return specialHandlers.keySet();
		}

		public boolean isSpecialKey(String key){
			return specialHandlers.containsKey(key);
		}

		/**
		 *
		 *
		 * @param key
		 * @return
		 * @throws NoSuchElementException if the key cannot be found
		 */
		public QueryCustomizer findCustomizer(String key){
			QueryCustomizer customizer = specialHandlers.get(key);
			if (customizer == null){
				throw new NoSuchElementException("Search Engine : cannot retrieve query customizer for key '"+key+"'");
			}
			return customizer;
		}

	}


	/**
	 * That interface exists because it's more expressive than a mere consumer of query.
	 * First argument is the query to modify, the second is the list of arguments.
	 */
	public interface QueryCustomizer extends BiConsumer<ExtendedHibernateQuery<?>, List<Object>> {

	}

}
