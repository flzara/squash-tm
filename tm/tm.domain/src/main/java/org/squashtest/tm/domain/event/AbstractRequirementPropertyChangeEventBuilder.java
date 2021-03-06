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
package org.squashtest.tm.domain.event;

import org.squashtest.tm.domain.requirement.RequirementVersion;

import javax.validation.constraints.NotNull;

abstract class AbstractRequirementPropertyChangeEventBuilder<EVENT extends RequirementAuditEvent> implements
		RequirementPropertyChangeEventBuilder<EVENT> {
	protected Object oldValue;
	protected Object newValue;
	protected String modifiedProperty;
	protected RequirementVersion eventSource;
	protected String author;

	@Override
	public RequirementPropertyChangeEventBuilder<EVENT> setOldValue(Object value) {
		oldValue = value;
		return this;
	}

	@Override
	public RequirementPropertyChangeEventBuilder<EVENT> setNewValue(Object value) {
		newValue = value;
		return this;
	}

	@Override
	public RequirementPropertyChangeEventBuilder<EVENT> setModifiedProperty(@NotNull String propertyName) {
		modifiedProperty = propertyName;
		return this;
	}

	@Override
	public RequirementPropertyChangeEventBuilder<EVENT> setSource(@NotNull RequirementVersion requirementVersion) {
		eventSource = requirementVersion;
		return this;
	}

	@Override
	public RequirementPropertyChangeEventBuilder<EVENT> setAuthor(@NotNull String author) {
		this.author = author;
		return this;
	}

}
