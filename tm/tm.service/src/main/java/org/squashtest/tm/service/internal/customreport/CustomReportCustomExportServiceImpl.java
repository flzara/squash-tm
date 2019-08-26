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
package org.squashtest.tm.service.internal.customreport;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.campaign.Campaign;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.campaign.TestSuite;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.campaign.CustomCampaignModificationService;
import org.squashtest.tm.service.campaign.IterationFinder;
import org.squashtest.tm.service.campaign.TestSuiteFinder;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customreport.CustomReportCustomExportService;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;
import org.squashtest.tm.service.internal.dto.CustomFieldJsonConverter;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class CustomReportCustomExportServiceImpl implements CustomReportCustomExportService {

	@Inject
	private CustomCampaignModificationService customCampaignModificationService;
	@Inject
	private IterationFinder iterationFinder;
	@Inject
	private TestSuiteFinder testSuiteFinder;
	@Inject
	private CustomFieldBindingFinderService cufBindingService;
	@Inject
	private CustomFieldJsonConverter customFieldConverter;


	@Override
	public String getScopeEntityName(EntityReference entityReference) {
		switch(entityReference.getType()) {
			case CAMPAIGN:
				Campaign campaign = customCampaignModificationService.findCampaigWithExistenceCheck(entityReference.getId());
				return campaign != null ? campaign.getName() : "";
			case ITERATION:
				Iteration iteration = iterationFinder.findById(entityReference.getId());
				return iteration != null ? iteration.getName() : "";
			case TEST_SUITE:
				try {
					TestSuite testSuite = testSuiteFinder.findById(entityReference.getId());
					return testSuite.getName();
				} catch (EntityNotFoundException e) {
					return "";
				}
			default:
				throw new IllegalArgumentException("Entity of type " + entityReference.getType().name() + " is not supported");
		}
	}

	@Override
	public Map<String, List<CustomFieldBindingModel>> getCustomFieldsData(Long mainProjectId, List<IterationTestPlanItem> itpis, Map<String, List<CustomFieldBindingModel>> map) {

		// Get the ids of the projects of the test case linked to the given campaign (excluding the main project)
		List<Long> projectIds = itpis.stream()
			.map(itpi -> {
				// for deleted TestCases
				TestCase testCase = itpi.getReferencedTestCase();
				if(testCase != null) {
					return testCase.getProject().getId();
				} else {
					return null;
				}
			})
			.distinct()
			.filter(projectId -> projectId!= null && !projectId.equals(mainProjectId))
			.collect(Collectors.toList());

		for (Long projectId : projectIds) {
			List<CustomFieldBinding> cufs = cufBindingService.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.TEST_CASE);
			for (CustomFieldBinding binding : cufs) {
				map.get(BindableEntity.TEST_CASE.toString()).add(customFieldConverter.toJson(binding));
			}
		}
		return map;
	}

}
