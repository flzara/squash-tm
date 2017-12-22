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
/**
 * <p>This package define the following extensions to the HQL language:</p>
 * 
 * <ul>
 * 	<li>group_concat : support for group_concat, if the underlying database supports something similar (eg stringagg for instance)</li>
 * 	<li>s_sum : 'sum' over a subquery</li>
 * 	<li>s_count : 'count(distinct ())' over a subquery</li>
 * 	<li>s_avg : 'avg' over a subquery</li>
 * 	<li>s_min : 'min' over a subquery</li>
 * 	<li>s_max : 'max' over a subquery</li>
 * </ul>
 * 
 * <p>Note that here s_count has a semantic of 'count(distinct ...)'. Also the result type for the numeric aggregate functions will be a long :
 * so don't use them if use them on float/double or you will be disappointed.</p>
 * 
 * <p>
 * There extensions are available for Hibernate and QueryDSL. Note that if you want to use them in QueryDSL, you still need register them
 * in the Hibernate SessionFactory first.
 * </p>
 * 
 * <h4>Hibernate</h4>
 * 
 * <p>
 * 	There are several dialect extensions defined in this package. They are helped 
 * by HibernateDialectExtensions, which contain more documentation
 * </p>
 * 
 * <h4>QueryDSL</h4>
 * 
 * <strong>
 * 	Note : for them to work you need to use class {@link org.squashtest.tm.domain.jpql.ExtendedHibernateQuery} instead of the regular
 * 	HibernateQuery. Use the
 * 	constructors immediately, there is no factory for it (and you don't need any actually).
 * </strong>
 * 
 * <p>The features are available both for the fluent API and the PathBuilder API.</p>
 * 
 * <p>
 * 	For the PathBuilder API you may use new operators defined in {@link org.squashtest.tm.domain.jpql.ExtOps}.
 * Bits of documentation can be found in {@link org.squashtest.tm.domain.jpql.ExtOps}, {@link org.squashtest.tm.domain.jpql.SessionFactoryEnhancer}.
 * </p>
 * 
 * 
 * <p> For the fluent API:
 * 	<ul>
 * <li>
 * 		any String-typed can now invoke .groupConcat(path) or .orderedGroupConcat(coincatPath, 'order by', sortPath[, 'asc|desc'])
 * </li>
 * <li>The aggregate numeric functions are methods of ExtendedHibernateQuery itself : you can invoke on them query.s_sum() for instance</li>
 * </ul>. Examples :
 * 
 * <table>
 * 	<tr>
 * 		<td>querydsl expression</td><td>generated HQL</td>
 * 	</tr>
 * 	<tr>
 * 		<td><pre>testCase.name.orderedGroupConcat(project.id, 'asc')</pre></td>
 * 		<td><pre>group_concat(testCase.name, 'order by', project.id, 'asc')</pre></td>
 * </tr>
 * <tr>
 * 		<td>(assume subquery is 'select st.id from TestCase.steps st');<pre> mainquery.select(tc.name, subquery.s_avg())</pre></td>
 * 		<td><pre>select tc.name, s_avg((select st.id from TestCase.steps st))</pre></td>
 * </tr>
 * 
 * </table>
 * 
 * </p>
 * <p>
 * Main documentation on that is in {@link org.squashtest.tm.domain.jpql.SessionFactoryEnhancer}.
 * </p>
 * 
 * 
 */
package org.squashtest.tm.domain.jpql;