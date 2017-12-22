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
package org.squashtest.tm.web.internal.controller.customfield;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.service.internal.dto.CustomFieldJsonConverter;
import org.squashtest.tm.service.internal.dto.CustomFieldValueModel;
import org.squashtest.tm.service.internal.dto.RawValueModel;

@Controller
@RequestMapping("/denormalized-fields/values")
public class DenormalizedFieldValuesController {

	private static final String DENORMALIZED_FIELD_HOLDER_TYPE = "denormalizedFieldHolderType";

	private static final String DENORMALIZED_FIELD_HOLDER_ID = "denormalizedFieldHolderId";

	@Inject
	private DenormalizedFieldValueManager denormalizedFieldValueFinder;

	@Inject
	private CustomFieldJsonConverter converter;


	@RequestMapping(method = RequestMethod.GET, params = { DENORMALIZED_FIELD_HOLDER_ID, DENORMALIZED_FIELD_HOLDER_TYPE }, headers = AcceptHeaders.CONTENT_JSON)
	@ResponseBody
	public List<CustomFieldValueModel> getDenormalizedFieldValuesForEntity(
			@RequestParam(DENORMALIZED_FIELD_HOLDER_ID) long id,
			@RequestParam(DENORMALIZED_FIELD_HOLDER_TYPE) DenormalizedFieldHolderType entityType) {

		List<DenormalizedFieldValue> values = denormalizedFieldValueFinder.findAllForEntity(id, entityType);

		return valuesToJson(values);

	}


	@RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes="application/json")
	@ResponseBody
	public void updateDenormalizedValue(@PathVariable long id, @RequestBody RawValueModel value) {
		denormalizedFieldValueFinder.changeValue(id, value.toRawValue());
	}

	private List<CustomFieldValueModel> valuesToJson(List<DenormalizedFieldValue> values) {
		List<CustomFieldValueModel> models = new LinkedList<>();

		for (DenormalizedFieldValue value : values) {
			CustomFieldValueModel model = converter.toJson(value);
			models.add(model);
		}

		return models;
	}
}
