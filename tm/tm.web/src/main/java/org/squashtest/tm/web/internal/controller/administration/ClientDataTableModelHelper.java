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
import java.util.Set;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;

public class ClientDataTableModelHelper  extends DataTableModelBuilder<ClientDetails> {

	private InternationalizationHelper messageSource;
	private Locale locale;
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public InternationalizationHelper getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;
	}

	public ClientDataTableModelHelper(InternationalizationHelper messageSource) {
		this.messageSource = messageSource;
	}

	public String formatRegisteredRedirectUri(Set<String> registeredRedirectUri){
		StringBuilder builder = new StringBuilder();
		for(String uri : registeredRedirectUri){
			builder.append(uri).append(" ");
		}
		return builder.toString();
	}

	@Override
	protected Object buildItemData(ClientDetails item) {
		Map<String, Object> row = new HashMap<>(3);
		row.put("entity-id", item.getClientId());
		row.put("index", getCurrentIndex() +1);
		row.put("name", item.getClientId());
		row.put("secret", item.getClientSecret());
		row.put("redirect_uri", formatRegisteredRedirectUri(item.getRegisteredRedirectUri()));
		row.put("delete", "");

		return row;
	}


}
