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
package org.squashtest.tm.exception.execution;

import org.squashtest.tm.core.foundation.exception.ActionException;

public class TestPlanTerminatedOrNoStepsException extends ActionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private static final String ERROR_MESSAGE_KEY = "squashtm.action.exception.testsuite.testplan.terminated.or.no.steps";
	private static final String ERROR_MESSAGE = " no execution is to be resumed because : all terminated, or no execution-step on executions";
	public TestPlanTerminatedOrNoStepsException() {
		super(ERROR_MESSAGE);
	}
	
	public TestPlanTerminatedOrNoStepsException(Exception e){
		super(ERROR_MESSAGE, e);
	}
	@Override
	public String getI18nKey() {
		return ERROR_MESSAGE_KEY;
	}

}
