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
package org.squashtest.tm.domain.customreport;

import org.squashtest.tm.core.foundation.lang.Wrapped;
import org.squashtest.tm.domain.chart.ChartDefinition;
import org.squashtest.tm.domain.report.ReportDefinition;

public class GetCustomReportTreeDefinitionVisitor implements CustomReportTreeEntityVisitor {

	private Wrapped<CustomReportTreeDefinition> customReportTreeDefinition = new Wrapped<>();

	@Override
	public void visit(CustomReportFolder crf) {
		customReportTreeDefinition.setValue(CustomReportTreeDefinition.FOLDER);
	}

	@Override
	public void visit(CustomReportLibrary crl) {
		customReportTreeDefinition.setValue(CustomReportTreeDefinition.LIBRARY);
	}

	@Override
	public void visit(CustomReportDashboard crf) {
		customReportTreeDefinition.setValue(CustomReportTreeDefinition.DASHBOARD);
	}

	@Override
	public void visit(ChartDefinition chartDefinition) {
		customReportTreeDefinition.setValue(CustomReportTreeDefinition.CHART);
	}

	@Override
	public void visit(ReportDefinition reportDefinition) {
		customReportTreeDefinition.setValue(CustomReportTreeDefinition.REPORT);
	}

	@Override
	public void visit(CustomReportCustomExport crce) {
		customReportTreeDefinition.setValue(CustomReportTreeDefinition.CUSTOM_EXPORT);
	}

	public CustomReportTreeDefinition getCustomReportTreeDefinition() {
		return customReportTreeDefinition.getValue();
	}
}
