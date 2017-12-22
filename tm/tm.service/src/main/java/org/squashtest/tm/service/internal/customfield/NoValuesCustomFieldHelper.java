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
package org.squashtest.tm.service.internal.customfield;

import java.util.Collections;
import java.util.List;

import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.project.Project;

/**
 * Custom field helper to be used when there are no entities in the given custom field binding context.
 * 
 * @author Gregory Fouquet
 * 
 */
public class NoValuesCustomFieldHelper<BOUND extends BoundEntity> extends AbstractCustomFieldHelper<BOUND> {
	private final Project project;
	private final BindableEntity boundType;

	protected NoValuesCustomFieldHelper(Project project, BindableEntity boundType) {
		super();
		this.project = project;
		this.boundType = boundType;
	}

	/**
	 * @see org.squashtest.tm.service.internal.customfield.AbstractCustomFieldHelper#initCustomFields()
	 */
	@Override
	protected void initCustomFields() {
		customFields = findCustomFields(project.getId(), boundType, getLocations());

	}

	/**
	 * @see org.squashtest.tm.service.internal.customfield.AbstractCustomFieldHelper#doGetCustomFieldValues()
	 * @return an empty list
	 */
	@Override
	protected List<CustomFieldValue> doGetCustomFieldValues() {
		// no entities -> no values
		return Collections.emptyList();
	}
}
