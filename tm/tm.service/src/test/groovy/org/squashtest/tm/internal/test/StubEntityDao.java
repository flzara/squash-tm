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
package org.squashtest.tm.internal.test;

import org.squashtest.tm.service.internal.repository.EntityDao;

import java.util.Collection;
import java.util.List;

/**
 * IGNOREVIOLATIONS:FILE This is for test purpose
 * @author Gregory Fouquet
 *
 */
class StubEntityDao<ENTITY> implements EntityDao<ENTITY> {

	/**
	 * @see org.squashtest.tm.service.internal.repository.EntityDao#findById(long)
	 */
	//@Override
	public ENTITY findById(long id) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.EntityDao#findAllByIdd(java.util.Collection)
	 */
	//@Override
	public List<ENTITY> findAllByIds(Collection<Long> id) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.EntityDao#persist(java.lang.Object)
	 */
	//@Override
	public void persist(ENTITY transientEntity) {
		// NOOP

	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.EntityDao#remove(java.lang.Object)
	 */
	//@Override
	public void remove(ENTITY entity) {
		// NOOP
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.EntityDao#flush()
	 */
	//@Override
	public void flush() {
		// NOOP

	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.EntityDao#persist(List)
	 */
	//@Override
	public void persist(List<ENTITY> transientEntities) {
		// NOOP

	}

	//@Override
	public List<ENTITY> findAll() {
		return null;
	}

	//@Override
	public void clearFromCache(ENTITY entity) {
		// NOOP

	}

	//@Override
	public void clearFromCache(Collection<ENTITY> entities) {
		// NOOP

	}

	//@Override
	public void removeAll(List<ENTITY> entities) {
		// TODO Auto-generated method stub

	}

}
