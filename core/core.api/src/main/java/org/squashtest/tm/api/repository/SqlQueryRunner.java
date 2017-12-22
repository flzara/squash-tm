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
package org.squashtest.tm.api.repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Interface which can be used by plugins to query Squash database through SQL.
 * </p>
 * <p>
 * Any SELECT query can be executed using this service. Queries can be parameterized using named parameters. A
 * parameterized query looks like :
 * </p>
 * <p>
 * <code>
 * select * from PROJECT where NAME in ( :names ) and CREATED_ON > :creationDate
 * </code>
 * </p>
 * <p>
 * Named parameters are prefixed with a colon (:) <strong>in the query</string> and should be put in a {@link Map}.
 * </p>
 * <p>
 * <code>
 * Map<String, Object> params = new HashMap<String, Object>()<br />
 * params.put("names", new String[] {"foo", "bar"})<br />
 * params.put("creationDate", new Date())<br />
 * </code>
 * </p>
 * 
 * @author Gregory Fouquet
 * @since 1.2.0
 * 
 */
public interface SqlQueryRunner {
	/**
	 * Executes a SELECT which returns a list of rows.
	 * 
	 * @param <T>
	 * @param selectQuery a non <code>null</code>,  non parameterized SELECT query
	 * @return the list of results. Should never return <code>null</code>
	 */
	<T> List<T> executeSelect(String selectQuery);

	/**
	 * Executes a parameterized SELECT which returns a list of rows. The query uses named parameters.
	 * 
	 * @param <T>
	 * @param selectQuery a non <code>null</code>,  parameterized SELECT query
	 * @param namedParameters
	 *            the non <code>null</code> map of named parameters.
	 * @return the list of results. Should never return <code>null</code>
	 * @since 1.2.0
	 */
	<T> List<T> executeSelect(String selectQuery, Map<String, ?> namedParameters);

	/**
	 * Executes a SELECT which returns a unique row.
	 * 
	 * @param <T>
	 *            type of returned object, usually a scalar or a tuple (an array of scalars)
	 * @param selectQuery a non <code>null</code>,  non parameterized SELECT query
	 * @return
	 * @since 1.2.0
	 */
	<T> T executeUniqueSelect(String selectQuery);

	/**
	 * Executes a parametered SELECT which returns a unique row. The query uses named parameters.
	 * 
	 * @param <T>
	 * @param selectQuery a non <code>null</code>,  parameterized SELECT query
	 * @param namedParameters
	 *            the non null map of named parameters.
	 * @return
	 * @since 1.2.0
	 */
	<T> T executeUniqueSelect(String selectQuery, Map<String, ?> namedParameters);

}
