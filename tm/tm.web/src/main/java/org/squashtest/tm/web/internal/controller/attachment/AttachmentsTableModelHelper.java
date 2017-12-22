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
package org.squashtest.tm.web.internal.controller.attachment;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;

public class AttachmentsTableModelHelper extends DataTableModelBuilder<Attachment>{

	private InternationalizationHelper i18nHelper;
	private Locale locale;
	private static final int INT_MAX_FILENAME_LENGTH = 50;

	public AttachmentsTableModelHelper(InternationalizationHelper i18nHelper){
		this.i18nHelper = i18nHelper;
		this.locale = LocaleContextHolder.getLocale();
	}

	@Override
	protected Map<Object, Object> buildItemData(Attachment item) {

		Map<Object, Object> result = new HashMap<>();

		result.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
		result.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex());
		result.put(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, item.getName());
		result.put("hyphenated-name", hyphenateFilename(item.getName()));
		result.put("size",item.getFormattedSize(locale));
		result.put("added-on",localizedDate(item.getAddedOn(),locale));
		result.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, null);

		return result;
	}

	private String localizedDate(Date date, Locale locale){
		return i18nHelper.localizeDate(date, locale);

	}

	private String hyphenateFilename(String longName){
		String newName = longName;
		if (longName.length() > INT_MAX_FILENAME_LENGTH){
			newName = longName.substring(0, INT_MAX_FILENAME_LENGTH-3)+"...";
		}
		return newName;
	}


}
