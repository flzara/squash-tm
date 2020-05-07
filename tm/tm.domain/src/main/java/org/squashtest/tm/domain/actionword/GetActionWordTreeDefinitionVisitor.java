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
package org.squashtest.tm.domain.actionword;

import org.squashtest.tm.core.foundation.lang.Wrapped;
import org.squashtest.tm.domain.bdd.ActionWord;

public class GetActionWordTreeDefinitionVisitor implements ActionWordTreeEntityVisitor {

	private Wrapped<ActionWordTreeDefinition> actionWordTreeDefinition = new Wrapped<>();

	@Override
	public void visit(ActionWordLibrary actionWordLibrary) {
		actionWordTreeDefinition.setValue(ActionWordTreeDefinition.LIBRARY);
	}

	@Override
	public void visit(ActionWord actionWord) {
		actionWordTreeDefinition.setValue(ActionWordTreeDefinition.ACTION_WORD);
	}

	public ActionWordTreeDefinition getActionWordTreeDefinition() {
		return actionWordTreeDefinition.getValue();
	}
}
