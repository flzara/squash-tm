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
package org.squashtest.tm.api.report.spring.view.jasperreports;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;

import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;


/*
 * A JRGraphic2DExporter needs an awt Graphic object to print the report into. But guess what ? 
 * JasperReportsMultiFormatView doesn't give a damn of your parameters and will override them
 * with its own parameter map.
 * 
 * So we can't fully initialize the Exporter here, additional code to set up the java.awt.Graphic2d is
 * required elsewhere.
 * 
 */
public class JasperReportsGraphic2DView extends AbstractJasperReportsSingleFormatView {

	public JasperReportsGraphic2DView(){
		setContentType("image/bmp");
	}
	
	@Override
	protected JRExporter createExporter() {
		try {			
			return new JRGraphics2DExporter();
			
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean useWriter() {
		// TODO Auto-generated method stub
		return false;
	}



}
