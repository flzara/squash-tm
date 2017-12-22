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
package org.squashtest.tm.service.requirement;

import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;

import java.util.List;
import java.util.Map;

/**
 * Created by jlor on 14/06/2017.
 */
public interface RequirementVersionLinkTypeManagerService {

	void addLinkType(RequirementVersionLinkType linkType);

	boolean doesLinkTypeCodeAlreadyExist(String code);
	boolean doesLinkTypeCodeAlreadyExist(String code, Long linkTypeId);

	void changeDefault(Long linkTypeId);

	Map<String, Boolean> changeRole1(Long linkTypeId, String newRole1);
	Map<String, Boolean> changeRole2(Long linkTypeId, String newRole2);

	Map<String, Boolean> changeCode1(Long linkTypeId, String newCode1);
	Map<String, Boolean> changeCode2(Long linkTypeId, String newCode2);

	boolean isLinkTypeDefault(Long linkTypeId);
	boolean isLinkTypeUsed(Long linkTypeId);

	void deleteLinkType(Long linkTypeId);

	boolean doesContainDefault(List<Long> linkTypesIdsToCheck);

	void deleteLinkTypes(List<Long> linkTypeIdsToDelete);

	boolean areCodesAndRolesConsistent(RequirementVersionLinkType linkType);

}
