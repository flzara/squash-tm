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
package org.squashtest.tm.internal.domain.report.query;

import java.util.Collection;

import org.squashtest.tm.plugin.report.std.service.DataFilteringService;

/**
 * 
 *  Abstract :
 *  ==========
 * 
 * This interface represents the query that the client needs to use to retrieve the data and build the model that will feed the view for 
 * a given Report. 
 * 
 * An implementation of the ReportQuery interface is both dedicated to :
 * 		- a given Report,
 * 		- a given implementation of the underlying repository.
 * 
 * 
 *  Data Filtering :
 *  ===============
 * 
 * As for SquashTM v 0.23.0 and future version, as long as the issue is not addressed, a Report plugin is granted full, unfiltered access to the 
 * repository. Until low level security routines are injected in the middle, it's up to the author of a Report to check his data against a
 * dedicated service that will be gracefully injected by the Report service engine. That service is {@link DataFilteringService} instance.   
 * 
 * The use of the DataFilteringService is tightly coupled to the implementation of {@see ReportQueryDao} and the actual content of the query.
 * Thus, there is no formal way to use that service in the scope of that interface, but let's say that the DataFilteringService should be used
 * between the moment the query is executed and the moment its result is returned. 
 *  
 *  Implementing the ReportQuery interface :
 *  ========================================================
 *  
 * 
 *  1/ The Implementation must match a particular repository.
 *  
 *  since the query is meant to be executed by a particular Dao, we need to know if the said Dao can support the particular implementation
 *  of the ReportQuery. This will be tested using a ReportQueryFlavor. You must ensure that your report will return the correct
 *  ReportQueryFlavor subclass when asked to, ie the one supported by the ReportQueryDao.
 *  
 *  
 *  2/ Narrowing the result set.
 *  
 *  A ReportQuery will fetch data, that may be filtered by several means :
 *  <ul> 
 *  	<li>Security checks (see above),</li>
 *  	<li>User filters (see above),</li>
 *  	<li>Criterions (see right below)</li>
 *  </ul>
 *  
 *  Security checks and 
 *  
 *  you must provide a (hidden) structure to maintain the criteria that will narrow the results when querying the repository.
 *  The constructor of the actual class must then define which criteria do exists and what are their names. Beyond the interface here
 *  the inner management of the criteria can of course be as complex as you want (type safety, request generator etc).
 *
 * - by convention giving a null value to a criterion means that the said criterion will be unused. However providing no value
 *   at all to a given criterion before execution means that the criterion will be either unused, either applied with default value,
 *   it's up to the choice of the implementor. From an user point of view, when in doubt, always set a value to all of the criterion.   
 * 
 * @author bsiri
 *
 */

public interface ReportQuery {
	
	/**
	 * @return an instance of a subclass of ReportQueryFlavor, that will identify which kind of Dao that Query should be processed through.
	 */
	ReportQueryFlavor getFlavor();
	
	
	/**
	 * setter for a DataFilteringService. Will be invoked by the system. See above, Data Filtering.
	 * 
	 * @param service
	 */
	void setDataFilteringService(DataFilteringService service);
	
	/**
	 * This method will set a criterion. Using a null value will nullify the use of that criterion.
	 * @param name the name of the criterion you will feed with one or several values.
	 * @param values an array (which can contains 0, 1 or more elements) containing the actual parameters.
	 */
	void setCriterion(String name, Object... values);
	
	/**
	 * @return the list of the criterion filtering that report.
	 */
	Collection<String> getCriterionNames();
	
	/**
	 * Checks if the given name matches one of the criterion used by this query.
	 * @param name the name one wants to check.
	 * @return true if the ReportQuery instance declares a criterion bearing that name, false otherwise. 
	 */
	boolean isCriterionExists(String name);
	
	
	/**
	 * @param name the name of the criterion one want to check or retrieve the actual paramters
	 * @return the parameters for that criterion, or null if the criterion was deactivated or if the criterion doesn't exists
	 * (use isCriterionExists() if you need to know what does null means in that later case).  
	 */
	Object[] getValue(String name);
	
	
}
