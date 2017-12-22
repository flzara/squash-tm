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
package org.squashtest.tm.web.internal.controller.testcase.requirement;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;

/**
 * Controller which processes requests related to links between Requirement and Test-Case
 *
 * @author mpagnon
 *
 */
@Controller
@RequestMapping(value = "/requirement-version-coverage")
public class RequirementVersionCoverageController {

	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;


	@ResponseBody
	@RequestMapping(value = "{requirementVersionCoverageId}", method = RequestMethod.DELETE)
	public
	void removeVerifiedRequirementVersionFromTestCase(@PathVariable long requirementVersionId,
			@PathVariable long testCaseId) {
		verifiedRequirementsManagerService.removeVerifiedRequirementVersionFromTestCase(requirementVersionId, testCaseId);

	}

}
