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

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.LongType;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.infolist.ListItemReference;
import org.squashtest.tm.domain.testcase.ScriptedTestCaseLanguage;
import org.squashtest.tm.domain.testcase.TestCaseAutomatable;
import org.squashtest.tm.domain.testcase.TestCaseImportance;
import org.squashtest.tm.domain.testcase.TestCaseKind;
import org.squashtest.tm.domain.testcase.TestCaseStatus;
import org.squashtest.tm.jooq.domain.tables.InfoListItem;
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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static org.jooq.impl.DSL.countDistinct;
import static org.jooq.impl.DSL.groupConcat;
import static org.jooq.impl.DSL.groupConcatDistinct;
import static org.squashtest.tm.jooq.domain.Tables.ATTACHMENT;
import static org.squashtest.tm.jooq.domain.Tables.ATTACHMENT_LIST;
import static org.squashtest.tm.jooq.domain.Tables.CALL_TEST_STEP;
import static org.squashtest.tm.jooq.domain.Tables.INFO_LIST_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.ITEM_TEST_PLAN_LIST;
import static org.squashtest.tm.jooq.domain.Tables.ITERATION_TEST_PLAN_ITEM;
import static org.squashtest.tm.jooq.domain.Tables.KEYWORD_TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE;
import static org.squashtest.tm.jooq.domain.Tables.MILESTONE_TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.PROJECT;
import static org.squashtest.tm.jooq.domain.Tables.REQUIREMENT_VERSION_COVERAGE;
import static org.squashtest.tm.jooq.domain.Tables.SCRIPTED_TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TCLN_RELATIONSHIP;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_FOLDER;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_LIBRARY_CONTENT;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_LIBRARY_NODE;
import static org.squashtest.tm.jooq.domain.Tables.TEST_CASE_STEPS;
import static org.squashtest.tm.service.internal.repository.hibernate.NativeQueries.FIND_ID_REQUIREMENT_NUM_VERSION_NOT_OBSOLETE;

@Repository
public class ExportDao {


	private static final String TEST_CASE_IDS = "testCaseIds";

	private static final String VERSION_IDS = "versionIds";

	private static final InfoListItem TEST_CASE_NATURE = INFO_LIST_ITEM.as("nature");
	private static final InfoListItem TEST_CASE_TYPE = INFO_LIST_ITEM.as("type");

	@PersistenceContext
	private EntityManager em;

	@Inject
	private DSLContext dsl;

	@Inject
	private PathService pathService;

	public ExportDao() {
		super();
	}

	public RequirementExportModel findAllRequirementModel(List<Long> versionIds,  Boolean isCurrentVersion) {

		RequirementExportModel model;
		model = populateRequirementExportModel(versionIds, isCurrentVersion);

		List<CoverageModel> coverageModels = findRequirementVersionCoverageModel(versionIds);

		List<RequirementLinkModel> linkModels = findRequirementLinksModel(versionIds);

		setPathForCoverage(coverageModels);

		model.setCoverages(coverageModels);

		model.setReqLinks(linkModels);

		return model;
	}

	public RequirementExportModel populateRequirementExportModel (List<Long> versionIds, Boolean isCurrentVersion) {

		RequirementExportModel model = new RequirementExportModel();

		Map<Long, Long> mapRequirementNumVersion;
		List<RequirementModel> tmpRequirementsModel;
		List<RequirementModel> requirementsModel = new ArrayList<>() ;
		if(isCurrentVersion){
			mapRequirementNumVersion =  findIdRequirementAndNumCurrentVersionNotObsolete(versionIds);

			tmpRequirementsModel = findRequirementModelForCurrentVersion(versionIds );
			for (RequirementModel reqModel :  tmpRequirementsModel) {
				for (Long idRequirement : mapRequirementNumVersion.keySet()) {
					if (idRequirement.equals(reqModel.getRequirementId()) && mapRequirementNumVersion.get(idRequirement) == (reqModel.getRequirementVersionNumber())) {
						requirementsModel.add(reqModel);
					}
				}
			}
		}else  requirementsModel = findRequirementModel(versionIds);

		model.setRequirementsModels(requirementsModel);

		return model;
	}




