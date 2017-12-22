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

import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;

import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsSingleFormatView;

public class JasperReportsOdsView extends AbstractJasperReportsSingleFormatView {

	public JasperReportsOdsView(){
		setContentType("application/octet-stream");
	}
	
	@Override
	protected JRExporter createExporter() {
		return new JROdsExporter();
	}

	@Override
	protected boolean useWriter() {
		return false;
	}

}
