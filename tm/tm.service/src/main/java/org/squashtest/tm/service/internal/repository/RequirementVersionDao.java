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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.List;

/**
 * @author Gregory Fouquet
 */
public interface RequirementVersionDao extends CrudRepository<RequirementVersion, Long>, CustomRequirementVersionDao {

	@Override
	@EmptyCollectionGuard
	List<RequirementVersion> findAll(Iterable<Long> ids);

	long countVerifiedByTestCase(long testCaseId);

	List<RequirementVersion> findAllByRequirement(Requirement node);

	Page<RequirementVersion> findAllByRequirementId(long requirementId, Pageable pageable);

	RequirementVersion findByRequirementIdAndVersionNumber(Long requirementId, Integer versionNumber);

	@EmptyCollectionGuard
	List<Long> findAllForMilestones(@Param("milestonesIds") List<Long> milestonesIds);
}
