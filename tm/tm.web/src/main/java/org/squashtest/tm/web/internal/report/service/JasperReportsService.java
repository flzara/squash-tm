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
package org.squashtest.tm.web.internal.report.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * For now, internal use only. TODO : relocate that class the day we make a real reporting OSGI service ?
 *
 * @author bsiri
 *
 */
@Service("squashtest.tm.service.JasperReportsService")
public class JasperReportsService {

	private final Map<String,Class<? extends JRExporter>> exporterMaps = new HashMap<>();

	//todo : make it Spring configurable instead.
	public JasperReportsService(){
		registerFormat("csv", JRCsvExporter.class);
		registerFormat("xls", JRXlsExporter.class);
	}


	public Set<String> getSupportedformats(){
		return exporterMaps.keySet();
	}


	public boolean isSupported(String format){
		return exporterMaps.keySet().contains(format);
	}


	private void registerFormat(String format, Class<? extends JRExporter> jrExporterClass){
		if (isSupported(format)){
			Class<?> clazz = exporterMaps.get(format);
			throw new AlreadyMappedException("the format "+format+" is already mapped to "+clazz.getName());
		}
		exporterMaps.put(format, jrExporterClass);
	}


	private JRExporter getExporter(String format){
		Class<? extends JRExporter> exporterClass = exporterMaps.get(format);
		if (exporterClass==null) {
			throw new UnsupportedFormatException("no exporter defined for "+format);
		}
		try{
			return exporterClass.newInstance();
		}catch(Exception e){
			throw new RuntimeException(e);
		}

	}

	/**
	 *
	 *
	 * @param jasperStream
	 * @param format
	 * @param dataSource
	 * @param reportParameter, not null, empty maps are legals.
	 * @param exportParameter, not null, empty maps are legals.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public InputStream getReportAsStream(InputStream jasperStream, String format, Collection<?> dataSource,
			Map reportParameter, Map<JRExporterParameter, Object> exportParameter){
		try{
			JRExporter exporter = getExporter(format);

			//create the jasper print
			JRDataSource jasperDataSource = new JRBeanCollectionDataSource(dataSource);
			JasperPrint jPrint = JasperFillManager.fillReport(jasperStream, reportParameter, jasperDataSource);

			//export it
			File reportFile = File.createTempFile("export",format);
			reportFile.deleteOnExit();
			FileOutputStream reportOut = new FileOutputStream(reportFile);

			exportParameter.put(JRExporterParameter.OUTPUT_STREAM, reportOut);
			exportParameter.put(JRExporterParameter.JASPER_PRINT, jPrint);
			exporter.setParameters(exportParameter);
			exporter.exportReport();

			return new FileInputStream(reportFile);

		}catch(IOException | JRException ioe){
			throw new RuntimeException(ioe);
		}


	}


}
