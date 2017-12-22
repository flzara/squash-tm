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
/**
*     This file is part of the Squashtest platform.
*     Copyright (C) 2010 - 2012 Henix, henix.fr
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

import org.hibernate.Query;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.test.context.ContextConfiguration
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao;
import org.unitils.dbunit.annotation.DataSet;
import org.squashtest.tm.service.internal.repository.CustomFieldValueDao.CustomFieldValuesPair; 

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet
class HibernateCustomFieldValueDaoIT extends DbunitDaoSpecification{
	
	@Inject
	CustomFieldValueDao dao;
	
	
	def "should find all the custom field values for test case 1"(){
		
		when :
			List<CustomFieldValue> values = dao.findAllCustomValues(-111L, BindableEntity.TEST_CASE);
			
		then :
			values.size()==2
			values*.id.containsAll([-1111L, -1112L])
		
	}
	
	
	def "should find all the custom field values that are instances of a given custom field binding"(){
		
		when :
			List<CustomFieldValue> values = dao.findAllCustomValuesOfBinding(-112L)
			
		then :
			values.size()==2
			values*.id.containsAll([-1112L, -1122L])
		
	}
	
	def "should find pairs of custom field values"(){
		
		when :
			List<CustomFieldValuesPair> pairs = dao.findPairedCustomFieldValues(BindableEntity.TEST_CASE, -111L,-112L )
			
		then :
			pairs.collect{ return [it.original.id, it.recipient.id] } as Set == [ [-1111L, -1121L], [-1112L, -1122L] ] as Set
			
		
	}

}