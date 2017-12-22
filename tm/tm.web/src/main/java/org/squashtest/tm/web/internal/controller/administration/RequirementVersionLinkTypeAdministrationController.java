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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.Pagings;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableSorting;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import java.util.List;

/**
 * Controller for rendering requirement link types management pages
 *
 * @author jlor
 *
 */
@Controller
@RequestMapping("/administration/requirement-link-types")
public class RequirementVersionLinkTypeAdministrationController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementVersionLinkTypeAdministrationController.class);

	@Inject
	private InternationalizationHelper i18nHelper;
	// TODO: Do what is needed to separate into two services.
	@Inject
	private LinkedRequirementVersionManagerService reqLinksFinder;

	private final DatatableMapper<String> linkTypesMapper = new NameBasedMapper(4)
		.mapAttribute("type-role1", "role1", RequirementVersionLinkType.class)
		.mapAttribute("type-role1-code", "role1Code", RequirementVersionLinkType.class)
		.mapAttribute("type-role2", "role2", RequirementVersionLinkType.class)
		.mapAttribute("type-role2-code", "role2Code", RequirementVersionLinkType.class)
		.mapAttribute("type-is-default", "isDefault", RequirementVersionLinkType.class);

	@ModelAttribute("tablePageSize")
	public long populateTablePageSize() {
		return Pagings.DEFAULT_PAGING.getPageSize();
	}

	/**
	 * Shows the requirement link types manager.
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showManager(Model model) {
		DataTableModel reqLinkTypesModel = getReqLinkTypesInitialModel();
		model.addAttribute("linkTypesModel", reqLinkTypesModel);
		return "requirement-link-type-manager.html";
	}

	@RequestMapping(value = "/table", method = RequestMethod.GET, params = RequestParams.S_ECHO_PARAM)
	@ResponseBody
	public DataTableModel getReqLinkTypesTableModel(DataTableDrawParameters params) {
		PagingAndSorting pagingAndSorting = new DataTableSorting(params, linkTypesMapper);
		return buildReqLinkTypesModel(pagingAndSorting, params.getsEcho());
	}

	protected DataTableModel buildReqLinkTypesModel(PagingAndSorting pas, String sEcho) {
		PagedCollectionHolder<List<RequirementVersionLinkType>> holder =
			reqLinksFinder.getAllPagedAndSortedReqVersionLinkTypes(pas);

		return new RequirementLinkTypesTableModelHelper(i18nHelper).buildDataModel(holder, sEcho);
	}

	private DataTableModel getReqLinkTypesInitialModel() {
		PagedCollectionHolder<List<RequirementVersionLinkType>> holder =
			reqLinksFinder.getAllPagedAndSortedReqVersionLinkTypes(
				new DefaultPagingAndSorting("RequirementVersionLinkType.role1"));

		return new RequirementLinkTypesTableModelHelper(i18nHelper).buildDataModel(holder, "0");
	}

}
