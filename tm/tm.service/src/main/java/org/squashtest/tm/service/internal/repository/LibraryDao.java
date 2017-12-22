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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.domain.library.Library;
import org.squashtest.tm.domain.library.LibraryNode;

/**
 * Defines a DAO for {@link Library} entities.
 * 
 * @author Gregory Fouquet
 * 
 * @param <LIBRARY>
 *            Type of the library entity
 * @param <NODE>
 *            Type of the {@link LibraryNode} entities contained by the library.
 */
public interface LibraryDao<LIBRARY extends Library<? extends NODE>, NODE extends LibraryNode> {

	List<LIBRARY> findAll();

	List<NODE> findAllRootContentById(long libraryId);

	LIBRARY findById(long id);

	/**
	 * Finds the library which has the given node in its root content.
	 * 
	 * @param node
	 * @return
	 */
	LIBRARY findByRootContent(NODE node);

}