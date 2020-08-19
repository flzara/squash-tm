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
package org.springframework.web.servlet.view.jasperreports;

import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.ui.jasperreports.JasperReportsUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Extends {@code AbstractJasperReportsView} to provide basic rendering logic
 * for views that use a fixed format, e.g. always PDF or always HTML.
 *
 * <p>Subclasses need to implement two template methods: {@code createExporter}
 * to create a JasperReports exporter for a specific output format, and
 * {@code useWriter} to determine whether to write text or binary content.
 *
 * <p><b>This class is compatible with classic JasperReports releases back until 2.x.</b>
 * As a consequence, it keeps using the {@link net.sf.jasperreports.engine.JRExporter}
 * API which got deprecated as of JasperReports 5.5.2 (early 2014).
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see #createExporter()
 * @see #useWriter()
 */
@SuppressWarnings({"deprecation", "rawtypes"})
public abstract class AbstractJasperReportsSingleFormatView extends AbstractJasperReportsView {

	@Override
	protected boolean generatesDownloadContent() {
		return !useWriter();
	}

	/**
	 * Perform rendering for a single Jasper Reports exporter, that is,
	 * for a pre-defined output format.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void renderReport(JasperPrint populatedReport, Map<String, Object> model, HttpServletResponse response)
			throws Exception {

		net.sf.jasperreports.engine.JRExporter exporter = createExporter();

		Map<net.sf.jasperreports.engine.JRExporterParameter, Object> mergedExporterParameters = getConvertedExporterParameters();
		if (!CollectionUtils.isEmpty(mergedExporterParameters)) {
			exporter.setParameters(mergedExporterParameters);
		}

		if (useWriter()) {
			renderReportUsingWriter(exporter, populatedReport, response);
		}
		else {
			renderReportUsingOutputStream(exporter, populatedReport, response);
		}
	}

	/**
	 * We need to write text to the response Writer.
	 * @param exporter the JasperReports exporter to use
	 * @param populatedReport the populated {@code JasperPrint} to render
	 * @param response the HTTP response the report should be rendered to
	 * @throws Exception if rendering failed
	 */
	protected void renderReportUsingWriter(net.sf.jasperreports.engine.JRExporter exporter,
			JasperPrint populatedReport, HttpServletResponse response) throws Exception {

		// Copy the encoding configured for the report into the response.
		String contentType = getContentType();
		String encoding = (String) exporter.getParameter(net.sf.jasperreports.engine.JRExporterParameter.CHARACTER_ENCODING);
		if (encoding != null) {
			// Only apply encoding if content type is specified but does not contain charset clause already.
			if (contentType != null && !contentType.toLowerCase().contains(WebUtils.CONTENT_TYPE_CHARSET_PREFIX)) {
				contentType = contentType + WebUtils.CONTENT_TYPE_CHARSET_PREFIX + encoding;
			}
		}
		response.setContentType(contentType);

		// Render report into HttpServletResponse's Writer.
		JasperReportsUtils.render(exporter, populatedReport, response.getWriter());
	}

	/**
	 * We need to write binary output to the response OutputStream.
	 * @param exporter the JasperReports exporter to use
	 * @param populatedReport the populated {@code JasperPrint} to render
	 * @param response the HTTP response the report should be rendered to
	 * @throws Exception if rendering failed
	 */
	protected void renderReportUsingOutputStream(net.sf.jasperreports.engine.JRExporter exporter,
			JasperPrint populatedReport, HttpServletResponse response) throws Exception {

		// IE workaround: write into byte array first.
		ByteArrayOutputStream baos = createTemporaryOutputStream();
		JasperReportsUtils.render(exporter, populatedReport, baos);
		writeToResponse(response, baos);
	}


	/**
	 * Create a JasperReports exporter for a specific output format,
	 * which will be used to render the report to the HTTP response.
	 * <p>The {@code useWriter} method determines whether the
	 * output will be written as text or as binary content.
	 * @see #useWriter()
	 */
	protected abstract net.sf.jasperreports.engine.JRExporter createExporter();

	/**
	 * Return whether to use a {@code java.io.Writer} to write text content
	 * to the HTTP response. Else, a {@code java.io.OutputStream} will be used,
	 * to write binary content to the response.
	 * @see javax.servlet.ServletResponse#getWriter()
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	protected abstract boolean useWriter();

}
