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
package org.squashtest.tm.service.internal.batchimport.excel;

/**
 * Thrown when trying to set a mandatory property to null.
 * 
 * @author Gregory Fouquet
 * 
 */
public class NullMandatoryValueException extends RuntimeException {
	private static final long serialVersionUID = -709696016697558891L;

	public final String propertyName; // NOSONAR immutable public field

	/**
	 * @param propertyName
	 */
	public NullMandatoryValueException(String propertyName) {
		super("Cannor set mandatory property '" + propertyName + "' to `null`");
		this.propertyName = propertyName;
	}

}
