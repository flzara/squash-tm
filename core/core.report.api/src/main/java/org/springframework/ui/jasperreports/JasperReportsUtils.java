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
package org.springframework.ui.jasperreports;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;

/**
 * Utility methods for working with JasperReports. Provides a set of convenience
 * methods for generating reports in a CSV, HTML, PDF and XLS formats.
 *
 * <p><b>This class is compatible with classic JasperReports releases back until 2.x.</b>
 * As a consequence, it keeps using the {@link net.sf.jasperreports.engine.JRExporter}
 * API which has been deprecated in early 2014.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.1.3
 */
@SuppressWarnings({"deprecation", "rawtypes"})
public abstract class JasperReportsUtils {

	/**
	 * Convert the given report data value to a {@code JRDataSource}.
	 * <p>In the default implementation, a {@code JRDataSource},
	 * {@code java.util.Collection} or object array is detected.
	 * The latter are converted to {@code JRBeanCollectionDataSource}
	 * or {@code JRBeanArrayDataSource}, respectively.
	 * @param value the report data value to convert
	 * @return the JRDataSource (never {@code null})
	 * @throws IllegalArgumentException if the value could not be converted
	 * @see net.sf.jasperreports.engine.JRDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
	 */
	public static JRDataSource convertReportData(Object value) throws IllegalArgumentException {
		if (value instanceof JRDataSource) {
			return (JRDataSource) value;
		}
		else if (value instanceof Collection) {
			return new JRBeanCollectionDataSource((Collection<?>) value);
		}
		else if (value instanceof Object[]) {
			return new JRBeanArrayDataSource((Object[]) value);
		}
		else {
			throw new IllegalArgumentException("Value [" + value + "] cannot be converted to a JRDataSource");
		}
	}

