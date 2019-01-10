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
package org.squashtest.tm.core.scm.api.exception;

/**
 * Base class for exceptions concerning Source Code Management.
 */
public class ScmException extends RuntimeException {

	/**
	 * If the exception concerns a field.
	 */
	private String field;

	public ScmException(String message) {
		super(message);
	}

	public ScmException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScmException(String message, Throwable cause, String field) {
		super(message, cause);
		this.field = field;
	}

	public ScmException(Throwable cause) {
		super(cause);
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public boolean hasField() {
		return field != null && !field.isEmpty();
	}
}
