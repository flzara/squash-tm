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
package org.squashtest.tm.service.infolist;

import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.infolist.SystemListItem;

public interface InfoListItemFinderService {

	// ********** System (default) item methods ********

	SystemListItem getSystemRequirementCategory();

	SystemListItem getSystemTestCaseNature();

	SystemListItem getSystemTestCaseType();


	// ********** General methods ************

	InfoListItem findById(Long id);

	InfoListItem findByCode(String code);

	InfoListItem findReference(ListItemReference reference);

	InfoListItem findDefaultRequirementCategory(long projectId);

	InfoListItem findDefaultTestCaseNature(long projectId);

	InfoListItem findDefaultTestCaseType(long projectId);

	/**
	 * Tells whether the given item belongs to the categories assigned to this project
	 * 
	 * @param projectId
	 * @param itemCode
	 * @return
	 */
	boolean isCategoryConsistent(long projectId, String itemCode);

	/**
	 * Tells whether the given item belongs to the natures assigned to this project
	 * 
	 * @param projectId
	 * @param itemCode
	 * @return
	 */
	boolean isNatureConsistent(long projectId, String itemCode);

	/**
	 * Tells whether the given item belongs to the types assigned to this project
	 * 
	 * @param projectId
	 * @param itemCode
	 * @return
	 */
	boolean isTypeConsistent(long projectId, String itemCode);



}
