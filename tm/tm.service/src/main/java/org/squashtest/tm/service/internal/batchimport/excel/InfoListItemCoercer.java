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
package org.squashtest.tm.service.internal.batchimport.excel;

import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.testcase.TestCaseNature;
import org.squashtest.tm.domain.testcase.TestCaseType;

public final class InfoListItemCoercer<T extends InfoListItem>
extends TypeBasedCellValueCoercer<T >
implements CellValueCoercer<T> {

	public static enum ListRole { ROLE_NATURE, ROLE_TYPE, ROLE_CATEGORY };

	private static final Set<String> OLD_NATURES;
	private static final Set<String> OLD_TYPES;

	static {
		OLD_NATURES = new HashSet<>(8);
		for (TestCaseNature nat : TestCaseNature.values()){
			OLD_NATURES.add(nat.toString());
		}

		OLD_TYPES = new HashSet<>(7);
		for (TestCaseType typ : TestCaseType.values()){
			OLD_TYPES.add(typ.toString());
		}
	}

	private ListRole role;

	public InfoListItemCoercer(ListRole role){
		this.role = role;
	}

	@Override
	protected T coerceStringCell(Cell cell) {
		String cellValue = cell.getStringCellValue();
		InfoListItem coerced;

		/*
		 * Back compatibility concern : older exports still use the values from TestCaseNature and TestCaseType,
		 * now deprecated.
		 * 
		 * To ensure they still can be imported we must check for those special cases, if encoutered we translate them
		 * to the new format.
		 * 
		 */

		if (role == ListRole.ROLE_NATURE && OLD_NATURES.contains(cellValue)){
			coerced = new ListItemReference("NAT_"+cellValue);
		}
		else if (role == ListRole.ROLE_TYPE && OLD_TYPES.contains(cellValue)){
			coerced = new ListItemReference("TYP_"+cellValue);
		}
		else{
			coerced = new ListItemReference(cellValue);
		}

		return (T) coerced;
	}

	@Override
	protected T coerceBlankCell(Cell cell) {
		return null;
	}
}
