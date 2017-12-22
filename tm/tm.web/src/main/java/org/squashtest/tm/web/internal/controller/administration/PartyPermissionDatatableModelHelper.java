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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.squashtest.tm.domain.users.ActivePartyDetector;
import org.squashtest.tm.domain.users.Party;
import org.squashtest.tm.domain.users.PartyProjectPermissionsBean;
import org.squashtest.tm.security.acls.PermissionGroup;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

public class PartyPermissionDatatableModelHelper extends DataTableModelBuilder<PartyProjectPermissionsBean> {

	private InternationalizationHelper messageSource;
	private Locale locale;
	private ActivePartyDetector activePartyDetector = new ActivePartyDetector();

	public PartyPermissionDatatableModelHelper(Locale locale, InternationalizationHelper messageSource) {
		this.locale = locale;
		this.messageSource = messageSource;
	}

	@Override
	protected Map<?, ?> buildItemData(PartyProjectPermissionsBean item) {

		Map<Object, Object> result = new HashMap<>();
		Party party = item.getParty();
		PermissionGroup group = item.getPermissionGroup();
		Boolean active = activePartyDetector.isActive(party);

		result.put("party-id", party.getId());
		result.put("party-active", active);
		result.put("party-name", party.getName());
		result.put("party-index", getCurrentIndex());
		result.put("permission-group", group);
		result.put("party-type", messageSource.internationalize("label." + party.getType().toLowerCase(), locale));
		result.put("empty-delete-holder", null);

		return result;

	}



}
