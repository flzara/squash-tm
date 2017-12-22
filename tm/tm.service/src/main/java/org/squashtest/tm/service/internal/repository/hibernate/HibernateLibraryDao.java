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

import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.Query;
import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.service.internal.repository.LibraryDao;

/**
 * Superclass for DAOs of {@link Library} objects.
 *
 * @author Gregory Fouquet
 *
 * @param <LIBRARY>
 * @param <NODE>
 */
public abstract class HibernateLibraryDao<LIBRARY extends Library<NODE>, NODE extends LibraryNode> extends
HibernateEntityDao<LIBRARY> implements LibraryDao<LIBRARY, NODE>{
	private final String entityClassName;

	public HibernateLibraryDao() {
		super();
		entityClassName = WordUtils.uncapitalize(entityType.getSimpleName());
	}

	@Override
	public List<LIBRARY> findAll(){
		return executeListNamedQuery(entityClassName+".findAll");
	}
	/**
	 * Finds the library root content. Template method which invokes a named query named
	 * "{libraryUnquilifiedClassName}.findAllRootContentById" with a parameter named "libraryId"
	 */
	@Override
	public /*final*/ List<NODE> findAllRootContentById(final long libraryId) {
		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setLong("libraryId", libraryId);
			}
		};

		return executeListNamedQuery(entityClassName + ".findAllRootContentById", callback);
	}

	@SuppressWarnings("unchecked")
	@Override
	public LIBRARY findByRootContent(final NODE node) {
		SetQueryParametersCallback callback = new SetQueryParametersCallback() {

			@Override
			public void setQueryParameters(Query query) {
				query.setParameter("content", node);

			}

		};

		return (LIBRARY) executeEntityNamedQuery(entityClassName + ".findByRootContent", callback);
	}






}
