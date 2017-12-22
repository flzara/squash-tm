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
package org.squashtest.tm.web.internal.helper;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.Level;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.service.infolist.InfoListFinderService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.i18n.MessageObject;

@Component
public class I18nLevelEnumInfolistHelper {
	
	@Inject
	private InternationalizationHelper i18nHelper;
	
	@Inject
	private InfoListFinderService infoListFinder;
	
	public MessageObject getInternationalizedDefaultList(Locale locale){
		//default infolist values
		Map<String, InfoList> listMap = new HashMap<>();
		listMap.put("REQUIREMENT_VERSION_CATEGORY",infoListFinder.findByCode(SystemInfoListCode.REQUIREMENT_CATEGORY.getCode()));
		listMap.put("TEST_CASE_NATURE", infoListFinder.findByCode(SystemInfoListCode.TEST_CASE_NATURE.getCode()));
		listMap.put("TEST_CASE_TYPE", infoListFinder.findByCode(SystemInfoListCode.TEST_CASE_TYPE.getCode()));
		
		MessageObject mapItems = new MessageObject();
		for (InfoList infoList : listMap.values()) {
			List<InfoListItem> infoListItems = infoList.getItems();
			for (InfoListItem infoListItem : infoListItems) {
				mapItems.put(infoListItem.getLabel(), infoListItem.getLabel());
			}
		}
		
		i18nHelper.resolve(mapItems, locale);
		return mapItems;
	}
	
	public <E extends Enum<E> & Level> MessageObject getI18nLevelEnum(Class<E> clazz, Locale locale) {
		MessageObject i18nEnums = new MessageObject();
		EnumSet<E> levels = EnumSet.allOf(clazz);
		for (E level : levels) {
			i18nEnums.put(level.name(), level.getI18nKey());
		}
		i18nHelper.resolve(i18nEnums, locale);
		return i18nEnums;
	}
	
}
