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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.customfield.InputType;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory;
import org.squashtest.tm.service.internal.dto.CustomFieldValueModel;
import org.squashtest.tm.web.internal.controller.customfield.CustomFieldValuesController;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;

import javax.inject.Inject;
import java.io.IOException;

public class CleanCustomFieldValueSerializer extends JsonSerializer<String> {

	@Override
	public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

		Object target = jsonGenerator.getCurrentValue();
		if (CustomFieldValueModel.class.isAssignableFrom(target.getClass())) {
			CustomFieldValueModel model = (CustomFieldValueModel) target;
			String input = model.getBinding().getCustomField().getInputType().getEnumName();
			s = cleanCustomFieldValue(s, input);
		} else if (CustomFieldModelFactory.SingleValuedCustomFieldModel.class.isAssignableFrom(target.getClass())){
			CustomFieldModelFactory.SingleValuedCustomFieldModel model = (CustomFieldModelFactory.SingleValuedCustomFieldModel) target;
			String input = model.getInputType().getEnumName();
			s = cleanCustomFieldValue(s,input);
		}
		jsonGenerator.writeString(s);

	}

	public String cleanCustomFieldValue(String s, String input) {
		if (input.equals(InputType.RICH_TEXT.name())) {
			String valueModel = HtmlUtils.htmlUnescape(s);
			s = HTMLCleanupUtils.cleanHtml(valueModel);
			s = HTMLCleanupUtils.cleanHtml(s);

		} else if (input.equals(InputType.PLAIN_TEXT.name())) {
			String valueModel = HtmlUtils.htmlUnescape(s);
			s = HTMLCleanupUtils.stripJavascript(valueModel);
			s = HTMLCleanupUtils.stripJavascript(s);


		} else {
			String valueModel = HtmlUtils.htmlUnescape(s);
			valueModel = HtmlUtils.htmlEscape(valueModel);
			s = HtmlUtils.htmlUnescape(valueModel);
		}
		return s;
	}

}
