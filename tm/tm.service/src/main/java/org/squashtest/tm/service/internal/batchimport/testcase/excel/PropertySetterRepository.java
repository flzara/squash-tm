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
package org.squashtest.tm.service.internal.batchimport.testcase.excel;

import org.squashtest.tm.service.internal.batchimport.excel.NullPropertySetter;
import org.squashtest.tm.service.internal.batchimport.excel.PropertySetter;
import org.squashtest.tm.service.internal.batchimport.excel.ReflectionFieldSetter;
import org.squashtest.tm.service.internal.batchimport.excel.ReflectionMutatorSetter;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementLinksSheetColumn;
import org.squashtest.tm.service.internal.batchimport.requirement.excel.RequirementSheetColumn;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository of {@link PropertySetter}s in the context of a specific {@link TemplateWorksheet}
 *
 * @author Gregory Fouquet
 *
 */
final class PropertySetterRepository<COL extends Enum<COL> & TemplateColumn> {
	private static final String PROPERTY_PATH = "path";
	private static final String PROPERTY_MODE = "mode";
	private static final String PROPERTY_NAME = "name";
	private static final Map<TemplateWorksheet, PropertySetterRepository<?>> FINDER_REPO_BY_WORKSHEET = new HashMap<>(
		TemplateWorksheet.values().length);

