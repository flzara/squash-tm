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

/**
 * Implementation of {@code AbstractJasperReportsSingleFormatView}
 * that renders report results in HTML format.
 *
 * <p><b>This class is compatible with classic JasperReports releases back until 2.x.</b>
 * As a consequence, it keeps using the {@link net.sf.jasperreports.engine.JRExporter}
 * API which got deprecated as of JasperReports 5.5.2 (early 2014).
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.1.3
 */
@SuppressWarnings({"deprecation", "rawtypes"})
public class JasperReportsHtmlView extends AbstractJasperReportsSingleFormatView {

	public JasperReportsHtmlView() {
		setContentType("text/html");
	}

	@Override
	protected net.sf.jasperreports.engine.JRExporter createExporter() {
		return new net.sf.jasperreports.engine.export.JRHtmlExporter();
	}

	@Override
	protected boolean useWriter() {
		return true;
	}

}
