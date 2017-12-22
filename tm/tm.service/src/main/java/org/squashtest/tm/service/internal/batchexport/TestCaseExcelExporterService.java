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
package org.squashtest.tm.service.internal.batchexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CoverageModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.DatasetModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.ParameterModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestStepModel;
import org.squashtest.tm.service.internal.library.PathService;
import org.squashtest.tm.service.internal.repository.TestCaseLibraryNodeDao;

@Service
public class TestCaseExcelExporterService {
	private static final int BATCH_SIZE = 50;

	@Inject
	private ExportDao exportDao;

	@Inject private PathService pathService;

	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private TestCaseLibraryNodeDao nodeDao;

	@Inject
	@Named("excelExporter")
	private Provider<ExcelExporter> exporterProvider;

	@Inject
	private Provider<SearchTestCaseExcelExporter> searchExporterProvider;

	public File exportAsExcel(List<Long> testCaseIds, boolean keepRteFormat, MessageSource messageSource){
		return doExportAsExcel(testCaseIds, keepRteFormat, messageSource, exporterProvider.get());
	}

	public File searchExportAsExcel(List<Long> testCaseIds, boolean keepRteFormat, MessageSource messageSource){
		return doExportAsExcel(testCaseIds, keepRteFormat, messageSource, searchExporterProvider.get());
	}

	private File doExportAsExcel(List<Long> testCaseIds, boolean keepRteFormat, MessageSource messageSource, ExcelExporter exporter){

		// let's chunk the job by batches of 50 test cases
		List<Long> ids;
		int idx=0;
		int max = Math.min(idx+BATCH_SIZE, testCaseIds.size());
		exporter.setMessageSource(messageSource);

		Map<Long, String> pathById = new HashMap<>(testCaseIds.size());
		populatePathsCache(pathById, testCaseIds);

		while (idx < testCaseIds.size()){

			ids = testCaseIds.subList(idx, max);

			ExportModel model = exportDao.findModel(ids);
			addPaths(pathById, model);
			sort(model);

			exporter.appendToWorkbook(model, keepRteFormat);

			idx = max;
			max = Math.min(idx+BATCH_SIZE, testCaseIds.size());
		}

		return exporter.print();

	}


	private void populatePathsCache(Map<Long, String> pathById, Set<Long> ids){

		populatePathsCache(pathById, new ArrayList<>(ids));

	}

	private void populatePathsCache(Map<Long, String> pathById, List<Long> ids){


		List<String> paths = !ids.isEmpty() ? pathService.buildTestCasesPaths(ids) : Collections.<String>emptyList();

		for (int i=0; i< ids.size(); i++){
			pathById.put(ids.get(i), paths.get(i));
		}

	}

	private void addPaths(Map<Long, String> pathById, ExportModel models){

		addPathsForTestCase(pathById, models);
		addPathsForTestSteps(pathById, models);
		addPathsForParameters(pathById, models);
		addPathsForDatasets(pathById, models);

	}

	private void addPathsForTestCase(Map<Long, String> pathById, ExportModel models){

		for (TestCaseModel model : models.getTestCases()){
			Long id = model.getId();
			String path = pathById.get(id);
			model.setPath(path);
		}

	}

	private void addPathsForTestSteps(Map<Long, String> pathById, ExportModel models){


		List<TestStepModel> callsteps = new LinkedList<>();
		Set<Long> calledTC = new HashSet<>();

		for (TestStepModel model : models.getTestSteps()){

			// add the path to the owner id
			Long id = model.getTcOwnerId();
			String path = pathById.get(id);
			model.setTcOwnerPath(path);

			// if it is a call step, treat the path of the called test case or save the reference for a second round
			if (model.getIsCallStep()>0){
				Long callid = Long.valueOf(model.getAction());
				if (pathById.containsKey(callid)){
					String callaction = "CALL "+pathById.get(callid);
					model.setAction(callaction);
				}
				else{
					callsteps.add(model);
					calledTC.add(callid);
				}
			}
		}

		// if some call steps were left unresolved, let's do them.
		if (! calledTC.isEmpty()){
			populatePathsCache(pathById, calledTC);
			for (TestStepModel model : callsteps){
				Long callid = Long.valueOf(model.getAction());
				String callaction = "CALL "+pathById.get(callid);
				model.setAction(callaction);
			}
		}
	}


	private void addPathsForParameters(Map<Long, String> pathById, ExportModel models){
		for (ParameterModel model : models.getParameters()){
			Long id = model.getTcOwnerId();
			String path = pathById.get(id);
			model.setTcOwnerPath(path);
		}
	}


	private void addPathsForDatasets(Map<Long, String> pathById, ExportModel models){

		List<DatasetModel>  unresolvedPOwnerPath = new LinkedList<>();
		List<Long> pOwnerIds = new LinkedList<>();

		for (DatasetModel model : models.getDatasets()){

			Long id = model.getOwnerId();
			String path = pathById.get(id);
			model.setTcOwnerPath(path);

			// also needs the path for the param owner. Like for the test steps
			// the param owner path may be unknown yet so we need
			// to see if further resolution is required.

			Long pOwnerId = model.getParamOwnerId();
			if (pathById.containsKey(pOwnerId)){
				String pOwnerPath = pathById.get(pOwnerId);
				model.setParamOwnerPath(pOwnerPath);
			}else{
				unresolvedPOwnerPath.add(model);
				pOwnerIds.add(pOwnerId);
			}
		}

		// now resolve the param owner paths left over
		if (! pOwnerIds.isEmpty()){
			populatePathsCache(pathById, pOwnerIds);
			for (DatasetModel model : unresolvedPOwnerPath){
				Long ownId = model.getParamOwnerId();
				String path = pathById.get(ownId);
				model.setParamOwnerPath(path);
			}
		}

	}



	private void sort(ExportModel models){
		Collections.sort(models.getTestCases(), TestCaseModel.COMPARATOR);
		Collections.sort(models.getTestSteps(), TestStepModel.COMPARATOR);
		Collections.sort(models.getParameters(), ParameterModel.COMPARATOR);
		Collections.sort(models.getDatasets(), DatasetModel.COMPARATOR);
		Collections.sort(models.getCoverages(), CoverageModel.TC_COMPARATOR);
	}



}
