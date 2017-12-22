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
package org.squashtest.tm.service.internal.infolist

import javax.inject.Inject

import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.squashtest.tm.service.internal.repository.InfoListDao
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.datasetloadstrategy.DataSetLoadStrategy;
import org.unitils.dbunit.datasetloadstrategy.impl.InsertLoadStrategy;
import org.unitils.dbunit.datasetloadstrategy.impl.RefreshLoadStrategy;

import spock.unitils.UnitilsSupport;

/**
 * @author Gregory Fouquet
 *
 */
@UnitilsSupport
class InfoListDaoIT extends DbunitDaoSpecification {
	@Inject
	InfoListDao infoListDao;

	@DataSet(value="InfoListDaoIT.findAll.xml", loadStrategy = RefreshLoadStrategy)
	def "should find all unbound lists"() {
		when:
		def res = infoListDao.findAllUnbound()

		then:
		res*.id.containsAll([1L, 9000002L, 9000003L])
		res*.id.size() == 3
	}

	@DataSet(value= "InfoListDaoIT.findAll.xml", loadStrategy = RefreshLoadStrategy)
	def "should find all bound lists"() {
		when:
		def res = infoListDao.findAllBound()

		then:
		res*.id.containsAll([2L, 3L, 9000001L])
		res*.id.size() == 3
	}
}
