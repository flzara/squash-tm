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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;

/**
 * Data access methods for {@link RequirementVersionLinkType}. Methods are all dynamically generated.
 *
 * @author jlor
 *
 * Note: This Dao uses NamedQueries written in hibernate/package-info.
 */
public interface RequirementVersionLinkTypeDao extends CrudRepository<RequirementVersionLinkType, Long>, CustomRequirementVersionLinkTypeDao {

	/**
	 * Get the only {@link RequirementVersionLinkType} set as the Default one.
	 * @return
	 */
	RequirementVersionLinkType getDefaultRequirementVersionLinkType();
	/**
	 * Find all the RequirementVersionLinkTypes that exist.
	 */
	List<RequirementVersionLinkType> getAllRequirementVersionLinkTypes();

	@Query("from RequirementVersionLinkType where role1Code = :roleCode or role2Code = :roleCode")
	RequirementVersionLinkType findByRoleCode(@Param("roleCode") String roleCode);

	boolean isLinkTypeDefault(@Param("linkTypeId") Long linkTypeId);

	boolean isLinkTypeUsed(@Param("linkTypeId") Long linkTypeId);
}
