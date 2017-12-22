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
package org.squashtest.tm.core.foundation.collection;

public final class DefaultColumnFiltering implements ColumnFiltering{

	public static final DefaultColumnFiltering NO_FILTERING = new DefaultColumnFiltering();

	public DefaultColumnFiltering() {
		super();
	}

	@Override
	public boolean isDefined() {
		return false;
	}

	@Override
	public String getFilter(Integer index) {
		return "";
	}

	@Override
	public boolean hasFilter(Integer index) {
		return false;
	}

	@Override
	public String getFilter(String mDataProp) {
		return "";
	}

	@Override
	public boolean hasFilter(String mDataProp) {
		return false;
	}

	@Override
	public String getFilter(String mDataProp, int offset) {
		return "";
	}
}
