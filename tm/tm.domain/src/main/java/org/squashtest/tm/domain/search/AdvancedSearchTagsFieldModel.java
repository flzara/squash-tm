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
package org.squashtest.tm.domain.search;

import java.util.ArrayList;
import java.util.List;

public class AdvancedSearchTagsFieldModel implements AdvancedSearchFieldModel {

	public static enum Operation{
		AND, OR;
	}

	private AdvancedSearchFieldModelType type = AdvancedSearchFieldModelType.TAGS;
	private boolean ignoreBridge = false;

	private List<String> tags = new ArrayList<>();
	private Operation operation;

	@Override
	public AdvancedSearchFieldModelType getType() {
		return type;
	}

	@Override
	public boolean isIgnoreBridge() {
		return ignoreBridge;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}



}
