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
package org.squashtest.tm.service.internal.denormalizedField;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolder;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.service.denormalizedfield.DenormalizedFieldValueManager;


/**
 * That interface should remain private to this bundle. The reason is that the methods will not be secured. 
 * 
 * @author mpagnon
 *
 */
@Transactional
public interface PrivateDenormalizedFieldValueService extends DenormalizedFieldValueManager {

	
	/**
	 * Will create all the denormalized field values for one entity.
	 * 
	 * @param source : the {@link BoundEntity} from which the destination is created
	 * @param destination : the {@link DenormalizedFieldHolder} newly created
	 * 
	 */
	void createAllDenormalizedFieldValues(BoundEntity source, DenormalizedFieldHolder destination);
	
	
	/**
	 * will delete all the denormalized field values for one entity
	 * 
	 * @param entity
	 */
	void deleteAllDenormalizedFieldValues(DenormalizedFieldHolder entity);


	/**
	 * Will create the custom field values of all the execution steps in that execution in a lot. 
	 * If some execution steps come from call steps that belong to a different project, 
	 * will apply the reordering as requested and implemented in {@link #createAllDenormalizedFieldValues(ActionTestStep, ExecutionStep, Project)}
	 *
	 * @param execution
	 */
	void createAllDenormalizedFieldValuesForSteps(Execution execution);
}
