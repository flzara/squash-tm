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
package org.squashtest.tm.api.report.spring.view.docxtemplater;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.web.servlet.view.AbstractView;




public class DocxTemplaterDocxView extends AbstractView{


	private String[] templatePath = new String[0];

	private static final Logger LOGGER = LoggerFactory.getLogger(DocxTemplaterDocxView.class);


	public void setTemplatePath(String... templatePath) {
		if (templatePath != null) {
			this.templatePath = new String[templatePath.length];
			for (int i = 0; i < templatePath.length; i++) {
				String basename = templatePath[i];
				Assert.hasText(basename, "templatePath must not be empty");
				this.templatePath[i] = basename.trim();
			}
		}
		else {
			this.templatePath = new String[0];
		}
	}

	@Override
	protected boolean generatesDownloadContent() {
		return true;
	};

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {


		for (String aTemplatePath : templatePath) {
			Resource resource = getApplicationContext().getResource(aTemplatePath);
			try {
				InputStream inputStream = resource.getInputStream();
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition", "attachment; filename=" + resource.getFilename());
				IOUtils.copy(inputStream, response.getOutputStream());
				response.flushBuffer();
				inputStream.close();
				break;
			} catch (Exception e) {
				LOGGER.debug("file don't exist" + resource.getFilename(), e);
			}

		}
	}
}
