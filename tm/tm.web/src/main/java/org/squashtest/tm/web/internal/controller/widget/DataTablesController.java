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
package org.squashtest.tm.web.internal.controller.widget;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Gregory Fouquet
 *
 */
@Controller
public class DataTablesController {
	@Inject
	private MessageSource messageSource;

	private Map<String, Object> legacyMessages;
	private Map<String, Object> messages;
	private Locale locale = Locale.getDefault();

	private void setLocale(Locale paramLocale) {
		if (paramLocale != null) {
			locale = paramLocale;
		} else {
			locale = Locale.getDefault();
		}
	}

	/**
	 * Internationalized messages for Datatable < 1.10
	 *
	 * @param paramLocale
	 * @return
	 */
	@RequestMapping("/datatables/messages")
	@ResponseBody
	public Map<String, Object> getInternationalizedMessages(Locale paramLocale) {

		/* Issue #6782:
		* The messages were loaded only once, even if the Locale was changed.
		* We now compare if the Locale has changed since the previous loading
		* and reload messages if it is the case. */

		if (legacyMessages == null || !locale.equals(paramLocale)) {

			setLocale(paramLocale);

			legacyMessages = new HashMap<>();

			legacyMessages.put("sLengthMenu", messageSource.getMessage("generics.datatable.lengthMenu",null, paramLocale));
			legacyMessages.put("sZeroRecords", messageSource.getMessage("generics.datatable.zeroRecords",null, paramLocale));
			legacyMessages.put("sInfo", messageSource.getMessage("generics.datatable.info",null, paramLocale));
			legacyMessages.put("sInfoEmpty", messageSource.getMessage("generics.datatable.infoEmpty",null, paramLocale));
			legacyMessages.put("sInfoFiltered", messageSource.getMessage("generics.datatable.infoFiltered",null, paramLocale));
			legacyMessages.put("sSearch", messageSource.getMessage("generics.datatable.search",null, paramLocale));

			Map<String, Object> pagination = new HashMap<>();
			legacyMessages.put("oPaginate", pagination);

			pagination.put("sFirst", messageSource.getMessage("generics.datatable.paginate.first", null, paramLocale));
			pagination.put("sPrevious", messageSource.getMessage("generics.datatable.paginate.previous", null, paramLocale));
			pagination.put("sNext", messageSource.getMessage("generics.datatable.paginate.next", null, paramLocale));
			pagination.put("sLast", messageSource.getMessage("generics.datatable.paginate.last", null, paramLocale));
		}

		return legacyMessages;
	}

	/**
	 * Internationalized messages for Datatable >= 1.10
	 *
	 * @param locale
	 * @return
	 */
	@RequestMapping("/datatables/language")
	@ResponseBody
	public Map<String, Object> getLanguage(Locale locale) {
		if (messages == null) {
			messages = new HashMap<>();

			messages.put("search", messageSource.getMessage("generics.datatable.search", null, locale));
			messages.put("lengthMenu", messageSource.getMessage("generics.datatable.lengthMenu", null, locale));
			messages.put("info", messageSource.getMessage("generics.datatable.info", null, locale));
			messages.put("infoEmpty", messageSource.getMessage("generics.datatable.infoEmpty", null, locale));
			messages.put("infoFiltered", messageSource.getMessage("generics.datatable.infoFiltered", null, locale));
			messages.put("zeroRecords", messageSource.getMessage("generics.datatable.zeroRecords", null, locale));

			Map<String, Object> pagination = new HashMap<>();
			messages.put("paginate", pagination);

			pagination.put("first", messageSource.getMessage("generics.datatable.paginate.first", null, locale));
			pagination.put("previous", messageSource.getMessage("generics.datatable.paginate.previous", null, locale));
			pagination.put("next", messageSource.getMessage("generics.datatable.paginate.next", null, locale));
			pagination.put("last", messageSource.getMessage("generics.datatable.paginate.last", null, locale));
		}

		return messages;
	}
}
