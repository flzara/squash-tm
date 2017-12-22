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
package org.squashtest.tm.web.internal.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

/**
 * Helper class to manipulate jsTree related data.
 * 
 * @author Gregory Fouquet
 * 
 */
public final class JsTreeHelper {

	/**
	 * 
	 */
	private JsTreeHelper() {
		super();
	}

	/**
	 * Coerces an array of dom nodes ids (["#TestCase-10", "#TestCaseLibrary-20"]) into a map. The result maps entities
	 * ids by their short class name ([TestCase: [10], TestCaseLibrary: [20]]).
	 * 
	 * @param domNodesIds
	 * @return
	 */
	public static MultiMap mapIdsByType(String[] domNodesIds) {
		MultiMap res = new MultiValueMap();

		Pattern pattern = Pattern.compile("(\\w+)-(\\d+)");
		
		for (String domNodeId : domNodesIds) {
			Matcher matcher = pattern.matcher(domNodeId);

			while (matcher.find()) {
				if (matcher.groupCount() == 2) { // extra cautious not to get a null group below
					String type = matcher.group(1);
					Long id = Long.valueOf(matcher.group(2)); // the regexp pattern
					res.put(type, id);

				}
			}
		}

		return res;
	}

}
