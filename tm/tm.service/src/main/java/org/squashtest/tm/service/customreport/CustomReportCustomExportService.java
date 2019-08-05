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
package org.squashtest.tm.service.customreport;

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;

import java.util.List;
import java.util.Map;

public interface CustomReportCustomExportService {

	/**
	 * Get the entity name from entity reference
	 * @param entityReference the entity selected
	 * @return entity name
	 */
	String getScopeEntityName(EntityReference entityReference);

	/**
	 * Get all custom fields data from itpis
	 * @param mainProjectId the project id
	 * @param itpis list of itpi
	 * @param map initial map
	 * @return map with cufs by entity
	 */
	Map<String, List<CustomFieldBindingModel>> getCustomFieldsData(
		Long mainProjectId, List<IterationTestPlanItem> itpis, Map<String, List<CustomFieldBindingModel>> map);

}
