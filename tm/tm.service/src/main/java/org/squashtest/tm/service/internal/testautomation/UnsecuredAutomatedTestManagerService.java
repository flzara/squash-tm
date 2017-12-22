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

import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.testautomation.AutomatedTestFinderService;

/**
 * This one is called "Unsecured" because many (if not all) methods aren't properly secured. This interface should never
 * be exposed through OSGI, and the methods accessed via secured services instead.
 * 
 * @author bsiri
 * 
 */
public interface UnsecuredAutomatedTestManagerService extends AutomatedTestFinderService{

	// ************************ Entity management **********************

	TestAutomationProject findProjectById(long projectId);

	/**
	 * Will persist this test if really new, or return the persisted instance if this test already exists. Due to this
	 * the calling code should always rely on the returned instance of AutomatedTest.
	 * 
	 * @param newTest
	 */
	AutomatedTest persistOrAttach(AutomatedTest newTest);

	/**
	 * Will remove the test from the database, if and only if no TestCase nor AutomatedExecutionExtender still refer to
	 * it.
	 * 
	 * @param test
	 */
	void removeIfUnused(AutomatedTest test);



}
