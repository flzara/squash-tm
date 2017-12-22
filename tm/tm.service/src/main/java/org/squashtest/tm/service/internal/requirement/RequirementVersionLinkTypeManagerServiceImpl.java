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
package org.squashtest.tm.service.internal.requirement;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.exception.requirement.link.LinkTypeCodeAlreadyExistsException;
import org.squashtest.tm.exception.requirement.link.LinkTypeIsDefaultTypeException;
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkTypeDao;
import org.squashtest.tm.service.requirement.RequirementVersionLinkTypeManagerService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jlor on 14/06/2017.
 */
@Transactional
@Service("squashtest.tm.service.RequirementVersionLinkTypeManagerService")
public class RequirementVersionLinkTypeManagerServiceImpl implements RequirementVersionLinkTypeManagerService {

	@Inject
	private RequirementVersionLinkTypeDao linkTypeDao;

	@Inject
	private RequirementVersionLinkDao reqLinkDao;

	@Override
	public void addLinkType(RequirementVersionLinkType newLinkType) {
		List<RequirementVersionLinkType> typeList = linkTypeDao.getAllRequirementVersionLinkTypes();
		if(linkTypeDao.doesCodeAlreadyExist(newLinkType.getRole1Code())
			|| linkTypeDao.doesCodeAlreadyExist(newLinkType.getRole2Code())) {

			throw new LinkTypeCodeAlreadyExistsException();
		}
		linkTypeDao.save(newLinkType);
	}

	@Override
	public boolean doesLinkTypeCodeAlreadyExist(String code) {
		return linkTypeDao.doesCodeAlreadyExist(code);
	}

	@Override
	public boolean doesLinkTypeCodeAlreadyExist(String code, Long linkTypeId) {
		return linkTypeDao.doesCodeAlreadyExist(code, linkTypeId);
	}

	@Override
	public void changeDefault(Long linkTypeId) {
		RequirementVersionLinkType newDefaultReqLinkType = linkTypeDao.findOne(linkTypeId);
		List<RequirementVersionLinkType> allReqLinkTypes = linkTypeDao.getAllRequirementVersionLinkTypes();
		for(RequirementVersionLinkType linkType : allReqLinkTypes) {
			linkType.setDefault(false);
		}
		newDefaultReqLinkType.setDefault(true);

	}

	@Override
	public Map<String, Boolean> changeRole1(Long linkTypeId, String newRole1) {
		Map<String, Boolean> result = new HashMap<>();

		RequirementVersionLinkType linkType = linkTypeDao.findOne(linkTypeId);
		RequirementVersionLinkType copy = linkType.createCopy();
		copy.setRole1(newRole1);

		Boolean areCodesAndRolesConsistent = areCodesAndRolesConsistent(copy);

		if(areCodesAndRolesConsistent) {
			linkType.setRole1(newRole1);
		}

		result.put("areCodesAndRolesConsistent", areCodesAndRolesConsistent);
		return result;
	}

	@Override
	public Map<String, Boolean> changeRole2(Long linkTypeId, String newRole2) {
		Map<String, Boolean> result = new HashMap<>();

		RequirementVersionLinkType linkType = linkTypeDao.findOne(linkTypeId);
		RequirementVersionLinkType copy = linkType.createCopy();
		copy.setRole2(newRole2);

		Boolean areCodesAndRolesConsistent = areCodesAndRolesConsistent(copy);
		result.put("areCodesAndRolesConsistent", areCodesAndRolesConsistent);

		if(areCodesAndRolesConsistent) {
			linkType.setRole2(newRole2);
		}

		return result;
	}

	@Override
	public Map<String, Boolean> changeCode1(Long linkTypeId, String newCode1) {
		Map<String, Boolean> result = new HashMap<>();

		RequirementVersionLinkType linkType = linkTypeDao.findOne(linkTypeId);
		RequirementVersionLinkType copy = linkType.createCopy();
		copy.setRole1Code(newCode1);

		Boolean areCodesAndRolesConsistent = areCodesAndRolesConsistent(copy);
		result.put("areCodesAndRolesConsistent", areCodesAndRolesConsistent);

		if(!linkTypeDao.doesCodeAlreadyExist(newCode1, linkTypeId)
			&& areCodesAndRolesConsistent) {
			linkType.setRole1Code(newCode1);
		}

		return result;
	}

	@Override
	public Map<String, Boolean> changeCode2(Long linkTypeId, String newCode2) {
		Map<String, Boolean> result = new HashMap<>();

		RequirementVersionLinkType linkType = linkTypeDao.findOne(linkTypeId);
		RequirementVersionLinkType copy = linkType.createCopy();
		copy.setRole2Code(newCode2);

		Boolean areCodesAndRolesConsistent = areCodesAndRolesConsistent(copy);
		result.put("areCodesAndRolesConsistent", areCodesAndRolesConsistent);

		if(!linkTypeDao.doesCodeAlreadyExist(newCode2, linkTypeId)
			&& areCodesAndRolesConsistent) {
			linkType.setRole2Code(newCode2);
		}

		return result;
	}

	@Override
	public boolean isLinkTypeDefault(Long linkTypeId) {
		return linkTypeDao.isLinkTypeDefault(linkTypeId);
	}

	@Override
	public boolean isLinkTypeUsed(Long linkTypeId) {
		return linkTypeDao.isLinkTypeUsed(linkTypeId);
	}

	@Override
	public void deleteLinkType(Long linkTypeId) {
		RequirementVersionLinkType linkTypeToDelete = linkTypeDao.findOne(linkTypeId);
		if(linkTypeDao.isLinkTypeDefault(linkTypeId)) {
			throw new LinkTypeIsDefaultTypeException();
		} else {
			RequirementVersionLinkType defaultLinkType = linkTypeDao.getDefaultRequirementVersionLinkType();
			reqLinkDao.setLinksTypeToDefault(linkTypeToDelete, defaultLinkType);
			linkTypeDao.delete(linkTypeToDelete);
		}
	}

	@Override
	public boolean doesContainDefault(List<Long> linkTypesIdsToCheck) {
		Iterable<RequirementVersionLinkType> linkTypesToCheck = linkTypeDao.findAll(linkTypesIdsToCheck);
		for(RequirementVersionLinkType linkType : linkTypesToCheck) {
			if(linkType.isDefault()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void deleteLinkTypes(List<Long> linkTypeIdsToDelete) {
		for(Long id : linkTypeIdsToDelete) {
			deleteLinkType(id);
		}
	}

	@Override
	public boolean areCodesAndRolesConsistent(RequirementVersionLinkType linkType) {
		if(linkType.getRole1Code().equals(linkType.getRole2Code())
			&& !linkType.getRole1().equals(linkType.getRole2())) {
			return false;
		}
		return true;
	}

}
