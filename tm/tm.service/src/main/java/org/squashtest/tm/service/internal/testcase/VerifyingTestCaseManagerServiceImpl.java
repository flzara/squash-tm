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
package org.squashtest.tm.service.internal.testcase;

import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.DefaultPagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.IdentifiedUtil;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;

@Service("squashtest.tm.service.VerifyingTestCaseManagerService")
@Transactional
public class VerifyingTestCaseManagerServiceImpl implements VerifyingTestCaseManagerService {

	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	@Inject
	private RequirementVersionDao requirementVersionDao;
	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;
	@Inject
	private RequirementVersionCoverageDao requirementVersionCoverageDao;
	@Inject
	private IndexationService indexationService;
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;
	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;
	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Override
	@PostFilter("hasPermission(filterObject, 'LINK')" + OR_HAS_ROLE_ADMIN)
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'LINK')" + OR_HAS_ROLE_ADMIN)
	public Map<String, Collection<?>> addVerifyingTestCasesToRequirementVersion(List<Long> testCasesIds,
			long requirementVersionId) {
		// nodes are returned unsorted
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIds(testCasesIds);

		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);
		List<Long> ids = IdentifiedUtil.extractIds(testCases);
		Collection<VerifiedRequirementException> rejections= Collections.emptyList();
		if (!testCases.isEmpty()) {
			rejections = doAddVerifyingTestCasesToRequirementVersion(testCases, requirementVersionId);
		}
		Map<String, Collection<?>> result = new HashMap<>(2);
		result.put(IDS_KEY, ids);
		result.put(REJECTION_KEY, rejections);
		return result;
	}

	private Collection<VerifiedRequirementException> doAddVerifyingTestCasesToRequirementVersion(
			List<TestCase> testCases, long requirementVersionId) {
		RequirementVersion requirementVersion = requirementVersionDao.findOne(requirementVersionId);
		return doAddVerifyingTestCasesToRequirementVersion(testCases, requirementVersion);
	}

	private Collection<VerifiedRequirementException> doAddVerifyingTestCasesToRequirementVersion(
			List<TestCase> testCases, RequirementVersion requirementVersion) {

		List<VerifiedRequirementException> rejections = new ArrayList<>(testCases.size());

		Iterator<TestCase> iterator = testCases.iterator();
		while (iterator.hasNext()) {
			TestCase testCase = iterator.next();
			try {
				RequirementVersionCoverage coverage = new RequirementVersionCoverage(requirementVersion, testCase);
				requirementVersionCoverageDao.persist(coverage);
				indexationService.reindexTestCase(testCase.getId());
				indexationService.reindexRequirementVersion(requirementVersion.getId());
			} catch (RequirementAlreadyVerifiedException ex) {
				rejections.add(ex);
				iterator.remove();
			}

		}
		testCaseImportanceManagerService.changeImportanceIfRelationsAddedToReq(testCases, requirementVersion);

		return rejections;
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'LINK')" + OR_HAS_ROLE_ADMIN)
	public void removeVerifyingTestCasesFromRequirementVersion(List<Long> testCasesIds, long requirementVersionId) {

		List<TestCase> testCases = testCaseDao.findAllByIds(testCasesIds);

		if (!testCases.isEmpty()) {
			List<RequirementVersionCoverage> coverages = requirementVersionCoverageDao
					.byRequirementVersionAndTestCases(testCasesIds, requirementVersionId);
			for (RequirementVersionCoverage coverage : coverages) {
				coverage.checkCanRemoveTestCaseFromRequirementVersion();
				requirementVersionCoverageDao.delete(coverage);
			}

			indexationService.reindexTestCases(testCases);
			indexationService.reindexRequirementVersion(requirementVersionId);

			testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromReq(testCasesIds,
					requirementVersionId);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'LINK')" + OR_HAS_ROLE_ADMIN)
	public void removeVerifyingTestCaseFromRequirementVersion(long testCaseId, long requirementVersionId) {
		RequirementVersionCoverage coverage = requirementVersionCoverageDao.byRequirementVersionAndTestCase(
				requirementVersionId, testCaseId);
		coverage.checkCanRemoveTestCaseFromRequirementVersion();
		requirementVersionCoverageDao.delete(coverage);
		testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromReq(Arrays.asList(testCaseId),
				requirementVersionId);
	}

	@Override
	public PagedCollectionHolder<List<TestCase>> findAllByRequirementVersion(long requirementVersionId,
			PagingAndSorting pagingAndSorting) {
		List<TestCase> verifiers = testCaseDao.findAllByVerifiedRequirementVersion(requirementVersionId,
				pagingAndSorting);

		long verifiersCount = testCaseDao.countByVerifiedRequirementVersion(requirementVersionId);

		return new PagingBackedPagedCollectionHolder<>(pagingAndSorting, verifiersCount, verifiers);
	}

	@Override
	public List<TestCase> findAllByRequirementVersion(long requirementVersionId) {

		DefaultPagingAndSorting pas = new DefaultPagingAndSorting( "Project.name", true);
		return findAllByRequirementVersion(requirementVersionId, pas).getPagedItems();
	}

}
