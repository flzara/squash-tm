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
package org.squashtest.tm.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 * 	Spring repositories methods annotated with this will not crash when running with {@link Iterable} parameters which could be empty.
 * </p>
 * 
 * <p>
 * 	The default behavior of a Database, when receiving a query with empty list arguments - ie, textually '()' - is to return 
 * 	a query syntax error (instead of a more meaningful error message). This forces us to program defensively and prevent 
 * 	queries to run on the DB if the parameters would make it fail.  
 * </p>
 *  
 *  <p>
 *   If you want to reduce this boilerplate you may annotate a DAO method with this annotation, in which case the behavior specified
 *   in {@link SpringDaoMetaAnnotationAspect} will be applied, essentially returning 'no results'. The meaning of 'no results' may 
 *   vary depending on the expected return type : Object, primitive type, or a collection of these (see the doc on the aspect for 
 *   details). 
 *  </p>
 *  
 *  <p>Of course in some case you don't want the result to be "no result". In that case, just don't use that annotation.</p>
 * 
 * 
 * @author bsiri
 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EmptyCollectionGuard {
}
