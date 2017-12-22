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
package org.squashtest.tm.service.internal.repository.hibernate

import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.core.foundation.collection.SortOrder
import org.squashtest.tm.core.foundation.collection.SpringPaginationUtils;
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.domain.customfield.InputType
import org.squashtest.tm.service.internal.repository.CustomFieldDao
import org.unitils.dbunit.annotation.DataSet
import spock.lang.Ignore
import spock.unitils.UnitilsSupport
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

import javax.inject.Inject

@UnitilsSupport
class CustomFieldDaoIT extends DbunitDaoSpecification {
	@Inject
	CustomFieldDao customFieldDao

	@DataSet("HibernateCustomFieldDaoIT.should return list of cuf ordered by name.xml")
	def "should return list of cuf ordered by name" () {
		when:
		List<CustomField> list = customFieldDao.findAllByOrderByNameAsc()

		then:
		list.size() == 3
		list.get(0).name == "abc"
		list.get(1).name == "cde"
		list.get(2).name == "fde"
	}

	@DataSet("HibernateCustomFieldDaoIT.should return sorted list of cuf.xml")
	def "should return sorted list of cuf"(){
		when:
		Page<CustomField> list = customFieldDao.findAll(new PageRequest(1,2,new Sort("inputType")))

		then:
		list.size == 2
		list.content[0].inputType == InputType.DROPDOWN_LIST
		list.content[1].inputType == InputType.PLAIN_TEXT

	}


	@DataSet("HibernateCustomFieldDeletionDaoIT.should delete custom field.xml")
	def "should delete custom field" () {
		given:
		CustomField cuf =  em.find(CustomField, -1L)
                
		when:
		customFieldDao.delete(cuf)
		then:
		!found(CustomField.class, -1L)
		found(CustomField.class, -2L)
	}

	@DataSet("HibernateCustomFieldDeletionDaoIT.should delete custom field and option.xml")
	def "should delete custom field and options" () {
		given:
		CustomField cuf =  em.find(CustomField, -1L)
		when:
		customFieldDao.delete(cuf)
		then:
		!found(CustomField.class, -1L)
		!foundCustomFieldOption("first")
		!foundCustomFieldOption("second")
		!foundCustomFieldOption("third")
	}



	private foundCustomFieldOption(String label){
		em.createNativeQuery("select count(*) from CUSTOM_FIELD_OPTION  where label = :label")
			.setParameter("label", label)
			.singleResult == 1
	}

}
