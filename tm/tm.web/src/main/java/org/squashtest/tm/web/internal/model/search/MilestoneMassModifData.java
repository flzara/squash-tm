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
package org.squashtest.tm.web.internal.model.search;

import java.util.Collection;

public class MilestoneMassModifData {

	private Collection<Long> checkedIds;
	private boolean isSamePerimeter;
	private boolean hasData;

	public Collection<Long> getCheckedIds() {
		return checkedIds;
	}
	public void setCheckedIds(Collection<Long> checkedIds) {
		this.checkedIds = checkedIds;
	}
	public boolean isSamePerimeter() {
		return isSamePerimeter;
	}
	public void setSamePerimeter(boolean isSamePerimeter) {
		this.isSamePerimeter = isSamePerimeter;
	}
	public boolean isHasData() {
		return hasData;
	}
	public void setHasData(boolean hasData) {
		this.hasData = hasData;
	}

}
