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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public final class JsonHelper {
	private static JsonHelper INSTANCE;

	private final ObjectMapper objectMapper;

	@Inject
	public JsonHelper(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
		INSTANCE = this; // NOSONAR cannot be inited any other way
	}

	public static String serialize(Object value) throws JsonMarshallerException {
		try {
			return INSTANCE.objectMapper.writeValueAsString(value);
		} catch (IOException e) {
			throw new JsonMarshallerException(e);
		}
	}


	// temporary fix for issue 6305
	public static String serializeCustomfields(Collection<CustomFieldModel> cufs) throws JsonMarshallerException {
		try {
			ObjectWriter writer = INSTANCE.objectMapper.writer();
			TypeFactory tf = INSTANCE.objectMapper.getTypeFactory();
			Type type = ParameterizedTypeImpl.make(Collection.class, new Type[]{CustomFieldModel.class}, null);

			JavaType zetype = tf.constructType(type);
			writer = writer.forType(zetype);

			return writer.writeValueAsString(cufs);
		} catch (IOException e) {
			throw new JsonMarshallerException(e);
		}
	}

	public static Map<String, Object> deserialize(String json) throws
		IOException {
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};
		return INSTANCE.objectMapper.readValue(json, typeRef);
	}


	/**
	 * alias for {@link #serialize(Object)}
	 *
	 * @param value
	 * @return
	 */
	public static String marshall(Object value) throws JsonMarshallerException {
		return serialize(value);
	}

	/**
	 * alias for {@link #deserialize(String)}
	 *
	 * @param json
	 * @return
	 */
	public static Map<String, Object> unmarshall(String json) throws IOException {
		return deserialize(json);
	}
}
