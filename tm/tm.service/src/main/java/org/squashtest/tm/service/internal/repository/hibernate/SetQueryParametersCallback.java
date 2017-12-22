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

import org.hibernate.Query;

/**
 * Callback object invoked by {@link HibernateDao} when executing a query.
 *
 * @author Gregory Fouquet
 * @see HibernateDao#executeListNamedQuery(String, SetQueryParametersCallback)
 */
@Deprecated
public interface SetQueryParametersCallback {
	/**
	 * This method should set the query parameters so it can be executed.
	 *
	 * @param query
	 */
	void setQueryParameters(Query query);
}
