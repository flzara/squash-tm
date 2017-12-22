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
package org.squashtest.tm.service.internal.customreport;

import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.customreport.TreeEntityVisitor;
import org.squashtest.tm.domain.report.ReportDefinition;
import org.squashtest.tm.service.deletion.OperationReport;

/**
 * Class used to do specific entity job on delete operations
 * @author jthebault
 */
public class CRLNDeletionVisitor implements TreeEntityVisitor{

	private OperationReport operationReport;
	private CustomReportLibraryNode customReportLibraryNode;

	public CRLNDeletionVisitor(OperationReport operationReport,CustomReportLibraryNode customReportLibraryNode) {
		super();
		this.operationReport = operationReport;
		this.customReportLibraryNode = customReportLibraryNode;
	}

	@Override
	public void visit(CustomReportFolder crf) {
		addRemoved("folder");
	}

	@Override
	public void visit(CustomReportLibrary crl) {
		throw new UnsupportedOperationException("Cannot delete libraies by this service. The only way to delete a library is to delete the project");
	}

	@Override
	public void visit(CustomReportDashboard crf) {
		addRemoved("dashboard");
	}

	@Override
	public void visit(ChartDefinition chartDefinition) {
		addRemoved("chart");
	}

	public void visit(ReportDefinition reportDefinition) {
		addRemoved("report");
	}

	private void addRemoved(String relType){
		operationReport.addRemoved(relType, customReportLibraryNode.getId());
	}

}
