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
package org.squashtest.tm.service.internal.testautomation;

import java.util.Map;

import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * Builds parameters hash which shall be passed when executing an automated test.<br/>
 * 
 * Builder sould be discarded after the {@link ParametersBuilder#build()} method is invoked.<br/>
 * 
 * Usage :
 * <pre>
 * Map params = builder.testCase()
 *                         .addEntity(testCase)
 *                         .addCustomFields(testCaseFields)
 *                     .iteration()
 *                         .addCustomFields(iterationFields)
 *                     .build();
 * </pre>
 * 
 * @author Gregory Fouquet
 * 
 */
public interface ParametersBuilder {
	/**
	 * opens the test case scope
	 * 
	 * @return
	 */
	ScopedParametersBuilder<TestCase> testCase();

	/**
	 * opens the iteration scope
	 * 
	 * @return
	 */
	ScopedParametersBuilder<Iteration> iteration();

	/**
	 * opens the campaign scope
	 * 
	 * @return
	 */
	ScopedParametersBuilder<Campaign> campaign();

	/**
	 * builds the parameter map
	 * 
	 * @return
	 */
	Map<String, Object> build();
}
