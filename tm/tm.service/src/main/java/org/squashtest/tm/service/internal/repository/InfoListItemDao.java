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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemListItem;

public interface InfoListItemDao extends CustomInfoListItemDao, JpaRepository<InfoListItem, Long> {

	@Query(name = "systemListItem.getSystemRequirementCategory")
	SystemListItem getSystemRequirementCategory();

	@Query(name = "systemListItem.getSystemTestCaseNature")
	SystemListItem getSystemTestCaseNature();

	@Query(name = "systemListItem.getSystemTestCaseType")
	SystemListItem getSystemTestCaseType();

	@Query
	InfoListItem findByCode(@Param("code") String code);

	@Query
	InfoListItem findDefaultRequirementCategory(@Param("projectId") long projectId);

	@Query
	InfoListItem findDefaultTestCaseNature(@Param("projectId") long projectId);

	@Query
	InfoListItem findDefaultTestCaseType(@Param("projectId") long projectId);


}
