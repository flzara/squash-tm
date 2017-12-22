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
package org.squashtest.tm.service.customfield;

import java.util.List;

import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.TestStep;

public interface CustomFieldHelperService {

	boolean hasCustomFields(BoundEntity entity);

	<X extends BoundEntity> CustomFieldHelper<X> newHelper(X entity);

	<X extends BoundEntity> CustomFieldHelper<X> newHelper(List<X> entities);

	/**
	 * Creates a {@link CustomFieldHelper} for the given test steps contained in the given project.
	 * 
	 * @param steps
	 * @param project
	 * @return
	 */
	CustomFieldHelper<ActionTestStep> newStepsHelper(List<TestStep> steps, Project project);

	<X extends DenormalizedFieldHolder> DenormalizedFieldHelper<X> newDenormalizedHelper(X entity);

	<X extends DenormalizedFieldHolder> DenormalizedFieldHelper<X> newDenormalizedHelper(List<X> entities);
}