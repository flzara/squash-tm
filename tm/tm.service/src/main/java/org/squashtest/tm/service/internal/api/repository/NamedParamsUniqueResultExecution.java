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
package org.squashtest.tm.service.internal.api.repository;

import org.hibernate.Query;

import java.util.Map;

/**
 * Execution of a query which uses named parameters and returns a singler row.
 * 
 * @author Gregory Fouquet
 * 
 */
public class NamedParamsUniqueResultExecution extends TemplateNamedParamsExecution {
	/**
	 * @param namedParameters
	 */
	public NamedParamsUniqueResultExecution(Map<String, ?> namedParameters) {
		super(namedParameters);
	}

	/**
	 * @param query
	 * @return the query's unique result.
	 * @see org.squashtest.tm.service.internal.api.repository.TemplateNamedParamsExecution#results(org.hibernate.Query)
	 */
	@Override
	protected Object results(Query query) {
		return query.uniqueResult();
	}

}
