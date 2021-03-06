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
package org.squashtest.tm.service.query;

import com.querydsl.core.Tuple;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.jpql.ExtendedHibernateQuery;

import java.util.List;

@Transactional(readOnly = true)
public interface QueryProcessingService {


	/**
	 * Will execute the query as configured (with paging and scope), and returns the
	 * resultset as a list of tuples.
	 *
	 * The effects of missing bits of configuration in the ConfiguredQuery are explained
	 * in ConfiguredQuery itself.
	 *
	 * @param configuredQuery
	 * @return
	 */
	List<Tuple> executeQuery(ConfiguredQuery configuredQuery);

	/**
	 * <p>
	 * Will build the query according to the specified ConfiguredQuery without executing it :
	 * the resulting ExtendedHibernateQuery is returned instead.
	 * </p>
	 * <p>Note that the query is detached
	 * (it has no session yet). You will need to clone it and attach to a session before you running it.
	 * </p>
	 *
	 * @param configuredQuery
	 * @return
	 */
	ExtendedHibernateQuery prepareQuery(ConfiguredQuery configuredQuery);

}
