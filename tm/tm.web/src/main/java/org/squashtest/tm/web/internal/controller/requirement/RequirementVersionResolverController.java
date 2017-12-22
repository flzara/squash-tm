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
package org.squashtest.tm.web.internal.controller.requirement;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.requirement.RequirementVersionManagerService;
import org.squashtest.tm.service.requirement.RequirementVersionResolverService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.jquery.RenameModel;


/**
 * This class will resolve which version of a requirement the user wants to browse according to whether he uses milestones mode or not.
 */

@Controller
@RequestMapping("/requirements/{requirementId}")
public class RequirementVersionResolverController {


	@Inject
	private RequirementVersionResolverService versionResolver;

	@Inject
	private RequirementVersionManagerService requirementVersionManager;


	@RequestMapping(value = "/info", method = RequestMethod.GET)
	public String resolveRequirementInfo(@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId) {

		RequirementVersion version = versionResolver.resolveByRequirementId(requirementId);
		return "redirect:/requirement-versions/"+version.getId()+"/info";

	}

	// will return the fragment only
	@RequestMapping(method = RequestMethod.GET)
	public String resolveRequirement(@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId) {
		RequirementVersion version = versionResolver.resolveByRequirementId(requirementId);
		return "redirect:/requirement-versions/"+version.getId();
	}



	/*
	 * Normally the method RequirementVersionModificationController#rename should
	 * have been used.
	 *
	 * Requests this method is mapped to come from the library tree, that doesn't know which
	 * requirement version it is actually talking to and the purpose of this controller is
	 * to redirect requests to the correct URL.
	 *
	 * Unfortunately one can't redirect POST methods. So in this particular case we must handle
	 * such requests (like 'POST newName') here.
	 *
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, params = {"newName"})
	public
 Object rename(@PathVariable(RequestParams.REQUIREMENT_ID) long requirementId,
			@RequestParam("newName") String newName) {

		RequirementVersion version = versionResolver.resolveByRequirementId(requirementId);

		requirementVersionManager.rename(version.getId(), newName);

		return new  RenameModel(newName);
	}

}
