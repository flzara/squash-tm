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
package org.squashtest.tm.exception.library;

import org.squashtest.tm.core.foundation.exception.ActionException;

/**
 * Thrown when trying to delete a TestCase that is called by another TestCase.
 *
 * @author Gregory Fouquet
 *
 */
public class NotDeletableException extends ActionException {

	private static final long serialVersionUID = -2560628563913390771L;
	private static final String NOT_DELETABLE_MESSAGE_KEY =  "squashtm.action.exception.notdeletable.label";

	private final long testCaseId;

	public NotDeletableException(long testCaseId) {
		super("Cannot delete TestCase[id:" + testCaseId + "] because it is called by another TestCase");
		this.testCaseId = testCaseId;
	}

	public long getTestCaseId() {
		return testCaseId;
	}
	
	@Override
	public String getI18nKey() {
		return NOT_DELETABLE_MESSAGE_KEY;
	}	

}
