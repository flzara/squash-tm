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
package org.squashtest.tm.exception.user;

import org.squashtest.tm.core.foundation.exception.ActionException;

public class ActiveUserDeleteException extends ActionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2679054987340981477L;
	private static final String MESSAGE_KEY = "squashtm.action.exception.user.delete.current";

	public ActiveUserDeleteException(Exception ex) {
		super(ex);
	}

	public ActiveUserDeleteException(String message) {
		super(message);
	}

	public ActiveUserDeleteException(String message, Exception ex) {
		super(message, ex);
	}

	public ActiveUserDeleteException() {
		super();
	}

	@Override
	public String getI18nKey() {
		return MESSAGE_KEY;
	}

}
