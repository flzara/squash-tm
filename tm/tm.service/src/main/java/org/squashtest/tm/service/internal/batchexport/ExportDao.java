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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CoverageModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.CustomField;
import org.squashtest.tm.service.internal.batchexport.ExportModel.DatasetModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.ParameterModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestCaseModel;
import org.squashtest.tm.service.internal.batchexport.ExportModel.TestStepModel;
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel.RequirementLinkModel;
import org.squashtest.tm.service.internal.batchexport.RequirementExportModel.RequirementModel;
import org.squashtest.tm.service.internal.library.HibernatePathService;
import org.squashtest.tm.service.internal.library.PathService;
import org.squashtest.tm.service.internal.repository.hibernate.EasyConstructorResultTransformer;

@Repository
public class ExportDao {


	@PersistenceContext
	private EntityManager em;

	@Inject
	private PathService pathService;

	public ExportDao(){
		super();
	}


	public RequirementExportModel findAllRequirementModel(List<Long> versionIds){
	
		RequirementExportModel model = new RequirementExportModel();		
		
		List<RequirementModel> requirementsModel = findRequirementModel(versionIds);
		List<CoverageModel> coverageModels = findRequirementVersionCoverageModel(versionIds);		
		List<RequirementLinkModel> linkModels = findRequirementLinksModel(versionIds);

		setPathForCoverage(coverageModels);

		model.setCoverages(coverageModels);
		model.setRequirementsModels(requirementsModel);
		model.setReqLinks(linkModels);
		
		return model;
	}


	public ExportModel findModel(List<Long> tclnIds){

		ExportModel model = new ExportModel();

		List<TestCaseModel> tclnModels = findTestCaseModels(tclnIds);
		List<TestStepModel> stepModels = findStepsModel(tclnIds);
		List<ParameterModel> paramModels = findParametersModel(tclnIds);
		List<DatasetModel> datasetModels = findDatasetsModel(tclnIds);
		List<CoverageModel> coverageModels = findTestCaseCoverageModel(tclnIds);

		setPathForCoverage(coverageModels);

		model.setCoverages(coverageModels);
		model.setTestCases(tclnModels);
		model.setTestSteps(stepModels);
		model.setParameters(paramModels);
		model.setDatasets(datasetModels);

		return model;

	}

	private void setPathForCoverage(List<CoverageModel> coverageModels) {
		for (CoverageModel model : coverageModels){
			model.setReqPath(getRequirementPath(model.getRequirementId(), model.getRequirementProjectName()));
			model.setTcPath(pathService.buildTestCasePath(model.getTcId()));
		}

	}


	private List<CoverageModel> findTestCaseCoverageModel(List<Long> tcIds) {
		return loadModels("testCase.excelExportCoverage", tcIds, "testCaseIds", CoverageModel.class);
	}


	private List<TestCaseModel> findTestCaseModels(List<Long> tclnIds){

		Session session = getStatelessSession();
		List<TestCaseModel> models = new ArrayList<>(tclnIds.size());
		List<TestCaseModel> buffer;


		// get the models
		buffer = loadModels("testCase.excelExportDataFromFolder", tclnIds, "testCaseIds", TestCaseModel.class);
		models.addAll(buffer);

		buffer = loadModels("testCase.excelExportDataFromLibrary", tclnIds,"testCaseIds", TestCaseModel.class);
		models.addAll(buffer);

		//get the cufs
		List<CustomField> cufModels = loadModels("testCase.excelExportCUF", tclnIds, "testCaseIds",CustomField.class);

		// add them to the test case models
		for (TestCaseModel model : models){
			Long id = model.getId();
			ListIterator<CustomField> cufIter = cufModels.listIterator();

			while (cufIter.hasNext()){
				CustomField cuf = cufIter.next();
				if (id.equals(cuf.getOwnerId())){
					model.addCuf(cuf);
					cufIter.remove();
				}
			}
		}

		// end
		return models;

	}


	private List<TestStepModel> findStepsModel(List<Long> tcIds){

		Session session = getStatelessSession();
		List<TestStepModel> models = new ArrayList<>(tcIds.size());
		List<TestStepModel> buffer;


		buffer = loadModels("testStep.excelExportActionSteps", tcIds, "testCaseIds", TestStepModel.class);
		models.addAll(buffer);

		buffer = loadModels("testStep.excelExportCallSteps", tcIds, "testCaseIds", TestStepModel.class);
		models.addAll(buffer);

		//get the cufs
		List<CustomField> cufModels = loadModels("testStep.excelExportCUF", tcIds, "testCaseIds", CustomField.class);

		// add them to the test case models
		for (TestStepModel model : models){
			Long id = model.getId();
			ListIterator<CustomField> cufIter = cufModels.listIterator();

			while (cufIter.hasNext()){
				CustomField cuf = cufIter.next();
				if (id.equals(cuf.getOwnerId())){
					model.addCuf(cuf);
					cufIter.remove();
				}
			}
		}


		// done
		return models;
	}

	private List<ParameterModel> findParametersModel(List<Long> tcIds){
		return loadModels("parameter.excelExport", tcIds, "testCaseIds", ParameterModel.class);
	}


	private List<DatasetModel> findDatasetsModel(List<Long> tcIds){

		return loadModels("dataset.excelExport", tcIds, "testCaseIds", DatasetModel.class);
	}



	private Session getStatelessSession(){
		Session s = em.unwrap(Session.class);
		s.setFlushMode(FlushMode.MANUAL);
		return s;
	}

	private List<CoverageModel> findRequirementVersionCoverageModel(List<Long> versionIds) {
		return loadModels("requirementVersion.excelExportCoverage", versionIds, "versionIds", CoverageModel.class);
	}
	
