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
package org.squashtest.tm.domain.jpql;

import com.querydsl.core.types.Operator;

/**
 * <p>These Operators for QueryDsl represents the operators supported by our extensions
 * of JPQL (see {@link SessionFactoryEnhancer}).</p>
 * 
 * <h4>usage</h4>
 * 
 * <p>
 * 	Example :
 * 
 * 	<pre>Expressions.simpleOperation(Long.class, ExAggOps.S_SUM, mySubQueryExpression)</pre>
 * 	<pre>Expressions.simpleOperation(String.class, ExAggOps.ORDERED_GROUP_CONCAT_DIR, attrConcatPath, Expressions.constant('order by'), attrSortPath, Expressions.constant('asc'))</pre>
 * 
 *  The usage syntax for GROUP_CONCAT is explained in {@link SessionFactoryEnhancer}
 * </p>
 * 
 * </p>
 * 
 * 
 * @author bsiri
 *
 */
public enum ExtOps implements Operator {


	// the aggregate functions wrappers
	S_COUNT(Number.class),
	S_SUM(Number.class),
	S_MIN(Comparable.class),
	S_MAX(Comparable.class),
	S_AVG(Comparable.class),

	// group concat
	GROUP_CONCAT(String.class),
	ORDERED_GROUP_CONCAT(String.class),
	ORDERED_GROUP_CONCAT_DIR(String.class),

	// boolean case when
	TRUE_IF(Boolean.class),
	
	// by_day date operator
	YEAR_MONTH_DAY(Integer.class);

	private final Class<?> type;

	private ExtOps(Class<?> type){
		this.type = type;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	public static enum ConcatOrder{
		ASC,
		DESC;
	}

}
