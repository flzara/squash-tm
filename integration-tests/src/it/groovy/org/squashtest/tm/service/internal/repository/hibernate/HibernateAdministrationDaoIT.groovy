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

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.squashtest.it.basespecs.DbunitDaoSpecification;
import org.unitils.dbunit.annotation.DataSet
import spock.unitils.UnitilsSupport

import javax.inject.Inject

@UnitilsSupport
class HibernateAdministrationDaoIT extends DbunitDaoSpecification {
	@Inject HibernateAdministrationDao administrationDao;


	@DataSet("HibernateAdministrationDaoIT.should return administration statistics.xml")
	def "should return administration statistics"(){
		given:
		DataSourceProperties ds = Mock();
		ds.getUrl() >> "jdbc:h2://127.0.0.1:3306/squash-tm";
		administrationDao.dataSourceProperties = ds;

		when:
		def result = administrationDao.findAdministrationStatistics()

		then:
		result.projectsNumber == 1;
	}
}
