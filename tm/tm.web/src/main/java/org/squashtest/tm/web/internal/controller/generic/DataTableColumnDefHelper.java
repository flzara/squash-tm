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
package org.squashtest.tm.web.internal.controller.generic;

import org.squashtest.tm.web.internal.controller.widget.AoColumnDef;

import java.util.List;

/**
 * Help creates the list of objects representing aoColumnDefs to give to the DataTable jquery plugin.
 * @author mpagnon
 *
 */

// XSS OK
public class DataTableColumnDefHelper {
	protected void addATargets(List<AoColumnDef> columns2) {
		for (int i = 0; i < columns2.size(); i++) {
			int[] aTarget = new int[1];
			aTarget[0] = i;
			columns2.get(i).setaTargets(aTarget);
		}

	}
}
