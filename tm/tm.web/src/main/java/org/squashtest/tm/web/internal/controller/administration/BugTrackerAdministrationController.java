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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.csp.core.bugtracker.domain.BugTracker;
import org.squashtest.tm.service.bugtracker.BugTrackerManagerService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.datatable.DataTableModelConstants;
import org.squashtest.tm.web.internal.model.datatable.SpringPagination;
import org.squashtest.tm.web.internal.model.viewmapper.DatatableMapper;
import org.squashtest.tm.web.internal.model.viewmapper.NameBasedMapper;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.Locale;
import java.util.Set;

@Controller
@RequestMapping("/administration/bugtrackers")
public class BugTrackerAdministrationController {

	@Inject
	private InternationalizationHelper messageSource;
	@Inject
	private BugTrackerManagerService bugTrackerManagerService;
	private static final Logger LOGGER = LoggerFactory.getLogger(BugTrackerAdministrationController.class);


	private DatatableMapper<String> bugtrackerMapper = new NameBasedMapper()
		.map("id", "id")
		.map(DataTableModelConstants.DEFAULT_ENTITY_NAME_KEY, "name")
		.map("kind", "kind")
		.map("url", "url")
		.map("iframe-friendly", "iframeFriendly");


	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public void createBugTracker(@Valid @ModelAttribute("add-bugtracker") BugTracker bugtracker) {

		LOGGER.info("name " + bugtracker.getName());
		LOGGER.info("kind " + bugtracker.getKind());
		LOGGER.info("iframe " + bugtracker.isIframeFriendly());
		LOGGER.info("url " + bugtracker.getUrl());
		bugTrackerManagerService.addBugTracker(bugtracker);

	}


	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showBugtrackers() {
		Set<String> bugtrackerKinds = bugTrackerManagerService.findBugTrackerKinds();
		ModelAndView mav = new ModelAndView("page/bugtrackers/show-bugtrackers");
		mav.addObject("bugtrackers", bugTrackerManagerService.findAll());
		mav.addObject("bugtrackerKinds", bugtrackerKinds);
		return mav;
	}

	@ResponseBody
	@RequestMapping(value = "/list", params = RequestParams.S_ECHO_PARAM)
	public DataTableModel getBugtrackerTableModel(final DataTableDrawParameters params, final Locale locale) {

		Pageable pageable = SpringPagination.pageable(params, bugtrackerMapper);

		Page<BugTracker> holder = bugTrackerManagerService.findSortedBugtrackers(pageable);


		BugtrackerDataTableModelHelper helper = new BugtrackerDataTableModelHelper(messageSource);
		helper.setLocale(locale);
		return helper.buildDataModel(holder, params.getsEcho());

	}


}
