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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;

/**
 * @author Gregory Fouquet
 * 
 */
abstract class TemplateNamedParamsExecution implements QueryExecution<Query> {
	private final Map<String, ?> namedParameters;

	public TemplateNamedParamsExecution(Map<String, ?> namedParameters) {
		super();
		this.namedParameters = namedParameters;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R executeQuery(Query query) {
		for (Entry<String, ?> entry : namedParameters.entrySet()) {
			setParam(query, entry);
		}
		return (R) results(query);
	}

	/**
	 * This method should return the results of given query.
	 * 
	 * @param query
	 * @return
	 */
	protected abstract Object results(Query query);

	private void setParam(Query query, Entry<String, ?> entry) {
		Object param = entry.getValue();

		if (param instanceof Collection) {
			query.setParameterList(entry.getKey(), (Collection<?>) param);

		} else if (param instanceof Object[]) {
			query.setParameterList(entry.getKey(), (Object[]) param);

		} else {
			query.setParameter(entry.getKey(), entry.getValue());

		}
	}
}
