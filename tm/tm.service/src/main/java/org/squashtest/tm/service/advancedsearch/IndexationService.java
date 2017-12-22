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
package org.squashtest.tm.service.advancedsearch;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.library.IndexModel;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.TestCase;

public interface IndexationService {

	void indexAll();

	IndexModel findIndexModel();

	Boolean isIndexedOnPreviousVersion();

	// Indexing Requirement Versions
	void indexRequirementVersions();

	void reindexRequirementVersion(Long requirementVersionId);

	void reindexRequirementVersions(List<RequirementVersion> requirementVersionList);

	void reindexRequirementVersionsByIds(List<Long> requirementVersionsIds);

	// Indexing Test Cases
	void indexTestCases();

	void reindexTestCase(Long testCaseId);

	void reindexTestCases(List<TestCase> testCaseList);

	// Indexing IterationTestPlanItem
	void indexIterationTestPlanItem();


	void batchReindexTc(Collection<Long> tcIdsToIndex);

	void batchReindexReqVersion(Collection<Long> reqVersionIdsToIndex);

	void batchReindexItpi(Collection<Long> itpisIdsToIndex);

}
