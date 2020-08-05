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
package org.squashtest.tm.service.internal.actionword;

import org.squashtest.tm.domain.actionword.ActionWordLibrary;
import org.squashtest.tm.domain.actionword.ActionWordLibraryNode;
import org.squashtest.tm.domain.actionword.ActionWordTreeEntityVisitor;
import org.squashtest.tm.domain.bdd.ActionWord;
import org.squashtest.tm.service.deletion.OperationReport;

/**
 * Class used to do specific entity job on delete operations
 * @author qtran
 */
public class AWLNDeletionVisitor implements ActionWordTreeEntityVisitor {

	private final OperationReport operationReport;
	private final ActionWordLibraryNode actionWordLibraryNode;

	public AWLNDeletionVisitor(OperationReport operationReport, ActionWordLibraryNode actionWordLibraryNode) {
		super();
		this.operationReport = operationReport;
		this.actionWordLibraryNode = actionWordLibraryNode;
	}

	@Override
	public void visit(ActionWordLibrary actionWordLibrary) {
		throw new UnsupportedOperationException("Cannot delete libraies by this service. The only way to delete a library is to delete the project");
	}

	@Override
	public void visit(ActionWord actionWord) {
		operationReport.addRemoved("action-word", actionWordLibraryNode.getId());
	}
}
