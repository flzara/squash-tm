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

import org.springframework.data.domain.Pageable;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.query.QueryModel;

import java.util.Collection;

/**
 *
 * A ConfiguredQuery is a {@link org.squashtest.tm.domain.query.QueryModel} with the required, context-dependant information it was missing.
 * While a QueryModel describes of how to model the desired data, a ConfiguredQuery brings the rest : pagination and scope. The pagination
 * is already a familiar concept. The scope defines a subset of data we are interested in, and can encompass one or two entities,
 * or everything in a project.
 *
 */
public class ConfiguredQuery {

	private QueryModel queryModel;

	public ConfiguredQuery(){
		super();
	}

	public ConfiguredQuery(QueryModel queryModel){
		this.queryModel = queryModel;
	}

	/**
	 * The scope defines the set of domain object instances that that chart will account for. An instance will be considered
	 * if it is either directly referenced by one of the EntityReference, of indirectly if it belongs to a referenced
	 * container object (folder etc).
	 *
	 * The scope will also be checked against the current user's ACLs. The ACls can further restrict the scope if one of
	 * the referenced entity cannot be accessed to by the user.
	 *
	 * If the scope is empty or null, the query will apply to the whole database and no ACLs will be checked.
	 *
	 */
	private Collection<EntityReference> scope;

	/**
	 * The range of results we want to retrieve. If left to null, no paging will be applied.
	 */
	private Pageable paging;


	public QueryModel getQueryModel() {
		return queryModel;
	}

	public void setQueryModel(QueryModel queryModel) {
		this.queryModel = queryModel;
	}

	public Collection<EntityReference> getScope() {
		return scope;
	}

	public void setScope(Collection<EntityReference> scope) {
		this.scope = scope;
	}

	public Pageable getPaging() {
		return paging;
	}

	public void setPaging(Pageable paging) {
		this.paging = paging;
	}
}
