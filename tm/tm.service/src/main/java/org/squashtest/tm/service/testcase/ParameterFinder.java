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
package org.squashtest.tm.service.testcase;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.Parameter;

/**
 * 
 * @author flaurens, mpagnon
 *
 */
@Transactional(readOnly = true)
public interface ParameterFinder {


	/**
	 * Given a test case id, will find the parameters that are proper to that test case. Inherited parameters
	 * will not be returned, for this see {@link #findAllParameters(long)} instead.
	 * 
	 * @param testCaseId
	 * @return returns a list of parameters ordered by name
	 */
	List<Parameter> findOwnParameters(long testCaseId);

	/**
	 * Will find all parameters for the test case along with all parameters found for the call steps that
	 * uses the parameter delegation mode.
	 * 
	 * @param testCaseId
	 * @return returns a list of parameters ordered by test case and name
	 */
	List<Parameter> findAllParameters(long testCaseId);


	/**
	 * Simply find the parameter matching the given id
	 * @param parameterId : the id of the {@link Parameter} to find
	 * @return the {@link Parameter} matching the id or <code>null</code>
	 */
	Parameter findById(long parameterId);

	/**
	 * Returns true if the parameter is used in one of it's test case action steps
	 * 
	 * @param parameterId : the id of the concerned parameter
	 * @return <code>true</code> if the parameter is used in one of it's test case action step.
	 */
	boolean isUsed(long parameterId);
}
