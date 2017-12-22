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
package org.squashtest.tm.api.report.query;

import java.util.Map;

import org.squashtest.tm.api.report.criteria.Criteria;

/**
 * Interface for objects which encapsulate a query used to populate a report.
 * 
 * @author bsiri
 * @author Gregory Fouquet
 * 
 */
public interface ReportQuery {
	/**
	 * This method should run the method query (or queries) and populate a Map containing the dataset(s) mapped by the
	 * name expected by the report.
	 * 
	 * @param criteria
	 *            the criteria to apply to the request.
	 * @param model
	 *            a map which should be populated with the dataset(s)
	 * @return
	 */
	void executeQuery(Map<String, Criteria> criteria, Map<String, Object> model);
}
