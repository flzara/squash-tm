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
package org.squashtest.tm.service.testcase;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.Dataset;

@Transactional
public interface DatasetModificationService {

	Collection<Dataset> findAllForTestCase(long testCaseId);

	Dataset findById(long datasetId);

	/**
	 * 
	 * @param dataset
	 * @param testCaseId
	 */
	void persist(Dataset dataset, long testCaseId);

	/**
	 * 
	 * @param dataset
	 */
	void remove(Dataset dataset);


	/**
	 * 
	 * @param datasetId
	 */
	void removeById(long datasetId);


	void removeAllByTestCaseIds(List<Long> testCaseIds);


	/**
	 * 
	 * @param datasetId
	 * @param name
	 */
	void changeName(long datasetId, String name);

	/**
	 * 
	 * @param datasetId
	 * @param paramId
	 * @param value
	 */
	void changeParamValue(long datasetParamValueId, String value);

	/**
	 * <p>This method updates the dataset of this test case, and every dataset upstream that inherits
	 * from this test case, by creating the missing parameter values.</p>
	 * 
	 * <p>
	 * 	However in its current state it won't delete the values from the datasets when some delegated parameters
	 * 	aren't accessible anymore (ie a call step was deleted, or is no more in parameter delegation mode).
	 * </p>
	 * 
	 * @param testCaseId
	 */
	void cascadeDatasetsUpdate(long testCaseId);
}
