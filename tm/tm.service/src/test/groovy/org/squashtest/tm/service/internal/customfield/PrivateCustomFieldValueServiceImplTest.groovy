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
package org.squashtest.tm.service.internal.customfield 

import javax.inject.Provider;

import org.squashtest.tm.domain.customfield.BindableEntity;

import static org.squashtest.tm.domain.customfield.BindableEntity.*;
import spock.lang.Specification;
import spock.lang.Unroll;

/**
 * @author Gregory
 *
 */
class PrivateCustomFieldValueServiceImplTest  extends Specification {
	PrivateCustomFieldValueServiceImpl service = new PrivateCustomFieldValueServiceImpl();
	DefaultEditionStatusStrategy defaultStrategy = Mock()
	RequirementBoundEditionStatusStrategy requirementStrategy = Mock()
	
	def setup() {
		service.defaultEditionStatusStrategy = defaultStrategy
		service.requirementBoundEditionStatusStrategy = requirementStrategy 
	}
	
	@Unroll
	def "#bindableEntity should have editable CF values"() {
		given:
		defaultStrategy.isEditable(10, bindableEntity) >> { bindableEntity != REQUIREMENT_VERSION } 
		requirementStrategy.isEditable(10, bindableEntity) >> { bindableEntity == REQUIREMENT_VERSION }
		
		expect: 
		service.areValuesEditable(10, bindableEntity)
		
		where:
		bindableEntity << BindableEntity.values()
		
	}

}
