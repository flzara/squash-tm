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
package org.squashtest.tm.service.internal.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;

import java.net.URL;
import java.util.List;

public interface TestAutomationServerDao extends JpaRepository<TestAutomationServer, Long>, CustomTestAutomationServerDao {

	/**
	 * Will find all occurrences of {@link TestAutomationServer} in the database ordered by their name.
	 *
	 * @return : all {@link TestAutomationServer} ordered by their name
	 */
	List<TestAutomationServer> findAllByOrderByNameAsc();

	/**
	 * Find the {@linkplain TestAutomationServer} by it's name.
	 *
	 * @param serverName : the name of the entity to find
	 * @return : the entity matching the given name (must be only one or database is corrupted) or <code>null</code>.
	 */
	TestAutomationServer findByName(String serverName);

	/**
	 *
	 * Find the {@linkplain TestAutomationServer} using its URL and the login used to log on it. There is indeed a
	 * unique constraint on it (one can log on a given server only once with a given account).
	 */
	// tech note : because of the name of the property baseURL, I'd rather not rely on
	// the spring jpa dsl (I don't know how it will handle the case)
	@Query
	TestAutomationServer findByUrlAndLogin(@Param("url") URL url, @Param("login") String login);


}
