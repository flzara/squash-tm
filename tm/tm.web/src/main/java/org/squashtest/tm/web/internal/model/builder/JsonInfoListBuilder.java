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
package org.squashtest.tm.web.internal.model.builder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.infolist.DenormalizedInfoListItem;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.SystemListItem;
import org.squashtest.tm.service.internal.dto.json.JsonInfoList;
import org.squashtest.tm.service.internal.dto.json.JsonInfoListItem;

@Component
public class JsonInfoListBuilder {

	@Inject
	private MessageSource messageSource;


	public JsonInfoList toJson(InfoList list){

		JsonInfoList res = new JsonInfoList();

		res.setId(list.getId());
		res.setCode(list.getCode());
		res.setUri("todo");
		res.setLabel(list.getLabel());
		res.setDescription(list.getDescription());

		List<JsonInfoListItem> items = new ArrayList<>(list.getItems().size());
		for (InfoListItem item : list.getItems()){
			JsonInfoListItem jsItem = toJson(item);
			items.add(jsItem);
		}

		res.setItems(items);

		return res;

	}


	public JsonInfoListItem toJson(InfoListItem item){
		JsonInfoListItem res = new JsonInfoListItem();
		res.setId(item.getId());
		res.setUri("todo");
		res.setCode(item.getCode());
		res.setLabel(item.getLabel());
		res.setDefault(item.isDefault());
		res.setIconName(item.getIconName());
		res.setDenormalized(false);
		// TODO : something less sloppy once we have time for something better
		res.setSystem(SystemListItem.class.isAssignableFrom(item.getClass()));
		res.setFriendlyLabel(messageSource.getMessage(res.getLabel(), null, res.getLabel(), LocaleContextHolder.getLocale()));
		return res;
	}


	public JsonInfoListItem toJson(DenormalizedInfoListItem item){
		JsonInfoListItem res = new JsonInfoListItem();
		res.setCode(item.getCode());
		res.setLabel(item.getLabel());
		res.setIconName(item.getIconName());
		res.setDenormalized(true);
		res.setFriendlyLabel(messageSource.getMessage(res.getLabel(), null, res.getLabel(), LocaleContextHolder.getLocale()));
		return res;
	}


}
