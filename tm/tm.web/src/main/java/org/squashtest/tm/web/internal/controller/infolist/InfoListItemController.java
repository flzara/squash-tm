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
package org.squashtest.tm.web.internal.controller.infolist;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.service.infolist.InfoListItemManagerService;
import org.squashtest.tm.service.internal.dto.json.JsonInfoListItem;
import org.squashtest.tm.web.exception.ResourceNotFoundException;
import org.squashtest.tm.web.internal.helper.JEditablePostParams;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.model.builder.JsonInfoListBuilder;
import org.squashtest.tm.web.internal.util.IconLibrary;

import javax.inject.Inject;
import java.util.List;

// XSS OK
@Controller
@RequestMapping("/info-list-items")
public class InfoListItemController {

	private static final String INFO_LIST_ITEM_ID_MAPPING = "/{infoListItemId}";

	@Inject
	private InfoListItemManagerService listItemManager;
	@Inject
	private JsonInfoListBuilder jsonBuilder;

	@RequestMapping(value = INFO_LIST_ITEM_ID_MAPPING, method = RequestMethod.POST, params = {"id=info-list-item-label",
		JEditablePostParams.VALUE})
	@ResponseBody
	public String changeLabel(@PathVariable Long infoListItemId, @RequestParam(JEditablePostParams.VALUE) String label) {
		listItemManager.changeLabel(infoListItemId, label);
		return HtmlUtils.htmlEscape(label);
	}

	@RequestMapping(value = INFO_LIST_ITEM_ID_MAPPING, method = RequestMethod.POST, params = {"id=info-list-item-code",
		JEditablePostParams.VALUE})
	@ResponseBody
	public String changeCode(@PathVariable Long infoListItemId, @RequestParam(JEditablePostParams.VALUE) String code) {
		listItemManager.changeCode(infoListItemId, code);
		return HtmlUtils.htmlEscape(code);
	}

	@RequestMapping(value = INFO_LIST_ITEM_ID_MAPPING, method = RequestMethod.POST, params = {"id=info-list-item-colour",
		JEditablePostParams.VALUE})
	@ResponseBody
	public String changeColour(@PathVariable Long infoListItemId, @RequestParam(JEditablePostParams.VALUE) String colour) {
		listItemManager.changeColour(infoListItemId, colour);
		return HtmlUtils.htmlEscape(colour);
	}

	@RequestMapping(value = INFO_LIST_ITEM_ID_MAPPING, method = RequestMethod.POST, params = {"id=info-list-item-default"})
	@ResponseBody
	public void changeDefault(@PathVariable Long infoListItemId) {
		listItemManager.changeDefault(infoListItemId);
	}

	@RequestMapping(value = INFO_LIST_ITEM_ID_MAPPING, method = RequestMethod.POST, params = {"id=info-list-item-icon"})
	@ResponseBody
	public void changeIcon(@PathVariable Long infoListItemId, @RequestParam(JEditablePostParams.VALUE) String icon) {
		listItemManager.changeIcon(infoListItemId, icon);
	}

	@RequestMapping(value = "/{infoListItemId}/isUsed", method = RequestMethod.GET)
	@ResponseBody
	public boolean isUsed(@PathVariable long infoListItemId) {

		return listItemManager.isUsed(infoListItemId);
	}

	@RequestMapping(value = "/code/{code}", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonInfoListItem getItemByCode(@PathVariable String code) {
		InfoListItem item = listItemManager.findByCode(code);
		if (item == null) {
			throw new ResourceNotFoundException();
		}
		return jsonBuilder.toJson(item);
	}


	@RequestMapping(value = "/icons", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getInfoListIconsList() {
		return IconLibrary.getIconNames();
	}

}
