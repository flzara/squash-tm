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

import javax.inject.Inject
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.SessionFactory
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.customfield.CustomFieldValue
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.squashtest.tm.service.customfield.CustomFieldManagerService
import org.unitils.dbunit.annotation.DataSet

import spock.unitils.UnitilsSupport

@UnitilsSupport
@Transactional
@DataSet("CustomFieldVariousIT.sandbox.xml")
class CustomCustomFieldManagerServiceIT extends DbunitServiceSpecification {

	@Inject
	CustomFieldManagerService service


	def "should add default value to custom fields without a value"(){

		when :
		service.changeOptional(-1L,false)
		CustomFieldValue value1 = findEntity(CustomFieldValue.class, -1111L)
		CustomFieldValue value2 = findEntity(CustomFieldValue.class, -1112L)

		then :
		value1.getValue().equals("NOSEC")
		value2.getValue().equals("true")
	}
}
