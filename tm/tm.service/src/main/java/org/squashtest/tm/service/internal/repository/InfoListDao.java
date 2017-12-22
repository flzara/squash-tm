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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;

public interface InfoListDao  extends JpaRepository<InfoList, Long>, CustomInfoListDao {

	@Query
	InfoList findByCode(@Param("code") String code);

	@Query
	List<InfoList> findAllOrdered();

	@Modifying @Query
	void setDefaultCategoryForProject(@Param("projectId") long projectId, @Param("defaultItem") InfoListItem defaultItem);

	@Modifying @Query
	void setDefaultNatureForProject(@Param("projectId") long projectId, @Param("defaultItem") InfoListItem defaultItem);

	@Modifying @Query
	void setDefaultTypeForProject(@Param("projectId") long projectId, @Param("defaultItem") InfoListItem defaultItem);

	/**
	 * Fetches all InfoLists bound to at least 1 Project in their natural order
	 */
	List<InfoList> findAllBound();
	/**
	 * Fetches all InfoLists which are not bound to any Project in their natural order
	 */
	List<InfoList> findAllUnbound();
}
