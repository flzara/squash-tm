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
package org.squashtest.tm.service.testcase;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RawValue;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.testcase.ExportTestCaseData;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.service.annotation.BatchPreventConcurrent;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.annotation.Ids;
import org.squashtest.tm.service.annotation.PreventConcurrent;
import org.squashtest.tm.service.annotation.PreventConcurrents;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.internal.testcase.coercers.TCLNAndParentIdsCoercerForArray;
import org.squashtest.tm.service.internal.testcase.coercers.TCLNAndParentIdsCoercerForList;
import org.squashtest.tm.service.internal.testcase.coercers.TestCaseLibraryIdsCoercerForArray;
import org.squashtest.tm.service.internal.testcase.coercers.TestCaseLibraryIdsCoercerForList;
import org.squashtest.tm.service.library.LibraryNavigationService;

/**
 * Service for navigation in a TestCase library use case.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface TestCaseLibraryNavigationService extends
LibraryNavigationService<TestCaseLibrary, TestCaseFolder, TestCaseLibraryNode>, TestCaseLibraryFinderService {

	/**
	 * Adds a TestCase to the root of the library. The custom fields will be created with their default value.
	 * 
	 * @param libraryId
	 * @param testCase
	 * @param position if non-null and non-negative, the test case will be inserted at that index.
	 */
	@PreventConcurrent(entityType=TestCaseLibrary.class)
	void addTestCaseToLibrary(@Id long libraryId, TestCase testCase, Integer position);


	/**
	 * Adds a TestCase to the root of the Library, and its initial custom field values. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 * 
	 * @param libraryId
	 * @param testCase
	 * @param customFieldValues
	 * @param position if non-null and non-negative, the test case will be inserted at that index.
	 * @param milestoneIds the list of the ids of the milestones the test case must be bound to
	 */
	@PreventConcurrent(entityType=TestCaseLibrary.class)
	void addTestCaseToLibrary(@Id long libraryId, TestCase testCase, Map<Long, RawValue> customFieldValues, Integer position, List<Long> milestoneIds);



	/**
	 * Adds a TestCase to a folder. The custom fields will be created with their default value.
	 * 
	 * @param libraryId
	 * @param testCase
	 * @param position if non-null and non-negative, the test case will be inserted at that index.
	 */
	@PreventConcurrent(entityType=TestCaseLibraryNode.class)
	void addTestCaseToFolder(@Id long folderId, TestCase testCase, Integer position);


	/**
	 * Adds a TestCase to a folder, and its initial custom field values. The initial custom field values
	 * are passed as a Map<Long, String>, that maps the id of the {@link CustomField} to the values of the corresponding {@link CustomFieldValue}.
	 * Read that last sentence again.
	 * 
	 * @param libraryId
	 * @param testCase
	 * @param customFieldValues Map&lt;String, String||String[]&gt;
	 * @param position if non-null and non-negative, the test case will be inserted at that index.
	 * @param milestoneIds the ids of the milestones the test case will be bound to
	 */
	@PreventConcurrent(entityType=TestCaseLibraryNode.class)
	void addTestCaseToFolder(@Id long folderId, TestCase testCase, Map<Long, RawValue> customFieldValues, Integer position, List<Long> milestoneIds);


	/**
	 * given a well formed path (starting with /project), creates a folder at this position if the user is allowed to.
	 * If a folder already exists in that place, creates nothing and returns its id instead.
	 * 
	 * @param folderpath
	 * @return the id of that folder
	 */
	Long mkdirs(String folderpath);

	/**
	 * Accepts a stream to a .zip file containing regular folders or excel files and nothing else. Will convert the test
	 * cases from excel to squash.
	 * 
	 * @param archiveStream
	 * @param libraryId
	 *            the identifier of the library we are importing test cases into.
	 * @param encoding
	 *            the encoding
	 * @return a summary of the operations.
	 */
	ImportSummary importZipTestCase(InputStream archiveStream, long libraryId, String encoding);


	/**
	 * Simulates an import and returns a complete log
	 * for any invalid informations it contains. It does not proceed to an actual import. Note that the structure of that
	 * excel file is totally different that the one contained in the zip archive consumed by
	 * {@link #importZipTestCase(InputStream, long, String)}
	 * 
	 * @param excelFile
	 * @return
	 */
	ImportLog simulateImportExcelTestCase(File excelFile);


	/**
	 * Proceed to an import returns a complete log
	 * for any invalid informations it contains. Contrary to {@link #simulateImportExcelTestCase(File)} data are actually processed.
	 * Note that the structure of that
	 * excel file is totally different that the one contained in the zip archive consumed by
	 * {@link #importZipTestCase(InputStream, long, String)}
	 * 
	 * @param excelFile
	 * @return
	 */

	ImportLog performImportExcelTestCase(File excelFile);



	/**
	 * Will find all test cases in the library and node ids supplied in arguments, and return their
	 * information as a list of {@linkplain ExportTestCaseData}
	 * 
	 * @param nodesIds
	 *            ids of {@linkplain TestCaseLibraryNode}
	 * @return a list of {@linkplain ExportTestCaseData}
	 */
	List<ExportTestCaseData> findTestCasesToExport(List<Long> libraryIds, List<Long> nodeIds, boolean includeCalledTests);

	/**
	 * <p>Will export a selection of test cases as an Excel 2003 (.xls) spreadsheet.
	 * The selection consists of :</p>
	 * 
	 *  <ul>
	 *  	<li>zero to several libraries</li>
	 *  	<li>zero to several nodes</li>
	 *  </ul>
	 * 
	 *  <p>Last, if the selection have some call steps, the called test case can be included in the export
	 *  if the parameter includeCalledTests is set to true.</p>
	 * 
	 * @param libraryIds
	 * @param nodeIds
	 * @param includeCalledTests
	 * @return
	 */
	File exportTestCaseAsExcel(List<Long> libraryIds, List<Long> nodeIds, boolean includeCalledTests, boolean keepRteFormat, MessageSource messageSource);
	
	/**
	 * <p>Will export a selection of test cases as an Excel 2003 (.xls) spreadsheet.
	 * The selection consists of :</p>
	 * 
	 *  <ul>
	 *  	<li>zero to several nodes</li>
	 *  </ul>
	 * 
	 *  <p>Last, if the selection have some call steps, the called test case can be included in the export
	 *  if the parameter includeCalledTests is set to true.</p>
	 * 
	 * @param nodeIds
	 * @param includeCalledTests
	 * @return
	 */
	File searchExportTestCaseAsExcel( List<Long> nodeIds, boolean includeCalledTests, boolean keepRteFormat, MessageSource messageSource);


	List<String> getParentNodesAsStringList(Long nodeId);


	public List<String> findNamesInFolderStartingWith(final long folderId, final String nameStart);

	public List<String> findNamesInLibraryStartingWith(final long libraryId, final String nameStart);

	public List<Long> findAllTestCasesLibraryForMilestone(Milestone activeMilestone);

	public List<Long> findAllTestCasesLibraryNodeForMilestone(Milestone activeMilestone);
	
	
	//####################### PREVENT CONCURENCY OVERRIDES #########################//
	
	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="sourceNodesIds", coercer=TCLNAndParentIdsCoercerForArray.class),
							@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName="sourceNodesIds", coercer=TestCaseLibraryIdsCoercerForArray.class)}
			)
	public List<TestCaseLibraryNode> copyNodesToFolder(@Id("destinationId") long destinationId, @Ids("sourceNodesIds") Long[] sourceNodesIds);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType = TestCaseLibrary.class, paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="targetId", coercer=TCLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName="targetId", coercer=TestCaseLibraryIdsCoercerForArray.class)}
			)
	public List<TestCaseLibraryNode> copyNodesToLibrary(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="targetId", coercer=TCLNAndParentIdsCoercerForArray.class),
							@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName="targetId", coercer=TestCaseLibraryIdsCoercerForArray.class)}
			)
	public void moveNodesToFolder(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType = TestCaseLibrary.class, paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="targetId", coercer=TCLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName="targetId", coercer=TestCaseLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToLibrary(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetId);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="targetId", coercer=TCLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName="targetId", coercer=TestCaseLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToFolder(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetId, int position);

	@Override
	@PreventConcurrents(
			simplesLocks={@PreventConcurrent(entityType = TestCaseLibrary.class, paramName="destinationId")},
			batchsLocks={@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="targetId", coercer=TCLNAndParentIdsCoercerForArray.class),
					@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName="targetId", coercer=TestCaseLibraryIdsCoercerForArray.class)}
			)
	void moveNodesToLibrary(@Id("destinationId") long destinationId, @Ids("targetId") Long[] targetId, int position);

	@Override
	@PreventConcurrents(
			batchsLocks={@BatchPreventConcurrent(entityType = TestCaseLibraryNode.class, paramName="targetIds", coercer=TCLNAndParentIdsCoercerForList.class),
					@BatchPreventConcurrent(entityType = TestCaseLibrary.class, paramName="targetIds", coercer=TestCaseLibraryIdsCoercerForList.class)}
			)
	OperationReport deleteNodes(@Ids("targetIds") List<Long> targetIds);
	
	//####################### /PREVENT CONCURENCY OVERRIDES #########################//
}
