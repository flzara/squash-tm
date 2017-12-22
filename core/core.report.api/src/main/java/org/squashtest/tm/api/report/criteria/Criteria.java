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
package org.squashtest.tm.api.report.criteria;

import org.squashtest.tm.api.report.form.InputType;

/**
 * @author Gregory Fouquet
 * 
 */
public interface Criteria {
	Object NO_VALUE = new Object();

	String getName();

	Object getValue();

	InputType getSourceInput();

	/**
	 * Indicates if this criteria has a value. When a criteria does not have a value, {@link #getValue()} should return
	 * {@link #NO_VALUE}. An example of a criteria with no value is a checkbox group with no selected checkbox.
	 * 
	 * @return false if this criteria has no value.
	 */
	boolean hasValue();

}
