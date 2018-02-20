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
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory;
import org.squashtest.tm.service.internal.dto.CustomFieldValueModel;
import org.squashtest.tm.web.internal.controller.customfield.CustomFieldValuesController;
import org.squashtest.tm.web.internal.util.HTMLCleanupUtils;
import org.unbescape.html.HtmlEscape;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HtmlEscapeCustomFielOptionsSerializer extends JsonSerializer<CustomFieldBindingModel> {

	@Inject
	private CustomFieldValuesController cufController;

	@Override
	public void serialize(CustomFieldBindingModel customFieldBindingModel, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
		Object target = jsonGenerator.getCurrentValue();
		if (CustomFieldValueModel.class.isAssignableFrom(target.getClass())) {
			CustomFieldValueModel model = (CustomFieldValueModel) target;
			String input = model.getBinding().getCustomField().getInputType().getEnumName();
			if (input.equals(InputType.DROPDOWN_LIST.name())) {
				cufController.escapeOptions(model);
			}
			jsonGenerator.writeObject(model.getBinding());

		}
	}
}
