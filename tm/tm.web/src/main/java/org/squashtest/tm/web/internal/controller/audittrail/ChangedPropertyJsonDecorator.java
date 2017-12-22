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
package org.squashtest.tm.web.internal.controller.audittrail;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.squashtest.tm.domain.event.ChangedProperty;

/**
 * Decorates a {@link ChangedProperty} so that it can be serialized using Jackson.
 * 
 * @author Gregory Fouquet
 * 
 */
public class ChangedPropertyJsonDecorator implements ChangedProperty {
	/**
	 * @param changedProperty
	 */
	public ChangedPropertyJsonDecorator(@NotNull ChangedProperty changedProperty) {
		super();
		this.changedProperty = changedProperty;
	}

	@JsonIgnore
	private final ChangedProperty changedProperty;

	@Override
	public String getPropertyName() {
		return changedProperty.getPropertyName();
	}

	@Override
	public String getOldValue() {
		return changedProperty.getOldValue();
	}

	@Override
	public String getNewValue() {
		return changedProperty.getNewValue();
	}

}
