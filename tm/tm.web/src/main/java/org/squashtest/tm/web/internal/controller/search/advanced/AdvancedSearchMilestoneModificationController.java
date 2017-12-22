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
package org.squashtest.tm.web.internal.controller.search.advanced;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.testcase.TestCaseModificationService;
import org.squashtest.tm.web.internal.controller.administration.MilestoneDataTableModelHelper;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;
import org.squashtest.tm.web.internal.model.search.MilestoneMassModifData;

@Controller
@RequestMapping("/advanced-search")
public class AdvancedSearchMilestoneModificationController {

	private static final String IDS = "ids[]";

	@Inject
	private RequirementVersionManagerService versionService;

	@Inject
	private TestCaseModificationService testCaseModificationService;

	@Inject
	private InternationalizationHelper internationalizationHelper;



	@RequestMapping(value = "/milestones/tc-mass-modif-associables/{testCaseIds}", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getMilestonesForMassTcModif(@PathVariable List<Long> testCaseIds,
													  Locale locale) {
		Collection<Milestone> milestones = testCaseModificationService
				.findAssociableMilestonesForMassModif(testCaseIds);
		return buildMilestoneTableModelForMassModif(milestones, locale);
	}

	@RequestMapping(value = "/milestones/reqV-mass-modif-associables/{reqVersionIds}", method = RequestMethod.GET)
	@ResponseBody
	public DataTableModel getMilestonesForMassReqVersionModif(@PathVariable List<Long> reqVersionIds,
															  Locale locale) {
		Collection<Milestone> milestones = versionService.findAssociableMilestonesForMassModif(reqVersionIds);
		return buildMilestoneTableModelForMassModif(milestones, locale);
	}


	@RequestMapping(value = "/milestones/tc-mass-modif-data/{testCaseIds}", method = RequestMethod.GET)
	@ResponseBody
	public MilestoneMassModifData getMilestoneMassModifDataForTc(@PathVariable List<Long> testCaseIds){

		MilestoneMassModifData data = new MilestoneMassModifData();
		data.setCheckedIds(testCaseModificationService.findBindedMilestonesIdForMassModif(testCaseIds));
		boolean hasData = !testCaseModificationService.findAssociableMilestonesForMassModif(testCaseIds).isEmpty();
		data.setHasData(hasData);
		data.setSamePerimeter(testCaseModificationService.haveSamePerimeter(testCaseIds));
		return data;
	}



	@RequestMapping(value = "/milestones/reqV-mass-modif-data/{reqVersionIds}", method = RequestMethod.GET)
	@ResponseBody
	public MilestoneMassModifData getMilestoneMassModifDataForReqVersion(@PathVariable List<Long> reqVersionIds) {

		MilestoneMassModifData data = new MilestoneMassModifData();
		data.setCheckedIds(versionService.findBindedMilestonesIdForMassModif(reqVersionIds));
		boolean hasData = !versionService.findAssociableMilestonesForMassModif(reqVersionIds).isEmpty();
		data.setHasData(hasData);
		data.setSamePerimeter(versionService.haveSamePerimeter(reqVersionIds));
		return data;
	}

	@RequestMapping(value = "/tcs/{testCaseIds}/milestones", method = RequestMethod.POST, params = IDS)
	@ResponseBody
	public void bindMilestonesToTcs(@PathVariable List<Long> testCaseIds, @RequestParam(IDS) List<Long> milestoneIds) {

		Collection<Long> bindedBefore = testCaseModificationService.findBindedMilestonesIdForMassModif(testCaseIds);
		bindedBefore.removeAll(milestoneIds);

		for (Long testCaseId : testCaseIds) {
			testCaseModificationService.bindMilestones(testCaseId, milestoneIds);
			testCaseModificationService.unbindMilestones(testCaseId, bindedBefore);
		}
	}


	@RequestMapping(value = "/reqVersions/{reqVIds}/milestones", method = RequestMethod.POST, params = IDS)
	@ResponseBody
	public boolean bindMilestonesToReqV(@PathVariable List<Long> reqVIds, @RequestParam(IDS) List<Long> milestoneIds) {
		Collection<Long> bindedBefore = versionService.findBindedMilestonesIdForMassModif(reqVIds);
		//was binded before but is not now so need to unbind
		bindedBefore.removeAll(milestoneIds);

		boolean isOneVersionAlreadyBind = !milestoneIds.isEmpty() && versionService.isOneMilestoneAlreadyBindToAnotherRequirementVersion(reqVIds, milestoneIds);

		for (Long reqVId : reqVIds) {
			versionService.bindMilestones(reqVId, milestoneIds);
			versionService.unbindMilestones(reqVId, bindedBefore);
		}

		return isOneVersionAlreadyBind;
	}



	private DataTableModel buildMilestoneTableModelForMassModif(Collection<Milestone> data, Locale locale) {
		MilestoneDataTableModelHelper helper = new MilestoneDataTableModelHelper(internationalizationHelper, locale);
		Collection<Object> aaData = helper.buildRawModel(data);
		DataTableModel model = new DataTableModel("");
		model.setAaData((List<Object>) aaData);
		return model;
	}

}
