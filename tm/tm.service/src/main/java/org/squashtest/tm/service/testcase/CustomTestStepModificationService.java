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

import java.util.Map;

import org.squashtest.tm.domain.customfield.RawValue;

/**
 * Methods non automatically generated for TestStep Modification
 * @author mpagnon
 *
 */
public interface CustomTestStepModificationService  extends CustomTestStepFinder{

	/**
	 * Will update the TestStep of the given id with the given params.
	 * If TestStep is a CallStep params "action" and "expectedResult" are ignored.
	 * 
	 * @param testStepId
	 * @param action
	 * @param expectedResult
	 * @param cufValues
	 * 
	 */
	void updateTestStep(Long testStepId, String action, String expectedResult,
			Map<Long, RawValue> cufValues);

}
