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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.project.Project;

import java.util.Collection;
import java.util.List;

public interface ProjectDao extends CustomProjectDao, JpaRepository<Project,Long> {

	Project findByName(String name);

	List<Project> findByIdIn(List<Long> ids);

	List<Project> findAllByOrderByName();

	@Query
	Collection<Project> findAllBoundToTemplate(@Param("templateId") long templateId);

	@Query
	Collection<Long> findAllIdsBoundToTemplate(@Param("templateId") long templateId);

	@Query
	@Modifying
	void unbindAllFromTemplate(@Param("templateId") long templateId);
}
