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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import java.io.IOException;

public class HtmlEscapeCustomFielOptionSerializer extends JsonSerializer<CustomFieldModelFactory.CustomFieldOptionModel> {

	@Override
	public void serialize(CustomFieldModelFactory.CustomFieldOptionModel option, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

		String label = HtmlUtils.htmlUnescape(option.getLabel());
		option.setLabel(HTMLCleanupUtils.stripJavascript(label));
		String code =  HtmlUtils.htmlUnescape(option.getCode());
		option.setCode(HTMLCleanupUtils.stripJavascript(code));
		jsonGenerator.writeObject(option);
	}
}
