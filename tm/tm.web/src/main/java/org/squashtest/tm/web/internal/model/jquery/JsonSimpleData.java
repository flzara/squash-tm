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
package org.squashtest.tm.web.internal.model.jquery;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

// TODO this class does Jackson's work ie JSON marshalling. @Controllers can return Map or custom objects, they will be marshalled to JSON automatically.
public class JsonSimpleData {

	private List<String> attrList = new LinkedList<>();

	public JsonSimpleData addAttr(String attrName, String attrValue) {
		attrList.add("\"" + attrName + "\" : \"" + attrValue + "\"");
		return this;
	}

	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();

		toReturn.append("{ ");

		ListIterator<String> iterator = attrList.listIterator();

		while (iterator.hasNext()) {
			toReturn.append(iterator.next());
			if (iterator.hasNext()) {
				toReturn.append(" , ");
			}
		}

		toReturn.append(" }");

		return toReturn.toString();

	}

}