	private List<RequirementLinkModel> findRequirementLinksModel(List<Long> versionIds){
		// get the models
		List<RequirementLinkModel> models = loadModels("requirementVersion.excelExportRequirementLinks", versionIds, "versionIds", RequirementLinkModel.class);
		
		/*
		 * more complex part : computing the pathes. The steps are the following :
		 * - gather all the requirement ids involved
		 * - gather the paths for all these requirements and map them by id
		 * - for each model, assign the paths. 
		 */
		List<Long> reqIds = gatherRequirementIdsFromLinkModels(models);
		Map<Long, String> pathById = gatherRequirementPaths(reqIds);
		assignPaths(models, pathById);
		
		return models;
	}


	private List<RequirementModel> findRequirementModel(List<Long> versionIds) {
		List<RequirementModel> requirementModels = loadModels("requirement.findVersionsModels", versionIds,"versionIds",
				RequirementModel.class);
		getOtherProperties(requirementModels);
		return requirementModels;
	}

	private void getOtherProperties(List<RequirementModel> requirementModels){
		for (RequirementModel requirementModel : requirementModels) {
			requirementModel.setPath(getPathAsString(requirementModel));
			getModelRequirementPosition(requirementModel);
			getModelRequirementCUF(requirementModel);
		}
	}


	@SuppressWarnings("unchecked")
	private void getModelRequirementCUF(RequirementModel requirementModel) {
		Session session = getStatelessSession();
		Query q = session.getNamedQuery("requirement.excelRequirementExportCUF");
		q.setLong("requirementVersionId", requirementModel.getId());
		q.setResultTransformer(new EasyConstructorResultTransformer(CustomField.class));
		requirementModel.setCufs(q.list());
	}

	private void getModelRequirementPosition(RequirementModel requirementModel) {
			Long reqId = requirementModel.getRequirementId();
			int index = getRequirementPositionInLibrary(reqId);
			if (index == 0) {
				index = getRequirementPositionInFolder(reqId);
			}
			if (index == 0) {
				index = getPositionChildrenRequirement(reqId);
			}
			requirementModel.setRequirementIndex(index);
	}


	private int getPositionChildrenRequirement(Long reqId) {
		return requirementVersionQuery("requirement.findVersionsModelsIndexChildrenRequirement", reqId, 0);
	}


	private int getRequirementPositionInFolder(Long reqId) {
		return requirementVersionQuery("requirement.findVersionsModelsIndexInFolder", reqId, 0);
	}


	private int getRequirementPositionInLibrary(Long reqId){
		return requirementVersionQuery("requirement.findVersionsModelsIndexInLibrary", reqId, 0);
	}


	public String getPathAsString(RequirementModel exportedRequirement) {
		return getRequirementPath(exportedRequirement.getRequirementId(), exportedRequirement.getProjectName());
	}


	private String getRequirementPath(Long requirementId, String requirementProjectName){
		StringBuilder sb = new StringBuilder(HibernatePathService.PATH_SEPARATOR);
		sb.append(requirementProjectName);
		sb.append(HibernatePathService.PATH_SEPARATOR);
		String pathFromFolder = getPathFromFolder(requirementId);
		String pathFromParents = getPathFromParentsRequirements(requirementId);
		sb.append(pathFromFolder);
		sb.append(pathFromParents);
		return HibernatePathService.escapePath(sb.toString());


	}
	
	private List<Long> gatherRequirementIdsFromLinkModels(List<RequirementLinkModel> models){
		Set<Long> ids = new HashSet<>(models.size());
		for (RequirementLinkModel model : models){
			ids.add(model.getReqId());
			ids.add(model.getRelReqId());
		}
		return new ArrayList<>(ids);
	}
	
	private Map<Long, String> gatherRequirementPaths(List<Long> requirementIds){
		
		int nbReqs = requirementIds.size();
		
		List<String> pathes = pathService.buildRequirementsPaths(requirementIds);
		Map<Long, String> pathById = new HashMap<>(nbReqs);
		for (int i=0; i < nbReqs; i++){
			pathById.put(requirementIds.get(i), pathes.get(i));
		}
		
		return pathById;
	}
	
	private void assignPaths(List<RequirementLinkModel> models, Map<Long, String> pathById){
		for (RequirementLinkModel model : models){
			String reqPath = pathById.get(model.getReqId());
			String relPath = pathById.get(model.getRelReqId());
			model.setReqPath(reqPath);
			model.setRelReqPath(relPath);
		}
	}

	private String getPathFromParentsRequirements(Long requirementId) {
		return requirementVersionQuery("requirement.findReqParentPath", requirementId, "");
	}


	private String getPathFromFolder(Long requirementId) {
		String result = requirementVersionQuery("requirement.findReqFolderPath", requirementId, "");
		return result != null && result.isEmpty() ? result : result + HibernatePathService.PATH_SEPARATOR;
	}

	@SuppressWarnings("unchecked")
	private <R> R requirementVersionQuery(String queryName, Long requirementId, R defaultValue) {
		Session session = getStatelessSession();
		Query q = session.getNamedQuery(queryName);
		q.setParameter("requirementId", requirementId);
		R result = (R) q.uniqueResult();
		return result != null ? result : defaultValue;
	}


	@SuppressWarnings("unchecked")
	private <R> List<R> loadModels(String queryName, List<Long> ids, String paramName,
			Class<R> resclass) {
		Session session = getStatelessSession();
		Query q = session.getNamedQuery(queryName);
		q.setParameterList(paramName, ids, LongType.INSTANCE);
		q.setResultTransformer(new EasyConstructorResultTransformer(resclass));
		return q.list();
	}

	
}
