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
package org.squashtest.tm.web.internal.report.criteria;

import java.util.Collection;
import java.util.Map;

/**
 * @author Gregory Fouquet
 *
 */
public class InconsistentMultiValuedEntryException extends RuntimeException {
	/**
	 * @param multiValued
	 */
	public InconsistentMultiValuedEntryException(Collection<Map<String, Object>> multiValued) {
		super(message(multiValued));
	}

	/**
	 * @param multiValued
	 * @return
	 */
	private static String message(Collection<Map<String, Object>> multiValued) {
		return "Inconsistent type in multi-valued form entry " + multiValued.toString();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2116033712812898030L;

}
