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

import org.apache.commons.collections.map.HashedMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.service.requirement.RequirementVersionLinkTypeManagerService;
import org.squashtest.tm.web.internal.http.ContentTypes;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by jlor on 14/06/2017.
 */
@Controller
@RequestMapping("/requirement-link-type")
public class RequirementVersionLinkTypeController {

	@Inject
	private RequirementVersionLinkTypeManagerService linkTypeManagerService;

	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(method = RequestMethod.POST)
	public void addLinkType(@Valid @ModelAttribute RequirementVersionLinkType newLinkType) {
		linkTypeManagerService.addLinkType(newLinkType);
	}

	@ResponseBody
	@RequestMapping(value = "/check-codes", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON)
	public Map<String, Object> doesLinkTypeCodesExist(@Valid @ModelAttribute RequirementVersionLinkType linkType) {
		Map<String, Object> resultMap = new HashMap<>(2);
		resultMap.put("code1Exists", linkTypeManagerService.doesLinkTypeCodeAlreadyExist(linkType.getRole1Code()));
		resultMap.put("code2Exists", linkTypeManagerService.doesLinkTypeCodeAlreadyExist(linkType.getRole2Code()));
		resultMap.put("areCodesAndRolesConsistent", linkTypeManagerService.areCodesAndRolesConsistent(linkType));
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.POST, params = { "id=requirement-link-type-default" })
	public void changeDefault(@PathVariable Long linkTypeId) {
		linkTypeManagerService.changeDefault(linkTypeId);
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.POST, params = { "id=requirement-link-type-role1", "value" })
	public Map<String, Boolean> changeRole1(@PathVariable Long linkTypeId, @RequestParam("value") String newRole1) {
		return linkTypeManagerService.changeRole1(linkTypeId, newRole1);
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.POST, params = { "id=requirement-link-type-role2", "value" })
	public Map<String, Boolean> changeRole2(@PathVariable Long linkTypeId, @RequestParam("value") String newRole2) {
		return linkTypeManagerService.changeRole2(linkTypeId, newRole2);
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.POST, params = { "id=requirement-link-type-code1", "value" })
	public Map<String, Boolean> changeCode1(@PathVariable Long linkTypeId, @RequestParam("value") String newCode1) {
		return linkTypeManagerService.changeCode1(linkTypeId, newCode1);
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.POST, params = { "id=requirement-link-type-code2", "value" })
	public Map<String, Boolean> changeCode2(@PathVariable Long linkTypeId, @RequestParam("value") String codeRole2) {
		return linkTypeManagerService.changeCode2(linkTypeId, codeRole2);
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON, params = {"id=check-code", "value"})
	public Map<String, Object> doesLinkTypeCodeAlreadyExist(@PathVariable Long linkTypeId, @RequestParam("value") String code) {
		Map<String, Object> resultMap = new HashMap<>(1);
		resultMap.put("codeExists", linkTypeManagerService.doesLinkTypeCodeAlreadyExist(code, linkTypeId));
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON, params = {"id=isDefault"})
	public Map<String, Object> isLinkTypeDefault(@PathVariable Long linkTypeId) {
		Map<String, Object> resultMap = new HashedMap(1);
		resultMap.put("isTypeDefault", linkTypeManagerService.isLinkTypeDefault(linkTypeId));
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.GET, produces = ContentTypes.APPLICATION_JSON, params = {"id=isUsed"})
	public Map<String, Object> isLinkTypeUsed(@PathVariable Long linkTypeId) {
		Map<String, Object> resultMap = new HashedMap(1);
		resultMap.put("isLinkTypeUsed", linkTypeManagerService.isLinkTypeUsed(linkTypeId));
		return resultMap;
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeId}", method = RequestMethod.DELETE, produces = ContentTypes.APPLICATION_JSON)
	public void deleteLinkType(@PathVariable Long linkTypeId) {
		linkTypeManagerService.deleteLinkType(linkTypeId);
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypeIdsToDelete}", method = RequestMethod.DELETE)
	public void deleteLinkTypes (@PathVariable List<Long> linkTypeIdsToDelete) {
		linkTypeManagerService.deleteLinkTypes(linkTypeIdsToDelete);
	}

	@ResponseBody
	@RequestMapping(value = "/{linkTypesIdsToCheck}", method = RequestMethod.GET, params = {"id=doesContainDefault"})
	public Map<String, Object> doesLinkTypesContainDefault (@PathVariable List<Long> linkTypesIdsToCheck) {
		Map<String, Object> resultMap = new HashedMap(1);
		resultMap.put("containsDefault", linkTypeManagerService.doesContainDefault(linkTypesIdsToCheck));
		return resultMap;
	}
}
