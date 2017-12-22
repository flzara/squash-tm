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
package org.squashtest.csp.core.infrastructure.dynamicdao;

import java.util.List;

import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;


/**
 * @author Gregory Fouquet
 *
 */
public interface DummyDao extends CustomDummyDao {
	DummyEntity findById(long id);
	
	DummyEntity findById(String id);

	DummyEntity findByNameAndSuperpower(String name, String superpower);
	
	DummyEntity findBySuperpowerPaged(String superpower, Paging paging);

	List<DummyEntity> findAllByNameAndSuperpower(String name, String superpower);
	
	void delete(DummyEntity entity);

	void remove(DummyEntity entity);
	
	void persist(DummyEntity entity);
	
	List<DummyEntity> findAllBySuperpowers(List<String> superpowers);

	List<DummyEntity> findAll(PagingAndSorting pagingAndSorting);
}
