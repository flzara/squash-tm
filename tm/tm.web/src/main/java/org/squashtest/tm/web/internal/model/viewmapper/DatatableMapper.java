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
package org.squashtest.tm.web.internal.model.viewmapper;


/**
 * @param <KEY>
 */
public interface DatatableMapper<KEY> {

	/**
	 * Registers a new mapping. 'key' is the name of the column in the datatable.
	 * 
	 * @param key
	 * @param mapping
	 * @return
	 */
	DatatableMapper<KEY> map(KEY key, Mapping mapping);
	
	/**
	 * Will map an attribute named 'attribute' of the model to the datatable column 'key'. Note that this
	 * implementation is equivalent to  map(key, new SimpleMapping(attribute)).
	 * 
	 * @param key : the key used to register. Usually corresponds to a column of the datatable.
	 * @param attribute : the name of an attribute of a class 
	 * @return this
	 */
	DatatableMapper<KEY> map(KEY key, String attribute);
	
	
	/**
	 * Will map an attribute named 'attribute' of a class 'ownerType' to the datatable column 'key'. Note that this
	 * implementation is equivalent to  map(key, new AttributeMapping(ownerType, attribute)).
	 * 
	 * @param key : the key used to register. Usually corresponds to a column of the datatable.
	 * @param attribute : the name of an attribute of a class 
	 * @param ownerType : the class that own the attribute  stated above
	 * @return this
	 */
	DatatableMapper<KEY> mapAttribute(KEY key, String attribute, Class<?> ownerType);


	/**
	 * 
	 * @returns the String that represents the model mapping associated to that key. Note that what is returned depends on the 
	 * implementation of DatatableMapper.Mapping actually used. 
	 * 
	 */
	String getMapping(KEY key);
	
	public static interface Mapping{
		
		public String getMapping();
		
	}
	
}
