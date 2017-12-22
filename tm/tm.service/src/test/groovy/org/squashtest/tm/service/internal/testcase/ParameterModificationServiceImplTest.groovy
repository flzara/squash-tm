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
package org.squashtest.tm.service.internal.testcase;

import org.squashtest.tm.domain.testcase.Parameter
import org.squashtest.tm.service.internal.repository.DatasetDao
import org.squashtest.tm.service.internal.repository.ParameterDao;

import spock.lang.Specification;

public class ParameterModificationServiceImplTest extends Specification {

	ParameterModificationServiceImpl service = new ParameterModificationServiceImpl();
	ParameterDao parameterDao = Mock()
	
	def setup() {
		service.parameterDao = parameterDao;
	}
	
	def "should delete parameter "(){
		given:
		Parameter parameter = Mock()
		parameterDao.findById(1L) >> parameter
			
		when :
		service.removeById(1L)
		
		then:
		1* parameterDao.delete(parameter)
	}
}
