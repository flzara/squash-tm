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
package org.squashtest.tm.service.internal.infolist;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.service.infolist.InfoListModelService;
import org.squashtest.tm.service.internal.dto.json.JsonInfoList;
import org.squashtest.tm.service.internal.dto.json.JsonInfoListItem;
import org.squashtest.tm.service.internal.workspace.StreamUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.squashtest.tm.domain.infolist.SystemListItem.SYSTEM_INFO_LIST_IDENTIFIER;
import static org.squashtest.tm.jooq.domain.Tables.INFO_LIST;
import static org.squashtest.tm.jooq.domain.Tables.INFO_LIST_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;

@Service
@Transactional(readOnly = true)
public class InfoListModelServiceImpl implements InfoListModelService {

	@Inject
	private DSLContext DSL;

	@Inject
	private MessageSource messageSource;

	@Override
	public Map<Long, JsonInfoList> findUsedInfoList(List<Long> projectIds) {
		Set<Long> usedInfoListIds = findUsedInfoListIds(projectIds);
		return findInfoListMap(usedInfoListIds);
	}

	@Override
	public Map<String, String> findSystemInfoListItemLabels() {
		return DSL.select(INFO_LIST_ITEM.LABEL)
			.from(INFO_LIST_ITEM)
			.where(INFO_LIST_ITEM.ITEM_TYPE.eq(SYSTEM_INFO_LIST_IDENTIFIER))
			.fetch(INFO_LIST_ITEM.LABEL, String.class)
			.stream()
			.collect(Collectors.toMap(Function.identity(), this::getMessage));
	}

	protected Set<Long> findUsedInfoListIds(List<Long> readableProjectIds) {
		Set<Long> ids = new HashSet<>();
		DSL.select(PROJECT.REQ_CATEGORIES_LIST, PROJECT.TC_NATURES_LIST, PROJECT.TC_TYPES_LIST)
			.from(PROJECT)
			.where(PROJECT.PROJECT_ID.in(readableProjectIds))
			.fetch()
			.forEach(r -> {
				ids.add(r.get(PROJECT.REQ_CATEGORIES_LIST));
				ids.add(r.get(PROJECT.TC_NATURES_LIST));
				ids.add(r.get(PROJECT.TC_TYPES_LIST));
			});

		return ids;
	}

	protected Map<Long, JsonInfoList> findInfoListMap(Set<Long> usedInfoListIds) {
		Result result = DSL.select(INFO_LIST.INFO_LIST_ID, INFO_LIST.CODE, INFO_LIST.LABEL, INFO_LIST.DESCRIPTION
			, INFO_LIST_ITEM.ITEM_ID, INFO_LIST_ITEM.CODE, INFO_LIST_ITEM.LABEL, INFO_LIST_ITEM.ICON_NAME, INFO_LIST_ITEM.IS_DEFAULT, INFO_LIST_ITEM.ITEM_TYPE)
			.from(INFO_LIST)
			.innerJoin(INFO_LIST_ITEM).on(INFO_LIST.INFO_LIST_ID.eq(INFO_LIST_ITEM.LIST_ID))
			.where(INFO_LIST.INFO_LIST_ID.in(usedInfoListIds))
			.fetch();

		Function<Record, JsonInfoList> infolistTransformer = getInfoListTransformer();

		Function<Record, JsonInfoListItem> infoListItemTransformer = getInfoListItemTransformer();



		return StreamUtils.<Record,JsonInfoList, JsonInfoListItem>performJoinAggregateIntoMap(infolistTransformer, infoListItemTransformer, (JsonInfoList jsonInfoList, List<JsonInfoListItem> items) -> jsonInfoList.setItems(items), result);
	}

	private Function<Record, JsonInfoListItem> getInfoListItemTransformer() {
		return r -> {
			JsonInfoListItem jsonInfoListItem = new JsonInfoListItem();
			jsonInfoListItem.setId(r.get(INFO_LIST_ITEM.ITEM_ID));
			jsonInfoListItem.setCode(r.get(INFO_LIST_ITEM.CODE));
			jsonInfoListItem.setLabel(r.get(INFO_LIST_ITEM.LABEL));
			jsonInfoListItem.setIconName(r.get(INFO_LIST_ITEM.ICON_NAME));
			jsonInfoListItem.setDefault(r.get(INFO_LIST_ITEM.IS_DEFAULT));
			jsonInfoListItem.setSystem(r.get(INFO_LIST_ITEM.ITEM_TYPE).equals(SYSTEM_INFO_LIST_IDENTIFIER));
			jsonInfoListItem.setDenormalized(false);
			jsonInfoListItem.setFriendlyLabel(messageSource.getMessage(r.get(INFO_LIST_ITEM.LABEL), null, r.get(INFO_LIST_ITEM.LABEL), LocaleContextHolder.getLocale()));
			jsonInfoListItem.setUri("todo");
			return jsonInfoListItem;
		};
	}

	private Function<Record, JsonInfoList> getInfoListTransformer() {
		return r -> {
			Long id = r.get(INFO_LIST.INFO_LIST_ID);
			String code = r.get(INFO_LIST.CODE);
			String label = r.get(INFO_LIST.LABEL);
			String description = r.get(INFO_LIST.DESCRIPTION);
			return new JsonInfoList(id, "todo", code, label, description);
		};
	}

	private String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}

}
