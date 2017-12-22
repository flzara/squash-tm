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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.UserListItem;
import org.squashtest.tm.service.infolist.InfoListItemManagerService;
import org.squashtest.tm.service.infolist.InfoListManagerService;
import org.squashtest.tm.web.exception.ResourceNotFoundException;
import org.squashtest.tm.web.internal.helper.JEditablePostParams;
import org.squashtest.tm.web.internal.http.ContentTypes;
import org.squashtest.tm.web.internal.http.JsonEmptyResponseEntity;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelBuilder;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.service.internal.dto.json.JsonInfoListItem;

@Controller
@RequestMapping("/info-lists")
public class InfoListController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InfoListController.class);

	@Inject
	private InfoListManagerService infoListManager;

	@Inject
	private InfoListItemManagerService infoListItemManager;

	@Inject
	private InfoListItemController itemsController;

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.POST, params = {"id=info-list-label",
		JEditablePostParams.VALUE})
	@ResponseBody
	public String changeLabel(@PathVariable long infoListId, @RequestParam(JEditablePostParams.VALUE) String label) {
		infoListManager.changeLabel(infoListId, label);
		return label;
	}

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.POST, params = {"id=info-list-code",
		JEditablePostParams.VALUE})
	@ResponseBody
	public String changeCode(@PathVariable Long infoListId, @RequestParam(JEditablePostParams.VALUE) String code) {
		infoListManager.changeCode(infoListId, code);
		return HtmlUtils.htmlEscape(code);
	}

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.POST, params = {"id=info-list-description",
		JEditablePostParams.VALUE})
	@ResponseBody
	public String changeDescription(@PathVariable Long infoListId,
		@RequestParam(JEditablePostParams.VALUE) String description) {
		infoListManager.changeDescription(infoListId, description);
		return HtmlUtils.htmlEscape(description);
	}

	@RequestMapping(value = "/{infoListId}/items", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel<?> getInfoListItems(@PathVariable Long infoListId) {
		InfoList list = infoListManager.findById(infoListId);

		return buildInfoListItemTableModel(list.getItems());
	}

	/**
	 * Tells if an item identified by its code exists or not. Existence is
	 * checked across all items of all info lists.
	 *
	 * @param code
	 * @return JSON <code>{ exists: <true|false> }</code>
	 */
	@RequestMapping(value = "{id}/items/label/{label}", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON, params = "format=exists")
	@ResponseBody
	public Map<String, Object> doesLabelExist(@PathVariable long id, @PathVariable String label) {
		InfoList list = infoListManager.findById(id);

		InfoListItem found = null;

		for (InfoListItem item : list.getItems()) {
			if (item.getLabel().equals(label)) {
				found = item;
				break;
			}
		}

		Map<String, Object> res = new HashMap<>(1);
		res.put("exists", found != null);

		return res;
	}

	/**
	 * Tells if an item identified by its code exists or not. Existence is
	 * checked across all items of all info lists.
	 *
	 * @param code
	 * @return JSON <code>{ exists: <true|false> }</code>
	 */
	@RequestMapping(value = "/items/code/{code}", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON, params = "format=exists")
	@ResponseBody
	public Map<String, Object> doesItemExist(@PathVariable String code) {
		InfoListItem item = infoListItemManager.findByCode(code);

		Map<String, Object> res = new HashMap<>(1);
		res.put("exists", item != null);

		return res;
	}

	/**
	 * Alias of {@link InfoListItemController#getItemByCode(String)}
	 *
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "/items/code/{code}", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	@ResponseBody
	public JsonInfoListItem getItemByCode(@PathVariable String code) {
		return itemsController.getItemByCode(code);
	}

	/**
	 * Tells if a list identified by a unique property exists or not.
	 *
	 * @param prop
	 *            name of the property. `label` and `code` are supported
	 * @value
	 * @return JSON <code>{ exists: <true|false> }</code>
	 */
	@RequestMapping(value = "/{prop}/{value}", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON, params = "format=exists")
	@ResponseBody
	public Map<String, Object> doesListExist(@PathVariable String prop, @PathVariable String value) {
		if ("label".equals(prop) || "code".equals(prop)) {
			InfoList item = infoListManager.findByUniqueProperty(prop, value);

			Map<String, Object> res = new HashMap<>(1);
			res.put("exists", item != null);

			return res;
		}

		throw new ResourceNotFoundException();
	}

	@RequestMapping(value = "/{infoListId}/items/positions", method = RequestMethod.POST, params = {"itemIds[]",
		"newIndex"})
	@ResponseBody
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void changeOptionsPositions(@PathVariable long infoListId, @RequestParam int newIndex,
		@RequestParam("itemIds[]") List<Long> itemsIds) {
		infoListManager.changeItemsPositions(infoListId, newIndex, itemsIds);
	}

	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/{infoListId}/items", method = RequestMethod.POST)
	public void addInfoListItem(@PathVariable long infoListId, @Valid @ModelAttribute("new-info-list-item") UserListItem item) {
		infoListItemManager.addInfoListItem(infoListId, item);
	}

	@RequestMapping(value = "/{infoListId}/isUsed", method = RequestMethod.GET)
	@ResponseBody
	public boolean isUsed(@PathVariable long infoListId) {
		return infoListManager.isUsedByOneOrMoreProject(infoListId);
	}

	@RequestMapping(value = "/{infoListIds}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	public void delete(@PathVariable List<Long> infoListIds) {
		infoListManager.remove(infoListIds);
	}

	@RequestMapping(value = "/{infoListId}/defaultItem", method = RequestMethod.GET)
	@ResponseBody
	public long getDefaultItemId(@PathVariable long infoListId) {
		InfoList infoList = infoListManager.findById(infoListId);
		return infoList.getDefaultItem().getId();
	}

	@RequestMapping(value = "/{infoListId}/items/{infoListItemId}", method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
	public void delete(@PathVariable long infoListId, @PathVariable long infoListItemId) {
		infoListItemManager.removeInfoListItem(infoListItemId, infoListId);
	}

	private DataTableModel<Object> buildInfoListItemTableModel(Collection<InfoListItem> data) {
		InfoListItemDataTableModelHelper helper = new InfoListItemDataTableModelHelper();
		List<Object> aaData = helper.buildRawModel(data);
		DataTableModel<Object> model = new DataTableModel<>("");
		model.setAaData(aaData);
		return model;
	}

	private static final class InfoListItemDataTableModelHelper extends DataTableModelBuilder<InfoListItem> {

		private InfoListItemDataTableModelHelper() {
		}

		@Override
		public Object buildItemData(InfoListItem item) {
			Map<String, Object> data = new HashMap<>(7);
			data.put(DataTableModelConstants.DEFAULT_ENTITY_ID_KEY, item.getId());
			data.put(DataTableModelConstants.DEFAULT_ENTITY_INDEX_KEY, getCurrentIndex() + 1);
			data.put("default", item.isDefault());
			data.put("label", item.getLabel());
			data.put("code", item.getCode());
			data.put("iconName", item.getIconName());
			data.put(DataTableModelConstants.DEFAULT_EMPTY_ICON_HOLDER_KEY, "");
			data.put(DataTableModelConstants.DEFAULT_EMPTY_DELETE_HOLDER_KEY, " ");
			return data;
		}
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST, produces = ContentTypes.APPLICATION_JSON)
	public JsonEmptyResponseEntity createNew(@RequestBody @Valid InfoList infoList) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Will create Info list {}", ToStringBuilder.reflectionToString(infoList));
		}
		infoListManager.persist(infoList);
		return new JsonEmptyResponseEntity(HttpStatus.CREATED);
	}
}
