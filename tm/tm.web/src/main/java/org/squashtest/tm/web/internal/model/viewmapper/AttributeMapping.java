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
package org.squashtest.tm.web.internal.model.viewmapper;

import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper.Mapping;

class AttributeMapping implements Mapping{

	private String attribute;

	private Class<?> ownerType;

	
	public AttributeMapping(String attribute, Class<?> ownerType) {
		super();
		this.attribute = attribute;
		this.ownerType = ownerType;
	}


	@Override
	public String getMapping() {
		return ownerType.getSimpleName() + "." + attribute;
	}
	
}