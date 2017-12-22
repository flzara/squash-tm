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
package org.squashtest.tm.service.internal.workspace;

import org.jooq.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StubFunctions {

	public static Function<Record, Map<String,Object>> getLeftTupleTransformer(){
		return record -> {
			Map<String, Object> map = new HashMap<>();
			map.put("LEFT_ID", record.get("LEFT_ID"));
			map.put("LEFT_NAME", record.get("LEFT_NAME"));
			map.put("LEFT_ATTR", record.get("LEFT_ATTR"));
			return map;
		};
	}

	public static Function<Record, Map<String,Object>> getRightTupleTransformer(){
		return record -> {
			if(record.get("RIGHT_ID") == null){
				return null;
			}
			Map<String, Object> map = new HashMap<>();
			map.put("RIGHT_ID", record.get("RIGHT_ID"));
			map.put("RIGHT_NAME", record.get("RIGHT_NAME"));
			return map;
		};
	}

	public static BiConsumer<Map<String,Object>,List<Map<String,Object>>> injector (){
		return (keyMap, valueMaps) -> {
			keyMap.put("RIGHT_ELEMENTS", valueMaps);
		};
	}


}
