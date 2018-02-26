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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.squashtest.tm.service.internal.dto.CustomFieldModelFactory;
import org.squashtest.tm.web.internal.model.serializer.CleanCustomFieldValueSerializer;
import org.squashtest.tm.web.internal.model.serializer.CleanedUnescapedStringSerializer;
import org.squashtest.tm.web.internal.model.serializer.HtmlEscapeCustomFielOptionSerializer;
import org.squashtest.tm.web.internal.model.serializer.HtmlEscapeSerializer;

import java.util.List;
import java.util.Set;

@JsonAutoDetect
public class SingleValuedCustomFieldModelMixin {
	@JsonSerialize(using = HtmlEscapeSerializer.class)
	private String name;

	@JsonSerialize(using = CleanedUnescapedStringSerializer.class)
	private String label;

	@JsonSerialize(using = HtmlEscapeSerializer.class)
	private String code;

	@JsonSerialize(using = CleanCustomFieldValueSerializer.class)
	private String defaultValue;


}
