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

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.List;

/**
 * @author Gregory Fouquet
 */
public interface GenericProjectDao extends JpaRepository<GenericProject, Long>, CustomGenericProjectDao {

	long countByName(String name);

	/**
	 * @deprecated use findOne
	 */
	@Deprecated
	GenericProject findById(long projectId);

	@Query
	List<TestAutomationProject> findBoundTestAutomationProjects(@Param(ParameterNames.PROJECT_ID) long id);

	@Query
	List<String> findBoundTestAutomationProjectJobNames(@Param(ParameterNames.PROJECT_ID) long id);

	@Query
	List<String> findBoundTestAutomationProjectLabels(@Param(ParameterNames.PROJECT_ID) long projectId);

	@EmptyCollectionGuard
	List<GenericProject> findAllByIdIn(List<Long> idList, Sort sorting);

	@Query
	TestAutomationServer findTestAutomationServer(@Param(ParameterNames.PROJECT_ID) long projectId);


}
