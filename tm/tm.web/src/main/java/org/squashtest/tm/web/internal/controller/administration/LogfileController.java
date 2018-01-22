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
package org.squashtest.tm.web.internal.controller.administration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/administration/log-file")
public class LogfileController {

	@Value("${logging.path}")
	private String loggingPath;

	@RequestMapping(method = RequestMethod.GET)
	public void downloadLogfile(HttpServletResponse response) {
		try {
			// dev
			File logfile = new File(loggingPath + "/spring.log");
			if(!logfile.exists()) {
				// prod
				logfile = new File(loggingPath + "/squash-tm.log");
			}
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "logfile; filename=" + logfile.getName().replace(" ", "_"));

			ServletOutputStream outStream = response.getOutputStream();
			writeContent(logfile, outStream);

		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void writeContent(File logfile, OutputStream outStream) throws IOException {

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(logfile);
			int readByte;
			do {
				readByte = fis.read();
				if (readByte != -1) {
					outStream.write(readByte);
				}
			} while (readByte != -1);
		} finally {
			if(fis != null) {
				fis.close();
			}
		}

	}
}
