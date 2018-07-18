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

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

/**
 * Configurable JasperReports View, allowing to specify the JasperReports exporter
 * to be specified through bean properties rather than through the view class name.
 *
 * <p><b>This class is compatible with classic JasperReports releases back until 2.x.</b>
 * As a consequence, it keeps using the {@link net.sf.jasperreports.engine.JRExporter}
 * API which got deprecated as of JasperReports 5.5.2 (early 2014).
 *
 * @author Rob Harrop
 * @since 2.0
 * @see JasperReportsCsvView
 * @see JasperReportsHtmlView
 * @see JasperReportsPdfView
 * @see JasperReportsXlsView
 */
@SuppressWarnings({"deprecation", "rawtypes"})
public class ConfigurableJasperReportsView extends AbstractJasperReportsSingleFormatView {

	private Class<? extends net.sf.jasperreports.engine.JRExporter> exporterClass;

	private boolean useWriter = true;


	/**
	 * Set the {@code JRExporter} implementation {@code Class} to use. Throws
	 * {@link IllegalArgumentException} if the {@code Class} doesn't implement
	 * {@code JRExporter}. Required setting, as it does not have a default.
	 */
	public void setExporterClass(Class<? extends net.sf.jasperreports.engine.JRExporter> exporterClass) {
		Assert.isAssignable(net.sf.jasperreports.engine.JRExporter.class, exporterClass);
		this.exporterClass = exporterClass;
	}

	/**
	 * Specifies whether or not the {@code JRExporter} writes to the {@link java.io.PrintWriter}
	 * of the associated with the request ({@code true}) or whether it writes directly to the
	 * {@link java.io.InputStream} of the request ({@code false}). Default is {@code true}.
	 */
	public void setUseWriter(boolean useWriter) {
		this.useWriter = useWriter;
	}

	/**
	 * Checks that the {@link #setExporterClass(Class) exporterClass} property is specified.
	 */
	@Override
	protected void onInit() {
		if (this.exporterClass == null) {
			throw new IllegalArgumentException("exporterClass is required");
		}
	}


	/**
	 * Returns a new instance of the specified {@link net.sf.jasperreports.engine.JRExporter} class.
	 * @see #setExporterClass(Class)
	 * @see BeanUtils#instantiateClass(Class)
	 */
	@Override
	protected net.sf.jasperreports.engine.JRExporter createExporter() {
		return BeanUtils.instantiateClass(this.exporterClass);
	}

	/**
	 * Indicates how the {@code JRExporter} should render its data.
	 * @see #setUseWriter(boolean)
	 */
	@Override
	protected boolean useWriter() {
		return this.useWriter;
	}

}
