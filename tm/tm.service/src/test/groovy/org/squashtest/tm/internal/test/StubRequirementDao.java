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
package org.squashtest.tm.internal.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.squashtest.tm.domain.requirement.ExportRequirementData;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.internal.repository.RequirementDao;

/**
 * IGNOREVIOLATIONS:FILE This is for test purpose
 * @author Gregory Fouquet
 */
public class StubRequirementDao extends StubEntityDao<Requirement> implements RequirementDao {

	/**
	 * @see org.squashtest.tm.service.internal.repository.RequirementDao#findAllByIds(java.util.Collection)
	 */
	// @Override
	public List<Requirement> findAllByIds(Collection<Long> requirementsIds) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.RequirementDao#findNamesInFolderStartingWith(long,
	 *      java.lang.String)
	 */
	//@Override
	public List<String> findNamesInNodeStartingWith(long folderId, String nameStart) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.RequirementDao#findNamesInLibraryStartingWith(long,
	 *      java.lang.String)
	 */
	// @Override
	public List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.RequirementDao#findRequirementToExportFromFolder(java.util.List)
	 */
	// @Override
	public List<ExportRequirementData> findRequirementToExportFromNodes(List<Long> folderIds) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.RequirementDao#findRequirementToExportFromLibrary(java.util.List)
	 */
	// @Override
	public List<ExportRequirementData> findRequirementToExportFromLibrary(List<Long> folderIds) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.RequirementDao#findDistinctRequirementsCriticalitiesVerifiedByTestCases(java.util.List)
	 */
	// @Override
	public List<RequirementCriticality> findDistinctRequirementsCriticalitiesVerifiedByTestCases(Set<Long> testCasesIds) {
		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.RequirementDao#findDistinctRequirementsCriticalities(java.util.List)
	 */
	// @Override
	public List<RequirementCriticality> findDistinctRequirementsCriticalities(List<Long> requirementsIds) {

		return null;
	}

	/**
	 * @see org.squashtest.tm.service.internal.repository.RequirementDao#findVersions(Long)
	 */
	// @Override
	public List<RequirementVersion> findVersions(Long requirementId) {
		return null;
	}

	// @Override
	public List<RequirementVersion> findVersionsForAll(List<Long> requirementIds) {
		return null;
	}

	// @Override
	public List<Requirement> findAll() {
		return null;
	}

	// @Override
	public List<Requirement> findAllByIdListOrderedByName(List<Long> requirementsIds) {
		return null;
	}

	@Override
	public List<Long> findAllRequirementsIdsByLibrary(long libraryId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Requirement> findChildrenRequirements(long requirementId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Requirement findByContent(Requirement childRequirement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object[]> findAllParentsOf(List<Long> requirementIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> findNonBoundRequirement(Collection<Long> nodeIds, Long milestoneId) {
		return new ArrayList<>();
	}

	@Override
	public List<Long> findByRequirementVersion(Collection<Long> versionIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> filterRequirementHavingManyVersions(Collection<Long> requirementIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> findAllRequirementsIdsByLibrary(Collection<Long> libraryIds) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<Long> findIdsVersionsForAll(List<Long> requirementIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> findAllRequirementsIdsByNodes(Collection<Long> nodeIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> findDescendantRequirementIds(
			Collection<Long> requirementIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long findNodeIdByRemoteKey(String remoteKey, String projectName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> findNodeIdsByRemoteKeys(Collection<String> remoteKeys, String projectName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> findAllRequirementIdsFromMilestones(Collection<Long> milestoneIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Long> sortRequirementByNodeRelationship(List<Long> requirementVersionIds) {
		return null;
	}

	@Override
	public Long findNodeIdByRemoteKeyAndRemoteSyncId(String remoteKey, Long remoteSyncId) {
		return null;
	}
}
