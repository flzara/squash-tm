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

import org.squashtest.tm.exception.DomainException;

/**
 * This should be raised when an option already exists for a given custom-field.
 *  @author mpagnon
 */
public class OptionAlreadyExistException extends DomainException {
	/**
	 *TODO make serial version , my eclipse can't.
	 */
	private static final long serialVersionUID = 1L;
	private final String label;
	private static final String FIELD_NAME = "label";

	public OptionAlreadyExistException(String label) {
		super("The option '" + label + "' already exist for the custom-field", FIELD_NAME);
		this.label = label;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.tm.core.foundation.i18n.Internationalizable#getI18nKey()
	 */
	@Override
	public String getI18nKey() {
		return "message.exception.optionAlreadyExists";
	}

	@Override
	public Object[] getI18nParams() {
		return new Object[] {label};
	}

}
