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
package org.squashtest.tm.web.internal.controller.requirement.importer;

import java.io.File;
import java.io.IOException;

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
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService;
import org.squashtest.tm.web.importer.ImportHelper;
import org.squashtest.tm.web.internal.controller.testcase.importer.ImportFormatFailure;
import org.squashtest.tm.web.internal.controller.testcase.importer.RequirementImportLogHelper;

@Controller
@RequestMapping("/requirements/importer")
public class RequirementImportController {

	private interface Command<T, U> {
		U execute(T arg);
	}

	@Inject
	private RequirementLibraryNavigationService requirementLibraryNavigationService;

	@Inject
	private ImportHelper importHelper;

	@Inject
	private RequirementImportLogHelper logHelper;

	private static final Logger LOGGER = LoggerFactory.getLogger(RequirementImportController.class);

	@RequestMapping(value = "/xls", method = RequestMethod.POST, params = "dry-run")
	public ModelAndView dryRunExcelWorkbook(@RequestParam("archive") MultipartFile uploadedFile, WebRequest request) {
		LOGGER.debug("Req-Import" + "In controller, DRY RUN");
		return importWorkbook(uploadedFile, request, new Command<File, ImportLog>(){
				@Override
				public ImportLog execute(File xls) {
					return requirementLibraryNavigationService.simulateImportExcelRequirement(xls);
				}
			}
		);
	}

	@RequestMapping(value = "/xls", params = "!dry-run", method = RequestMethod.POST)
	public ModelAndView importExcelWorkbook(@RequestParam("archive") MultipartFile uploadedFile, WebRequest request) {
		LOGGER.debug("Req-Import" + "In controller, RUN");
		return importWorkbook(uploadedFile, request, new Command<File, ImportLog>(){
				@Override
				public ImportLog execute(File xls) {
					return requirementLibraryNavigationService.importExcelRequirement(xls);
				}
			}
		);
	}

	//A factoriser avec l'import de TC ?
	private ModelAndView importWorkbook(MultipartFile uploadedFile, WebRequest request,
			Command<File, ImportLog> callback) {
		ModelAndView mav = new ModelAndView("fragment/import/import-summary");

		File xls = null;

		try {
			xls = importHelper.multipartToImportFile(uploadedFile,"requirement-import-", ".xls");
			ImportLog summary = callback.execute(xls); // TODO parser may throw ex we should handle
			summary.recompute(); // why is it here ? shouldnt it be in service ?
			generateImportLog(request, summary);
			mav.addObject("summary", summary); // TODO

		} catch (IOException e) {
			LOGGER.error("An exception prevented processing of requirement import file", e);
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
		mav.addObject("workspace", "requirement"); // TODO

		LOGGER.debug("Req-Import" + "OUT controller, RUN");
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

			String reportUrl = request.getContextPath() + "/requirement/import-logs/" + xlsSummary.getName();
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
}
