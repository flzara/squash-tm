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
package org.squashtest.tm.exception.customfield;

import org.squashtest.tm.core.foundation.exception.ActionException;

/**
 * Created by jthebault on 20/07/2016.
 */
public class WrongCufNumericFormatException extends ActionException {
	/**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;

	private static final String WRONG_NUMERIC_CUF_VALUE_KEY = "message.cuf.value.wrong.numeric.value";

	public WrongCufNumericFormatException() {
		super();
	}

	public WrongCufNumericFormatException(Exception cause) {
		super(cause);
	}

	@Override
	public String getI18nKey() {
		return WRONG_NUMERIC_CUF_VALUE_KEY;
	}

}
