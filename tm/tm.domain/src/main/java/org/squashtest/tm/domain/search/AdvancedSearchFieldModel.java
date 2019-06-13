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
package org.squashtest.tm.domain.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "type")
@JsonSubTypes({
	@Type(value = AdvancedSearchListFieldModel.class, name = "LIST"),
	@Type(value = AdvancedSearchMultiListFieldModel.class, name = "MULTILIST"),
	@Type(value = AdvancedSearchSingleFieldModel.class, name = "SINGLE"),
	@Type(value = AdvancedSearchTextFieldModel.class, name = "TEXT"),
	@Type(value = AdvancedSearchRangeFieldModel.class, name = "RANGE"),
	@Type(value = AdvancedSearchNumericRangeFieldModel.class, name = "NUMERIC_RANGE"),
	@Type(value = AdvancedSearchTimeIntervalFieldModel.class, name = "TIME_INTERVAL"),
	@Type(value = SearchCustomFieldTimeIntervalFieldModel.class, name = "CF_TIME_INTERVAL"),
	@Type(value = AdvancedSearchTagsFieldModel.class, name = "TAGS"),
	@Type(value = SearchCustomFieldCheckBoxFieldModel.class, name = "CF_CHECKBOX"),
	@Type(value = SearchCustomFieldNumericFieldModel.class, name = "CF_NUMERIC_RANGE"),
	@Type(value = SearchCustomFieldSingleFieldModel.class, name = "CF_SINGLE"),
	@Type(value = SearchCustomFieldTextFieldModel.class, name = "CF_TEXT"),
	@Type(value = SearchCustomFieldListFieldModel.class, name = "CF_LIST")})
public interface AdvancedSearchFieldModel {

	AdvancedSearchFieldModelType getType();

	/**
	 * Tells whether this search field model holds any data to filter on or if it has been
	 * left blank.
	 *
	 * @return
	 */
	@JsonIgnore
	boolean isSet();


}
