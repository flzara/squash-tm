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
package org.squashtest.tm.domain.library.structures;

import java.util.*;
import java.util.Map.Entry;

public class StringPathMap<T> {

	private Map<String, T> map = new HashMap<>();


	public void put(String path, T ref) {
		map.put(path, ref);
	}

	public T getMappedElement(String path) {
		return map.get(path);
	}

	public String getPath(T needle) {
		for (Entry<String, T> entry : map.entrySet()) {
			T ref = entry.getValue();
			if (ref.equals(needle)) {
				return entry.getKey();
			}
		}
		return null;
	}


	/**
	 * given a path, will return paths corresponding to children present in the map.
	 *
	 * @param path
	 * @return
	 */
	public List<String> getKnownChildrenPath(String path) {

		if (!map.containsKey(path)) {

			return Collections.emptyList();

		} else {
			List<String> children = new LinkedList<>(); // TODO why a linked list ?
			for (String p : map.keySet()) {
				if (p.matches("^" + path + "/?[^/]+$")) {
					children.add(p);
				}
			}
			return children;
		}
	}


	/**
	 * Given a path begining with a '/', will return all the names composing the path. The first returned element will always be
	 * "/", which means the root of course.
	 *
	 */
	public static List<String> tokenizePath(String path) {
		List<String> tokens = new LinkedList<>();

		String[] toks = path.split("/");

		tokens.add("/");
		tokens.addAll(Arrays.asList(toks));
		tokens.remove("");
		return tokens;
	}

}
