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
package org.squashtest.tm.domain.testcase;


/**
 * <ul>
 * 	<li>CALLED_DATASET : the values for the parameters in a called test case will be assigned using one of the datasets from the called test case</li>
 * 	<li>DELEGATE : the values for the parameters in a called test case will be assigned using one of the datasets from the caller test case (that can itself be called and delegated)</li>
 * 	<li>NOTHING : we don't know yet how and when the parameters will be assigned.</li>
 * </ul>
 * 
 * @author bsiri
 *
 */
public enum ParameterAssignationMode {

	NOTHING,
	CALLED_DATASET,
	DELEGATE;
}
