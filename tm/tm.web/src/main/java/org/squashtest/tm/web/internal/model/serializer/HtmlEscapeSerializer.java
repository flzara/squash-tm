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
package org.squashtest.tm.web.internal.model.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import java.io.IOException;

public class HtmlEscapeSerializer extends StdSerializer<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlEscapeSerializer.class);

	public HtmlEscapeSerializer() {
		super(String.class);
	}

	@Override
	public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("CLEANING STRING {}", s);
		}
		s=HtmlUtils.htmlUnescape(s);
		String cleaned = HtmlUtils.htmlEscape(s);
		jsonGenerator.writeString(cleaned);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("CLEANED STRING {}", cleaned);
		}
	}
}
