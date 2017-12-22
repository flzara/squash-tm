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

import java.io.InputStream;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.testcase.TestCaseFolder;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.internal.archive.ArchiveReader;
import org.squashtest.tm.service.internal.archive.ArchiveReaderFactory;
import org.squashtest.tm.service.internal.archive.ArchiveReaderFactoryImpl;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;


@Component
public class TestCaseImporter {

	private static final String DEFAULT_ENCODING= "Cp858";
	public static final String  DEFAULT_ENCODING_KEY = "default";

	@Inject
	private TestCaseLibraryNavigationService service;



	private ArchiveReaderFactory factory = new ArchiveReaderFactoryImpl();

	private ExcelTestCaseParser parser = new ExcelTestCaseParserImpl();


	public ImportSummary importExcelTestCases(InputStream archiveStream, Long libraryId, String encoding){

		String finalEncoding = encoding.equals(DEFAULT_ENCODING_KEY) ? DEFAULT_ENCODING : encoding;

		ArchiveReader reader = factory.createReader(archiveStream, finalEncoding);


		TestCaseLibrary library = service.findCreatableLibrary(libraryId);

		ImportSummaryImpl summary = new ImportSummaryImpl();

		/* phase 1 : convert the content of the archive into Squash entities */

		HierarchyCreator creator = new HierarchyCreator();
		creator.setArchiveReader(reader);
		creator.setParser(parser);
		creator.setProject(library.getProject());

		creator.create();

		TestCaseFolder root = creator.getNodes();
		summary.add(creator.getSummary());


		/* phase 2 : merge with the actual database content */

		TestCaseLibraryMerger merger = new TestCaseLibraryMerger();
		merger.setLibraryService(service);
		merger.mergeIntoLibrary(library, root);

		summary.add(merger.getSummary());


		return summary;
	}




}
