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

import spock.lang.Specification


class HibernateAdministrationDaoTest extends Specification {

	HibernateAdministrationDao administrationDao = new HibernateAdministrationDao();

	def "should return the database name from jdbc url"(){
		given:
		def urlWithParams = "jdbc:mysql://127.0.0.1:3306/squash-tm?param1=true&param2=2";
		def urlWithoutParams = "jdbc:postgresql://127.0.0.1:3306/squash-tm";
		def urlWeird = "jdbc:fal/se-u/rl/squash-tm";

		when:
		def resultWithParams = administrationDao.getDatabaseName(urlWithParams);
		def resultWithoutParams = administrationDao.getDatabaseName(urlWithoutParams);
		def resultWeird = administrationDao.getDatabaseName(urlWeird);

		then:
		resultWithParams == "squash-tm";
		resultWithoutParams == "squash-tm";
		resultWeird == "squash-tm";
	}
}