	public ExportModel findModel(List<Long> tclnIds) {

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


	public ExportModel findSimpleModel(List<Long> tclnIds) {

		ExportModel model = new ExportModel();

		List<TestCaseModel> tclnModels = findTestCaseModels(tclnIds);

		model.setTestCases(tclnModels);

		return model;

	}

	private void setPathForCoverage(List<CoverageModel> coverageModels) {
		for (CoverageModel model : coverageModels) {
			model.setReqPath(getRequirementPath(model.getRequirementId(), model.getRequirementProjectName()));
			model.setTcPath(pathService.buildTestCasePath(model.getTcId()));
		}

	}


	private List<CoverageModel> findTestCaseCoverageModel(List<Long> tcIds) {
		return loadModels("testCase.excelExportCoverage", tcIds, TEST_CASE_IDS, CoverageModel.class);
	}

	private List<TestCaseModel> loadTestCaseModelsFromFolderWithJOOQ(List<Long> tclnIds) {

		List<TestCaseModel> result = new ArrayList<>();

		dsl.select(TEST_CASE.TCLN_ID, TEST_CASE.UUID, TEST_CASE.REFERENCE,
			TEST_CASE.IMPORTANCE, TEST_CASE.IMPORTANCE_AUTO, TEST_CASE.AUTOMATABLE,
			TEST_CASE.TC_STATUS, TEST_CASE.PREREQUISITE,
			TEST_CASE_LIBRARY_NODE.DESCRIPTION, TEST_CASE_LIBRARY_NODE.NAME,
			TEST_CASE_LIBRARY_NODE.CREATED_BY, TEST_CASE_LIBRARY_NODE.CREATED_ON,
			TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_BY, TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_ON,
			PROJECT.PROJECT_ID, PROJECT.NAME,
			TEST_CASE_NATURE.LABEL, TEST_CASE_NATURE.CODE,
			TEST_CASE_TYPE.LABEL, TEST_CASE_TYPE.CODE,
			SCRIPTED_TEST_CASE.LANGUAGE, SCRIPTED_TEST_CASE.SCRIPT,
			KEYWORD_TEST_CASE.TCLN_ID,
			groupConcatDistinct(MILESTONE.LABEL).separator("|").as("milestones"),
			countDistinct(ATTACHMENT).as("countAttachments"),
			countDistinct(REQUIREMENT_VERSION_COVERAGE).as("countReqCoverages"),
			countDistinct(ITEM_TEST_PLAN_LIST.ITERATION_ID).as("countIteration"),
			countDistinct(TEST_CASE_STEPS.TEST_CASE_ID).as("countCaller"),
			TCLN_RELATIONSHIP.CONTENT_ORDER)
		.from(TEST_CASE)
			.join(TEST_CASE_LIBRARY_NODE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.join(PROJECT).on(PROJECT.PROJECT_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
			.leftJoin(MILESTONE_TEST_CASE).on(MILESTONE_TEST_CASE.TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(MILESTONE).on(MILESTONE.MILESTONE_ID.eq(MILESTONE_TEST_CASE.MILESTONE_ID))
			.join(TEST_CASE_NATURE).on(TEST_CASE_NATURE.ITEM_ID.eq(TEST_CASE.TC_NATURE))
			.join(TEST_CASE_TYPE).on(TEST_CASE_TYPE.ITEM_ID.eq(TEST_CASE.TC_TYPE))
			.join(TCLN_RELATIONSHIP).on(TCLN_RELATIONSHIP.DESCENDANT_ID.eq(TEST_CASE.TCLN_ID))
			.join(TEST_CASE_FOLDER).on(TEST_CASE_FOLDER.TCLN_ID.eq(TCLN_RELATIONSHIP.ANCESTOR_ID))
			.join(ATTACHMENT_LIST).on(ATTACHMENT_LIST.ATTACHMENT_LIST_ID.eq(TEST_CASE_LIBRARY_NODE.ATTACHMENT_LIST_ID))
			.leftJoin(ATTACHMENT).on(ATTACHMENT.ATTACHMENT_LIST_ID.eq(ATTACHMENT_LIST.ATTACHMENT_LIST_ID))
			.leftJoin(REQUIREMENT_VERSION_COVERAGE).on(REQUIREMENT_VERSION_COVERAGE.VERIFYING_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
			.leftJoin(CALL_TEST_STEP).on(CALL_TEST_STEP.CALLED_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(TEST_CASE_STEPS).on(TEST_CASE_STEPS.STEP_ID.eq(CALL_TEST_STEP.TEST_STEP_ID))
			.leftJoin(SCRIPTED_TEST_CASE).on(SCRIPTED_TEST_CASE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(KEYWORD_TEST_CASE).on(KEYWORD_TEST_CASE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
		.where(TEST_CASE.TCLN_ID.in(tclnIds))
		.groupBy(TEST_CASE.TCLN_ID, TEST_CASE_LIBRARY_NODE.TCLN_ID, PROJECT.PROJECT_ID,
			TEST_CASE_NATURE.ITEM_ID, TEST_CASE_TYPE.ITEM_ID, TCLN_RELATIONSHIP.CONTENT_ORDER,
			TEST_CASE_FOLDER.TCLN_ID, ATTACHMENT_LIST.ATTACHMENT_LIST_ID,
			SCRIPTED_TEST_CASE.LANGUAGE, SCRIPTED_TEST_CASE.SCRIPT,
			KEYWORD_TEST_CASE.TCLN_ID)
		.fetch()
		.forEach(record -> {
			TestCaseModel model = createTestCaseModelFromQueryResult(record, record.get(TCLN_RELATIONSHIP.CONTENT_ORDER));
			result.add(model);
		});

		return result;
	}

	private List<TestCaseModel> loadTestCaseModelsFromLibraryWithJOOQ(List<Long> tclnIds) {

		List<TestCaseModel> result = new ArrayList<>();

		dsl.select(TEST_CASE.TCLN_ID, TEST_CASE.UUID, TEST_CASE.REFERENCE,
			TEST_CASE.IMPORTANCE, TEST_CASE.IMPORTANCE_AUTO, TEST_CASE.AUTOMATABLE,
			TEST_CASE.TC_STATUS, TEST_CASE.PREREQUISITE,
			TEST_CASE_LIBRARY_NODE.DESCRIPTION, TEST_CASE_LIBRARY_NODE.NAME,
			TEST_CASE_LIBRARY_NODE.CREATED_BY, TEST_CASE_LIBRARY_NODE.CREATED_ON,
			TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_BY, TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_ON,
			PROJECT.PROJECT_ID, PROJECT.NAME,
			TEST_CASE_NATURE.LABEL, TEST_CASE_NATURE.CODE,
			TEST_CASE_TYPE.LABEL, TEST_CASE_TYPE.CODE,
			SCRIPTED_TEST_CASE.LANGUAGE, SCRIPTED_TEST_CASE.SCRIPT,
			KEYWORD_TEST_CASE.TCLN_ID,
			groupConcatDistinct(MILESTONE.LABEL).separator("|").as("milestones"),
			countDistinct(ATTACHMENT).as("countAttachments"),
			countDistinct(REQUIREMENT_VERSION_COVERAGE).as("countReqCoverages"),
			countDistinct(ITEM_TEST_PLAN_LIST.ITERATION_ID).as("countIteration"),
			countDistinct(TEST_CASE_STEPS.TEST_CASE_ID).as("countCaller"),
			TEST_CASE_LIBRARY_CONTENT.CONTENT_ORDER)
			.from(TEST_CASE)
			.join(TEST_CASE_LIBRARY_NODE).on(TEST_CASE_LIBRARY_NODE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.join(PROJECT).on(PROJECT.PROJECT_ID.eq(TEST_CASE_LIBRARY_NODE.PROJECT_ID))
			.leftJoin(MILESTONE_TEST_CASE).on(MILESTONE_TEST_CASE.TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(MILESTONE).on(MILESTONE.MILESTONE_ID.eq(MILESTONE_TEST_CASE.MILESTONE_ID))
			.join(TEST_CASE_NATURE).on(TEST_CASE_NATURE.ITEM_ID.eq(TEST_CASE.TC_NATURE))
			.join(TEST_CASE_TYPE).on(TEST_CASE_TYPE.ITEM_ID.eq(TEST_CASE.TC_TYPE))
			.join(TEST_CASE_LIBRARY_CONTENT).on(TEST_CASE_LIBRARY_CONTENT.CONTENT_ID.eq(TEST_CASE.TCLN_ID))
			.join(ATTACHMENT_LIST).on(ATTACHMENT_LIST.ATTACHMENT_LIST_ID.eq(TEST_CASE_LIBRARY_NODE.ATTACHMENT_LIST_ID))
			.leftJoin(ATTACHMENT).on(ATTACHMENT.ATTACHMENT_LIST_ID.eq(ATTACHMENT_LIST.ATTACHMENT_LIST_ID))
			.leftJoin(REQUIREMENT_VERSION_COVERAGE).on(REQUIREMENT_VERSION_COVERAGE.VERIFYING_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(ITERATION_TEST_PLAN_ITEM).on(ITERATION_TEST_PLAN_ITEM.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(ITEM_TEST_PLAN_LIST).on(ITEM_TEST_PLAN_LIST.ITEM_TEST_PLAN_ID.eq(ITERATION_TEST_PLAN_ITEM.ITEM_TEST_PLAN_ID))
			.leftJoin(CALL_TEST_STEP).on(CALL_TEST_STEP.CALLED_TEST_CASE_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(TEST_CASE_STEPS).on(TEST_CASE_STEPS.STEP_ID.eq(CALL_TEST_STEP.TEST_STEP_ID))
			.leftJoin(SCRIPTED_TEST_CASE).on(SCRIPTED_TEST_CASE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.leftJoin(KEYWORD_TEST_CASE).on(KEYWORD_TEST_CASE.TCLN_ID.eq(TEST_CASE.TCLN_ID))
			.where(TEST_CASE.TCLN_ID.in(tclnIds))
			.groupBy(TEST_CASE.TCLN_ID, TEST_CASE_LIBRARY_NODE.TCLN_ID, PROJECT.PROJECT_ID,
				TEST_CASE_NATURE.ITEM_ID, TEST_CASE_TYPE.ITEM_ID, TEST_CASE_LIBRARY_CONTENT.CONTENT_ORDER,
				ATTACHMENT_LIST.ATTACHMENT_LIST_ID,
				SCRIPTED_TEST_CASE.LANGUAGE, SCRIPTED_TEST_CASE.SCRIPT,
				KEYWORD_TEST_CASE.TCLN_ID)
			.fetch()
			.forEach(record -> {
				TestCaseModel model = createTestCaseModelFromQueryResult(record, record.get(TEST_CASE_LIBRARY_CONTENT.CONTENT_ORDER));
				result.add(model);
			});

		return result;
	}

	private TestCaseModel createTestCaseModelFromQueryResult(Record record, Integer testCaseOrderInContainer) {
		TestCaseModel model = new TestCaseModel(
			record.get(PROJECT.PROJECT_ID),
			record.get(PROJECT.NAME), testCaseOrderInContainer + 1, record.get(TEST_CASE.TCLN_ID),
			record.get(TEST_CASE.UUID), record.get(TEST_CASE.REFERENCE), record.get(TEST_CASE_LIBRARY_NODE.NAME),
			(String) record.get("milestones"), record.get(TEST_CASE.IMPORTANCE_AUTO), TestCaseImportance.valueOf(record.get(TEST_CASE.IMPORTANCE)),
			new ListItemReference(record.get(TEST_CASE_NATURE.CODE), record.get(TEST_CASE_NATURE.LABEL)),
			new ListItemReference(record.get(TEST_CASE_TYPE.CODE), record.get(TEST_CASE_TYPE.LABEL)),
			TestCaseStatus.valueOf(record.get(TEST_CASE.TC_STATUS)),
			TestCaseAutomatable.valueOf(record.get(TEST_CASE.AUTOMATABLE)), record.get(TEST_CASE_LIBRARY_NODE.DESCRIPTION), record.get(TEST_CASE.PREREQUISITE),
			((Integer) record.get("countReqCoverages")).longValue(), ((Integer) record.get("countCaller")).longValue(), Long.valueOf(record.get("countAttachments").toString()),
			((Integer) record.get("countIteration")).longValue(), record.get(TEST_CASE_LIBRARY_NODE.CREATED_ON), record.get(TEST_CASE_LIBRARY_NODE.CREATED_BY),
			record.get(TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_ON), record.get(TEST_CASE_LIBRARY_NODE.LAST_MODIFIED_BY), null,
			null, null);

		String scriptedLanguage = record.get(SCRIPTED_TEST_CASE.LANGUAGE);
		String script = record.get(SCRIPTED_TEST_CASE.SCRIPT);

		Long keywordTCLNId = record.get(KEYWORD_TEST_CASE.TCLN_ID);
		if (scriptedLanguage != null && !scriptedLanguage.isEmpty()) {
			model.setScriptedTestCaseLanguage(ScriptedTestCaseLanguage.GHERKIN);
			model.setTestCaseKind(TestCaseKind.GHERKIN);
			if (script != null && !script.isEmpty()) {
				model.setTcScript(script);
			}
		} else if (keywordTCLNId != null) {
			model.setTestCaseKind(TestCaseKind.KEYWORD);
		} else {
			model.setTestCaseKind(TestCaseKind.STANDARD);
		}
		return model;
	}

	private List<TestCaseModel> findTestCaseModels(List<Long> tclnIds) {

		List<TestCaseModel> models = new ArrayList<>(tclnIds.size());
		List<TestCaseModel> buffer;

		// get the models
		//buffer = loadModels("testCase.excelExportDataFromFolder", tclnIds, TEST_CASE_IDS, TestCaseModel.class);
		buffer = loadTestCaseModelsFromFolderWithJOOQ(tclnIds);
		models.addAll(buffer);

//		buffer = loadModels("testCase.excelExportDataFromLibrary", tclnIds, TEST_CASE_IDS, TestCaseModel.class);
		buffer = loadTestCaseModelsFromLibraryWithJOOQ(tclnIds);
		models.addAll(buffer);

		//get the cufs
		List<CustomField> cufModels = loadModels("testCase.excelExportCUF", tclnIds, TEST_CASE_IDS, CustomField.class);

		// add them to the test case models
		for (TestCaseModel model : models) {
			Long id = model.getId();
			ListIterator<CustomField> cufIter = cufModels.listIterator();

			while (cufIter.hasNext()) {
				CustomField cuf = cufIter.next();
				if (id.equals(cuf.getOwnerId())) {
					model.addCuf(cuf);
					cufIter.remove();
				}
			}
		}

		// end
		return models;

	}


	private List<TestStepModel> findStepsModel(List<Long> tcIds) {

		List<TestStepModel> models = new ArrayList<>(tcIds.size());
		List<TestStepModel> buffer;


		buffer = loadModels("testStep.excelExportActionSteps", tcIds, TEST_CASE_IDS, TestStepModel.class);
		models.addAll(buffer);

		buffer = loadModels("testStep.excelExportCallSteps", tcIds, TEST_CASE_IDS, TestStepModel.class);
		models.addAll(buffer);

		//get the cufs
		List<CustomField> cufModels = loadModels("testStep.excelExportCUF", tcIds, TEST_CASE_IDS, CustomField.class);

		// add them to the test case models
		for (TestStepModel model : models) {
			Long id = model.getId();
			ListIterator<CustomField> cufIter = cufModels.listIterator();

			while (cufIter.hasNext()) {
				CustomField cuf = cufIter.next();
				if (id.equals(cuf.getOwnerId())) {
					model.addCuf(cuf);
					cufIter.remove();
				}
			}
		}


		// done
		return models;
	}

	private List<ParameterModel> findParametersModel(List<Long> tcIds) {
		return loadModels("parameter.excelExport", tcIds, TEST_CASE_IDS, ParameterModel.class);
	}


	private List<DatasetModel> findDatasetsModel(List<Long> tcIds) {

		return loadModels("dataset.excelExport", tcIds, TEST_CASE_IDS, DatasetModel.class);
	}


	private Session getStatelessSession() {
		Session s = em.unwrap(Session.class);
		s.setFlushMode(FlushMode.MANUAL);
		return s;
	}

	private List<CoverageModel> findRequirementVersionCoverageModel(List<Long> versionIds) {
		return loadModels("requirementVersion.excelExportCoverage", versionIds, VERSION_IDS, CoverageModel.class);
	}

	private List<RequirementLinkModel> findRequirementLinksModel(List<Long> versionIds) {
		// get the models
		List<RequirementLinkModel> models = loadModels("requirementVersion.excelExportRequirementLinks", versionIds, VERSION_IDS, RequirementLinkModel.class);

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
		List<RequirementModel> requirementModels = loadModels("requirement.findVersionsModels", versionIds, VERSION_IDS,
			RequirementModel.class);
		getOtherProperties(requirementModels);
		return requirementModels;
	}
	private List<RequirementModel> findRequirementModelForCurrentVersion(List<Long> versionIds) {
		List<RequirementModel> requirementModels = loadModels("requirement.findCurrentVersionsModels", versionIds, VERSION_IDS,
			RequirementModel.class);
		getOtherProperties(requirementModels);
		return requirementModels;
	}
	/*private List<Couple> findIdRequirementAndNumCurrentVersionNotObsolete(List<Long> versionIds) {
		List<Couple> listIdRequirementNumVersion = loadModels("requirement.findIdRequirementAndNumCurrentVersionNotObsolete", versionIds, VERSION_IDS,
			Couple.class);
		return listIdRequirementNumVersion;
	}*/

	private Map<Long, Long> findIdRequirementAndNumCurrentVersionNotObsolete(List<Long> versionIds) {
		Map<Long, Long> mapIdRequirementNumVersion = null;
		if(versionIds.size()==0){
			return mapIdRequirementNumVersion;
		}
		javax.persistence.Query query = em.createNativeQuery(FIND_ID_REQUIREMENT_NUM_VERSION_NOT_OBSOLETE);
		query.setParameter("versionIds", versionIds);

		List<Object[]> pairIdOffset = query.getResultList();
		mapIdRequirementNumVersion = buildMapOfOffsetAndIds(pairIdOffset);
		return mapIdRequirementNumVersion;
	}





	private void getOtherProperties(List<RequirementModel> requirementModels) {
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


	private int getRequirementPositionInLibrary(Long reqId) {
		return requirementVersionQuery("requirement.findVersionsModelsIndexInLibrary", reqId, 0);
	}


	public String getPathAsString(RequirementModel exportedRequirement) {
		return getRequirementPath(exportedRequirement.getRequirementId(), exportedRequirement.getProjectName());
	}


	private String getRequirementPath(Long requirementId, String requirementProjectName) {
		StringBuilder sb = new StringBuilder(HibernatePathService.PATH_SEPARATOR);
		sb.append(requirementProjectName);
		sb.append(HibernatePathService.PATH_SEPARATOR);
		String pathFromFolder = getPathFromFolder(requirementId);
		String pathFromParents = getPathFromParentsRequirements(requirementId);
		sb.append(pathFromFolder);
		sb.append(pathFromParents);
		return HibernatePathService.escapePath(sb.toString());


	}

	private List<Long> gatherRequirementIdsFromLinkModels(List<RequirementLinkModel> models) {
		Set<Long> ids = new HashSet<>(models.size());
		for (RequirementLinkModel model : models) {
			ids.add(model.getReqId());
			ids.add(model.getRelReqId());
		}
		return new ArrayList<>(ids);
	}

	private Map<Long, String> gatherRequirementPaths(List<Long> requirementIds) {

		int nbReqs = requirementIds.size();

		List<String> pathes = pathService.buildRequirementsPaths(requirementIds);
		Map<Long, String> pathById = new HashMap<>(nbReqs);
		for (int i = 0; i < nbReqs; i++) {
			pathById.put(requirementIds.get(i), pathes.get(i));
		}

		return pathById;
	}

	private void assignPaths(List<RequirementLinkModel> models, Map<Long, String> pathById) {
		for (RequirementLinkModel model : models) {
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
		ids = !ids.isEmpty() ? ids : Collections.singletonList(-1L);
		Session session = getStatelessSession();
		Query q = session.getNamedQuery(queryName);
		q.setParameterList(paramName, ids, LongType.INSTANCE);
		q.setResultTransformer(new EasyConstructorResultTransformer(resclass));
		return q.list();
	}

	private Map<Long, Long> buildMapOfOffsetAndIds(List<Object[]> list) {
		Map<Long, Long> result = new HashMap<>();

		for (Object[] pair : list) {
			result.put(((BigInteger) pair[0]).longValue(),((Integer) pair[1]).longValue());
		}
		return result;
	}


}
