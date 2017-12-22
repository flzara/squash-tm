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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DefaultDatatableMapper<KEY> implements DatatableMapper<KEY> {

	private Map<KEY, Mapping> mappings;

	public DefaultDatatableMapper() {
		super();
		mappings = new HashMap<>();
	}

	public DefaultDatatableMapper(int initialCapacity) {
		super();
		mappings = new HashMap<>(initialCapacity);
	}

	
	
	@Override
	public DatatableMapper<KEY> map(KEY key, Mapping mapping) {
		mappings.put(key,  mapping);
		return this;
	}
	
	@Override
	public DatatableMapper<KEY> map(KEY key, String expression) {
		mappings.put(key, new SimpleMapping(expression));
		return this;
	}
	
	@Override
	public DatatableMapper<KEY> mapAttribute(KEY key, String attribute, Class<?> ownerType) {
		AttributeMapping register = new AttributeMapping(attribute, ownerType);
		mappings.put(key, register);
		return this;
	}

	@Override
	public String getMapping(KEY key) {
		Mapping mapping = mappings.get(key);
		if (mapping != null) {
			return mapping.getMapping();
		} else {
			throw new NoSuchElementException("column '"+key+"' is not mapped");
		}
	}
	
}
