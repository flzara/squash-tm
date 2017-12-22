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
package org.squashtest.tm.service.campaign;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.milestone.Milestone;

public interface TestSuiteFinder {
	/**
	 * That method will find the test Suite by its ID
	 * 
	 * @param suiteId
	 */
	@Transactional(readOnly = true)
	TestSuite findById(long suiteId);



	/* ********************** milestones section ******************* */

	@Transactional(readOnly = true)
	Collection<Milestone> findAllMilestones(long suiteId);
}
