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

import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;

import java.util.List;

/**
 * Custom data access methods for {@link RequirementVersionLinkType}.
 *
 * @author jlor
 *
 */
public interface CustomRequirementVersionLinkTypeDao {

	/**
	 * Returns a paged and ordered list of all the {@link RequirementVersionLinkType}.
	 * @param pas
	 * @return Paged and sorted list of all existing RequirementVersionLinkTypes.
	 */
	List<RequirementVersionLinkType> getAllPagedAndSortedReqVersionLinkTypes(PagingAndSorting pas);
	boolean doesCodeAlreadyExist(String code);
	/**
	 * Check if the code is already used by another Type than the given one.
	 * */
	boolean doesCodeAlreadyExist(String newCode, Long linkTypeId);
}
