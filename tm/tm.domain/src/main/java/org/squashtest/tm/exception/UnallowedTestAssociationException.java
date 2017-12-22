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
package org.squashtest.tm.exception;

import org.squashtest.tm.core.foundation.exception.ActionException;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * <p>Thrown when one tries to bind a {@link AutomatedTest} to a {@link TestCase},
 * while the test automation feature is disabled for the TM project hosting it or if the test automation project
 * isn't bound to the TM project</p>
 * 
 * @author bsiri
 *
 */
public class UnallowedTestAssociationException extends ActionException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private static final String MESSAGE_KEY = "testautomation.exceptions.unallowedassociation";


	@Override
	public String getI18nKey() {
		return MESSAGE_KEY;
	}




}
