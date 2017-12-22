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
package org.squashtest.tm.web.internal.model.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.LevelComparator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LevelEnumSerializer extends JsonSerializer<EnumSet<? extends Level>> {

	@Override
	public void serialize(EnumSet<? extends Level> enumSet, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {

		List<? extends Level> levels = Arrays.asList(enumSet.toArray(new Level[enumSet.size()]));
		Collections.sort(levels, LevelComparator.getInstance());

		jgen.writeStartArray();

		for (Level lv : levels) {
			jgen.writeStartObject();
			jgen.writeStringField("name", ((Enum<?>) lv).name());
			jgen.writeNumberField("level", lv.getLevel());
			jgen.writeStringField("i18nkey", lv.getI18nKey());
			jgen.writeEndObject();
		}

		jgen.writeEndArray();

	}

}
