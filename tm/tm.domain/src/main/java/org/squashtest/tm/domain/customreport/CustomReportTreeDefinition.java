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

import org.squashtest.tm.domain.tree.TreeEntityDefinition;

/**
 * Due to the constant nature of column discriminator in {@link CustomReportLibraryNode#getEntity()}
 * keep the name of enum type strictly identical to the corresponding {@link CustomReportNodeType}
 * @author jthebault
 *
 */
public enum CustomReportTreeDefinition implements TreeEntityDefinition{
	LIBRARY(true, CustomReportNodeType.LIBRARY_NAME),
	DASHBOARD(false,CustomReportNodeType.DASHBOARD_NAME),
	CHART(false, CustomReportNodeType.CHART_NAME),
	FOLDER(true, CustomReportNodeType.FOLDER_NAME),
	REPORT(false, CustomReportNodeType.REPORT_NAME);

	private boolean container;

	private final String typeIdentifier;

	private CustomReportTreeDefinition(boolean container,String typeIdentifier) {
		this.container = container;
		this.typeIdentifier = typeIdentifier;
	}

	@Override
	public String getTypeName() {
		return typeIdentifier;
	}

	@Override
	public boolean isContainer() {
		return container;
	}

}
