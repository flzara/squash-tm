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


import javax.inject.Inject;

import org.hibernate.Query
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.service.internal.repository.CustomFieldBindingDao;
import org.squashtest.tm.service.internal.repository.hibernate.CustomFieldBindingDaoImpl.NewBindingPosition;
import org.unitils.dbunit.annotation.DataSet;

import spock.unitils.UnitilsSupport;


@UnitilsSupport
@DataSet
class CustomFieldBindingDaoIT extends DbunitDaoSpecification {

	@Inject
	CustomFieldBindingDaoImpl dao

	@Inject
	CustomFieldBindingDao dynamicDao;


	def "should get correct indexes from a messed up table"(){

		when :
		List<NewBindingPosition> newPositions = dao.recomputeBindingPositions();

		then :
		def collected = newPositions.collect { return [ it.bindingId, it.formerPosition, it.newPosition] }
		def expected = [
			[-121L, 5, 3],
			[-131L, 1 ,1],
			[-111L, 2, 2],
			[-221L, 8, 3],
			[-122L, 10, 3],
			[-241L, 1, 1],
			[-132L, 2, 2],
			[-211L, 3, 2],
			[-112L, 0, 1]
		]

		collected as Set == expected as Set
	}

	def "should update the position of some cuf binding"(){

		given :
		def newPositions = [
			newPosition(-241L, 1, 1),
			newPosition(-221L, 8, 3),
			newPosition(-211L, 3, 2),
		]

		when :
		dao.updateBindingPositions(newPositions);

		Query q = getSession().createQuery("from CustomFieldBinding where id in (-241, -221, -211)")
		List<CustomFieldBinding> bindings = q.list();

		then :
		bindings*.position as Set == [1, 3, 2] as Set
	}


	def "should find all the cfb having the same project and bound entity as this one"(){

		when :
		def res = dynamicDao.findAllAlike(-221L)

		then :
		res*.id as Set == [-211L, -221L, -241L] as Set



	}


	NewBindingPosition newPosition(id, former, newp){
		return new NewBindingPosition(bindingId : id, formerPosition : former, newPosition : newp);
	}

}
