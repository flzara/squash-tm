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
package org.squashtest.tm.web.internal.controller.testcase.importer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.service.batchimport.excel.TemplateMismatchException;
import org.squashtest.tm.service.importer.ImportLog;
import org.squashtest.tm.service.importer.ImportSummary;
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService;
import org.squashtest.tm.web.importer.ImportHelper;
import org.squashtest.tm.web.internal.controller.RequestParams;

/**
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping("/test-cases/importer")
public class TestCaseImportController {

	private interface Command<T, U> {
		U execute(T arg);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseImportController.class);

	@Inject
	private TestCaseLibraryNavigationService navigationService;

	@Inject
	private TestCaseImportLogHelper logHelper;

	@Inject
	private ImportHelper importHelper;

	/**
	 * Will import test cases given in the form of zipped archive. The zip must contain a folder hierarchy, with
	 * test-cases represented by xls files. One test-case is represented by one xls file where only the first tab of the
	 * file is read.
	 *
	 * @see TestCaseLibraryNavigationService#importZipTestCase(InputStream, long, String)
	 * @param archive
	 *            : the uploaded file
	 * @param projectId
	 *            : the id of the project where the hierarchy must be imported
	 * @param zipEncoding
	 *            : the encoding to use for file names,
	 * @return a view with the result of the import
	 * @throws IOException
	 */
	@RequestMapping(value = "/zip", method = RequestMethod.POST, produces = "text/html")
	public ModelAndView importZippedTestCases(@RequestParam("archive") MultipartFile archive,
			@RequestParam(RequestParams.PROJECT_ID) long projectId, @RequestParam("zipEncoding") String zipEncoding)
					throws IOException {

		InputStream stream = archive.getInputStream();
		ModelAndView mav = new ModelAndView("fragment/import/import-summary");

		ImportSummary summary = navigationService.importZipTestCase(stream, projectId, zipEncoding);
		mav.addObject("summary", summary);
		mav.addObject("workspace", "test-case");

		return mav;
	}

	/**
	 * Will simulate import of test cases in a one xls file format.
	 *
	 * @see TestCaseLibraryNavigationService#simulateImportExcelTestCase(File)
	 * @param uploadedFile
	 *            : the xls file to import in a {@link MultipartFile} form
	 * @param request
	 *            : the {@link WebRequest}
	 * @return a {@link ModelAndView} containing the summary of the import and the link to a complete log for any
	 *         invalid informations it contains
	 */
	@RequestMapping(value = "/xls", method = RequestMethod.POST, params = "dry-run")
	public ModelAndView dryRunExcelWorkbook(@RequestParam("archive") MultipartFile uploadedFile, WebRequest request) {
		LOGGER.debug("dryRunExcelWorkbook");
		return importWorkbook(uploadedFile, request, new Command<File, ImportLog>() {
			@Override
			public ImportLog execute(File xls) {
				return navigationService.simulateImportExcelTestCase(xls);
			}
		});
	}

	private ModelAndView importWorkbook(MultipartFile uploadedFile, WebRequest request,
			Command<File, ImportLog> callback) {
		ModelAndView mav = new ModelAndView("fragment/import/import-summary");

		File xls = null;

		try {
			xls = importHelper.multipartToImportFile(uploadedFile,"test-case-import-", ".xls");
			ImportLog summary = callback.execute(xls); // TODO parser may throw ex we should handle
			summary.recompute(); // TODO why is it here ? shouldnt it be in service ?
			generateImportLog(request, summary);
			mav.addObject("summary", summary);

		} catch (IOException e) {
			LOGGER.error("An exception prevented processing of test-case import file", e);

		}
		catch (TemplateMismatchException tme){
			ImportFormatFailure importFormatFailure = new ImportFormatFailure(tme);
			mav.addObject("summary", importFormatFailure);
		}
 finally {
			if (xls != null) {
				xls.deleteOnExit();
			}
		}
		mav.addObject("workspace", "test-case");

		return mav;
	}

	/**
	 * Generates a downloadable xls import log file and stores it where it should.
	 *
	 * @param request
	 *            : the {@link WebRequest} that lead here
	 * @param summary
	 *            : the {@link ImportLog} summary of the xls import/simulation
	 */
	private void generateImportLog(WebRequest request, ImportLog summary) {
		File xlsSummary = null;

		try {
			xlsSummary = importLogToLogFile(summary);

			String reportUrl = request.getContextPath() + "/test-cases/import-logs/" + xlsSummary.getName();
			summary.setReportUrl(reportUrl);

		} catch (IOException e) {
			LOGGER.warn("An error occured during import log generation", e);

		} finally {
			if (xlsSummary != null) {
				xlsSummary.deleteOnExit();
			}

		}
	}

	private File importLogToLogFile(ImportLog summary) throws IOException {
		return logHelper.storeLogFile(summary);
	}

	/**
	 * Will import test cases in a one xls file format.
	 *
	 * @see TestCaseLibraryNavigationService#performImportExcelTestCase(File)
	 * @param uploadedFile
	 *            : the xls file to import in a {@link MultipartFile} form
	 * @param request
	 *            : the {@link WebRequest}
	 * @return @return a {@link ModelAndView} containing the summary of the import and the link to a complete log for
	 *         any invalid informations it contains
	 */
	@RequestMapping(value = "/xls", params = "!dry-run", method = RequestMethod.POST)
	public ModelAndView importExcelWorkbook(@RequestParam("archive") MultipartFile uploadedFile, WebRequest request) {

		return importWorkbook(uploadedFile, request, new Command<File, ImportLog>() {
			@Override
			public ImportLog execute(File xls) {
				return navigationService.performImportExcelTestCase(xls);
			}
		});

	}
}
