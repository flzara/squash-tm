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
package org.squashtest.tm.service.internal.importer;

import java.util.Set;

import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.library.structures.StringPathMap;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.tm.exception.SheetCorruptedException;
import org.squashtest.tm.service.internal.archive.ArchiveReader;
import org.squashtest.tm.service.internal.archive.Entry;

/**
 * Must read an archive and make test cases from the files it includes.
 *
 * regarding the summary : may increment total test cases, warnings and failures, but not success.
 *
 * @author bsiri
 *
 */
class HierarchyCreator{

	private ArchiveReader reader;
	private ExcelTestCaseParser parser;
	private GenericProject project;

	private StringPathMap<TestCaseLibraryNode> pathMap = new StringPathMap<>();


	private ImportSummaryImpl summary = new ImportSummaryImpl();
	private TestCaseFolder root;


	public HierarchyCreator(){
		root = new TestCaseFolder();
		root.setName("/");

		pathMap.put("/", root);
	}


	public void setArchiveReader(ArchiveReader reader){
		this.reader = reader;
	}

	public void setParser(ExcelTestCaseParser parser){
		this.parser = parser;
	}

	public void setProject(GenericProject genericProject ){
		this.project = genericProject;
	}

	public ImportSummaryImpl getSummary(){
		return summary;
	}


	public TestCaseFolder getNodes(){
		return root;
	}

	public void create(){

		while(reader.hasNext()){

			Entry entry = reader.next();

			if (entry.isDirectory()){
				findOrCreateFolder(entry);
			}else{
				createTestCase(entry);
			}

		}
	}

	/**
	 * will chain-create folders if path elements do not exist. Will also store the path in a map
	 * for faster reference later.
	 *
	 * @param path
	 */
	private TestCaseFolder findOrCreateFolder(Entry entry){
		TestCaseFolder isFound = (TestCaseFolder)pathMap.getMappedElement(entry.getName());

		if (isFound != null){

			return isFound;

		}else{
			TestCaseFolder parent = findOrCreateFolder(entry.getParent());
			TestCaseFolder newFolder = new TestCaseFolder();
			newFolder.setName(entry.getShortName());
			parent.addContent(newFolder);

			pathMap.put(entry.getName(), newFolder);

			return newFolder;
		}
	}


	/**
	 * will chain-create folders if the parents does not exit, create the test case, and store the path in
	 * a map for faster reference later.
	 * @param entry
	 */
	private void createTestCase(Entry entry){
		try{
			summary.incrTotal();
			//create the test case
			TestCase testCase = parser.parseFile(entry.getStream(), summary);

			// fix the natures and types and report to the summary if necessary.
			fixNatureTypes(testCase);

			//add all parameters for used parameters in the step.
			Set<String> parameterNames = testCase.findUsedParamsNamesInSteps();
			for(String parameterName : parameterNames){
				new Parameter(parameterName, testCase);
			}
			//check whether the extension is correct
			if (hasValidExtension(entry)) {
				testCase.setName(stripExtension(entry.getShortName()));

				// find or create the parent folder
				TestCaseFolder parent = findOrCreateFolder(entry.getParent());

				parent.addContent(testCase);

				pathMap.put(entry.getName(), testCase);
			} else {
				summary.incrRejected();
			}

		}catch(SheetCorruptedException ex){ // NOSONAR : this exception is part of the nominal use case
			summary.incrFailures();
		}

	}


	private boolean hasValidExtension(Entry entry) {
		return entry.getShortName().endsWith(".xls") || entry.getShortName().endsWith(".xlsx");
	}

	private String stripExtension(String withExtension){
		return parser.stripFileExtension(withExtension);
	}

	private void fixNatureTypes(TestCase testCase){
		InfoListItem importNature = testCase.getNature();
		if (importNature == null || ! project.getTestCaseNatures().contains(importNature)){
			InfoListItem newNature = project.getTestCaseNatures().getDefaultItem();
			testCase.setNature(newNature);
			summary.incrModified();
		}

		InfoListItem importType = testCase.getType();
		if (importType == null || ! project.getTestCaseTypes().contains(importType)){
			InfoListItem newType = project.getTestCaseTypes().getDefaultItem();
			testCase.setType(newType);
			summary.incrModified();
		}
	}

}
