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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.infolist.InfoList;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNodeVisitor;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.exception.InconsistentInfoListItemException;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.Ids;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.internal.batchexport.TestCaseExcelExporterService;
import org.squashtest.tm.service.internal.batchimport.TestCaseExcelBatchImporter;
import org.squashtest.tm.service.internal.importer.TestCaseImporter;
import org.squashtest.tm.service.internal.library.AbstractLibraryNavigationService;
import org.squashtest.tm.service.internal.library.LibrarySelectionStrategy;
import org.squashtest.tm.service.internal.library.NodeDeletionHandler;
import org.squashtest.tm.service.internal.library.PasteStrategy;
import org.squashtest.tm.service.internal.library.PathService;
import org.squashtest.tm.service.internal.repository.FolderDao;
import org.squashtest.tm.service.internal.repository.LibraryDao;
import org.squashtest.tm.service.internal.repository.ProjectDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestCaseFolderDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryDao;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryNodeDao;
import org.squashtest.tm.service.internal.testcase.coercers.TCLNAndParentIdsCoercerForArray;
import org.squashtest.tm.service.internal.testcase.coercers.TCLNAndParentIdsCoercerForList;
import org.squashtest.tm.service.internal.testcase.coercers.TestCaseLibraryIdsCoercerForArray;
import org.squashtest.tm.service.internal.testcase.coercers.TestCaseLibraryIdsCoercerForList;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.project.ProjectFilterModificationService;
import org.squashtest.tm.service.statistics.testcase.TestCaseStatisticsBundle;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.service.testcase.TestCaseStatisticsService;

import java.util.Optional;

