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

import org.squashtest.tm.domain.jpql.ExtOps.ConcatOrder;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;


/**
 * Adds the non standard groupConcat() function to string paths.
 * 
 * See {@link ExtOps} and {@link SessionFactoryEnhancer} for more about for more about
 * about this.
 * 
 * 
 * @author bsiri
 *
 */
public class QueryDslExtensions {

	/**
	 * The simple, no arg implementation.
	 * 
	 * @param string
	 * @return
	 */
	@QueryDelegate(String.class)
	public static StringExpression groupConcat(StringPath attributeConcat){
		return Expressions.stringOperation(ExtOps.GROUP_CONCAT, attributeConcat);
	}

	/**
	 * Same, and precises on which column (attribute path) it should be ordered
	 * 
	 */

	@QueryDelegate(String.class)
	public static StringExpression orderedGroupConcat(StringPath attributeConcat, StringPath attributeOrder){
		return Expressions.stringOperation(ExtOps.ORDERED_GROUP_CONCAT, attributeConcat, Expressions.constant("order by"), attributeOrder);
	}


	/**
	 * Same, and precises on which column (attribute path) and how it should be ordered
	 * 
	 */

	@QueryDelegate(String.class)
	public static StringExpression orderedGroupConcat(StringPath attributeConcat, StringPath attributeOrder, ConcatOrder order){
		return Expressions.stringOperation(ExtOps.ORDERED_GROUP_CONCAT_DIR, attributeConcat, Expressions.constant("order by"), attributeOrder, Expressions.constant(order.toString().toLowerCase()));
	}

}
