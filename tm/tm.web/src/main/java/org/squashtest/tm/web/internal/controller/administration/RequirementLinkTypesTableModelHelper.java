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
package org.squashtest.tm.web.internal.controller.administration;

import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class RequirementLinkTypesTableModelHelper extends DataTableModelBuilder<RequirementVersionLinkType> {

	private InternationalizationHelper helper;
	private Locale locale = LocaleContextHolder.getLocale();

	public RequirementLinkTypesTableModelHelper(InternationalizationHelper helper){
		this.helper = helper;
	}

	@Override
	protected Object buildItemData(RequirementVersionLinkType type) {

		Map<String, String> row = new HashMap<>(8);

		row.put("type-id", type.getId().toString());
		row.put("type-index", Long.toString(getCurrentIndex()));
		row.put("type-role1", formatRole(type.getRole1()));
		row.put("type-role1-code", type.getRole1Code());
		row.put("type-role2", formatRole(type.getRole2()));
		row.put("type-role2-code", type.getRole2Code());
		row.put("type-is-default", Boolean.toString(type.isDefault()));
		row.put("empty-delete-holder", null);

		return row;
	}

	private String formatRole(String role) {
		return helper.getMessage(role, null, role, locale);
	}


}