@Service("squashtest.tm.service.TestCaseLibraryNavigationService")
@Transactional
public class TestCaseLibraryNavigationServiceImpl
	extends AbstractLibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode>
	implements TestCaseLibraryNavigationService {
	
	private static final String EXPORT = "EXPORT";
	private static final String TEST_CASE_CLASS_NAME = "org.squashtest.tm.domain.testcase.TestCase";
	private static final String DESTINATION_ID = "destinationId";
	private static final String SOURCE_NODES_IDS = "sourceNodesIds";
	private static final String TARGET_ID = "targetId";
	private static final String TARGET_IDS = "targetIds";

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	@Inject
	private TestCaseFolderDao testCaseFolderDao;
	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private TestCaseLibraryNodeDao testCaseLibraryNodeDao;

	@Inject
	private TestCaseImporter testCaseImporter;
	@Inject
	private TestCaseNodeDeletionHandler deletionHandler;
	@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToTestCaseFolderStrategy")
	private Provider<PasteStrategy<TestCaseFolder, TestCaseLibraryNode>> pasteToTestCaseFolderStrategyProvider;
	@Inject
	@Qualifier("squashtest.tm.service.internal.PasteToTestCaseLibraryStrategy")
	private Provider<PasteStrategy<TestCaseLibrary, TestCaseLibraryNode>> pasteToTestCaseLibraryStrategyProvider;

	@Inject
	private TestCaseStatisticsService statisticsService;

	@Inject
	private TestCaseCallTreeFinder calltreeService;

	@Inject
	private TestCaseExcelExporterService excelService;

	@Inject
	private ProjectDao projectDao;

	@Inject
	private TestCaseExcelBatchImporter batchImporter;

	@Inject
	private PathService pathService;

	@Inject
	private MilestoneMembershipManager milestoneService;

	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;

	@Override
	protected NodeDeletionHandler<TestCaseLibraryNode, TestCaseFolder> getDeletionHandler() {
		return deletionHandler;
	}

	@Override
	protected LibraryDao<TestCaseLibrary, TestCaseLibraryNode> getLibraryDao() {
		return testCaseLibraryDao;
	}

	@Override
	protected FolderDao<TestCaseFolder, TestCaseLibraryNode> getFolderDao() {
		return testCaseFolderDao;
	}

	@Override
	protected final TestCaseLibraryNodeDao getLibraryNodeDao() {
		return testCaseLibraryNodeDao;
	}

	@Override
	protected PasteStrategy<TestCaseFolder, TestCaseLibraryNode> getPasteToFolderStrategy() {
		return pasteToTestCaseFolderStrategyProvider.get();
	}

	@Override
	protected PasteStrategy<TestCaseLibrary, TestCaseLibraryNode> getPasteToLibraryStrategy() {
		return pasteToTestCaseLibraryStrategyProvider.get();
	}

	@Override
	@Transactional(readOnly = true)
	public String getPathAsString(long entityId) {
		return pathService.buildTestCasePath(entityId);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#getPathsAsString(java.util.List)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<String> getPathsAsString(List<Long> ids) {
		return pathService.buildTestCasesPaths(ids);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#findNodesByPath(java.util.List)
	 */

	@Override
	@Transactional(readOnly = true)
	public List<TestCaseLibraryNode> findNodesByPath(List<String> paths) {
		return getLibraryNodeDao().findNodesByPath(paths);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#findNodeIdsByPath(java.util.List)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Long> findNodeIdsByPath(List<String> paths) {
		return getLibraryNodeDao().findNodeIdsByPath(paths);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#findNodeIdByPath(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Long findNodeIdByPath(String path) {
		return getLibraryNodeDao().findNodeIdByPath(path);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.squashtest.tm.service.testcase.TestCaseLibraryFinderService#findNodeByPath(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public TestCaseLibraryNode findNodeByPath(String path) {
		return getLibraryNodeDao().findNodeByPath(path);
	}

	@Override
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary' , 'CREATE' )"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCaseLibrary.class)
	public void addFolderToLibrary(@Id long destinationId, TestCaseFolder newFolder) {

		TestCaseLibrary container = getLibraryDao().findById(destinationId);
		container.addContent(newFolder);

		// fix the nature and type for the possible nested test cases inside
		// that folder
		new NatureTypeChainFixer().fix(newFolder);

		// now proceed
		getFolderDao().persist(newFolder);

		// and then create the custom field values, as a better fix for [Issue
		// 2061]
		new CustomFieldValuesFixer().fix(newFolder);
	}

	@Override
	@PreAuthorize("hasPermission(#destinationId, 'org.squashtest.tm.domain.testcase.TestCaseFolder' , 'CREATE' )"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCaseLibraryNode.class)
	public final void addFolderToFolder(@Id long destinationId, TestCaseFolder newFolder) {

		TestCaseFolder container = getFolderDao().findById(destinationId);
		container.addContent(newFolder);

		// fix the nature and type for the possible nested test cases inside
		// that folder
		new NatureTypeChainFixer().fix(newFolder);

		// now proceed
		getFolderDao().persist(newFolder);

		// and then create the custom field values, as a better fix for [Issue
		// 2061]
		new CustomFieldValuesFixer().fix(newFolder);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary' , 'CREATE' )"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCaseLibrary.class)
	public void addTestCaseToLibrary(@Id long libraryId, TestCase testCase, Integer position) {

		TestCaseLibrary library = testCaseLibraryDao.findById(libraryId);

		if (!library.isContentNameAvailable(testCase.getName())) {
			throw new DuplicateNameException(testCase.getName(), testCase.getName());
		} else {
			if (position != null) {
				library.addContent(testCase, position);
			} else {
				library.addContent(testCase);
			}
			replaceInfoListReferences(testCase);
			testCaseDao.safePersist(testCase);
			createCustomFieldValuesForTestCase(testCase);

		}
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary' , 'CREATE' )"
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCaseLibrary.class)
	public void addTestCaseToLibrary(@Id long libraryId, TestCase testCase, Map<Long, RawValue> customFieldValues,
									 Integer position, List<Long> milestoneIds) {
		addTestCaseToLibrary(libraryId, testCase, position);
		initCustomFieldValues(testCase, customFieldValues);
		milestoneService.bindTestCaseToMilestones(testCase.getId(), milestoneIds);
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.testcase.TestCaseFolder' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCaseLibraryNode.class)
	public void addTestCaseToFolder(@Id long folderId, TestCase testCase, Integer position) {
		TestCaseFolder folder = testCaseFolderDao.findById(folderId);

		if (!folder.isContentNameAvailable(testCase.getName())) {
			throw new DuplicateNameException(testCase.getName(), testCase.getName());
		} else {
			if (position != null) {
				folder.addContent(testCase, position);
			} else {
				folder.addContent(testCase);
			}
			replaceInfoListReferences(testCase);
			testCaseDao.safePersist(testCase);
			createCustomFieldValuesForTestCase(testCase);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.testcase.TestCaseFolder' , 'CREATE') "
		+ OR_HAS_ROLE_ADMIN)
	@PreventConcurrent(entityType = TestCaseLibraryNode.class)
	public void addTestCaseToFolder(@Id long folderId, TestCase testCase, Map<Long, RawValue> customFieldValues,
									Integer position, List<Long> milestoneIds) {
		addTestCaseToFolder(folderId, testCase, position);
		initCustomFieldValues(testCase, customFieldValues);
		milestoneService.bindTestCaseToMilestones(testCase.getId(), milestoneIds);
	}

	@Override
	public Long mkdirs(String folderpath) {

		String path = folderpath.replaceFirst("^/", "").replaceFirst("/$", "");

		StringBuilder buffer = new StringBuilder();
		String[] split = path.split("(?<!\\\\)/");
		List<String> paths = new ArrayList<>(split.length - 1);

		// build all the paths on the way.
		buffer.append("/").append(split[0]);
		for (int i = 1; i < split.length; i++) {
			buffer.append("/");
			buffer.append(split[i]);
			paths.add(buffer.toString());
		}

		// find the folder ids, if exist
		List<Long> foundIds = findNodeIdsByPath(paths);

		int nullIdx = foundIds.indexOf(null);
		TestCaseFolder foldertree;

		switch (nullIdx) {
			case -1:
				return foundIds.get(foundIds.size() - 1); // all folders do exist,
			// simply return the
			// last element;

			case 0:
				Long libraryId = projectDao.findByName(split[0].replaceAll("\\\\\\/", "/")).getTestCaseLibrary().getId();
				foldertree = mkTransFolders(1, split);
				addFolderToLibrary(libraryId, foldertree);
				break;

			default:
				Long parentFolder = foundIds.get(nullIdx - 1);
				foldertree = mkTransFolders(nullIdx + 1, split);
				addFolderToFolder(parentFolder, foldertree);
				break;
		}

		TestCaseFolder lastFolder = foldertree;
		do {
			if (lastFolder.hasContent()) {
				lastFolder = (TestCaseFolder) lastFolder.getContent().get(0);
			}
		} while (lastFolder.hasContent());

		return lastFolder.getId();

	}

	private TestCaseFolder mkTransFolders(int startIndex, String[] names) {

		TestCaseFolder baseFolder = null;
		TestCaseFolder currentParent = null;
		TestCaseFolder currentChild;

		for (int i = startIndex; i < names.length; i++) {
			currentChild = new TestCaseFolder();
			currentChild.setName(names[i].replaceAll("\\\\\\/", "/")); // unescapes
			// escaped
			// slashes
			// '\/'
			// to
			// slashes
			// '/'
			currentChild.setDescription("");

			// if this is the first round in the loop we must initialize some
			// variables
			if (baseFolder == null) {
				baseFolder = currentChild;
			} else {
				currentParent.addContent(currentChild);
			}

			currentParent = currentChild;

		}

		return baseFolder;

	}

	/*
	 * CUF : this is a very quick fix for [Issue 2061], TODO : remove the lines
	 * that are related to this issue and replace it with another solution
	 * mentioned in the ticket same for requirement import
	 */
	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary', 'IMPORT')"
		+ OR_HAS_ROLE_ADMIN)
	public ImportSummary importZipTestCase(InputStream archiveStream, long libraryId, String encoding) {

		return testCaseImporter.importExcelTestCases(archiveStream, libraryId, encoding);
	}

	@Override
	public ImportLog simulateImportExcelTestCase(File excelFile) {
		return batchImporter.simulateImport(excelFile);
	}

	@Override
	public ImportLog performImportExcelTestCase(File excelFile) {
		return batchImporter.performImport(excelFile);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'LINK')" + OR_HAS_ROLE_ADMIN)
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects())
			: testCaseLibraryDao.findAll();

	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<ExportTestCaseData> findTestCasesToExport(List<Long> libraryIds, List<Long> nodeIds,
														  boolean includeCalledTests) {

		Collection<Long> allIds = findTestCaseIdsFromSelection(libraryIds, nodeIds, includeCalledTests);
		allIds = securityFilterIds(allIds, TEST_CASE_CLASS_NAME, EXPORT);

		List<ExportTestCaseData> testCases = testCaseDao.findTestCaseToExportFromNodes(new ArrayList<>(allIds));
		return (List<ExportTestCaseData>) setFullFolderPath(testCases);

	}

	@Override
	@Transactional(readOnly = true)
	public File exportTestCaseAsExcel(List<Long> libraryIds, List<Long> nodeIds, boolean includeCalledTests,
									  boolean keepRteFormat, MessageSource messageSource) {

		Collection<Long> allIds = findTestCaseIdsFromSelection(libraryIds, nodeIds, includeCalledTests);
		allIds = securityFilterIds(allIds, TEST_CASE_CLASS_NAME, EXPORT);

		return excelService.exportAsExcel(new ArrayList<>(allIds), keepRteFormat, messageSource);
	}

	@Override
	@SuppressWarnings("unchecked")
	public File searchExportTestCaseAsExcel(List<Long> nodeIds, boolean includeCalledTests, boolean keepRteFormat,
											MessageSource messageSource) {

		Collection<Long> allIds = findTestCaseIdsFromSelection(CollectionUtils.EMPTY_COLLECTION, nodeIds,
			includeCalledTests);
		allIds = securityFilterIds(allIds, TEST_CASE_CLASS_NAME, EXPORT);

		return excelService.searchExportAsExcel(new ArrayList<>(allIds), keepRteFormat, messageSource);

	}



	@Override
	public TestCaseStatisticsBundle getStatisticsForSelection(Collection<Long> libraryIds, Collection<Long> nodeIds) {

		Collection<Long> tcIds = findTestCaseIdsFromSelection(libraryIds, nodeIds);

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();
		if (activeMilestone.isPresent()) {
			tcIds = filterTcIdsListsByMilestone(tcIds, activeMilestone.get());
		}

		return statisticsService.gatherTestCaseStatisticsBundle(tcIds);
	}

	@Override
	public Collection<Long> findTestCaseIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds) {

		/*
		 * first, let's check the permissions on those root nodes By
		 * transitivity, if the user can read them then it will be allowed to
		 * read the test case below
		 */

		Collection<Long> readLibIds = securityFilterIds(libraryIds, TestCaseLibrary.class.getName(), "READ");
		Collection<Long> readNodeIds = securityFilterIds(nodeIds, TestCaseLibraryNode.class.getName(), "READ");

		// now we can collect the test cases
		Set<Long> tcIds = new HashSet<>();

		if (!readLibIds.isEmpty()) {
			tcIds.addAll(testCaseDao.findAllTestCaseIdsByLibraries(readLibIds));
		}
		if (!readNodeIds.isEmpty()) {
			tcIds.addAll(testCaseDao.findAllTestCaseIdsByNodeIds(readNodeIds));
		}

		// return
		return tcIds;
	}

	@Override
	public Collection<Long> findTestCaseIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds,
														 boolean includeCalledTests) {

		// first collect the test cases
		// the implementation guarantee there are no duplicates in the returned
		// collection
		Collection<Long> tcIds = findTestCaseIdsFromSelection(libraryIds, nodeIds);

		// collect if needed the called test cases
		if (includeCalledTests) {
			Set<Long> called = calltreeService.getTestCaseCallTree(tcIds);
			called = securityFilterIds(called, TEST_CASE_CLASS_NAME, "READ");
			tcIds.addAll(called);
		}

		// return
		return tcIds;

	}

	@Override
	public List<String> getParentNodesAsStringList(Long nodeId) {
		List<String> parents = new ArrayList<>();
		TestCase tc = testCaseDao.findById(nodeId);

		if (tc != null) {

			List<Long> ids = testCaseLibraryNodeDao.getParentsIds(nodeId);
			Long librabryId = tc.getLibrary().getId();

			parents.add("#TestCaseLibrary-" + librabryId);

			if (ids.size() > 1) {
				for (int i = 0; i < ids.size() - 1; i++) {
					parents.add("#TestCaseFolder-" + ids.get(i));
				}
			}
		}

		return parents;
	}

	@Override
	@PreAuthorize("hasPermission(#folderId, 'org.squashtest.tm.domain.testcase.TestCaseFolder', 'READ')"
		+ OR_HAS_ROLE_ADMIN)
	public List<String> findNamesInFolderStartingWith(long folderId, String nameStart) {
		return testCaseFolderDao.findNamesInFolderStartingWith(folderId, nameStart);
	}

	@Override
	@PreAuthorize("hasPermission(#libraryId, 'org.squashtest.tm.domain.testcase.TestCaseLibrary', 'READ')"
		+ OR_HAS_ROLE_ADMIN)
	public List<String> findNamesInLibraryStartingWith(long libraryId, String nameStart) {
		return testCaseFolderDao.findNamesInLibraryStartingWith(libraryId, nameStart);
	}

	@Override
	public int countSiblingsOfNode(long nodeId) {
		return testCaseLibraryNodeDao.countSiblingsOfNode(nodeId);
	}

	// ******************** more private code *******************

	private void createCustomFieldValuesForTestCase(TestCase testCase) {
		createCustomFieldValues(testCase);

		// also create the custom field values for the steps if any
		if (!testCase.getSteps().isEmpty()) {
			createCustomFieldValues(testCase.getActionSteps());
		}
	}

	/*
	 * 12/04/16 : about Nature and Type, and ListItemReferences :
	 *
	 * cannot use services
	 * 	- infoListItemService.isNatureConsistent
	 * 	- infoListItemService.isTypeConsistent
	 *  - infoListItemService.findReference
	 *
	 * anymore because this would trigger here an autoflush / persist
	 * before we have a chance to replace the ListItemReference by actual entities
	 */
	private void replaceInfoListReferences(TestCase testCase) {

		InfoList projectNatures = testCase.getProject().getTestCaseNatures();
		InfoList projectTypes = testCase.getProject().getTestCaseTypes();

		InfoListItem nature = testCase.getNature();

		// if no nature set -> use the default item configured for the project
		if (nature == null) {
			testCase.setNature(projectNatures.getDefaultItem());
		} else {
			// validate the code
			if (! projectNatures.contains(nature)) {
				throw new InconsistentInfoListItemException("nature", nature.getCode());
			}

			// in case the item used here is merely a reference we need to
			// replace it with
			// a persistent instance
			if (nature instanceof ListItemReference) {
				testCase.setNature(projectNatures.getItem(nature));
			}
		}

		// now unroll the same procedure with the type
		InfoListItem type = testCase.getType();

		if (type == null) {
			testCase.setType(projectTypes.getDefaultItem());
		} else {
			if (! projectTypes.contains(type)) {
				throw new InconsistentInfoListItemException("type", type.getCode());
			}
			if (type instanceof ListItemReference) {
				testCase.setType(projectTypes.getItem(type));
			}
		}
	}

	private class NatureTypeChainFixer implements TestCaseLibraryNodeVisitor {

		private void fix(TestCaseFolder folder) {
			for (TestCaseLibraryNode node : folder.getContent()) {
				node.accept(this);
			}
		}

		@Override
		public void visit(TestCase visited) {
			replaceInfoListReferences(visited);
		}

		@Override
		public void visit(TestCaseFolder visited) {
			fix(visited);
		}
	}

	private class CustomFieldValuesFixer implements TestCaseLibraryNodeVisitor {

		private void fix(TestCaseFolder folder) {
			for (TestCaseLibraryNode node : folder.getContent()) {
				node.accept(this);
			}
		}

		@Override
		public void visit(TestCase visited) {
			createCustomFieldValuesForTestCase(visited);
		}

		@Override
		public void visit(TestCaseFolder visited) {
			fix(visited);
		}

	}

	@Override
	public List<Long> findAllTestCasesLibraryForMilestone(Milestone activeMilestone) {
		List<Long> milestoneIds = new ArrayList<>();
		milestoneIds.add(activeMilestone.getId());
		return testCaseDao.findAllTestCasesLibraryForMilestone(milestoneIds);
	}

	@Override
	public List<Long> findAllTestCasesLibraryNodeForMilestone(Milestone activeMilestone) {
		if (activeMilestone != null) {
			List<Long> milestoneIds = new ArrayList<>();
			milestoneIds.add(activeMilestone.getId());
			return testCaseDao.findAllTestCasesLibraryNodeForMilestone(milestoneIds);
		} else {
			return new ArrayList<>();
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<Long> filterTcIdsListsByMilestone(Collection<Long> tcIds, Milestone activeMilestone) {

		List<Long> tcInMilestone = findAllTestCasesLibraryNodeForMilestone(activeMilestone);
		return CollectionUtils.retainAll(tcIds, tcInMilestone);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = DESTINATION_ID)}, batchsLocks = {
		@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = SOURCE_NODES_IDS, coercer = TCLNAndParentIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName = SOURCE_NODES_IDS, coercer = TestCaseLibraryIdsCoercerForArray.class)})
	public List<TestCaseLibraryNode> copyNodesToFolder(@Id(DESTINATION_ID) long destinationId,
													   @Ids(SOURCE_NODES_IDS) Long[] sourceNodesIds) {
		return super.copyNodesToFolder(destinationId, sourceNodesIds);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = TestCaseLibrary.class, paramName = DESTINATION_ID)}, batchsLocks = {
		@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = TARGET_ID, coercer = TCLNAndParentIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName = TARGET_ID, coercer = TestCaseLibraryIdsCoercerForArray.class)})
	public List<TestCaseLibraryNode> copyNodesToLibrary(@Id(DESTINATION_ID) long destinationId,
														@Ids(TARGET_ID) Long[] targetId) {
		return super.copyNodesToLibrary(destinationId, targetId);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = DESTINATION_ID)}, batchsLocks = {
		@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = TARGET_ID, coercer = TCLNAndParentIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName = TARGET_ID, coercer = TestCaseLibraryIdsCoercerForArray.class)})
	public void moveNodesToFolder(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId) {
		super.moveNodesToFolder(destinationId, targetId);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = TestCaseLibrary.class, paramName = DESTINATION_ID)}, batchsLocks = {
		@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = TARGET_ID, coercer = TCLNAndParentIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName = TARGET_ID, coercer = TestCaseLibraryIdsCoercerForArray.class)})
	public void moveNodesToLibrary(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId) {
		super.moveNodesToLibrary(destinationId, targetId);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = DESTINATION_ID)}, batchsLocks = {
		@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = TARGET_ID, coercer = TCLNAndParentIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName = TARGET_ID, coercer = TestCaseLibraryIdsCoercerForArray.class)})
	public void moveNodesToFolder(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId,
								  int position) {
		super.moveNodesToFolder(destinationId, targetId, position);
	}

	@Override
	@PreventConcurrents(simplesLocks = {
		@PreventConcurrent(entityType = TestCaseLibrary.class, paramName = DESTINATION_ID)}, batchsLocks = {
		@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = TARGET_ID, coercer = TCLNAndParentIdsCoercerForArray.class),
		@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName = TARGET_ID, coercer = TestCaseLibraryIdsCoercerForArray.class)})
	public void moveNodesToLibrary(@Id(DESTINATION_ID) long destinationId, @Ids(TARGET_ID) Long[] targetId,
								   int position) {
		super.moveNodesToLibrary(destinationId, targetId, position);
	}

	@Override
	@PreventConcurrents(batchsLocks = {
		@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName = TARGET_IDS, coercer = TCLNAndParentIdsCoercerForList.class),
		@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName = TARGET_IDS, coercer = TestCaseLibraryIdsCoercerForList.class)})
	public OperationReport deleteNodes(@Ids(TARGET_IDS) List<Long> targetIds) {
		return super.deleteNodes(targetIds);
	}

}