	/**
	 * Render the supplied {@code JasperPrint} instance using the
	 * supplied {@code JRAbstractExporter} instance and write the results
	 * to the supplied {@code Writer}.
	 * <p>Make sure that the {@code JRAbstractExporter} implementation
	 * you supply is capable of writing to a {@code Writer}.
	 * @param exporter the {@code JRAbstractExporter} to use to render the report
	 * @param print the {@code JasperPrint} instance to render
	 * @param writer the {@code Writer} to write the result to
	 * @throws JRException if rendering failed
	 */
	public static void render(net.sf.jasperreports.engine.JRExporter exporter, JasperPrint print, Writer writer)
		throws JRException {

		exporter.setParameter(net.sf.jasperreports.engine.JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(net.sf.jasperreports.engine.JRExporterParameter.OUTPUT_WRITER, writer);
		exporter.exportReport();
	}

	/**
	 * Render the supplied {@code JasperPrint} instance using the
	 * supplied {@code JRAbstractExporter} instance and write the results
	 * to the supplied {@code OutputStream}.
	 * <p>Make sure that the {@code JRAbstractExporter} implementation you
	 * supply is capable of writing to a {@code OutputStream}.
	 * @param exporter the {@code JRAbstractExporter} to use to render the report
	 * @param print the {@code JasperPrint} instance to render
	 * @param outputStream the {@code OutputStream} to write the result to
	 * @throws JRException if rendering failed
	 */
	public static void render(net.sf.jasperreports.engine.JRExporter exporter, JasperPrint print,
							  OutputStream outputStream) throws JRException {

		exporter.setParameter(net.sf.jasperreports.engine.JRExporterParameter.JASPER_PRINT, print);
		exporter.setParameter(net.sf.jasperreports.engine.JRExporterParameter.OUTPUT_STREAM, outputStream);
		exporter.exportReport();
	}

	/**
	 * Render a report in CSV format using the supplied report data.
	 * Writes the results to the supplied {@code Writer}.
	 * @param report the {@code JasperReport} instance to render
	 * @param parameters the parameters to use for rendering
	 * @param writer the {@code Writer} to write the rendered report to
	 * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
	 * (converted accordingly), representing the report data to read fields from
	 * @throws JRException if rendering failed
	 * @see #convertReportData
	 */
	public static void renderAsCsv(JasperReport report, Map<String, Object> parameters, Object reportData,
								   Writer writer) throws JRException {

		JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData));
		render(new JRCsvExporter(), print, writer);
	}

	/**
	 * Render a report in CSV format using the supplied report data.
	 * Writes the results to the supplied {@code Writer}.
	 * @param report the {@code JasperReport} instance to render
	 * @param parameters the parameters to use for rendering
	 * @param writer the {@code Writer} to write the rendered report to
	 * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
	 * (converted accordingly), representing the report data to read fields from
	 * @param exporterParameters a {@link Map} of {@code JRExporterParameter exporter parameters}
	 * @throws JRException if rendering failed
	 * @see #convertReportData
	 */
	public static void renderAsCsv(JasperReport report, Map<String, Object> parameters, Object reportData,
								   Writer writer, Map<net.sf.jasperreports.engine.JRExporterParameter, Object> exporterParameters)
		throws JRException {

		JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData));
		JRCsvExporter exporter = new JRCsvExporter();
		exporter.setParameters(exporterParameters);
		render(exporter, print, writer);
	}

	/**
	 * Render a report in HTML format using the supplied report data.
	 * Writes the results to the supplied {@code Writer}.
	 * @param report the {@code JasperReport} instance to render
	 * @param parameters the parameters to use for rendering
	 * @param writer the {@code Writer} to write the rendered report to
	 * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
	 * (converted accordingly), representing the report data to read fields from
	 * @throws JRException if rendering failed
	 * @see #convertReportData
	 */
	public static void renderAsHtml(JasperReport report, Map<String, Object> parameters, Object reportData,
									Writer writer) throws JRException {

		JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData));
		render(new net.sf.jasperreports.engine.export.JRHtmlExporter(), print, writer);
	}

	/**
	 * Render a report in HTML format using the supplied report data.
	 * Writes the results to the supplied {@code Writer}.
	 * @param report the {@code JasperReport} instance to render
	 * @param parameters the parameters to use for rendering
	 * @param writer the {@code Writer} to write the rendered report to
	 * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
	 * (converted accordingly), representing the report data to read fields from
	 * @param exporterParameters a {@link Map} of {@code JRExporterParameter exporter parameters}
	 * @throws JRException if rendering failed
	 * @see #convertReportData
	 */
	public static void renderAsHtml(JasperReport report, Map<String, Object> parameters, Object reportData,
									Writer writer, Map<net.sf.jasperreports.engine.JRExporterParameter, Object> exporterParameters)
		throws JRException {

		JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData));
		net.sf.jasperreports.engine.export.JRHtmlExporter exporter = new net.sf.jasperreports.engine.export.JRHtmlExporter();
		exporter.setParameters(exporterParameters);
		render(exporter, print, writer);
	}

	/**
	 * Render a report in PDF format using the supplied report data.
	 * Writes the results to the supplied {@code OutputStream}.
	 * @param report the {@code JasperReport} instance to render
	 * @param parameters the parameters to use for rendering
	 * @param stream the {@code OutputStream} to write the rendered report to
	 * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
	 * (converted accordingly), representing the report data to read fields from
	 * @throws JRException if rendering failed
	 * @see #convertReportData
	 */
	public static void renderAsPdf(JasperReport report, Map<String, Object> parameters, Object reportData,
								   OutputStream stream) throws JRException {

		JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData));
		render(new JRPdfExporter(), print, stream);
	}

	/**
	 * Render a report in PDF format using the supplied report data.
	 * Writes the results to the supplied {@code OutputStream}.
	 * @param report the {@code JasperReport} instance to render
	 * @param parameters the parameters to use for rendering
	 * @param stream the {@code OutputStream} to write the rendered report to
	 * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
	 * (converted accordingly), representing the report data to read fields from
	 * @param exporterParameters a {@link Map} of {@code JRExporterParameter exporter parameters}
	 * @throws JRException if rendering failed
	 * @see #convertReportData
	 */
	public static void renderAsPdf(JasperReport report, Map<String, Object> parameters, Object reportData,
								   OutputStream stream, Map<net.sf.jasperreports.engine.JRExporterParameter, Object> exporterParameters)
		throws JRException {

		JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData));
		JRPdfExporter exporter = new JRPdfExporter();
		exporter.setParameters(exporterParameters);
		render(exporter, print, stream);
	}

	/**
	 * Render a report in XLS format using the supplied report data.
	 * Writes the results to the supplied {@code OutputStream}.
	 * @param report the {@code JasperReport} instance to render
	 * @param parameters the parameters to use for rendering
	 * @param stream the {@code OutputStream} to write the rendered report to
	 * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
	 * (converted accordingly), representing the report data to read fields from
	 * @throws JRException if rendering failed
	 * @see #convertReportData
	 */
	public static void renderAsXls(JasperReport report, Map<String, Object> parameters, Object reportData,
								   OutputStream stream) throws JRException {

		JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData));
		render(new JRXlsExporter(), print, stream);
	}

	/**
	 * Render a report in XLS format using the supplied report data.
	 * Writes the results to the supplied {@code OutputStream}.
	 * @param report the {@code JasperReport} instance to render
	 * @param parameters the parameters to use for rendering
	 * @param stream the {@code OutputStream} to write the rendered report to
	 * @param reportData a {@code JRDataSource}, {@code java.util.Collection} or object array
	 * (converted accordingly), representing the report data to read fields from
	 * @param exporterParameters a {@link Map} of {@code JRExporterParameter exporter parameters}
	 * @throws JRException if rendering failed
	 * @see #convertReportData
	 */
	public static void renderAsXls(JasperReport report, Map<String, Object> parameters, Object reportData,
								   OutputStream stream, Map<net.sf.jasperreports.engine.JRExporterParameter, Object> exporterParameters)
		throws JRException {

		JasperPrint print = JasperFillManager.fillReport(report, parameters, convertReportData(reportData));
		JRXlsExporter exporter = new JRXlsExporter();
		exporter.setParameters(exporterParameters);
		render(exporter, print, stream);
	}

}
