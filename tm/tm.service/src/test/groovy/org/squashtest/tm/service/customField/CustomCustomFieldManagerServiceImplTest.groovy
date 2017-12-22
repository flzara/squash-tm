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
package org.squashtest.tm.service.customField

import org.springframework.context.ApplicationEventPublisher;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder
import org.squashtest.tm.core.foundation.collection.PagingAndSorting
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.exception.customfield.CodeAlreadyExistsException;
import org.squashtest.tm.service.internal.customfield.CustomCustomFieldManagerServiceImpl
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao
import org.squashtest.tm.service.internal.repository.CustomFieldDao

import spock.lang.Specification
import spock.lang.Unroll;

class CustomCustomFieldManagerServiceImplTest extends Specification {

	CustomCustomFieldManagerServiceImpl service = new CustomCustomFieldManagerServiceImpl();
	CustomFieldDao customFieldDao = Mock()
	CustomFieldBindingDao customFieldBindingDao = Mock();
	ApplicationEventPublisher eventPublisher = Mock()

	def setup() {
		service.customFieldDao = customFieldDao
		service.customFieldBindingDao = customFieldBindingDao
		service.eventPublisher = eventPublisher
	}

	def "should delete custom field"(){
		given:
		CustomField cuf = Mock()
		List<Long> bindingIds = new ArrayList<Long>();
		customFieldDao.findById(1L) >> cuf
		customFieldBindingDao.findAllByCustomFieldIdOrderByPositionAsc(1L) >> bindingIds;

		when :
		service.deleteCustomField(1L);

		then:
		1* customFieldDao.delete(cuf)
	}


	def "should change code to available code"() {
		given:
		CustomField field = Mock()
		field.code >> "CODE"
		customFieldDao.findById(10L) >> field

		and:
		customFieldDao.findByCode("NEW CODE") >> null

		when:
		service.changeCode(10L, "NEW CODE");

		then:
		notThrown(CodeAlreadyExistsException)
	}

	def "should change code to previous code"() {
		given:
		CustomField field = Mock()
		field.code >> "CODE"
		customFieldDao.findById(10L) >> field

		and:
		customFieldDao.findByCode("CODE") >> field

		when:
		service.changeCode(10L, "CODE");

		then:
		notThrown(CodeAlreadyExistsException)
	}

	def "should not changed code to an assigned one"() {
		given:
		CustomField field = Mock()
		field.code >> "CODE"
		customFieldDao.findById(10L) >> field

		and:
		customFieldDao.findByCode("CLASHING CODE") >> Mock(CustomField)

		when:
		service.changeCode(10L, "CLASHING CODE");

		then:
		thrown(CodeAlreadyExistsException)

	}
}
