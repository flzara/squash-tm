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

import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;

@Component
public class NameResolver {

	final String copySuffix = "-Copie";

	void resolveNewName(CustomReportLibraryNode node, CustomReportLibraryNode target) {
		if (target.childNameAlreadyUsed(node.getName())) {
			resolveNameConflict(target, node, 1);
		}
	}

	void resolveNameConflict(CustomReportLibraryNode target, CustomReportLibraryNode node, int i) {
		String testedName = node.getName() + copySuffix + i;
		if (target.childNameAlreadyUsed(testedName)) {
			resolveNameConflict(target, node, i + 1);
		} else {
			node.setName(testedName);
			node.getEntity().setName(testedName);
		}
	}
}