	static {
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.TEST_CASES_SHEET, createTestCasesWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.STEPS_SHEET, createStepsWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.PARAMETERS_SHEET, createParamsWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.DATASETS_SHEET, createDatasetsWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.DATASET_PARAM_VALUES_SHEET, createDatasetParamValuesWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.REQUIREMENT_SHEET, createRequirementWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.COVERAGE_SHEET, createCoverageWorksheetRepo());
		FINDER_REPO_BY_WORKSHEET.put(TemplateWorksheet.REQUIREMENT_LINKS_SHEET, createRequirementLinksWorksheetRepo());
	}

	/**
	 *
	 * @param worksheet
	 * @return the {@link PropertySetterRepository} suitable fot the given worksheet
	 */
	@SuppressWarnings("unchecked")
	public static <C extends Enum<C> & TemplateColumn> PropertySetterRepository<C> forWorksheet(
		@NotNull TemplateWorksheet worksheet) {
		return (PropertySetterRepository<C>) FINDER_REPO_BY_WORKSHEET.get(worksheet);
	}

	private static PropertySetterRepository<?> createCoverageWorksheetRepo() {
		PropertySetterRepository<CoverageSheetColumn> r = new PropertySetterRepository<>();
		r.propSetterByColumn.put(CoverageSheetColumn.REQ_PATH, ReflectionMutatorSetter.forProperty("reqPath", String.class));
		r.propSetterByColumn.put(CoverageSheetColumn.REQ_VERSION_NUM, ReflectionFieldSetter.forField("reqVersion"));
		r.propSetterByColumn.put(CoverageSheetColumn.TC_PATH, ReflectionMutatorSetter.forProperty("tcPath", String.class));
		return r;
	}
	
	private static PropertySetterRepository<?> createRequirementLinksWorksheetRepo(){
		PropertySetterRepository<RequirementLinksSheetColumn> r = new PropertySetterRepository<>();
		
		r.propSetterByColumn.put(RequirementLinksSheetColumn.ACTION, ReflectionMutatorSetter.forOptionalProperty(PROPERTY_MODE));
		r.propSetterByColumn.put(RequirementLinksSheetColumn.REQ_PATH, ReflectionMutatorSetter.forProperty(PROPERTY_PATH, String.class));
		r.propSetterByColumn.put(RequirementLinksSheetColumn.REQ_VERSION_NUM, ReflectionMutatorSetter.forProperty("version", Integer.class));
		r.propSetterByColumn.put(RequirementLinksSheetColumn.RELATED_REQ_PATH, ReflectionMutatorSetter.forProperty(PROPERTY_PATH, String.class));
		r.propSetterByColumn.put(RequirementLinksSheetColumn.RELATED_REQ_VERSION_NUM, ReflectionMutatorSetter.forProperty("version", Integer.class));
		r.propSetterByColumn.put(RequirementLinksSheetColumn.RELATED_REQ_ROLE, ReflectionMutatorSetter.forOptionalProperty("relationRole"));
		
		return r;
		
	}

	private static PropertySetterRepository<?> createRequirementWorksheetRepo() {
		PropertySetterRepository<RequirementSheetColumn> r = new PropertySetterRepository<>();


		r.propSetterByColumn.put(RequirementSheetColumn.REQ_PATH, ReflectionMutatorSetter.forProperty(PROPERTY_PATH, String.class));
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_NUM, ReflectionFieldSetter.forOptionalField("order"));

		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_REFERENCE, ReflectionFieldSetter.forOptionalField("reference"));
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_NAME, ReflectionFieldSetter.forOptionalField(PROPERTY_NAME));
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_MILESTONE, ReflectionFieldSetter.forOptionalField("milestones"));
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_NUM, ReflectionFieldSetter.forOptionalField("version"));
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_CATEGORY, ReflectionFieldSetter.forOptionalField("category"));
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_STATUS, ReflectionFieldSetter.forOptionalField("status"));
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_CRITICALITY, ReflectionFieldSetter.forOptionalField("criticality"));

		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_DESCRIPTION,
			ReflectionFieldSetter.forOptionalField("description"));

		// createdOn and createdBy field name is not known, we use mutators to set'entityManager
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_CREATED_ON,
			ReflectionMutatorSetter.forOptionalProperty("createdOn"));
		r.propSetterByColumn.put(RequirementSheetColumn.REQ_VERSION_CREATED_BY,
			ReflectionMutatorSetter.forOptionalProperty("createdBy"));

		// instruction
		r.propSetterByColumn.put(RequirementSheetColumn.ACTION, ReflectionMutatorSetter.forOptionalProperty(PROPERTY_MODE));


		return r;
	}

	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createDatasetsWorksheetRepo() {
		PropertySetterRepository<DatasetSheetColumn> r = new PropertySetterRepository<>();

		// target
		r.propSetterByColumn.put(DatasetSheetColumn.TC_OWNER_PATH,
			ReflectionMutatorSetter.forProperty(PROPERTY_PATH, String.class));
		r.propSetterByColumn.put(DatasetSheetColumn.TC_DATASET_NAME, ReflectionFieldSetter.forField(PROPERTY_NAME));

		// instruction
		r.propSetterByColumn.put(DatasetSheetColumn.ACTION, ReflectionFieldSetter.forOptionalField(PROPERTY_MODE));

		// datasetvalue
		// None of the following columns actually need processing (because they will be treated
		// in DatasetParamValuesWorksheetRepo).
		r.propSetterByColumn.put(DatasetSheetColumn.TC_PARAM_OWNER_PATH, NullPropertySetter.INSTANCE);
		r.propSetterByColumn.put(DatasetSheetColumn.TC_DATASET_PARAM_NAME, NullPropertySetter.INSTANCE);
		r.propSetterByColumn.put(DatasetSheetColumn.TC_DATASET_PARAM_VALUE, NullPropertySetter.INSTANCE);


		return r;
	}


	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createDatasetParamValuesWorksheetRepo() {
		PropertySetterRepository<DatasetParamValuesSheetColumn> r = new PropertySetterRepository<>();

		// target
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_OWNER_PATH,
			ReflectionMutatorSetter.forProperty(PROPERTY_PATH, String.class));
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_DATASET_NAME, ReflectionFieldSetter.forField(PROPERTY_NAME));

		// instruction
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.ACTION, ReflectionFieldSetter.forOptionalField(PROPERTY_MODE));

		// datasetvalue
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_PARAM_OWNER_PATH,
			ReflectionFieldSetter.forOptionalField("parameterOwnerPath"));
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_DATASET_PARAM_NAME,
			ReflectionFieldSetter.forField("parameterName"));
		r.propSetterByColumn.put(DatasetParamValuesSheetColumn.TC_DATASET_PARAM_VALUE,
			ReflectionFieldSetter.forOptionalField("value"));

		return r;
	}


	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createParamsWorksheetRepo() {
		PropertySetterRepository<ParameterSheetColumn> r = new PropertySetterRepository<>();

		// target
		r.propSetterByColumn.put(ParameterSheetColumn.TC_OWNER_PATH,
			ReflectionMutatorSetter.forProperty(PROPERTY_PATH, String.class));

		// instruction
		r.propSetterByColumn.put(ParameterSheetColumn.ACTION, ReflectionFieldSetter.forOptionalField(PROPERTY_MODE));

		// paraameter
		// param.setName(..) has logic we'd rather short-circuit
		r.propSetterByColumn.put(ParameterSheetColumn.TC_PARAM_NAME, ReflectionFieldSetter.forField(PROPERTY_NAME));
		r.propSetterByColumn.put(ParameterSheetColumn.TC_PARAM_DESCRIPTION,
			ReflectionFieldSetter.forOptionalField("description"));

		return r;
	}

	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createStepsWorksheetRepo() {
		PropertySetterRepository<StepSheetColumn> stepWorksheetRepo = new PropertySetterRepository<>();

		// target
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_OWNER_PATH,
			ReflectionMutatorSetter.forProperty(PROPERTY_PATH, String.class));
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_NUM, ReflectionFieldSetter.forOptionalField("index"));

		// instruction
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.ACTION, ReflectionMutatorSetter.forOptionalProperty(PROPERTY_MODE));

		// step props
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_IS_CALL_STEP, NullPropertySetter.INSTANCE);

		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_ACTION, StepActionPropSetter.INSTANCE);
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_EXPECTED_RESULT, StepResultPropSetter.INSTANCE);

		// call step prop only (will rant if the other step shows up)
		stepWorksheetRepo.propSetterByColumn.put(StepSheetColumn.TC_STEP_CALL_DATASET, ParamAssignationModeSetter.INSTANCE);

		return stepWorksheetRepo;
	}

	/**
	 * @return
	 */
	private static PropertySetterRepository<?> createTestCasesWorksheetRepo() {
		PropertySetterRepository<TestCaseSheetColumn> r = new PropertySetterRepository<>();

		// target
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_PATH, ReflectionMutatorSetter.forProperty(PROPERTY_PATH, String.class));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_NUM, ReflectionFieldSetter.forOptionalField("order"));

		// test case
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_REFERENCE, ReflectionFieldSetter.forOptionalField("reference"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_NAME, ReflectionFieldSetter.forOptionalField(PROPERTY_NAME));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_MILESTONE, ReflectionFieldSetter.forOptionalField("milestones"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_WEIGHT_AUTO,
			ReflectionFieldSetter.forOptionalField("importanceAuto"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_WEIGHT, ReflectionFieldSetter.forOptionalField("importance"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_NATURE, ReflectionFieldSetter.forOptionalField("nature"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_TYPE, ReflectionFieldSetter.forOptionalField("type"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_STATUS, ReflectionFieldSetter.forOptionalField("status"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_DESCRIPTION,
			ReflectionFieldSetter.forOptionalField("description"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_PRE_REQUISITE,
			ReflectionFieldSetter.forOptionalField("prerequisite"));
		// createdOn and createdBy field name is not known, we use mutators to set'entityManager
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_CREATED_ON,
			ReflectionMutatorSetter.forOptionalProperty("createdOn"));
		r.propSetterByColumn.put(TestCaseSheetColumn.TC_CREATED_BY,
			ReflectionMutatorSetter.forOptionalProperty("createdBy"));

		// instruction
		r.propSetterByColumn.put(TestCaseSheetColumn.ACTION, ReflectionMutatorSetter.forOptionalProperty(PROPERTY_MODE));

		return r;
	}

	private Map<COL, PropertySetter<?, ?>> propSetterByColumn = new HashMap<>();

	private PropertySetterRepository() {
		super();
	}

	/**
	 * Finds the {@link PropertySetter} for the given column.
	 *
	 * @param col
	 * @return the {@link PropertySetter} or <code>null</code> when nothing found.
	 */
	@SuppressWarnings("unchecked")
	public <V, T> PropertySetter<V, T> findPropSetter(COL col) {
		return (PropertySetter<V, T>) propSetterByColumn.get(col);
	}
}
