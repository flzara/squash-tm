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
package org.squashtest.tm.service.internal.chart

import javax.inject.Inject

import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.annotation.Transactional
import org.squashtest.tm.domain.customfield.BindableEntity
import org.squashtest.tm.domain.customfield.CustomField
import org.squashtest.tm.domain.customfield.CustomFieldBinding
import org.squashtest.tm.event.ChangeCustomFieldCodeEvent
import org.squashtest.tm.event.CreateCustomFieldBindingEvent
import org.squashtest.tm.event.DeleteCustomFieldBindingEvent
import org.squashtest.it.basespecs.DbunitServiceSpecification
import org.unitils.dbunit.annotation.DataSet

import spock.lang.Ignore
import spock.unitils.UnitilsSupport
@UnitilsSupport
@Transactional
@Ignore(value="The column prototype modification was implemented in 1.13 then desactivated because CUF in chart won't be part of 1.13. If the cuf part of 1.14 remove this annotation")
@DataSet("ColumnPrototypeModification.dataset.xml")
class ColumnPrototypeModificationIT extends DbunitServiceSpecification{

	@Inject
	ApplicationEventPublisher eventPublisher;

	def "should not create columnPrototype that already exist when new cufbinding is added" (){

		given:

		CustomField cuf = new CustomField(code:code)
		CustomFieldBinding cufBinding = new CustomFieldBinding(customField:cuf, boundEntity :  entityType )

		when :
		eventPublisher.publishEvent(new CreateCustomFieldBindingEvent(cufBinding));

		def result = findAll("ColumnPrototype");
		then :
		result.size == 6
		where :
		code        |        entityType                     || _
		"xx"        |        BindableEntity.TEST_CASE       || _
		"xx"        |        BindableEntity.CAMPAIGN        || _
	}

	def "should  create columnPrototype  when new cufbinding is added" (){

		given:

		CustomField cuf = new CustomField(code:code)
		CustomFieldBinding cufBinding = new CustomFieldBinding(customField:cuf, boundEntity :  entityType )

		when :
		eventPublisher.publishEvent(new CreateCustomFieldBindingEvent(cufBinding));

		def result = findAll("ColumnPrototype");
		then :
		result.size == 7

		where :
		code        |        entityType                     || _
		"xx"        |        BindableEntity.ITERATION       || _
		"ww"        |        BindableEntity.TEST_CASE       || _
		"ww"        |        BindableEntity.CAMPAIGN        || _
	}



	def "should delete columnPrototype when cufbinding is removed and no other binding are using this columnPrototype" (){

		when :
		eventPublisher.publishEvent(new DeleteCustomFieldBindingEvent(ids));

		then :
		def result = findAll("ColumnPrototype");
		result.size == remaining
		where :
		ids                                      || remaining
		[-1L]||     6
		[-2L]||     6
		[-3L]||     5
		[-4L]||     6
		[-5L]||     6
		[-6L]||     6
		[-7L]||     6
		[-8L]||     5
		[-1L, -2L]||     5
		[-1L, -2L, -3L]||     4
		[-1L, -4L, -6L]||     6
		[-1L, -4L, -7L]||     6
		[-1L, -5L, -6L]||     6
		[-1L, -5L, -7L]||     6
		[-3L, -8L]||     4
		[-1L, -2L, -3L, -4L, -5L]||     3
		[-1L, -2L, -3L, -4L, -5L, -6L, -8L]||     2
		[-1L, -2L, -3L, -4L, -5L, -6L, -7L]||     2
		[-1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L]||     1
	}


	def "should update column prototype when cuf code is changed" () {
		when :
		eventPublisher.publishEvent(new ChangeCustomFieldCodeEvent((String[]) codes));
		def result = findAll("ColumnPrototype").findAll {return it.attributeName == codes[1]};
		then :
		result.size == number
		where :
		codes             || number
		["xx", "AAAA"]||   2
		["aa", "AAAA"]||   3
		["ww", "AAAA"]||   0
	}

	def "should delete filter when corresponding columnPrototype is deleted" (){

		when :
		eventPublisher.publishEvent(new DeleteCustomFieldBindingEvent(ids));

		then :
		def filters = findAll("Filter")
		filters.size == remaining
		where :
		ids                                      || remaining
		[-1L]||     1
		[-2L]||     1
		[-1L, -2L]||     0
		[-1L, -2L, -3L]||     0
	}
}
