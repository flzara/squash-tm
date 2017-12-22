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

import java.util.Collections;
import java.util.List;

/**
 * Composite exception. All component exceptions must be of the type DomainException
 *
 * @author mpagnon
 *
 */
public class CompositeDomainException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Component exceptions.
	 */
	private final List<? extends DomainException> exceptions;

	private static String buildMessage(List<?> exceptions) {
		StringBuilder sb = new StringBuilder("Exceptions with the following messages were thrown : [");
		for (Object ex : exceptions) {
			sb.append('\'').append(((Exception) ex).getMessage()).append("', ");

		}
		sb.append(']');
		return sb.toString();
	}

	public <T extends Exception> CompositeDomainException(List<? extends DomainException> exceptions) {
		super(buildMessage(exceptions));
		this.exceptions = exceptions;
	}

	/**
	 *
	 * @return unmodifiable view of exceptions.
	 */
	public List<DomainException> getExceptions() {
		return Collections.unmodifiableList(exceptions);
	}
}
