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
package org.squashtest.tm.service.milestone;

import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.internal.dto.json.JsonMilestone;

import java.util.List;
import java.util.Map;

/**
 * Service Class dedicated to find, populate and return {@link JsonMilestone}.
 */
public interface MilestoneModelService {

	/**
	 * Find all {@link JsonMilestone} corresponding to {@link Milestone} linked to projects designed by ids
	 * @param readableProjectIds the ids of projects.
	 * @return a {@link Map} containing the milestones. Key = Milestone Id. Value = JsonMilestone
	 */
	Map<Long, JsonMilestone> findUsedMilestones(List<Long> readableProjectIds);

	/**
	 * Find the {@link JsonMilestone} corresponding to {@link Milestone} designed by the given id.
	 * @param milestoneId The id of the {@link Milestone}
	 * @return The {@link JsonMilestone}
	 */
	JsonMilestone findMilestoneModel(Long milestoneId);

	Map<Long, List<JsonMilestone>> findMilestoneByProject(List<Long> readableProjectIds);


}
