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

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Gregory Fouquet
 *
 */
@Controller
@RequestMapping("/test-cases/import-logs")
public class TestCaseImportLogController {

	@Inject
	private TestCaseImportLogHelper logHelper;

	// There are dots in `{filename}`. We need to parse using a regexp (`{:.+}`) because standard parser ditches file extensions.
	@ResponseBody
	@RequestMapping(value = "/{filename:.+}", method = RequestMethod.GET)
	public FileSystemResource getExcelImportLog(@PathVariable String filename,
			HttpServletResponse response) {
		File logFile = logHelper.fetchLogFile(filename);
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=" + logHelper.logFilename(filename));

		return new FileSystemResource(logFile);
	}
}
