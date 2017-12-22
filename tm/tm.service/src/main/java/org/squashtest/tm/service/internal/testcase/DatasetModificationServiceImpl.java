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
package org.squashtest.tm.service.internal.testcase;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.DatasetParamValue;
import org.squashtest.tm.domain.testcase.Parameter;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.DuplicateNameException;
import org.squashtest.tm.service.internal.repository.DatasetDao;
import org.squashtest.tm.service.internal.repository.DatasetParamValueDao;
import org.squashtest.tm.service.internal.repository.ParameterDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.testcase.DatasetModificationService;

@Service("squashtest.tm.service.DatasetModificationService")
public class DatasetModificationServiceImpl implements DatasetModificationService {

	@Inject
	private DatasetDao datasetDao;

	@Inject
	private ParameterDao parameterDao;

	@Inject
	private DatasetParamValueDao datasetParamValueDao;


	@Inject
	private TestCaseDao testCaseDao;


	@Override
	public Dataset findById(long datasetId) {
		return datasetDao.findById(datasetId);
	}

	@Override
	public void persist(Dataset dataset, long testCaseId) {
		Dataset sameName = datasetDao.findByTestCaseIdAndName(testCaseId, dataset.getName());

		if(sameName != null ){
			throw new DuplicateNameException(dataset.getName(), dataset.getName());
		}
		else {

			TestCase testCase = testCaseDao.findById(testCaseId);

			dataset.setTestCase(testCase);
			testCase.addDataset(dataset);

			Collection<Parameter> parameters = parameterDao.findAllParametersByTestCase(testCaseId);
			updateDatasetParameters(dataset, parameters);
		}
	}

	@Override
	public Collection<Dataset> findAllForTestCase(long testCaseId) {
		return datasetDao.findAllByTestCaseId(testCaseId);
	}


	@Override
	public void remove(Dataset dataset) {
		this.datasetDao.removeDatasetFromTestPlanItems(dataset.getId());
		this.datasetDao.delete(dataset);
	}


	@Override
	public void removeById(long datasetId) {
		Dataset dataset = this.datasetDao.findById(datasetId);
		remove(dataset);
	}

	@Override
	public void removeAllByTestCaseIds(List<Long> testCaseIds) {
		List<Dataset> datasets = this.datasetDao.findOwnDatasetsByTestCases(testCaseIds);
		for(Dataset dataset : datasets){
			remove(dataset);
		}
	}

	@Override
	public void changeName(long datasetId, String newName) {

		Dataset dataset = this.datasetDao.findById(datasetId);
		Dataset sameName = datasetDao.findByTestCaseIdAndName(dataset.getTestCase().getId(), dataset.getName());
		if(sameName != null && ! sameName.getId().equals(dataset.getId())){
			throw new DuplicateNameException(dataset.getName(), newName);
		} else {
			dataset.setName(newName);
		}
	}

	@Override
	public void changeParamValue(long datasetParamValueId, String value) {
		DatasetParamValue paramValue = this.datasetParamValueDao.findById(datasetParamValueId);
		paramValue.setParamValue(value);
	}

	public List<Dataset> getAllDatasetByTestCase(long testCaseId){
		return this.datasetDao.findOwnDatasetsByTestCase(testCaseId);
	}


	private DatasetParamValue findDatasetParamValue(Dataset dataset, Parameter parameter){

		DatasetParamValue result = null;
		Set<DatasetParamValue> datasetParamValues = dataset.getParameterValues();
		for(DatasetParamValue datasetParamValue : datasetParamValues){
			if(datasetParamValue.getParameter().equals(parameter)){
				result = datasetParamValue;
			}
		}
		return result;
	}

	private DatasetParamValue findOrAddParameter(Dataset dataset, Parameter parameter){
		DatasetParamValue datasetParamValue = findDatasetParamValue(dataset, parameter);
		if(datasetParamValue == null){
			datasetParamValue = new DatasetParamValue(parameter, dataset);
		}
		return datasetParamValue;
	}


	private void updateDatasetParameters(Dataset dataset, Collection<Parameter> parameters){

		for(Parameter parameter : parameters){
			findOrAddParameter(dataset, parameter);
		}

	}


	@Override
	public void cascadeDatasetsUpdate(long testCaseId) {

		Collection<Dataset> allDataset = datasetDao.findOwnDatasetsByTestCase(testCaseId);
		allDataset.addAll(datasetDao.findAllDelegateDatasets(testCaseId));

		Collection<Parameter> params = parameterDao.findAllParametersByTestCase(testCaseId);

		for(Dataset dataset : allDataset){
			this.updateDatasetParameters(dataset, params);
		}
	}
}
