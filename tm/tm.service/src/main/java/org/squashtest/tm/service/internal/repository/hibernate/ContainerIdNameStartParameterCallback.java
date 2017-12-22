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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.Query;

class ContainerIdNameStartParameterCallback implements SetQueryParametersCallback {
	private long containerId;
	private String nameStart;

	ContainerIdNameStartParameterCallback(long containerId, String nameStart) {
		this.containerId = containerId;
		this.nameStart = nameStart;
	}
	@Override
	public void setQueryParameters(Query query) {
		query.setParameter("containerId", containerId);
		query.setParameter("nameStart", nameStart + "%");
	}
}