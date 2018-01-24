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
package org.squashtest.tm.web.internal.controller.customfield;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.web.internal.controller.RequestParams;


@Controller
@RequestMapping("administration/projects/{projectId}/custom-fields-binding")
public class CustomFieldBindingManagerController {

	@Inject
	private CustomFieldBindingFinderService service;


	private static final int DEFAULT_PAGE_SIZE = 10;


	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getManager(@PathVariable(RequestParams.PROJECT_ID) Long projectId){

		List<CustomField> customFields = service.findAvailableCustomFields();
		ModelAndView mav;

		if (!customFields.isEmpty()){

			// Issue 6781 - only 10 CUFS were displaying for test case, just use the same method than the others
			List<CustomFieldBinding> testCaseBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.TEST_CASE);
			List<CustomFieldBinding> testStepBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.TEST_STEP);
			List<CustomFieldBinding> requirementBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.REQUIREMENT_VERSION);
			List<CustomFieldBinding> campaignBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.CAMPAIGN);
			List<CustomFieldBinding> iterationBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.ITERATION);
			List<CustomFieldBinding> testSuiteBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.TEST_SUITE);
			List<CustomFieldBinding> executionBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.EXECUTION);
			List<CustomFieldBinding> executionStepBindings = service.findCustomFieldsForProjectAndEntity(projectId, BindableEntity.EXECUTION_STEP);

			mav = new ModelAndView("project-tabs/custom-field-binding.html");
			mav.addObject("testCaseBindings", testCaseBindings);
			mav.addObject("testStepBindings", testStepBindings);
			mav.addObject("requirementBindings", requirementBindings);
			mav.addObject("campaignBindings", campaignBindings);
			mav.addObject("iterationBindings", iterationBindings);
			mav.addObject("testSuiteBindings", testSuiteBindings);
			mav.addObject("executionBindings", executionBindings);
			mav.addObject("executionStepBindings", executionStepBindings);

			mav.addObject("projectIdentifier", projectId);
		}
		else {
			mav = new ModelAndView("fragment/project/project-no-cuf-exists");
			mav.addObject("msg", "message.project.cuf.noCufExists");
		}

		return mav;

	}




}
