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

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.SystemInfoListCode;
import org.squashtest.tm.service.infolist.InfoListFinderService;
import org.squashtest.tm.service.infolist.IsBoundInfoListAdapter;
import org.squashtest.tm.web.internal.controller.AcceptHeaders;
import org.squashtest.tm.web.internal.model.datatable.DataTable10Model;
import org.squashtest.tm.web.internal.model.datatable.DataTable10ModelAdaptor;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.util.IconLibrary;

/**
 * Controller for rendering info list management pages
 * 
 * @author Gregory Fouquet
 * 
 */
@Controller
@RequestMapping("/administration/info-lists")
public class InfoListAdministrationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(InfoListAdministrationController.class);

	@Inject
	private InfoListFinderService infoListFinder;

	@ModelAttribute("tablePageSize")
	public long populateTablePageSize() {
		return Pagings.DEFAULT_PAGING.getPageSize();
	}

	/**
	 * Shows the custom fields manager.
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showManager(Model model) {
		List<IsBoundInfoListAdapter> lists = infoListFinder.findAllWithBoundInfo();

		model.addAttribute("infoLists", lists);
		model.addAttribute("icons", IconLibrary.getIconNames());
		return "info-list-manager.html";
	}


	@RequestMapping(method = RequestMethod.GET, params = "_", headers = AcceptHeaders.CONTENT_JSON)
	@ResponseBody
	public DataTable10Model<IsBoundInfoListAdapter> getTableModel(@RequestParam("_") String echo,
			final DataTableDrawParameters params, final Locale locale) {
		DataTableModel<IsBoundInfoListAdapter> model = new DataTableModel<>(echo);
		model.setAaData(infoListFinder.findAllWithBoundInfo());
		return DataTable10ModelAdaptor.adapt(model);
	}

	@RequestMapping(value = "/{infoListId}", method = RequestMethod.GET)
	public String showInfoListModificationPage(@PathVariable Long infoListId, Model model) {
		InfoList list = infoListFinder.findById(infoListId);
		SystemInfoListCode.verifyModificationPermission(list);
		model.addAttribute("infoList", list);
		model.addAttribute("itemListIcons", IconLibrary.getIconNames());

		LOGGER.debug("id " + list.getId());
		LOGGER.debug("label " + list.getLabel());
		LOGGER.debug("code " + list.getCode());
		LOGGER.debug("description " + list.getDescription());

		return "info-list-modification.html";
	}

}
