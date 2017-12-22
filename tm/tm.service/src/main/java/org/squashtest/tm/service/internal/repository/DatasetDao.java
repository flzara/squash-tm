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
import org.squashtest.tm.domain.testcase.Dataset;

import java.util.Collection;
import java.util.List;


public interface DatasetDao extends JpaRepository<Dataset, Long>, CustomDatasetDao {
	Dataset findById(Long id);


	/**
	 * Will return the dataset matching the given name and belonging to the test case matchine the given id.
	 *
	 * @param testCaseId : the id of the concerned test case
	 * @param name       : the name of the dataset to find
	 * @return the test case's dataset matching the given id or <code>null</code>
	 */
	@Query
	// note : this name is a valid jpa dsl expression, but to be fully ok with the named query it should be
	// findByTestCaseIdAndNameOrderByNameAsc, which is less cool
	Dataset findByTestCaseIdAndName(@Param("testCaseId") Long testCaseId, @Param("name") String name);


	Collection<Dataset> findAllByTestCaseId(Long testCaseId);

	/**
	 * Will return all datasets for the given test case.
	 *
	 * @param testCaseId
	 * @return the list of all test cases's datasets.
	 */
	List<Dataset> findOwnDatasetsByTestCase(@Param("testCaseId") Long testCaseId);

}
